package br.com.techseller.techsellers.repository;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteOrderByDataPedidoDesc(Cliente cliente);

}
