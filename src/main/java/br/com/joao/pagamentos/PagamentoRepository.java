package br.com.joao.pagamentos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Page<Pagamento> findByStatus(StatusPagamento status, Pageable pageable);

    long countByStatus(StatusPagamento status);
}
