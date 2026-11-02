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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MicrosservicoPagamentosApplicationTests {

    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void deveRetornarBadRequestParaPagamentoInvalido() throws Exception {
        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":0,\"moeda\":\"BRL\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveNormalizarMoedaEIgnorarStatusEnviadoPeloCliente() throws Exception {
        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"brl\",\"status\":\"APROVADO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moeda").value("BRL"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveRejeitarMoedaForaDoFormatoDeTresLetras() throws Exception {
        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.90,\"moeda\":\"BRL123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarValorComMaisDeDuasCasasDecimais() throws Exception {
        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":49.999,\"moeda\":\"BRL\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarNotFoundParaPagamentoInexistente() throws Exception {
        mockMvc.perform(get("/api/pagamentos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExporEndpointDeStatus() throws Exception {
        mockMvc.perform(get("/api/pagamentos/status"))
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
}
