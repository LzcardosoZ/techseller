package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import br.com.techseller.techsellers.utils.CpfValidator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @PostConstruct
    public void init() {
        System.out.println("🚀 UserServiceImpl iniciado!");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void registeruser(User user) {
        if (!CpfValidator.isValidCPF(user.getCpf())) {
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
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }
}
