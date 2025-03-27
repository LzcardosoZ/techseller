package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    // Campo opcional - manter temporariamente para migração
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.LAZY)
    private byte[] imagem;  // Pode ser removido após migração completa

    @Column(nullable = false)
    private Boolean imagemPrincipal;

    // Novos campos para armazenamento híbrido
    @Column(nullable = true, length = 255)
    private String caminhoArquivo;

    @Column(nullable = false, length = 100)
    private String nomeArquivo;

    @Column(nullable = false)
    private Long tamanho;

    @Column(nullable = false, length = 50)
    private String tipoMime;

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


    public String getExtensao() {
        return nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1);
    }
}