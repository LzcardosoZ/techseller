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

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.LAZY)
    private byte[] imagem;

    @Column(nullable = false)
    private Boolean imagemPrincipal;

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

    // Método para definir o produto mantendo a consistência bidirecional
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