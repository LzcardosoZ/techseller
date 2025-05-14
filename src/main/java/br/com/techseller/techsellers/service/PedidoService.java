package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoService {
    List<Pedido> listarTodosOrdenadosPorData();
    Optional<Pedido> buscarPorId(Long id);
    void salvar(Pedido pedido);
}
