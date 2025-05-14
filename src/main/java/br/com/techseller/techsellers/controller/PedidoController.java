package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.Pedido;
import br.com.techseller.techsellers.enums.StatusPedido;
import br.com.techseller.techsellers.repository.PedidoRepository;
import br.com.techseller.techsellers.service.ClienteService;
import br.com.techseller.techsellers.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PedidoService pedidoService;

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

    @GetMapping("/estoquista/pedidos")
    public String listarTodosOsPedidos(Model model) {
        List<Pedido> pedidos = pedidoService.listarTodosOrdenadosPorData();
        model.addAttribute("pedidos", pedidos);
        return "listar-pedidos";
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

    @GetMapping("/estoquista/pedidos/{id}/editar")
    public String editarPedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        model.addAttribute("pedido", pedido);
        return "editar-pedido";
    }

    @PostMapping("/estoquista/pedidos/{id}/atualizar")
    public String atualizarStatusPedido(@PathVariable Long id, @RequestParam("status") String novoStatus) {
        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus(StatusPedido.valueOf(novoStatus));
        pedidoService.salvar(pedido);

        return "redirect:/estoquista/pedidos";
    }

}
