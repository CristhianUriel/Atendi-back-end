package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mx.atendi.entity.Turno;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad Turno.
 */
@Repository
public interface TurnoRepository extends ReactiveMongoRepository<Turno, String>{
    /**
     * Obtiene todos los turnos de un hospital.
     *
     * @param hospitalId ID del hospital.
     * @return Flux con los turnos del hospital.
     */
	Flux<Turno> findByHospitalId(String hospitalId);
    /**
     * Obtiene turnos de un hospital filtrados por un conjunto de tipos de operación y estado.
     *
     * @param hospitalId     ID del hospital.
     * @param tiposOperacion Iterable de tipos de operación.
     * @param estado         Estado del turno.
     * @return Flux con los turnos filtrados.
     */
    Flux<Turno> findByHospitalIdAndTipoOperacionInAndEstado(String hospitalId, Iterable<String> tiposOperacion, String estado);
    /**
     * Obtiene un turno por su ID y hospital.
     *
     * @param id         ID del turno.
     * @param hospitalId ID del hospital.
     * @return Mono con el turno encontrado.
     */
    Mono<Turno> findByIdAndHospitalId(String id, String hospitalId);
}
