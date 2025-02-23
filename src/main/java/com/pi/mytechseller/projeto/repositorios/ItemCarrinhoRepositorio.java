package com.pi.mytechseller.projeto.repositorios;

import com.pi.mytechseller.projeto.modelos.ItemCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCarrinhoRepositorio extends JpaRepository<ItemCarrinho, Long> {
}
