package com.mx.atendi.service;

import java.util.List;

import com.mx.atendi.entity.Operacion;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IOperacionService {

	/**
	 * Crea una nueva operación en el catálogo.
	 *
	 * @param operacion Datos de la operación a registrar.
	 * @return Mono con la operación creada.
	 */
	Mono<Operacion> crearOperacion(Operacion operacion);

	/**
	 * Actualiza una operación existente.
	 *
	 * @param id ID de la operación.
	 * @param operacion Datos nuevos de la operación.
	 * @return Mono con la operación actualizada.
	 */
	Mono<Operacion> actualizarOperacion(String id, Operacion operacion);

	/**
	 * Elimina una operación por ID.
	 *
	 * @param id ID de la operación a eliminar.
	 * @return Mono<Void> indicando la operación completada.
	 */
	Mono<Void> eliminarOperacion(String id);

	/**
	 * Obtiene todas las operaciones.
	 *
	 * @return Flux<Operacion> con todas las operaciones registradas.
	 */
	Flux<Operacion> obtenerTodasOperaciones();

	/**
	 * Obtiene una operación por ID.
	 *
	 * @param id ID de la operación.
	 * @return Mono<Operacion> con la operación encontrada.
	 */
	Mono<Operacion> obtenerOperacionPorId(String id);
	
	Flux<Operacion> obtenerOperacionesPorIds(List<String> ids);

	Mono<String> obtenerNombrePorId(String operacionId);

}
