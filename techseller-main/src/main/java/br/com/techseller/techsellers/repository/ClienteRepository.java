package br.com.techseller.techsellers.repository;

import br.com.techseller.techsellers.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Verifica se e-mail já existe (para validação)
    boolean existsByEmail(String email);

    // Verifica se CPF já existe (para validação)
    boolean existsByCpf(String cpf);

    // Busca por e-mail (útil para login)
    Optional<Cliente> findByEmail(String email);


}

