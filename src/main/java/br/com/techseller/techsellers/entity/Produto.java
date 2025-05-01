package br.com.techseller.techsellers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "PRODUTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"imagens", "arquivosImagens"}) // Exclui ambos os campos do toString
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

    // Relacionamento com as imagens persistidas
    @OneToMany(
            mappedBy = "produto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 20)
    @JsonIgnore
    @Builder.Default
    private List<ImagemProduto> imagens = new ArrayList<>();

    // Campo transient para receber os arquivos do formulário
    @Transient
    @JsonIgnore
    private MultipartFile[] arquivosImagens;

    // ============== MÉTODOS AUXILIARES ==============

    /**
     * Garante que a lista de imagens nunca seja nula
     */
    public List<ImagemProduto> getImagens() {
        if (this.imagens == null) {
            this.imagens = new ArrayList<>();
        }
        return this.imagens;
    }

    /**
     * Adiciona uma imagem à lista mantendo a consistência bidirecional
     */
    public void adicionarImagem(ImagemProduto imagem) {
        getImagens().add(imagem);
        imagem.setProduto(this);
    }

    /**
     * Remove uma imagem da lista mantendo a consistência bidirecional
     */
    public void removerImagem(ImagemProduto imagem) {
        getImagens().remove(imagem);
        imagem.setProduto(null);
    }

    /**
     * Verifica se o produto possui imagens associadas
     */
    @Transient
    public boolean temImagens() {
        return !getImagens().isEmpty();
    }

    /**
     * Obtém a imagem principal do produto
     */
    @Transient
    public ImagemProduto getImagemPrincipal() {
        return getImagens().stream()
                .filter(ImagemProduto::getImagemPrincipal)
                .findFirst()
                .orElse(getImagens().isEmpty() ? null : getImagens().get(0));
    }

    /**
     * Método auxiliar para obter os arquivos de imagem
     */
    @Transient
    public MultipartFile[] getArquivosImagens() {
        return arquivosImagens;
    }

    /**
     * Método auxiliar para definir os arquivos de imagem
     */
    @Transient
    public void setArquivosImagens(MultipartFile[] arquivosImagens) {
        this.arquivosImagens = arquivosImagens;
    }

    /**
     * Verifica se há arquivos de imagem para upload
     */
    @Transient
    public boolean temArquivosImagens() {
        return arquivosImagens != null && arquivosImagens.length > 0 &&
                !Arrays.stream(arquivosImagens).allMatch(MultipartFile::isEmpty);
    }
}