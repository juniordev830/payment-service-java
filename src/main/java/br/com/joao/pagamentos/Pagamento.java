package br.com.joao.pagamentos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 3)
    private String moeda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    protected Pagamento() {
        // Construtor exigido pelo JPA.
    }

    public Pagamento(BigDecimal valor, String moeda) {
        this.valor = valor;
        this.moeda = moeda;
        this.status = StatusPagamento.PENDENTE;
    }

    @PrePersist
    void prepararPersistencia() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
        if (status == null) {
            status = StatusPagamento.PENDENTE;
        }
    }

    public Long getId() { return id; }
    public BigDecimal getValor() { return valor; }
    public String getMoeda() { return moeda; }
    public StatusPagamento getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
}
