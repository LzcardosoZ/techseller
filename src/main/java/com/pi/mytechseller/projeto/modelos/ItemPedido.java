package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "Item_Pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private BigDecimal precoTotal;

=======
    @Column(name = "ID_Item_Pedido")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "ID_Produto", nullable = false)
    private Produto produto;

    @Column(name = "Quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "Preco_Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal;

    // Método para atualizar o preço total automaticamente
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

    public Pedido getPedido() {
<<<<<<< HEAD
        return pedido;
=======
        return pedido != null ? pedido : new Pedido();
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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
        ItemPedido that = (ItemPedido) o;
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
