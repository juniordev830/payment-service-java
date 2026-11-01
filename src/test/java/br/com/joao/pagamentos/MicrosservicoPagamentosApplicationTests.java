package br.com.joao.pagamentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MicrosservicoPagamentosApplicationTests {

    @Autowired
    private PagamentoRepository repository;

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
