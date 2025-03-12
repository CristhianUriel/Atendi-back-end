package com.mx.atendi.service;

import com.mx.atendi.entity.Turno;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IImpresionService {

    /**
     * Imprime un ticket individual.
     *
     * @param turno Turno a imprimir.
     * @param impresora Nombre de la impresora (opcional).
     * @return Mono<String> con el resultado de la impresión.
     */
    Mono<String> imprimirTicket(Turno turno, String impresora);

    /**
     * Imprime múltiples tickets a la vez.
     *
     * @param turnos Lista de turnos a imprimir.
     * @param impresora Nombre de la impresora (opcional).
     * @return Mono<String> con el resultado de la impresión.
     */
    Mono<String> imprimirVariosTickets(List<Turno> turnos, String impresora);

	Mono<List<String>> listarImpresorasDisponibles();
}
