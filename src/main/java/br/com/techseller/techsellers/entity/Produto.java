package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUTO")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produto_id")
    private Long produtoId;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "O nome do produto é obrigatório")
    private String nome;

    @Column(nullable = false, length = 2000)
    private String descricaoDetalhada;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", message = "O preço deve ser positivo")
    private double preco;

    @Column(nullable = false)
    @Min(value = 0, message = "A quantidade em estoque deve ser positiva")
    private int quantidadeEstoque;

    @Column(nullable = false)
    private boolean ativo = true; // Produto começa como ativo

    @Column(nullable = true, length = 500)
    private String imagem; // URL ou caminho da imagem

    @Column(nullable = true, length = 500)
    private String imagemPrincipal; // URL da imagem principal

    @Column(nullable = false)
    @DecimalMin(value = "0.5", message = "A avaliação mínima é 0.5")
    @DecimalMax(value = "5.0", message = "A avaliação máxima é 5.0")
    private double avaliacao;

    private String imagemUrl;
}
