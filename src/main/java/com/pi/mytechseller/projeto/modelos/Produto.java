package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProduto tipo;

    private String fornecedor;

    private BigDecimal icms;

    private String lote;

    private LocalDateTime dataCompra;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
=======
    @Column(name = "ID_Produto")
    private Long id;

    @Column(name = "Nome", nullable = false)
    private String nome;

    @Column(name = "Descricao")
    private String descricao;

    @Column(name = "Preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "Estoque", nullable = false)
    private Integer estoque;

    @Column(name = "Tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoProduto tipo;

    @Column(name = "Fornecedor")
    private String fornecedor;

    @Column(name = "ICMS", precision = 5, scale = 2)
    private BigDecimal icms;

    @Column(name = "Lote")
    private String lote;

    @Column(name = "DataCompra")
    private LocalDateTime dataCompra = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "ID_Categoria", nullable = false)
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    private Categoria categoria;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public TipoProduto getTipo() {
        return tipo;
    }

    public void setTipo(TipoProduto tipo) {
        this.tipo = tipo;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public BigDecimal getIcms() {
        return icms;
    }

    public void setIcms(BigDecimal icms) {
        this.icms = icms;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDateTime getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(LocalDateTime dataCompra) {
        this.dataCompra = dataCompra;
    }

    public Categoria getCategoria() {
<<<<<<< HEAD
        return categoria;
=======
        return categoria != null ? categoria : new Categoria(); // Retorna um objeto vazio em vez de null
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
<<<<<<< HEAD
        return Objects.equals(id, produto.id);
=======
        return id != null && id.equals(produto.id);
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
<<<<<<< HEAD
=======

>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
}