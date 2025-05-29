package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static br.com.techseller.techsellers.utils.CpfValidator.isValidCPF;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registeruser(User user) {
        if (!isValidCPF(user.getCpf())) {
            throw new IllegalArgumentException("CPF inválido!");
        }

        if (userRepository.findByCpf(user.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(true);
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<User> usuario = userRepository.findByEmail(email);

        usuario.ifPresent(u ->
                System.out.println("Usuário encontrado: " + u.getEmail() + " | Grupo: " + u.getGrupo())
        );

        return usuario;
    }

    @Override
    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    @Override
    public User buscarPorId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Override
    public void atualizarUsuario(Long userId, User usuarioAtualizado) {
        User usuarioExistente = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuarioAtualizado.getNomeUsuario() != null && !usuarioAtualizado.getNomeUsuario().isEmpty()) {
            usuarioExistente.setUsername(usuarioAtualizado.getNomeUsuario());
        }
        if (usuarioAtualizado.getCpf() != null && !usuarioAtualizado.getCpf().isEmpty()) {
            String cpfLimpo = usuarioAtualizado.getCpf().replaceAll("[^0-9]", ""); // Remove formatação
            if (!isValidCPF(cpfLimpo)) {
                throw new IllegalArgumentException("CPF inválido!");
            }
            usuarioExistente.setCpf(cpfLimpo);
        }
        if (usuarioAtualizado.getGrupo() != null) {
            usuarioExistente.setGrupo(usuarioAtualizado.getGrupo());
        }
        usuarioExistente.setStatus(usuarioAtualizado.isStatus());

        userRepository.save(usuarioExistente);
    }
}