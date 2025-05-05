package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PagamentoCartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false)
    private String nomeTitular;

    @Column(nullable = false)
    private String numeroCartao;

    @Column(nullable = false)
    private String codigoVerificador;

    @Column(nullable = false)
    private String validade;

    @Column(nullable = false)
    private Integer parcelas;
}
