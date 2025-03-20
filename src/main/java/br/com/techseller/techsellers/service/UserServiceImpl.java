package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(true);
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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

        // ✅ Agora altera corretamente o nome do usuário
        if (usuarioAtualizado.getNomeUsuario() != null && !usuarioAtualizado.getNomeUsuario().isEmpty()) {
            usuarioExistente.setNomeUsuario(usuarioAtualizado.getNomeUsuario());
        }
        if (usuarioAtualizado.getCpf() != null && !usuarioAtualizado.getCpf().isEmpty()) {
            usuarioExistente.setCpf(usuarioAtualizado.getCpf());
        }
        if (usuarioAtualizado.getGrupo() != null) {
            usuarioExistente.setGrupo(usuarioAtualizado.getGrupo());
        }
        usuarioExistente.setStatus(usuarioAtualizado.isStatus());

        userRepository.save(usuarioExistente);
    }

}
