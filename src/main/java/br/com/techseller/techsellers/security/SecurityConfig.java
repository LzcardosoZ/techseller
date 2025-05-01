package br.com.techseller.techsellers.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/login", "/login_cliente", "/cadastro", "/registerUser", "/clientes/cadastro").permitAll()
                        .requestMatchers("/produtos/editar/**").hasAnyRole("ADMIN", "ESTOQUISTA")
                        .requestMatchers("/produtos/gerenciar-produtos").hasAnyRole("ADMIN", "ESTOQUISTA")
                        .requestMatchers(HttpMethod.GET, "/menu", "/listarUsuarios", "/usuarios/**", "/home").authenticated()
                        .requestMatchers("/", "/loja/**", "/carrinho", "/carrinho/adicionar", "/carrinho/remover", "/carrinho/atualizar", "/carrinho/frete", "/css/**", "/js/**", "/img/**").permitAll()
                        .requestMatchers("/carrinho/finalizar").authenticated()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // <- ESSENCIAL
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable()) // <- ou .sameOrigin() se quiser manter segurança
                )
                .formLogin(form -> form
                        .loginPage("/login_cliente")
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler)
                        .failureUrl("/login_cliente?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")  // troca aqui!
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );


        return http.build();
    }


    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
