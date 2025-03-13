package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.bridge.IMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="user", uniqueConstraints = { @UniqueConstraint(columnNames = "email") })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Geração automática do ID
    private Long user_id;

    private String username;

    private String cpf;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Digite um email válido")
    private String email;

    private String grupo;
    @NotBlank(message = "A senha é obrigatória")
    private String password;

    private String confPassword;
    private String status;

}
