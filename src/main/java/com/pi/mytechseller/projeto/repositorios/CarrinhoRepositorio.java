package com.pi.mytechseller.projeto.repositorios;

import com.pi.mytechseller.projeto.modelos.Carrinho;
import com.pi.mytechseller.projeto.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarrinhoRepositorio extends JpaRepository<Carrinho, Long> {
    Optional<Carrinho> findByUsuarioAndStatus(Usuario usuario, com.pi.mytechseller.projeto.modelos.StatusCarrinho status);
}
