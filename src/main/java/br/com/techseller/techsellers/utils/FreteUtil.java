package br.com.techseller.techsellers.utils;

import br.com.techseller.techsellers.enums.TipoFrete;

import java.math.BigDecimal;

public class FreteUtil {

    public static BigDecimal calcularFrete(TipoFrete tipo, BigDecimal subtotal) {
        switch (tipo) {
            case MUNICIPAL:
                return BigDecimal.valueOf(10.00);
            case ESTADUAL:
                return BigDecimal.valueOf(20.00);
            case NACIONAL:
                return BigDecimal.valueOf(35.00);
            default:
                return BigDecimal.ZERO;
        }
    }
}
