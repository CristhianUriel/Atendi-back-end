package com.mx.atendi.service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mx.atendi.entity.Operacion;
import com.mx.atendi.repository.OperacionRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OperacionService  implements IOperacionService{

    private final OperacionRepository operacionRepository;

    public OperacionService(OperacionRepository operacionRepository) {
        this.operacionRepository = operacionRepository;
    }

    /**
     * Crea una nueva operación en el catálogo.
     *
     * @param operacion Datos de la operación a registrar.
     * @return Mono con la operación creada.
     */
    @Override
    public Mono<Operacion> crearOperacion(Operacion operacion) {
        return operacionRepository.save(operacion)
                .doOnSuccess(op -> log.info("Operación creada: {}", op));
    }

    /**
     * Actualiza una operación existente.
     *
     * @param id ID de la operación.
     * @param operacion Datos nuevos de la operación.
     * @return Mono con la operación actualizada.
     */
    @Override
    public Mono<Operacion> actualizarOperacion(String id, Operacion operacion) {
        return operacionRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Operación no encontrada")))
                .flatMap(existingOperacion -> {
                    existingOperacion.setNombre(operacion.getNombre());
                    return operacionRepository.save(existingOperacion);
                }).doOnSuccess(op -> log.info("Operación actualizada: {}", op));
    }

    /**
     * Elimina una operación por ID.
     *
     * @param id ID de la operación a eliminar.
     * @return Mono<Void> indicando la operación completada.
     */
    @Override
    public Mono<Void> eliminarOperacion(String id) {
        return operacionRepository.deleteById(id)
                .doOnSuccess(unused -> log.info("Operación eliminada con ID: {}", id));
    }

    /**
     * Obtiene todas las operaciones.
     *
     * @return Flux<Operacion> con todas las operaciones registradas.
     */
    @Override
    public Flux<Operacion> obtenerTodasOperaciones() {
        return operacionRepository.findAll();
    }

    /**
     * Obtiene una operación por ID.
     *
     * @param id ID de la operación.
     * @return Mono<Operacion> con la operación encontrada.
     */
    @Override
    public Mono<Operacion> obtenerOperacionPorId(String id) {
        return operacionRepository.findById(id);
    }
    @Override
    public Flux<Operacion> obtenerOperacionesPorIds(List<String> ids){
    	// TODO Auto-generated method stub
   return operacionRepository.findByIdIn(ids);
    	
    }
}

