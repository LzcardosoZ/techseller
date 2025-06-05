package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Carrinho;
import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.service.CarrinhoService;
import br.com.techseller.techsellers.service.ProdutoService;
import br.com.techseller.techsellers.service.ArmazenamentoImagemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.commons.lang3.StringUtils;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/produtos")
@Slf4j
public class ProdutoController {

    @Value("${app.upload.dir}")
    private String diretorioBase;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ArmazenamentoImagemService armazenamentoImagemService;


    @GetMapping("/gerenciar-produtos")
    public String listarProdutos(@RequestParam(required = false) String filtro, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof br.com.techseller.techsellers.entity.User usuario) {
            model.addAttribute("nomeUsuario", usuario.getUsername());
            model.addAttribute("tipoUsuario", usuario.getGrupo().name());
            model.addAttribute("grupo", usuario.getGrupo());
        } else {
            model.addAttribute("nomeUsuario", auth.getName());
            model.addAttribute("tipoUsuario", null);
            model.addAttribute("grupo", null);
        }

        List<Produto> produtos = produtoService.listarProdutos(filtro);
        model.addAttribute("produtos", produtos);
        model.addAttribute("stringUtils", new StringUtils());

        return "listarProdutos";
    }


    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("produto", new Produto());
        return "cadastroProduto";
    }

    @PostMapping("/salvar")
    public String processarCadastroProduto(
            @ModelAttribute @Valid Produto produto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            log.warn("Erros de validação: {}", result.getAllErrors());
            model.addAttribute("produto", produto);
            return "cadastroProduto";
        }
        try {
            // Validação básica das imagens
            if (!produto.temArquivosImagens()) {
                model.addAttribute("error", "Pelo menos uma imagem é obrigatória");
                return "cadastroProduto";
            }

            // Usa o serviço para salvar o produto e processar as imagens
            Produto produtoSalvo = produtoService.salvarProduto(produto, produto.getArquivosImagens());

            redirectAttributes.addFlashAttribute("success", "Produto cadastrado com sucesso!");
            return "redirect:/produtos/gerenciar-produtos"; 


        } catch (IllegalArgumentException e) {
            log.error("Erro de validação: {}", e.getMessage());
            model.addAttribute("produto", produto);
            model.addAttribute("error", e.getMessage());
            return "cadastroProduto";
        } catch (IOException e) {
            log.error("Erro ao processar imagens: {}", e.getMessage());
            model.addAttribute("produto", produto);
            model.addAttribute("error", "Erro ao processar imagens: " + e.getMessage());
            return "cadastroProduto";
        } catch (Exception e) {
            log.error("Erro ao salvar produto: {}", e.getMessage());
            model.addAttribute("produto", produto);
            model.addAttribute("error", "Erro inesperado: " + e.getMessage());
            return "cadastroProduto";
        }
    }


    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority()); // Adicione esta linha

        Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            produto.setImagens(produto.getImagens().stream()
                    .sorted(Comparator.comparing(ImagemProduto::getOrdem))
                    .collect(Collectors.toList()));
            model.addAttribute("produto", produto);
            return "editarProduto";
        }
        redirectAttributes.addFlashAttribute("error", "Produto não encontrado");
        return "redirect:/produtos";
    }

    @PostMapping("/editar/{id}")
    public String processarEdicaoProduto(
            @PathVariable Long id,
            @ModelAttribute @Valid Produto produto,
            BindingResult result,
            @RequestParam(value = "novasImagens", required = false) MultipartFile[] novasImagens,
            @RequestParam(value = "imagensRemovidas", required = false) List<Long> imagensRemovidas,
            RedirectAttributes redirectAttributes,
            Model model,
            Authentication authentication) {

        if (result.hasErrors()) {
            model.addAttribute("produto", produtoService.buscarPorId(id).orElse(new Produto()));
            return "editarProduto";
        }

        try {
            boolean isEstoquista = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ESTOQUISTA"));

            if (isEstoquista) {
                Produto produtoExistente = produtoService.buscarPorId(id)
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

                produtoExistente.setQuantidadeEstoque(produto.getQuantidadeEstoque());

                // Chama salvar com a instância correta
                produtoService.salvarProduto(produtoExistente, new MultipartFile[0], true);

            } else {
                int totalImagensAtuais = produtoService.listarImagensPorProduto(id).size();
                int imagensSeremRemovidas = imagensRemovidas != null ? imagensRemovidas.size() : 0;
                int novasImagensCount = novasImagens != null ? Arrays.stream(novasImagens)
                        .filter(file -> file != null && !file.isEmpty())
                        .toArray().length : 0;

                if ((totalImagensAtuais - imagensSeremRemovidas + novasImagensCount) < 1) {
                    model.addAttribute("produto", produtoService.buscarPorId(id).orElse(new Produto()));
                    redirectAttributes.addFlashAttribute("error", "O produto deve ter pelo menos uma imagem");
                    return "editarProduto";
                }

                produtoService.editarProduto(produto, novasImagens, imagensRemovidas);
            }

            redirectAttributes.addFlashAttribute("success", "Produto atualizado com sucesso!");
            return "redirect:/produtos/gerenciar-produtos";

        } catch (Exception e) {
            log.error("Erro ao editar produto ID: {}", id, e);
            model.addAttribute("produto", produtoService.buscarPorId(id).orElse(new Produto()));
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
            return "editarProduto";
        }
    }

    @GetMapping("/imagem/{imagemId}")
    public ResponseEntity<Resource> exibirImagem(@PathVariable Long imagemId) {
        try {
            ImagemProduto imagem = produtoService.buscarImagemPorId(imagemId);

            if (imagem.getCaminhoArquivo() == null || imagem.getCaminhoArquivo().isEmpty()) {
                log.warn("Caminho da imagem {} é nulo ou vazio", imagemId);
                return ResponseEntity.notFound().build();
            }

            Path caminhoAbsoluto = Paths.get(diretorioBase).resolve(imagem.getCaminhoArquivo()).normalize();
            log.info("🧭 Buscando imagem no caminho: {}", caminhoAbsoluto); // ← ADICIONADO

            Resource resource = new UrlResource(caminhoAbsoluto.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Imagem não encontrada ou não legível: {}", caminhoAbsoluto);
                return ResponseEntity.notFound().build();
            }

            String contentType = imagem.getTipoMime() != null && !imagem.getTipoMime().isEmpty()
                    ? imagem.getTipoMime()
                    : Files.probeContentType(caminhoAbsoluto);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imagem.getNomeArquivo() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Erro ao carregar imagem ID: {}", imagemId, e);
            return fallbackImageResponse();
        }
    }

    @GetMapping("/detalhes/{id}")
    @ResponseBody
    public ResponseEntity<?> detalhesProduto(@PathVariable Long id) {
        Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();

            // Cria um Map com os dados necessários para o frontend
            Map<String, Object> response = new HashMap<>();
            response.put("produtoId", produto.getProdutoId());
            response.put("nome", produto.getNome());
            response.put("preco", produto.getPreco());
            response.put("descricaoDetalhada", produto.getDescricaoDetalhada());
            response.put("quantidadeEstoque", produto.getQuantidadeEstoque());

            // Adiciona a avaliação (convertendo BigDecimal para double)
            response.put("avaliacao", produto.getAvaliacao() != null ?
                    produto.getAvaliacao().doubleValue() : 0.0);

            // Processa as imagens
            List<Map<String, Object>> imagens = produto.getImagens().stream()
                    .sorted(Comparator.comparing(ImagemProduto::getOrdem))
                    .map(img -> {
                        Map<String, Object> imgMap = new HashMap<>();
                        imgMap.put("id", img.getId());
                        imgMap.put("nomeArquivo", img.getNomeArquivo());
                        return imgMap;
                    })
                    .collect(Collectors.toList());

            response.put("imagens", imagens);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/imagem/{imagemId}/principal")
    public ResponseEntity<Map<String, Object>> definirImagemPrincipal(
            @PathVariable Long imagemId,
            @RequestBody Map<String, Long> requestBody) {

        try {
            Long produtoId = requestBody.get("produtoId");
            produtoService.definirImagemComoPrincipal(imagemId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imagem definida como principal com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/{produtoId}/reordenar-imagens")
    public ResponseEntity<Map<String, Object>> reordenarImagens(
            @PathVariable Long produtoId,
            @RequestBody List<Long> novaOrdemIds) {
        try {
            if (novaOrdemIds == null || novaOrdemIds.isEmpty()) {
                throw new IllegalArgumentException("Lista de ordenação não pode ser vazia");
            }

            produtoService.reordenarImagens(produtoId, novaOrdemIds);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imagens reordenadas com sucesso"
            ));
        } catch (Exception e) {
            log.error("Erro ao reordenar imagens do produto {}", produtoId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/imagem/{imagemId}")
    public ResponseEntity<Map<String, Object>> removerImagem(@PathVariable Long imagemId) {
        try {
            produtoService.removerImagem(imagemId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imagem removida com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erro ao remover imagem: " + e.getMessage()
            ));
        }
    }

    private ResponseEntity<Resource> fallbackImageResponse() {
        try {
            Resource resource = new ClassPathResource("static/img/sem-imagem.jpg");

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // ou adapte se a imagem for PNG
                        .body(resource);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro ao carregar imagem padrão", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Erro no controlador de produtos", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocorreu um erro interno: " + e.getMessage()));
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("ProdutoController operacional");
    }

    @PostMapping("/inativar/{id}")
    public String inativarProduto(@PathVariable Long id) {
        produtoService.inativarProduto(id);
        return "redirect:/produtos/gerenciar-produtos";
    }

    @PostMapping("/reativar/{id}")
    public String reativarProduto(@PathVariable Long id) {
        produtoService.reativarProduto(id);
        return "redirect:/produtos/gerenciar-produtos";
    }
    @Controller
    @RequestMapping("/loja")
    public class LojaController {

        private final ProdutoService produtoService;
        private final CarrinhoService carrinhoService;

        @Autowired
        public LojaController(ProdutoService produtoService, CarrinhoService carrinhoService) {
            this.produtoService = produtoService;
            this.carrinhoService = carrinhoService;
        }

        @GetMapping
        public String mostrarLoja(@RequestParam(required = false) String filtro,
                                  Model model,
                                  @ModelAttribute("carrinho") Carrinho carrinho) {
            List<Produto> produtos = produtoService.listarProdutosAtivos(filtro);
            model.addAttribute("produtos", produtos);
            model.addAttribute("carrinho", carrinho);
            return "loja";
        }
    }

    @PostMapping("/test-salvar")
    @ResponseBody
    public ResponseEntity<?> salvarProdutoTest(
            @ModelAttribute Produto produto,
            @RequestParam(value = "imagens", required = false) MultipartFile[] imagens) throws IOException {

        // Defina as imagens no objeto Produto, se necessário
        produto.setArquivosImagens(imagens != null ? imagens : new MultipartFile[0]);

        produtoService.salvarProduto(produto, produto.getArquivosImagens());
        return ResponseEntity.ok("Produto salvo no modo teste");
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("imagens");
    }


}