package br.com.joao.pagamentos;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Page<Pagamento> findByStatus(StatusPagamento status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pagamento p where p.id = :id")
    Optional<Pagamento> buscarParaAtualizar(@Param("id") Long id);

    long countByStatus(StatusPagamento status);

    /** One aggregate query, so count and sum refer to the same database snapshot. */
    @Query("select count(p), sum(p.valor) from Pagamento p where p.moeda = :moeda")
    List<Object[]> summarizeRegisteredByCurrency(@Param("moeda") String moeda);
}
