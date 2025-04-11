package br.com.techseller.techsellers.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Usuário ou senha inválidos";
        if (exception instanceof UsernameNotFoundException) {
            errorMessage = "Usuário não encontrado";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "Senha incorreta";
        }
        // Redireciona para a página de login com o parâmetro error contendo a mensagem
        response.sendRedirect("/login_cliente?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString()));
    }
}
