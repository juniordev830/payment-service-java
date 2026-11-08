package br.com.joao.pagamentos;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<Pagamento> criar(@Valid @RequestBody CriarPagamentoRequest request) {
        Pagamento pagamento = new Pagamento(
                request.valor(),
                request.moeda().toUpperCase(Locale.ROOT)
        );
        Pagamento salvo = repository.save(pagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
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

    /**
     * Read-only sum of registered payments in a single currency.
     * This includes PENDING records and must not be interpreted as revenue.
     */
    @GetMapping("/resumo/moeda")
    public ResponseEntity<ResumoMoedaResponse> resumoPorMoeda(
            @RequestParam(required = false) String moeda) {
        if (moeda == null || !moeda.matches("(?i)[a-z]{3}")) {
            return ResponseEntity.badRequest().build();
        }
        String codigo = moeda.toUpperCase(Locale.ROOT);
        Object[] totals = repository.summarizeRegisteredByCurrency(codigo).get(0);
        long quantidade = ((Number) totals[0]).longValue();
        BigDecimal valor = totals[1] == null ? BigDecimal.ZERO : (BigDecimal) totals[1];
        return ResponseEntity.ok(new ResumoMoedaResponse(codigo, quantidade, valor));
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Microsserviço de pagamentos disponível");
    }
}
