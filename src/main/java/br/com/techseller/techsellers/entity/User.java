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
    @Column(name = "USER_ID")
    private Long user_id;

    @Getter
    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Column(name = "CPF", unique = true, nullable = false)
    private String cpf;

    @Column(name = "EMAIL", unique = true, nullable = false)
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Digite um email válido")
    private String email;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "GRUPO")
    private Grupo grupo;

    @Column(name = "PASSWORD", nullable = false)
    @NotBlank(message = "A senha é obrigatória")
    private String password;


    @Column(name = "CONF_PASSWORD", nullable = false)
    private String confPassword;

    @Column(name = "STATUS")
    @Getter
    private String status;



}
