package br.com.techseller.techsellers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static br.com.techseller.techsellers.utils.CpfValidator.isValidCPF;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class TechsellersApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechsellersApplication.class, args);
        System.out.println("running");


	}

	@Bean
	public CommandLineRunner testarSenhaManual(PasswordEncoder encoder, ClienteRepository clienteRepository) {
		return args -> {
			String senhaEmTexto = "123456";
			String hashGerada = encoder.encode(senhaEmTexto);
			System.out.println("➡️ Hash gerada agora: " + hashGerada);

			Optional<Cliente> clienteOpt = clienteRepository.findByEmail("davi.spatricio@senacsp.edu.br");
			if (clienteOpt.isPresent()) {
				Cliente cliente = clienteOpt.get();
				boolean ok = encoder.matches(senhaEmTexto, cliente.getSenha());
				System.out.println("🔐 Teste manual de senha: " + ok);
			} else {
				System.out.println("⚠️ Cliente não encontrado.");
			}
		};
	}

}
