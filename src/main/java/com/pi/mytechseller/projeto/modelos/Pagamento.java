package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false)
    private MetodoPagamento metodoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

=======
    @Column(name = "ID_Pagamento")
    private Long id;

    @OneToOne
    @JoinColumn(name = "ID_Pedido", unique = true, nullable = false)
    private Pedido pedido;

    @Column(name = "Metodo_Pagamento", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    @Column(name = "Status_Pagamento", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPagamento statusPagamento;

    @Column(name = "Data_Pagamento")
    private LocalDateTime dataPagamento;

    // Construtor padrão garantindo valor inicial
    public Pagamento() {
        this.statusPagamento = StatusPagamento.PENDENTE;
    }

    // Método para confirmar pagamento e registrar a data
    public void confirmarPagamento() {
        this.statusPagamento = StatusPagamento.APROVADO;
        this.dataPagamento = LocalDateTime.now();
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

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
<<<<<<< HEAD
        return Objects.equals(id, pagamento.id);
=======
        return id != null && id.equals(pagamento.id);
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
