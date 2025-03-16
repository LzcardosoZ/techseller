package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.User;

import java.util.Optional;

public interface UserService {
    //insere os dados no banco de dados



    public void registeruser(User user);
    Optional<User> findByEmail(String email);

}
