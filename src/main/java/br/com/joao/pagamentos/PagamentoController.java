package br.com.joao.pagamentos;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoRepository repository;

    public PagamentoController(PagamentoRepository repository) {
        this.repository = repository;
    }

    // Both filtered and unfiltered results are bounded by the same pagination.
    @GetMapping
    public ResponseEntity<List<Pagamento>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) StatusPagamento status) {
        if (page < 0 || page > 100_000 || size < 1 || size > 50) {
            return ResponseEntity.badRequest().build();
        }
        PageRequest paging = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id")));
        List<Pagamento> resultados = status == null
                ? repository.findAll(paging).getContent()
                : repository.findByStatus(status, paging).getContent();
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pagamento> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pagamento> criar(
            @Valid @RequestBody CriarPagamentoRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        // Optional for backward compatibility. Callers sending a key must use a
        // bounded opaque identifier; do not use passwords or personal data.
        if (key != null && !key.matches("[A-Za-z0-9_-]{16,64}")) {
            return ResponseEntity.badRequest().build();
        }

        String currency = request.moeda().toUpperCase(Locale.ROOT);
        if (key != null) {
            var previous = repository.findByIdempotencyKey(key);
            if (previous.isPresent()) {
                return replayOrConflict(previous.get(), request, currency);
            }
        }

        Pagamento pagamento = new Pagamento(request.valor(), currency, key);
        try {
            // The unique database constraint protects concurrent creates that
            // arrive after the initial lookup but before the insert commits.
            Pagamento salvo = repository.saveAndFlush(pagamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (DataIntegrityViolationException exception) {
            if (key == null) {
                throw exception;
            }
            // A concurrent request may have won the unique-key insert.
            // Never create another record with the same key.
            return repository.findByIdempotencyKey(key)
                    .map(previous -> replayOrConflict(previous, request, currency))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
        }
    }

    private ResponseEntity<Pagamento> replayOrConflict(
            Pagamento previous, CriarPagamentoRequest request, String currency) {
        if (previous.getValor().compareTo(request.valor()) == 0
                && previous.getMoeda().equals(currency)) {
            return ResponseEntity.ok(previous);
        }
        // Same key with different payload is not an idempotent retry.
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> resumo() {
        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (StatusPagamento status : StatusPagamento.values()) {
            porStatus.put(status.name(), repository.countByStatus(status));
        }
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("total", repository.count());
        resposta.put("porStatus", porStatus);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Microsserviço de pagamentos disponível");
    }
}
