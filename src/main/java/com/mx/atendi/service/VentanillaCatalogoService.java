package com.mx.atendi.service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mx.atendi.entity.VentanillaCatalogo;
import com.mx.atendi.repository.VentanillaCatalogoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VentanillaCatalogoService implements IVentanillaCatalogoService {

    private final VentanillaCatalogoRepository ventanillaCatalogoRepository;

    public VentanillaCatalogoService(VentanillaCatalogoRepository ventanillaCatalogoRepository) {
        this.ventanillaCatalogoRepository = ventanillaCatalogoRepository;
    }

    /**
     * Crea una nueva ventanilla en el catálogo.
     *
     * @param ventanilla Datos de la ventanilla a registrar.
     * @return Mono con la ventanilla creada.
     */
    @Override
    public Mono<VentanillaCatalogo> crearVentanilla(VentanillaCatalogo ventanilla) {
        return ventanillaCatalogoRepository.save(ventanilla)
                .doOnSuccess(v -> log.info("Ventanilla creada: {}", v));
    }

    /**
     * Actualiza una ventanilla existente.
     *
     * @param id ID de la ventanilla.
     * @param ventanilla Datos nuevos de la ventanilla.
     * @return Mono con la ventanilla actualizada.
     */
    @Override
    public Mono<VentanillaCatalogo> actualizarVentanilla(String id, VentanillaCatalogo ventanilla) {
        return ventanillaCatalogoRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Ventanilla no encontrada")))
                .flatMap(existingVentanilla -> {
                    existingVentanilla.setNombre(ventanilla.getNombre());
                    return ventanillaCatalogoRepository.save(existingVentanilla);
                }).doOnSuccess(v -> log.info("Ventanilla actualizada: {}", v));
    }

    /**
     * Elimina una ventanilla por ID.
     *
     * @param id ID de la ventanilla a eliminar.
     * @return Mono<Void> indicando la operación completada.
     */
    @Override
    public Mono<Void> eliminarVentanilla(String id) {
        return ventanillaCatalogoRepository.deleteById(id)
                .doOnSuccess(unused -> log.info("Ventanilla eliminada con ID: {}", id));
    }

    /**
     * Obtiene todas las ventanillas.
     *
     * @return Flux<VentanillaCatalogo> con todas las ventanillas registradas.
     */
    @Override
    public Flux<VentanillaCatalogo> obtenerTodasVentanillas() {
        return ventanillaCatalogoRepository.findAll();
    }

    /**
     * Obtiene una ventanilla por ID.
     *
     * @param id ID de la ventanilla.
     * @return Mono<VentanillaCatalogo> con la ventanilla encontrada.
     */
    @Override
    public Mono<VentanillaCatalogo> obtenerVentanillaPorId(String id) {
        return ventanillaCatalogoRepository.findById(id);
    }
    
    
    
    @Override
    public Mono<VentanillaCatalogo> cambiarEstadoVentanilla(String id, boolean activo) {
        return ventanillaCatalogoRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Ventanilla no encontrada")))
                .flatMap(existingVentanilla -> {
                    existingVentanilla.setActivo(activo);
                    return ventanillaCatalogoRepository.save(existingVentanilla);
                }).doOnSuccess(v -> log.info("Ventanilla {} {}", v.getId(), activo ? "activada" : "desactivada"));
    }

	@Override
	public Flux<VentanillaCatalogo> obtenerVentanillasPorIds(List<String> ids) {
		// TODO Auto-generated method stub
		return ventanillaCatalogoRepository.findByIdIn(ids);
	}



}
