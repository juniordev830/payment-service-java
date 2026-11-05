package br.com.joao.pagamentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "PAYMENT_READ_TOKEN=ci-read-token-0123456789abcdef0123456789",
    "PAYMENT_ADMIN_TOKEN=ci-admin-token-0123456789abcdef0123456789"
})
@AutoConfigureMockMvc
class PagamentoIdempotencyTests {
    private static final String ADMIN = "Bearer ci-admin-token-0123456789abcdef0123456789";
    private static final String KEY = "sample-payment-request-0001";

    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    @AfterEach
    void limparDadosDeTeste() {
        repository.deleteAll();
    }

    @Test
    void repeatedRequestWithSameKeyReturnsExistingPaymentWithoutDuplicate() throws Exception {
        mvc.perform(post("/api/pagamentos")
                        .header("Authorization", ADMIN)
                        .header("Idempotency-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"brl\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mvc.perform(post("/api/pagamentos")
                        .header("Authorization", ADMIN)
                        .header("Idempotency-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.9,\"moeda\":\"BRL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moeda").value("BRL"));

        assertEquals(1, repository.count());
        var saved = repository.findByIdempotencyKey(KEY).orElseThrow();
        assertEquals(0, saved.getValor().compareTo(new BigDecimal("49.90")));
    }

    @Test
    void sameKeyWithDifferentPayloadReturnsConflict() throws Exception {
        mvc.perform(post("/api/pagamentos")
                        .header("Authorization", ADMIN)
                        .header("Idempotency-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"BRL\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/pagamentos")
                        .header("Authorization", ADMIN)
                        .header("Idempotency-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":99.90,\"moeda\":\"BRL\"}"))
                .andExpect(status().isConflict());

        assertEquals(1, repository.count());
    }

    @Test
    void malformedKeyIsRejectedBeforeAnyWrite() throws Exception {
        for (String key : new String[]{"short", "has space in key", "x".repeat(65)}) {
            mvc.perform(post("/api/pagamentos")
                            .header("Authorization", ADMIN)
                            .header("Idempotency-Key", key)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"valor\":49.90,\"moeda\":\"BRL\"}"))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(0, repository.count());
    }

    @Test
    void duplicateOrCombinedIdempotencyHeadersAreRejected() throws Exception {
        mvc.perform(post("/api/pagamentos")
                        .header("Authorization", ADMIN)
                        .header("Idempotency-Key", KEY, "sample-payment-request-0002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"BRL\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/pagamentos")
                        .header("Authorization", ADMIN)
                        .header("Idempotency-Key", KEY + ", sample-payment-request-0002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"BRL\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(0, repository.count());
    }

    @Test
    void existingDemoClientsWithoutKeyRemainCompatible() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/pagamentos")
                            .header("Authorization", ADMIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"valor\":49.90,\"moeda\":\"BRL\"}"))
                    .andExpect(status().isCreated());
        }
        assertEquals(2, repository.count());
        assertTrue(repository.findByIdempotencyKey(KEY).isEmpty());
    }
}
