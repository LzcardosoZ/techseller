package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import br.com.techseller.techsellers.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    @Override
    public List<Produto> listarProdutos(String filtro) {
        List<Produto> produtos;
        if (filtro != null && !filtro.isEmpty()) {
            // Use the new method to search by name or ID
            produtos = produtoRepository.findByNomeOrId(filtro);
        } else {
            produtos = produtoRepository.findAll();
        }

        // Carrega as imagens para cada produto
        produtos.forEach(produto -> {
            List<ImagemProduto> imagens = imagemProdutoRepository.findByProdutoProdutoId(produto.getProdutoId());
            produto.setImagens(imagens);

            // Garante que a lista de imagens nunca seja nula
            if (produto.getImagens() == null) {
                produto.setImagens(new ArrayList<>());
            }
        });

        return produtos;
    }

    @Override
    @Transactional
    public void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        // Validação básica do produto
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }

        // Validação da imagem (obrigatória conforme regra de negócio)
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("O produto deve ter pelo menos uma imagem");
        }

        try {
            // Definir valores padrão
            if (produto.getAvaliacao() == null) {
                produto.setAvaliacao(new BigDecimal("0.0"));
            }
            if (produto.getAtivo() == null) {
                produto.setAtivo(true);
            }

            // Salva o produto primeiro para obter o ID
            produto = produtoRepository.save(produto);

            // Cria e configura a imagem
            ImagemProduto imagemProduto = ImagemProduto.builder()
                    .imagem(imagem.getBytes())
                    .imagemPrincipal(imagemPrincipal)
                    .produto(produto)
                    .build();

            // Salva a imagem
            imagemProdutoRepository.save(imagemProduto);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem do produto: " + e.getMessage(), e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Erro de integridade ao salvar produto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Produto> buscarPorId(Long produto_id) {
        return produtoRepository.findById(produto_id);
    }

    @Override
    public ImagemProduto buscarImagemPorId(Long imagemId) {
        return imagemProdutoRepository.findById(imagemId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));
    }

    @Override
    public void inativarProduto(Long produto_id) {
        produtoRepository.findById(produto_id).ifPresent(produto -> {
            produto.setAtivo(false);
            produtoRepository.save(produto);
        });
    }

    @Override
    public void reativarProduto(Long produto_id) {
        produtoRepository.findById(produto_id).ifPresent(produto -> {
            produto.setAtivo(true);
            produtoRepository.save(produto);
        });
    }

    @Override
    public void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal) {
        try {
            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            ImagemProduto imagemProduto = new ImagemProduto();
            imagemProduto.setProduto(produto);
            imagemProduto.setImagem(file.getBytes());
            imagemProduto.setImagemPrincipal(imagemPrincipal);

            imagemProdutoRepository.save(imagemProduto);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    @Override
    public List<ImagemProduto> listarImagensPorProduto(Long produtoId) {
        return imagemProdutoRepository.findByProdutoProdutoId(produtoId);
    }

    @Override
    @Transactional
    public Produto editarProduto(Produto produto) {
        if (produto.getProdutoId() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório para edição");
        }

        // Verifica se o produto existe
        Produto produtoExistente = produtoRepository.findById(produto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Atualiza os campos do produto existente
        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricaoDetalhada(produto.getDescricaoDetalhada());
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setQuantidadeEstoque(produto.getQuantidadeEstoque());

        // Preserve avaliacao if not set
        if (produto.getAvaliacao() != null) {
            produtoExistente.setAvaliacao(produto.getAvaliacao());
        }

        // Mantém o status de ativo como estava, a menos que seja explicitamente alterado
        if (produto.getAtivo() != null) {
            produtoExistente.setAtivo(produto.getAtivo());
        }

        // Salva e retorna o produto atualizado
        return produtoRepository.save(produtoExistente);
    }
}