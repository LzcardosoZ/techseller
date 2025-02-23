package com.pi.mytechseller.projeto.repositorios;

import com.pi.mytechseller.projeto.modelos.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentoRepositorio extends JpaRepository<Pagamento, Long> {
}
