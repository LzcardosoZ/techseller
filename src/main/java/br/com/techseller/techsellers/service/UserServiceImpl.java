package br.com.techseller.techsellers.service;


import br.com.techseller.techsellers.entity.Grupo;
import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void registeruser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User autenticar(String email, String senha) {
        Optional<User> usuario = userRepository.findByEmail(email);
        if(usuario.isPresent() && passwordEncoder.matches(senha, usuario.get().getPassword())) {
            return usuario.get();
        }
        throw new RuntimeException("E-mail ou senha inavlidos");
    }

    public boolean isAdmin(User user) {
        return user.getGrupo() == Grupo.ADMIN;
    }

    public boolean isEstoque(User user) {
        return user.getGrupo() == Grupo.ESTOQUE;
    }
}