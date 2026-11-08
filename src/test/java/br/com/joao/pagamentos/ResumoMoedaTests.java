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
class ResumoMoedaTests {
    private static final String READ = "Bearer ci-read-token-0123456789abcdef0123456789";

    @Autowired PagamentoRepository repository;
    @Autowired MockMvc mvc;

    @Test
    void somaValoresDaMesmaMoedaSemMisturarMoedasDiferentes() throws Exception {
        repository.saveAndFlush(new Pagamento(new BigDecimal("10.50"), "BRL"));
        repository.saveAndFlush(new Pagamento(new BigDecimal("20.00"), "BRL"));
        repository.saveAndFlush(new Pagamento(new BigDecimal("99.00"), "USD"));

        mvc.perform(get("/api/pagamentos/resumo/moeda")
                        .header("Authorization", READ).param("moeda", "brl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moeda").value("BRL"))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.valorTotalRegistrado").value(30.5));

        mvc.perform(get("/api/pagamentos/resumo/moeda")
                        .header("Authorization", READ).param("moeda", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(1))
                .andExpect(jsonPath("$.valorTotalRegistrado").value(99.0));
    }

    @Test
    void moedaSemPagamentosRetornaZerosEmVezDeErro() throws Exception {
        mvc.perform(get("/api/pagamentos/resumo/moeda")
                        .header("Authorization", READ).param("moeda", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moeda").value("EUR"))
                .andExpect(jsonPath("$.quantidade").value(0))
                .andExpect(jsonPath("$.valorTotalRegistrado").value(0));
    }

    @Test
    void rejeitaMoedaInvalidaEParametrosAusentes() throws Exception {
        for (String value : new String[]{"B", "BRL1", "BR L", "€€€", ""}) {
            mvc.perform(get("/api/pagamentos/resumo/moeda")
                            .header("Authorization", READ).param("moeda", value))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/pagamentos/resumo/moeda")
                        .header("Authorization", READ))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resumoDeMoedaContinuaExigindoAutenticacao() throws Exception {
        mvc.perform(get("/api/pagamentos/resumo/moeda").param("moeda", "BRL"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pagamentos/resumo/moeda")
                        .header("Authorization", "Bearer invalid-token")
                        .param("moeda", "BRL"))
                .andExpect(status().isUnauthorized());
    }
}
