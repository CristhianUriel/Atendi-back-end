package com.mx.atendi.service;

import java.util.List;

import com.mx.atendi.entity.HistorialTurnos;
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
	Mono<Turno> crearTurno(Turno turno, String rolUsuario);
    /**
     * Devuelve un flujo de turnos en tiempo real para un hospital.
     *
     * @param hospitalId ID del hospital.
     * @return Flux que emite los turnos del hospital.
     */
	 Flux<Turno> streamTurnos(String hospitalId, String departamentoId, boolean esMonitor);
	/**
	 * Devuelve los últimos turnos atendidos para mostrarlos en la pantalla.
	 *
	 * @param hospitalId ID del hospital
	 * @param cantidad Número de turnos a devolver
	 * @return Flux con los últimos turnos atendidos
	 */
	Flux<HistorialTurnos> obtenerTurnosUltimosAtendidos(String hospitalId, int cantidad);

	/**
	 * Finaliza un turno, marcándolo como atendido o no atendido.
	 *
	 * @param turnoId ID del turno.
	 * @param usuarioId ID del usuario que atendió el turno.
	 * @param estadoFinal Estado final del turno ("atendido" o "no atendido").
	 * @return Mono<Turno> con el turno finalizado.
	 */
	Mono<Turno> finalizarTurno(String turnoId, String usuarioId, String estadoFinal);
	/**
	 * Permite que un usuario tome un turno dentro de su departamento.
	 * - Solo los usuarios del mismo departamento pueden tomar el turno.
	 * - Un turno solo puede ser tomado si está en estado "pendiente".
	 * - Si otro usuario ya tomó el turno, se devuelve un error.
	 *
	 * @param turnoId ID del turno a tomar.
	 * @param usuarioId ID del usuario que desea tomar el turno.
	 * @param departamentoId ID del departamento del usuario.
	 * @return Mono<Turno> con el turno actualizado si la operación fue exitosa.
	 */
	Mono<Turno> tomarTurno(String turnoId, String usuarioId, String departamentoId);
}
