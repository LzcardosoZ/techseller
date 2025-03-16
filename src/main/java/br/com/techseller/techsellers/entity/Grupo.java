package br.com.techseller.techsellers.entity;

public enum Grupo {
    ADMIN(0),
    ESTOQUISTA(1);

    private final int value;

    Grupo(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Grupo fromValue(int value) {
        for (Grupo grupo : Grupo.values()) {
            if (grupo.getValue() == value) {
                return grupo;
            }
        }
        throw new IllegalArgumentException("Valor inválido para Grupo: " + value);
    }
}
