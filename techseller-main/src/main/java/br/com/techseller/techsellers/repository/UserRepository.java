package br.com.techseller.techsellers.repository;

import br.com.techseller.techsellers.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long>{

    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);
    List<User> findByUsernameContainingIgnoreCase(String username);
}
