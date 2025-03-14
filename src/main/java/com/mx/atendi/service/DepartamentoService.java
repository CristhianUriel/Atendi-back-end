package com.mx.atendi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mx.atendi.entity.Departamento;
import com.mx.atendi.entity.VentanillaCatalogo;
import com.mx.atendi.repository.DepartamentoRepository;
import com.mx.atendi.repository.OperacionRepository;
import com.mx.atendi.repository.VentanillaCatalogoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class DepartamentoService implements IDepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final OperacionRepository operacionRepository;
    private final VentanillaCatalogoRepository ventanillaCatalogoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository,
                               OperacionRepository operacionRepository,
                               VentanillaCatalogoRepository ventanillaCatalogoRepository) {
        this.departamentoRepository = departamentoRepository;
        this.operacionRepository = operacionRepository;
        this.ventanillaCatalogoRepository = ventanillaCatalogoRepository;
    }

    /**
     * Crea un nuevo departamento validando las operaciones y ventanillas antes de guardarlo.
     *
     * @param departamento Datos del departamento a registrar.
     * @return Mono con el departamento guardado.
     */
    @Override
    public Mono<Departamento> crearDepartamento(Departamento departamento) {
        return validarOperaciones(departamento.getOperacionesIds())
                .then(validarVentanillas(departamento.getVentanillasIds()))
                .then(departamentoRepository.save(departamento))
                .doOnSuccess(dep -> log.info("Departamento creado: {}", dep));
    }

    /**
     * Actualiza un departamento, validando las operaciones y ventanillas antes de guardar.
     *
     * @param id ID del departamento a actualizar.
     * @param departamento Datos actualizados del departamento.
     * @return Mono con el departamento actualizado.
     */
    @Override
    public Mono<Departamento> actualizarDepartamento(String id, Departamento departamento) {
        return departamentoRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Departamento no encontrado")))
                .flatMap(existingDepartamento ->
                        validarOperaciones(departamento.getOperacionesIds())
                                .then(validarVentanillas(departamento.getVentanillasIds()))
                                .then(Mono.defer(() -> {
                                    existingDepartamento.setNombre(departamento.getNombre());
                                    existingDepartamento.setVentanillasIds(departamento.getVentanillasIds());
                                    existingDepartamento.setOperacionesIds(departamento.getOperacionesIds());
                                    return departamentoRepository.save(existingDepartamento);
                                }))
                ).doOnSuccess(dep -> log.info("Departamento actualizado: {}", dep));
    }

    /**
     * Elimina un departamento por ID.
     *
     * @param id ID del departamento a eliminar.
     * @return Mono<Void> indicando la operación completada.
     */
    @Override
    public Mono<Void> eliminarDepartamento(String id) {
        return departamentoRepository.deleteById(id)
                .doOnSuccess(unused -> log.info("Departamento eliminado con ID: {}", id));
    }

    /**
     * Obtiene todos los departamentos registrados.
     *
     * @return Flux<Departamento> con la lista de departamentos.
     */
    @Override
    public Flux<Departamento> obtenerDepartamentos() {
        return departamentoRepository.findAll();
    }

    /**
     * Obtiene un departamento por su ID.
     *
     * @param id ID del departamento.
     * @return Mono<Departamento> con el departamento encontrado.
     */
    @Override
    public Mono<Departamento> obtenerDepartamentoPorId(String id) {
        return departamentoRepository.findById(id);
    }

    /**
     * Valida que los IDs de operaciones existan en la BD.
     *
     * @param operacionesIds Lista de IDs de operaciones.
     * @return Mono<Void> si todas las operaciones existen, error si alguna no existe.
     */
    private Mono<Void> validarOperaciones(List<String> operacionesIds) {
        return operacionRepository.findAllById(operacionesIds)
                .collectList()
                .flatMap(operaciones -> {
                    if (operaciones.size() != operacionesIds.size()) {
                        return Mono.error(new RuntimeException("Algunas operaciones no existen en la BD"));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Valida que los IDs de ventanillas existan y estén activas en la BD.
     *
     * @param ventanillasIds Lista de IDs de ventanillas.
     * @return Mono<Void> si todas las ventanillas existen y están activas, error en caso contrario.
     */
    private Mono<Void> validarVentanillas(List<String> ventanillasIds) {
        return ventanillaCatalogoRepository.findAllById(ventanillasIds)
                .filter(VentanillaCatalogo::isActivo)
                .collectList()
                .flatMap(ventanillas -> {
                    if (ventanillas.size() != ventanillasIds.size()) {
                        return Mono.error(new RuntimeException("Algunas ventanillas no existen o están inactivas"));
                    }
                    return Mono.empty();
                });
    }
    
    @Override
    public Mono<String> obtenerNombrePorId(String departamentoId) {
    	log.info("departamentoId [{}]",departamentoId);
        return departamentoRepository.findById(departamentoId)
                .map(Departamento::getNombre)
                .switchIfEmpty(Mono.just("Desconocido"));
    }
}

