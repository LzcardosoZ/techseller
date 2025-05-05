package br.com.techseller.techsellers.entity;

import br.com.techseller.techsellers.enums.Grupo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="users", uniqueConstraints = { @UniqueConstraint(columnNames = "email") })
public class User implements UserDetails {

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

    @Getter
    @Column(name = "STATUS", nullable = false)
    private boolean status = true; //usuario começa como ativo


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username; // Mantém o e-mail como identificador do Spring Security
    }

    public String getNomeUsuario() {
        return username;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.username = nomeUsuario;
    }

    public String getLoginEmail() {
        return email;
    }

    public String getName() {
        return username; // Retorna o nome do usuário corretamente
    }

    public String getDisplayName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_" + grupo.name()); // Retorna "ROLE_ADMIN" ou "ROLE_ESTOQUISTA"
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }



    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }


    @Transient
    private String newPassword;

    @Transient
    private String confNewPassword;
}
