package br.com.techseller.techsellers.repository;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagemProdutoRepository extends JpaRepository<ImagemProduto, Long> {
    List<ImagemProduto> findByProdutoProdutoId(Long produtoId);

    @Modifying
    @Query("UPDATE ImagemProduto i SET i.imagemPrincipal = false WHERE i.produto.produtoId = :produtoId")
    void updateAllImagensNotPrincipal(@Param("produtoId") Long produtoId);

    @Query("SELECT i FROM ImagemProduto i WHERE i.produto.produtoId = :produtoId ORDER BY i.ordem ASC")
    List<ImagemProduto> findByProdutoOrderByOrdem(@Param("produtoId") Long produtoId);

    // Novo método adicionado
    int countByProdutoProdutoId(Long produtoId);

    int countByProduto(Produto produto);

    // Ou alternativa com @Query
    // @Query("SELECT COUNT(i) FROM ImagemProduto i WHERE i.produto.produtoId = :produtoId")
    // int countByProdutoId(@Param("produtoId") Long produtoId);
}