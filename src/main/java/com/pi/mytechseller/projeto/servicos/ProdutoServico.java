package com.pi.mytechseller.projeto.servicos;

import com.pi.mytechseller.projeto.modelos.Produto;
import com.pi.mytechseller.projeto.repositorios.ProdutoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;

    public ProdutoServico(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    // Adicionar um novo produto
    public Produto adicionarProduto(Produto produto) {
        return produtoRepositorio.save(produto);
    }

    // Atualizar um produto existente
    public Produto atualizarProduto(Long id, Produto produtoAtualizado) {
        return produtoRepositorio.findById(id).map(produto -> {
            produto.setNome(produtoAtualizado.getNome());
            produto.setDescricao(produtoAtualizado.getDescricao());
            produto.setPreco(produtoAtualizado.getPreco());
            produto.setEstoque(produtoAtualizado.getEstoque());
            produto.setTipo(produtoAtualizado.getTipo());
            produto.setFornecedor(produtoAtualizado.getFornecedor());
            produto.setIcms(produtoAtualizado.getIcms());
            produto.setLote(produtoAtualizado.getLote());
            produto.setDataCompra(produtoAtualizado.getDataCompra());
            produto.setCategoria(produtoAtualizado.getCategoria());
            return produtoRepositorio.save(produto);
        }).orElseThrow(() -> new RuntimeException("Produto não encontrado."));
    }

    // Remover um produto
    public void removerProduto(Long id) {
        if (!produtoRepositorio.existsById(id)) {
            throw new RuntimeException("Produto não encontrado.");
        }
        produtoRepositorio.deleteById(id);
    }

    // Listar todos os produtos
    public List<Produto> listarProdutos() {
        return produtoRepositorio.findAll();
    }

    // Buscar um produto por ID
    public Produto buscarProdutoPorId(Long id) {
        return produtoRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Produto não encontrado."));
    }
}
