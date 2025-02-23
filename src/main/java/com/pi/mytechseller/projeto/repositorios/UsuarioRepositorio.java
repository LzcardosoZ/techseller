package com.pi.mytechseller.projeto.repositorios;

import com.pi.mytechseller.projeto.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}
