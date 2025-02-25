package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
import java.util.Objects;

@Entity
@Table(name = "Pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status = StatusPedido.PENDENTE;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens;
=======
    @Column(name = "ID_Pedido")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "Data_Pedido", nullable = false)
    private LocalDateTime dataPedido;

    @Column(name = "Status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    // Construtor padrão
    public Pedido() {
        this.dataPedido = LocalDateTime.now();
        this.status = StatusPedido.PENDENTE;
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

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

<<<<<<< HEAD
    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

=======
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
<<<<<<< HEAD
        return Objects.equals(id, pedido.id);
=======
        return id != null && id.equals(pedido.id);
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
