package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.Pedido;
import br.com.techseller.techsellers.repository.PedidoRepository;
import br.com.techseller.techsellers.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/meus-pedidos")
    public String listarPedidosDoCliente(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login_cliente";
        }

        Cliente cliente = clienteService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        List<Pedido> pedidos = pedidoRepository.findByClienteOrderByDataPedidoDesc(cliente);

        model.addAttribute("pedidos", pedidos);
        return "meus-pedidos";
    }

    @GetMapping("/pedido/{id}")
    public String exibirDetalhesPedido(@PathVariable Long id,
                                       Principal principal,
                                       Model model) {

        if (principal == null) {
            return "redirect:/login_cliente";
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // Validação opcional de segurança: garantir que o pedido é do cliente logado
        Cliente cliente = clienteService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            return "redirect:/meus-pedidos";
        }

        model.addAttribute("pedido", pedido);
        return "pedido-detalhes";
    }


}
