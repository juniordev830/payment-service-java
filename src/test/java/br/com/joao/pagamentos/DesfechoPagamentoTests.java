package br.com.joao.pagamentos;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "PAYMENT_READ_TOKEN=ci-read-token-0123456789abcdef0123456789",
    "PAYMENT_ADMIN_TOKEN=ci-admin-token-0123456789abcdef0123456789"
})
@AutoConfigureMockMvc
class DesfechoPagamentoTests {
    private static final String ADMIN = "Bearer ci-admin-token-0123456789abcdef0123456789";
    private static final String LEITURA = "Bearer ci-read-token-0123456789abcdef0123456789";

    @Autowired PagamentoRepository repository;
    @Autowired MockMvc mockMvc;

    @Test
    void desfechoEIdempotenteEIrreversivel() throws Exception {
        long id = repository.save(new Pagamento(new BigDecimal("10.00"), "BRL")).getId();
        try {
            atualizar(id, "APROVADO", LEITURA).andExpect(status().isForbidden());
            atualizar(id, "PENDENTE", ADMIN).andExpect(status().isBadRequest());
            atualizar(id, "APROVADO", ADMIN).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APROVADO"));
            atualizar(id, "APROVADO", ADMIN).andExpect(status().isOk());
            atualizar(id, "RECUSADO", ADMIN).andExpect(status().isConflict());
            assertEquals(StatusPagamento.APROVADO, repository.findById(id).orElseThrow().getStatus());
        } finally {
            repository.deleteById(id);
        }
        atualizar(id, "APROVADO", ADMIN).andExpect(status().isNotFound());
    }

    @Test
    void solicitaçõesConcorrentesEscolhemUmÚnicoDesfecho() throws Exception {
        long id = repository.save(new Pagamento(new BigDecimal("20.00"), "BRL")).getId();
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var approved = pool.submit(request(start, id, "APROVADO"));
            var canceled = pool.submit(request(start, id, "CANCELADO"));
            start.countDown();
            int first = approved.get(10, TimeUnit.SECONDS);
            int second = canceled.get(10, TimeUnit.SECONDS);
            assertEquals(List.of(200, 409), List.of(first, second).stream().sorted().toList());
            assertEquals(first == 200 ? StatusPagamento.APROVADO : StatusPagamento.CANCELADO,
                    repository.findById(id).orElseThrow().getStatus());
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(10, TimeUnit.SECONDS);
            repository.deleteById(id);
        }
    }

    private Callable<Integer> request(CountDownLatch start, long id, String result) {
        return () -> {
            start.await();
            return atualizar(id, result, ADMIN).andReturn().getResponse().getStatus();
        };
    }

    private ResultActions atualizar(long id, String result, String token) throws Exception {
        return mockMvc.perform(put("/api/pagamentos/{id}/status", id)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + result + "\"}"));
    }
}
