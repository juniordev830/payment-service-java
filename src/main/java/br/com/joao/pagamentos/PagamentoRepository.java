package br.com.joao.pagamentos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Page<Pagamento> findByStatus(StatusPagamento status, Pageable pageable);

    long countByStatus(StatusPagamento status);

    /** One aggregate query, so count and sum refer to the same database snapshot. */
    @Query("select count(p), sum(p.valor) from Pagamento p where p.moeda = :moeda")
    Object[] summarizeRegisteredByCurrency(@Param("moeda") String moeda);
}
