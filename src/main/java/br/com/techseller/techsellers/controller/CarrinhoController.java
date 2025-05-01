package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Carrinho;
import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.Endereco;
import br.com.techseller.techsellers.repository.EnderecoRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/carrinho")
@SessionAttributes("carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final ProdutoService produtoService;
    private final FreteService freteService;
    private final ClienteService clienteService;
    private final EnderecoRepository enderecoRepository;



    @Autowired
    public CarrinhoController(CarrinhoService carrinhoService,
                              ProdutoService produtoService,
                              FreteService freteService, ClienteService clienteService,
                              EnderecoRepository enderecoRepository) {
        this.carrinhoService = carrinhoService;
        this.produtoService = produtoService;
        this.freteService = freteService;
        this.clienteService = clienteService;
        this.enderecoRepository = enderecoRepository;
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
                                @RequestParam(value = "modalidade", defaultValue = "Econômico") String modalidade,
                                @ModelAttribute("carrinho") Carrinho carrinho,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        try {
            String cepOrigem = "01001-000"; // Centro de SP (ajuste conforme seu galpão)

            Coordenadas origem = freteService.obterCoordenadas(cepOrigem);
            Coordenadas destino = freteService.obterCoordenadas(cepDestino);
            double distancia = freteService.calcularDistancia(origem, destino);

            BigDecimal freteBase = freteService.calcularValorFrete(distancia);

            BigDecimal freteEconomico = freteBase;
            BigDecimal freteFast = freteBase.multiply(new BigDecimal("1.3")).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal freteFull = freteBase.multiply(new BigDecimal("1.6")).setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal valorFreteSelecionado = switch (modalidade) {
                case "FastExpress" -> freteFast;
                case "FullExpress" -> freteFull;
                default -> freteEconomico;
            };

            String prazo = freteService.estimarPrazoEntrega(distancia, modalidade);

            // Salva no carrinho
            carrinho.setValorFrete(valorFreteSelecionado);

            // Passa dados para a view
            redirectAttributes.addFlashAttribute("freteEconomico", freteEconomico);
            redirectAttributes.addFlashAttribute("freteFast", freteFast);
            redirectAttributes.addFlashAttribute("freteFull", freteFull);
            redirectAttributes.addFlashAttribute("modalidadeSelecionada", modalidade);
            redirectAttributes.addFlashAttribute("prazoEntrega", prazo);

            redirectAttributes.addFlashAttribute("success",
                    "Frete " + modalidade + " aplicado: R$ " + valorFreteSelecionado +
                            " (" + prazo + "). Total com frete: R$ " + carrinho.getTotalComFrete());

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
        model.addAttribute("enderecos", cliente.getEnderecosEntrega());

        // Dados do carrinho
        model.addAttribute("carrinho", carrinho);
        model.addAttribute("total", carrinhoService.calcularTotal(carrinho));
        model.addAttribute("frete", carrinho.getValorFrete());
        model.addAttribute("totalComFrete", carrinho.getTotalComFrete());

        return "checkout";
    }

    @PostMapping("/confirmar-pedido")
    public String confirmarPedido(@RequestParam("enderecoId") Long enderecoId,
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

        // ✅ VERIFICAÇÃO DE PROPRIEDADE DO ENDEREÇO
        if (!enderecoSelecionado.getCliente().getId().equals(cliente.getId())) {
            redirectAttributes.addFlashAttribute("error", "Você não pode usar esse endereço.");
            return "redirect:/carrinho/finalizar";
        }

        // Criar e salvar o pedido, se desejar
        // pedidoService.criarPedido(cliente, enderecoSelecionado, carrinho);

        carrinho.getItens().clear();
        carrinho.setValorFrete(BigDecimal.ZERO);

        redirectAttributes.addFlashAttribute("success", "Pedido confirmado com sucesso!");
        return "redirect:/loja";
    }



}
