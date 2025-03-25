package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import br.com.techseller.techsellers.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServiceImpl implements ProdutoService{

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public List<Produto> listarProdutos(String filtro) {
        if (filtro != null && !filtro.isEmpty()) {
            return produtoRepository.findByNomeContainingIgnoreCase(filtro); // Supondo que exista esse método no repository
        }
        return produtoRepository.findAll();
    }

    @Override
    public void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        produto = produtoRepository.save(produto); // Primeiro salva o produto para obter o ID

        if (imagem != null && !imagem.isEmpty()) {
            try {
                ImagemProduto imagemProduto = new ImagemProduto();
                imagemProduto.setProduto(produto);
                imagemProduto.setImagem(imagem.getBytes());
                imagemProduto.setImagemPrincipal(imagemPrincipal);
                imagemProdutoRepository.save(imagemProduto);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar imagem do produto: " + e.getMessage());
            }
        }
    }


    @Override
    public Optional<Produto> buscarPorId(Long produto_id) {
        return produtoRepository.findById(produto_id);
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
}
