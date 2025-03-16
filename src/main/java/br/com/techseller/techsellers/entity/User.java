package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.bridge.IMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="users", uniqueConstraints = { @UniqueConstraint(columnNames = "email") })
public class User {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Geração automática do ID
    private Long user_id;

    @Getter
    private String username;

    private String cpf;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Digite um email válido")
    private String email;

    private Grupo grupo;
    @NotBlank(message = "A senha é obrigatória")
    private String password;
    @Column(nullable = false)
    private String confPassword;
    @Getter
    private String status;



}
