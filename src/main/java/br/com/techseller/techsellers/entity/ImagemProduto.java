package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "IMAGEM_PRODUTO")
public class ImagemProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imagem_id")
    private Long id;

    @Lob
    @Column(nullable = false)
    private byte[] imagem; // Armazena a imagem como BLOB no banco

    @Column(nullable = false)
    private boolean imagemPrincipal; // Define se é a imagem principal

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
}
