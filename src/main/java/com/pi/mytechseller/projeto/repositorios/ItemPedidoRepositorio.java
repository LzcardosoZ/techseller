package com.pi.mytechseller.projeto.repositorios;

import com.pi.mytechseller.projeto.modelos.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoRepositorio extends JpaRepository<ItemPedido, Long> {
}
