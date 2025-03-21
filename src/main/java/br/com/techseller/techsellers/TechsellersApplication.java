package br.com.techseller.techsellers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import static br.com.techseller.techsellers.utils.CpfValidator.isValidCPF;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class TechsellersApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechsellersApplication.class, args);
        System.out.println("running");
		String cpf = "529.982.247-25";
		String cpfLimpo = cpf.replaceAll("[^0-9]", "");
		System.out.println("CPF recebido: '" + cpf + "'");
		System.out.println("CPF formatado: '" + cpfLimpo + "'");
		System.out.println("CPF válido? " + isValidCPF(cpfLimpo));

	}

}
