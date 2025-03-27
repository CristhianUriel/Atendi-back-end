package com.mx.atendi.utils;

public enum EstatusTurno {
    
    PENDIENTE("pendiente"),
    EN_PROCESO("en proceso"),
    ATENDIDO("atendido"),
    CANCELADO("cancelado"),
    ELIMINADO("eliminado");

    private final String valor;

    EstatusTurno(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return this.valor;
    }
}
