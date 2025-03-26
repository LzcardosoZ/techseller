package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRODUTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produto_id")
    private Long produtoId;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 3, max = 200, message = "O nome deve ter entre 3 e 200 caracteres")
    private String nome;

    @Column(nullable = false, length = 2000)
    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 10, max = 2000, message = "A descrição deve ter entre 10 e 2000 caracteres")
    private String descricaoDetalhada;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @DecimalMin(value = "0.01", message = "O preço mínimo é R$ 0,01")
    private BigDecimal preco;

    @Column(nullable = false)
    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    private Integer quantidadeEstoque;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(nullable = false, columnDefinition = "DECIMAL(2,1)")
    @DecimalMin(value = "0.5", message = "A avaliação mínima é 0.5")
    @DecimalMax(value = "5.0", message = "A avaliação máxima é 5.0")
    @Builder.Default
    private BigDecimal avaliacao = new BigDecimal("0.0");

    @OneToMany(
            mappedBy = "produto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 20)
    @Builder.Default
    private List<ImagemProduto> imagens = new ArrayList<>();

    // Métodos auxiliares para manter a consistência bidirecional
    public void adicionarImagem(ImagemProduto imagem) {
        imagens.add(imagem);
        imagem.setProduto(this);
    }


}