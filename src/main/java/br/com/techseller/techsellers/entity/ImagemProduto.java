package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "IMAGEM_PRODUTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagemProduto {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imagem_id")
    private Long id;


    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.LAZY)
    private byte[] imagem;

    @Column(nullable = false)
    private Boolean imagemPrincipal;

    // Novos campos para armazenamento híbrido
    @Column(nullable = false, length = 255)
    private String caminhoArquivo;

    @Column(nullable = false, length = 100)
    private String nomeArquivo;

    @Column(nullable = false)
    private Long tamanho;

    @Column(nullable = false, length = 50)
    private String tipoMime;

    @Column(nullable = false)
    private Integer ordem; // Novo campo para ordenação das imagens

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "produto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_imagem_produto")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Produto produto;

    public void setProduto(Produto produto) {
        if (this.produto != null) {
            this.produto.getImagens().remove(this);
        }
        this.produto = produto;
        if (produto != null && !produto.getImagens().contains(this)) {
            produto.getImagens().add(this);
        }
    }

}