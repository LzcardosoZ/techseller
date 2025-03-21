package br.com.techseller.techsellers.utils;

public class CpfValidator {
    public static boolean isValidCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", ""); // Remove pontos e traço

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) { // Verifica se tem 11 dígitos e não é repetitivo
            return false;
        }

        int[] pesos1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        try {
            int digito1 = calcularDigito(cpf.substring(0, 9), pesos1);
            int digito2 = calcularDigito(cpf.substring(0, 9) + digito1, pesos2);

            return cpf.equals(cpf.substring(0, 9) + digito1 + digito2);
        } catch (Exception e) {
            return false;
        }
    }

    private static int calcularDigito(String str, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < str.length(); i++) {
            soma += (str.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : (11 - resto);
    }

}

