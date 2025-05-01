/*package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarUsuarioComDadosValidos() {
        User user = new User();
        user.setNome("Fulano");
        user.setEmail("fulano@email.com");
        user.setSenha("123456");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User salvo = userService.salvar(user);

        assertNotNull(salvo);
        assertEquals("Fulano", salvo.getNome());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        String email = "teste@email.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> resultado = userService.buscarPorEmail(email);

        assertTrue(resultado.isPresent());
        assertEquals(email, resultado.get().getEmail());
    }
}
*/