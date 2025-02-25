package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "Item_Carrinho")
public class ItemCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_carrinho", nullable = false)
    private Carrinho carrinho;

    @ManyToOne
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private BigDecimal precoTotal;

=======
    @Column(name = "ID_Item")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Carrinho")
    private Carrinho carrinho;

    @ManyToOne
    @JoinColumn(name = "ID_Produto")
    private Produto produto;

    @Column(name = "Quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "Preco_Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal;

    // metodo para atualizar o preço total automaticamente
    public void atualizarPrecoTotal() {
        if (produto != null && quantidade != null) {
            this.precoTotal = produto.getPreco().multiply(new BigDecimal(quantidade));
        }
    }

>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carrinho getCarrinho() {
<<<<<<< HEAD
        return carrinho;
=======
        return carrinho != null ? carrinho : new Carrinho();
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    public void setCarrinho(Carrinho carrinho) {
        this.carrinho = carrinho;
    }

    public Produto getProduto() {
<<<<<<< HEAD
        return produto;
=======
        return produto != null ? produto : new Produto();
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
<<<<<<< HEAD
=======
        atualizarPrecoTotal(); // Atualiza o preço total ao definir o produto
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
<<<<<<< HEAD
=======
        atualizarPrecoTotal(); // Atualiza o preço total ao definir a quantidade
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    public BigDecimal getPrecoTotal() {
        return precoTotal;
    }

    public void setPrecoTotal(BigDecimal precoTotal) {
        this.precoTotal = precoTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCarrinho that = (ItemCarrinho) o;
<<<<<<< HEAD
        return Objects.equals(id, that.id);
=======
        return id != null && id.equals(that.id);
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
