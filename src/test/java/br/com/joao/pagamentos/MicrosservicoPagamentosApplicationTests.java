package br.com.joao.pagamentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "PAYMENT_READ_TOKEN=ci-read-token-0123456789abcdef0123456789",
    "PAYMENT_ADMIN_TOKEN=ci-admin-token-0123456789abcdef0123456789"
})
@AutoConfigureMockMvc
@Transactional
class MicrosservicoPagamentosApplicationTests {

    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void devePaginarPagamentosSemExporUmaListaIlimitada() throws Exception {
        repository.saveAndFlush(new Pagamento(new BigDecimal("10.00"), "BRL"));
        repository.saveAndFlush(new Pagamento(new BigDecimal("20.00"), "BRL"));
        repository.saveAndFlush(new Pagamento(new BigDecimal("30.00"), "BRL"));

        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789").param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRejeitarPaginasOuLimitesInvalidos() throws Exception {
        for (String[] params : new String[][]{
                {"-1", "20"}, {"100001", "20"}, {"0", "0"}, {"0", "51"}
        }) {
            mockMvc.perform(get("/api/pagamentos").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789")
                            .param("page", params[0])
                            .param("size", params[1]))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void deveRetornarBadRequestParaPagamentoInvalido() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", "Bearer ci-admin-token-0123456789abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":0,\"moeda\":\"BRL\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveNormalizarMoedaEIgnorarStatusEnviadoPeloCliente() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", "Bearer ci-admin-token-0123456789abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"brl\",\"status\":\"APROVADO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moeda").value("BRL"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveRejeitarMoedaForaDoFormatoDeTresLetras() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", "Bearer ci-admin-token-0123456789abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"BRL123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarValorComMaisDeDuasCasasDecimais() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", "Bearer ci-admin-token-0123456789abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.999,\"moeda\":\"BRL\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarNotFoundParaPagamentoInexistente() throws Exception {
        mockMvc.perform(get("/api/pagamentos/999999").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExporEndpointDeStatus() throws Exception {
        mockMvc.perform(get("/api/pagamentos/status").header("Authorization", "Bearer ci-read-token-0123456789abcdef0123456789"))
                .andExpect(status().isOk());
    }

    @Test
    void devePersistirPagamentoComStatusInicialPendente() {
        Pagamento pagamento = new Pagamento(new BigDecimal("49.90"), "BRL");

        Pagamento salvo = repository.saveAndFlush(pagamento);

        assertNotNull(salvo.getId());
        assertEquals(new BigDecimal("49.90"), salvo.getValor());
        assertEquals("BRL", salvo.getMoeda());
        assertEquals(StatusPagamento.PENDENTE, salvo.getStatus());
        assertNotNull(salvo.getCriadoEm());
    }

    @Test
    void deveConsultarPagamentoPersistidoPeloId() {
        Pagamento salvo = repository.saveAndFlush(new Pagamento(new BigDecimal("10.00"), "BRL"));

        Pagamento encontrado = repository.findById(salvo.getId()).orElseThrow();

        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals(new BigDecimal("10.00"), encontrado.getValor());
    }

    @Test
    void deveRetornarDetalhesEstruturadosParaErroDeValidacao() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", "Bearer ci-admin-token-0123456789abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":0,\"moeda\":\"BRL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A requisição contém campos inválidos"))
                .andExpect(jsonPath("$.errors.valor").exists());
    }

    @Test
    void deveRetornarErroEstruturadoQuandoMoedaForInvalida() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", "Bearer ci-admin-token-0123456789abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"BRL123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.moeda").exists());
    }

    @Test
    void deveRetornarErroEstruturadoParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", "Bearer ci-admin-token-0123456789abcdef0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O corpo da requisição é inválido"))
                .andExpect(jsonPath("$.errors.body").value("JSON inválido ou incompleto"));
    }
}
