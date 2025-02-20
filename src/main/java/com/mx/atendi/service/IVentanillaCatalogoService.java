package com.mx.atendi.service;



import java.util.List;

import com.mx.atendi.entity.VentanillaCatalogo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IVentanillaCatalogoService {

	/**
	 * Crea una nueva ventanilla en el catálogo.
	 *
	 * @param ventanilla Datos de la ventanilla a registrar.
	 * @return Mono con la ventanilla creada.
	 */
	Mono<VentanillaCatalogo> crearVentanilla(VentanillaCatalogo ventanilla);

	/**
	 * Actualiza una ventanilla existente.
	 *
	 * @param id ID de la ventanilla.
	 * @param ventanilla Datos nuevos de la ventanilla.
	 * @return Mono con la ventanilla actualizada.
	 */
	Mono<VentanillaCatalogo> actualizarVentanilla(String id, VentanillaCatalogo ventanilla);

	/**
	 * Elimina una ventanilla por ID.
	 *
	 * @param id ID de la ventanilla a eliminar.
	 * @return Mono<Void> indicando la operación completada.
	 */
	Mono<Void> eliminarVentanilla(String id);

	/**
	 * Obtiene todas las ventanillas.
	 *
	 * @return Flux<VentanillaCatalogo> con todas las ventanillas registradas.
	 */
	Flux<VentanillaCatalogo> obtenerTodasVentanillas();

	/**
	 * Obtiene una ventanilla por ID.
	 *
	 * @param id ID de la ventanilla.
	 * @return Mono<VentanillaCatalogo> con la ventanilla encontrada.
	 */
	Mono<VentanillaCatalogo> obtenerVentanillaPorId(String id);

	Mono<VentanillaCatalogo> cambiarEstadoVentanilla(String id, boolean activo);
	
	Flux<VentanillaCatalogo> obtenerVentanillasPorIds(List<String> ids);



}
