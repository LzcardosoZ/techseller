package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService; // Use a implementação concreta, não a interface

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarUsuarioComDadosValidos() {
        User user = new User();
        user.setUsername("Fulano");
        user.setEmail("fulano@email.com");
        user.setPassword("123456");
        user.setCpf("12345678909");

        when(userRepository.save(any(User.class))).thenReturn(user);

        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("senhaCriptografada");

        userService.registeruser(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User salvo = captor.getValue();
        assertNotNull(salvo);
        assertEquals("Fulano", salvo.getUsername());
        assertEquals("fulano@email.com", salvo.getEmail());
        assertEquals("senhaCriptografada", salvo.getPassword());
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        String email = "teste@email.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> resultado = userService.findByEmail(email);

        assertTrue(resultado.isPresent());
        assertEquals(email, resultado.get().getEmail());
    }
}
