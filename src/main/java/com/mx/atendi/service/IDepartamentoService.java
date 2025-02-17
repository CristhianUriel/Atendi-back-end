package com.mx.atendi.service;

import com.mx.atendi.entity.Departamento;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IDepartamentoService {

	/**
	 * Crea un nuevo departamento, validando que las operaciones y ventanillas existan.
	 *
	 * @param departamento Datos del departamento a crear.
	 * @return Mono con el departamento guardado.
	 */
	Mono<Departamento> crearDepartamento(Departamento departamento);

	/**
	 * Actualiza un departamento, validando las operaciones y ventanillas.
	 *
	 * @param id ID del departamento a actualizar.
	 * @param departamento Datos del departamento actualizado.
	 * @return Mono con el departamento actualizado.
	 */
	Mono<Departamento> actualizarDepartamento(String id, Departamento departamento);

	/**
	 * Elimina un departamento por ID.
	 *
	 * @param id ID del departamento a eliminar.
	 * @return Mono<Void> indicando la operación completada.
	 */
	Mono<Void> eliminarDepartamento(String id);

	/**
	 * Obtiene todos los departamentos.
	 *
	 * @return Flux<Departamento> con la lista de departamentos.
	 */
	Flux<Departamento> obtenerDepartamentos();

	/**
	 * Obtiene un departamento por ID.
	 *
	 * @param id ID del departamento.
	 * @return Mono<Departamento> con el departamento encontrado.
	 */
	Mono<Departamento> obtenerDepartamentoPorId(String id);

}
