package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServiceImpl implements ProdutoService{

    @Autowired
    private ProdutoRepository produtoRepository;

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
    public void salvarProduto(Produto produto) {
        produtoRepository.save(produto);
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
    public void salvarUrlImagem(Long produtoId, String urlImagem) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setImagemUrl(urlImagem);
        produtoRepository.save(produto);
    }
}
