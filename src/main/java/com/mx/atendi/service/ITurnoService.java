package com.mx.atendi.service;

import java.util.List;

import com.mx.atendi.entity.Turno;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Define la API del servicio de turnos.
 */
public interface ITurnoService {
    /**
     * Crea un nuevo turno.
     *
     * @param turno El turno a crear.
     * @return Mono que emite el turno creado.
     */
	Mono<Turno> crearTurno(Turno turno);
	 /**
     * Actualiza el estado de un turno.
     *
     * @param id     ID del turno.
     * @param estado Nuevo estado.
     * @return Mono que emite el turno actualizado.
     */
	Mono<Turno> actualizarEstado(String id, String estado);
    /**
     * Devuelve un flujo de turnos en tiempo real para un hospital.
     *
     * @param hospitalId ID del hospital.
     * @return Flux que emite los turnos del hospital.
     */
	Flux<Turno> streamTurnos(String hospitalId);
    /**
     * Devuelve un flujo de turnos pendientes filtrados por tipos de operación (para ventanillas).
     *
     * @param hospitalId            ID del hospital.
     * @param tiposOperacionAllowed Iterable de tipos de operación permitidos.
     * @return Flux que emite los turnos filtrados.
     */
	Flux<Turno> streamTurnosPorOperacion(String hospitalId, Iterable<String> tiposOperacionAllowed);
}
