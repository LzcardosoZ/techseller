package com.pi.mytechseller.projeto.repositorios;

import com.pi.mytechseller.projeto.modelos.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepositorio extends JpaRepository<Produto, Long> {
}
