package br.com.techseller.techsellers.service;


import br.com.techseller.techsellers.entity.Grupo;
import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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



}