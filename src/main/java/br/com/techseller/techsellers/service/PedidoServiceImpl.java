package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Pedido;
import br.com.techseller.techsellers.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public List<Pedido> listarTodosOrdenadosPorData() {
        return pedidoRepository.findAllByOrderByDataPedidoDesc();
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Override
    public void salvar(Pedido pedido) {
        pedidoRepository.save(pedido);
    }

}
