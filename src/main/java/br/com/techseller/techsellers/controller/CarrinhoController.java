package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.*;
import br.com.techseller.techsellers.enums.FormaPagamento;
import br.com.techseller.techsellers.repository.EnderecoRepository;
import br.com.techseller.techsellers.repository.PagamentoCartaoRepository;
import br.com.techseller.techsellers.repository.PedidoRepository;
import br.com.techseller.techsellers.service.CarrinhoService;
import br.com.techseller.techsellers.service.ClienteService;
import br.com.techseller.techsellers.service.FreteService;
import br.com.techseller.techsellers.service.ProdutoService;
import br.com.techseller.techsellers.utils.Coordenadas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import br.com.techseller.techsellers.enums.StatusPedido;


import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/carrinho")
@SessionAttributes("carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final ProdutoService produtoService;
    private final FreteService freteService;
    private final ClienteService clienteService;
    private final EnderecoRepository enderecoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagamentoCartaoRepository pagamentoCartaoRepository;


    @Autowired
    public CarrinhoController(CarrinhoService carrinhoService,
                              ProdutoService produtoService,
                              FreteService freteService,
                              ClienteService clienteService,
                              EnderecoRepository enderecoRepository,
                              PedidoRepository pedidoRepository,
                              PagamentoCartaoRepository pagamentoCartaoRepository) {
        this.carrinhoService = carrinhoService;
        this.produtoService = produtoService;
        this.freteService = freteService;
        this.clienteService = clienteService;
        this.enderecoRepository = enderecoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagamentoCartaoRepository = pagamentoCartaoRepository;
    }


    @ModelAttribute("carrinho")
    public Carrinho getCarrinho() {
        return new Carrinho();
    }

    @PostMapping("/adicionar")
    public String adicionarItem(@RequestParam Long produtoId,
                                @ModelAttribute Carrinho carrinho,
                                RedirectAttributes redirectAttributes) {
        try {
            carrinhoService.adicionarItem(carrinho, produtoId, 1);
            redirectAttributes.addFlashAttribute("success", "Produto adicionado ao carrinho");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/loja";
    }

    @PostMapping("/atualizar")
    public String atualizarQuantidade(@RequestParam Long produtoId,
                                      @RequestParam int quantidade,
                                      @ModelAttribute Carrinho carrinho,
                                      RedirectAttributes redirectAttributes) {
        try {
            carrinhoService.atualizarQuantidade(carrinho, produtoId, quantidade);
            redirectAttributes.addFlashAttribute("success", "Quantidade atualizada");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/remover")
    public String removerItem(@RequestParam Long produtoId,
                              @ModelAttribute Carrinho carrinho,
                              RedirectAttributes redirectAttributes) {
        carrinhoService.removerItem(carrinho, produtoId);
        redirectAttributes.addFlashAttribute("success", "Produto removido do carrinho");
        return "redirect:/carrinho";
    }

    @GetMapping
    public String verCarrinho(@ModelAttribute Carrinho carrinho, Model model) {
        model.addAttribute("total", carrinhoService.calcularTotal(carrinho));
        model.addAttribute("totalItens", carrinhoService.contarItens(carrinho));
        model.addAttribute("carrinho", carrinho);
        return "carrinho";
    }

    @PostMapping("/frete")
    public String escolherFrete(@RequestParam("cepDestino") String cepDestino,
                                @ModelAttribute("carrinho") Carrinho carrinho,
                                RedirectAttributes redirectAttributes) {

        try {
            // Obtém o endereço via ViaCEP
            Endereco endereco = freteService.obterEnderecoPorCep(cepDestino);

            // Calcula frete com base no UF do endereço
            BigDecimal valorFrete = freteService.calcularFretePorUF(endereco.getUf());

            carrinho.setValorFrete(valorFrete);

            redirectAttributes.addFlashAttribute("success", "Frete calculado para " + endereco.getUf() +
                    ": R$ " + valorFrete + ". Total com frete: R$ " + carrinho.getTotalComFrete());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao calcular o frete: " + e.getMessage());
        }

        return "redirect:/carrinho";
    }


    @GetMapping("/finalizar")
    public String finalizarPedido(@ModelAttribute Carrinho carrinho,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal) {
        // Verifica se está logado
        if (principal == null) {
            return "redirect:/login_cliente";
        }

        // Verifica se o carrinho está vazio
        if (carrinho.getItens().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Seu carrinho está vazio.");
            return "redirect:/carrinho";
        }

        // Buscar cliente logado
        Cliente cliente = clienteService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Adiciona à model a lista de endereços e o padrão
        model.addAttribute("enderecos", cliente.getEnderecosEntrega());
        model.addAttribute("enderecoPadrao", cliente.getEnderecoPadrao()); // <-- Adicionado aqui

        // Dados do carrinho
        model.addAttribute("carrinho", carrinho);
        model.addAttribute("total", carrinhoService.calcularTotal(carrinho));
        model.addAttribute("frete", carrinho.getValorFrete());
        model.addAttribute("totalComFrete", carrinho.getTotalComFrete());

        return "checkout";
    }


    @PostMapping("/confirmar-pedido")
    public String confirmarPedido(@RequestParam("enderecoId") Long enderecoId,
                                  @RequestParam("formaPagamento") String formaPagamento,
                                  @RequestParam(required = false) String nomeTitular,
                                  @RequestParam(required = false) String numeroCartao,
                                  @RequestParam(required = false) String codigoVerificador,
                                  @RequestParam(required = false) String validade,
                                  @RequestParam(required = false) Integer parcelas,
                                  @ModelAttribute Carrinho carrinho,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login_cliente";
        }

        Cliente cliente = clienteService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Endereco enderecoSelecionado = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RuntimeException("Endereço inválido"));

        if (!enderecoSelecionado.getCliente().getId().equals(cliente.getId())) {
            redirectAttributes.addFlashAttribute("error", "Você não pode usar esse endereço.");
            return "redirect:/carrinho/finalizar";
        }

        if (!formaPagamento.equals("BOLETO") && !formaPagamento.equals("CARTAO")) {
            redirectAttributes.addFlashAttribute("error", "Forma de pagamento inválida.");
            return "redirect:/carrinho/finalizar";
        }

        // Validação dos campos obrigatórios se for CARTÃO
        if (formaPagamento.equals("CARTAO")) {
            if (nomeTitular == null || nomeTitular.isBlank() ||
                    numeroCartao == null || numeroCartao.isBlank() ||
                    codigoVerificador == null || codigoVerificador.isBlank() ||
                    validade == null || validade.isBlank() ||
                    parcelas == null || parcelas < 1) {

                redirectAttributes.addFlashAttribute("error", "Preencha todos os dados do cartão corretamente.");
                return "redirect:/carrinho/finalizar";
            }
        }


        // Cria o pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEnderecoEntrega(enderecoSelecionado);
        pedido.setValorTotal(carrinho.getTotalComFrete());
        pedido.setFormaPagamento(FormaPagamento.valueOf(formaPagamento));
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        // Converte itens do carrinho em itens do pedido
        List<PedidoItem> itensPedido = new ArrayList<>();

        for (ItemCarrinho item : carrinho.getItens()) {
            PedidoItem pedidoItem = new PedidoItem();
            pedidoItem.setPedido(pedido);
            pedidoItem.setProduto(item.getProduto());
            pedidoItem.setQuantidade(item.getQuantidade());
            pedidoItem.setPrecoUnitario(item.getProduto().getPreco());
            pedidoItem.setSubtotal(item.getSubtotal());

            itensPedido.add(pedidoItem);
        }

        pedido.setItens(itensPedido);
        pedidoRepository.save(pedido);


        // Se for pagamento com cartão, salva os dados
        if (formaPagamento.equals("CARTAO")) {
            PagamentoCartao pagamento = new PagamentoCartao();
            pagamento.setPedido(pedido);
            pagamento.setNomeTitular(nomeTitular);
            pagamento.setNumeroCartao(numeroCartao);
            pagamento.setCodigoVerificador(codigoVerificador);
            pagamento.setValidade(validade);
            pagamento.setParcelas(parcelas);

            pagamentoCartaoRepository.save(pagamento);
        }

        // Limpa o carrinho
        carrinho.getItens().clear();
        carrinho.setValorFrete(BigDecimal.ZERO);

        redirectAttributes.addFlashAttribute("numeroPedido", pedido.getId());
        redirectAttributes.addFlashAttribute("valorPedido", pedido.getValorTotal());
        redirectAttributes.addFlashAttribute("formaPagamento", formaPagamento);
        redirectAttributes.addFlashAttribute("mensagem", "Pedido criado com sucesso!");

        return "redirect:/carrinho/pedido/sucesso";


    }

    @PostMapping("/resumo")
    public String exibirResumoPedido(@RequestParam("enderecoId") Long enderecoId,
                                     @RequestParam("formaPagamento") String formaPagamento,
                                     @RequestParam(required = false) String nomeTitular,
                                     @RequestParam(required = false) String numeroCartao,
                                     @RequestParam(required = false) String codigoVerificador,
                                     @RequestParam(required = false) String validade,
                                     @RequestParam(required = false) Integer parcelas,
                                     @ModelAttribute Carrinho carrinho,
                                     Principal principal,
                                     Model model) {

        if (principal == null) {
            return "redirect:/login_cliente";
        }

        Cliente cliente = clienteService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RuntimeException("Endereço inválido"));

        String enderecoStr = endereco.getLogradouro() + ", " + endereco.getNumero()
                + " - " + endereco.getCidade() + "/" + endereco.getUf();

        String formaPagamentoStr = formaPagamento.equals("CARTAO") ? "Cartão de Crédito" : "Boleto Bancário";

        model.addAttribute("carrinho", carrinho);
        model.addAttribute("total", carrinhoService.calcularTotal(carrinho));
        model.addAttribute("frete", carrinho.getValorFrete());
        model.addAttribute("totalComFrete", carrinho.getTotalComFrete());

        model.addAttribute("enderecoEntregaStr", enderecoStr);
        model.addAttribute("formaPagamentoStr", formaPagamentoStr);

        model.addAttribute("enderecoId", enderecoId);
        model.addAttribute("formaPagamento", formaPagamento);
        model.addAttribute("nomeTitular", nomeTitular);
        model.addAttribute("numeroCartao", numeroCartao);
        model.addAttribute("codigoVerificador", codigoVerificador);
        model.addAttribute("validade", validade);
        model.addAttribute("parcelas", parcelas);

        return "resumo";
    }

    @GetMapping("/pedido/sucesso")
    public String pedidoSucesso() {
        return "pedido-sucesso";
    }


}
