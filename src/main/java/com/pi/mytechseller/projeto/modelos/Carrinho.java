package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
<<<<<<< HEAD
=======
import java.util.ArrayList;
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Carrinho")
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCarrinho status = StatusCarrinho.ABERTO;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrinho> itens;
=======
    @Column(name = "ID_Carrinho")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Usuario")
    private Usuario usuario;

    @Column(name = "Status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusCarrinho status;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrinho> itens = new ArrayList<>();

    // Construtor padrão
    public Carrinho() {
        this.status = StatusCarrinho.ABERTO;
    }

    // Métodos auxiliares para manipulação de itens
    public void adicionarItem(ItemCarrinho item) {
        itens.add(item);
        item.setCarrinho(this);
    }

    public void removerItem(ItemCarrinho item) {
        itens.remove(item);
        item.setCarrinho(null);
    }
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public StatusCarrinho getStatus() {
        return status;
    }

    public void setStatus(StatusCarrinho status) {
        this.status = status;
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }

    public void setItens(List<ItemCarrinho> itens) {
        this.itens = itens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Carrinho carrinho = (Carrinho) o;
<<<<<<< HEAD
        return Objects.equals(id, carrinho.id);
=======
        return id != null && id.equals(carrinho.id);
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
