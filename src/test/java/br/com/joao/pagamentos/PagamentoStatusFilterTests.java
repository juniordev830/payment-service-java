package br.com.joao.pagamentos;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "PAYMENT_READ_TOKEN=ci-read-token-0123456789abcdef0123456789",
    "PAYMENT_ADMIN_TOKEN=ci-admin-token-0123456789abcdef0123456789"
})
@AutoConfigureMockMvc
@Transactional
class PagamentoStatusFilterTests {
    @Autowired PagamentoRepository repository;
    @Autowired MockMvc mockMvc;

    @Test
    void filtraPagamentosPendentesSemAlterarFormatoDaLista() throws Exception {
        repository.saveAndFlush(new Pagamento(new BigDecimal("10.00"), "BRL"));
        repository.saveAndFlush(new Pagamento(new BigDecimal("20.00"), "BRL"));

        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789").param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));

        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789").param("status", "RECUSADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void mantemPaginacaoAoFiltrarPorStatus() throws Exception {
        repository.saveAndFlush(new Pagamento(new BigDecimal("10.00"), "BRL"));
        repository.saveAndFlush(new Pagamento(new BigDecimal("20.00"), "BRL"));

        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789")
                        .param("status", "PENDENTE")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789")
                        .param("status", "PENDENTE")
                        .param("size", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaStatusDesconhecido() throws Exception {
        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789").param("status", "PAGO"))
                .andExpect(status().isBadRequest());
    }
}
