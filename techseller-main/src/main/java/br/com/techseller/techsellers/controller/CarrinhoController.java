package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Carrinho;
import br.com.techseller.techsellers.service.CarrinhoService;
import br.com.techseller.techsellers.service.FreteService;
import br.com.techseller.techsellers.service.ProdutoService;
import br.com.techseller.techsellers.utils.Coordenadas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/carrinho")
@SessionAttributes("carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final ProdutoService produtoService;
    private final FreteService freteService;

    @Autowired
    public CarrinhoController(CarrinhoService carrinhoService,
                              ProdutoService produtoService,
                              FreteService freteService) {
        this.carrinhoService = carrinhoService;
        this.produtoService = produtoService;
        this.freteService = freteService;
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
}
