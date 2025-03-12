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
     * Obtiene un turno por su ID y hospital.
     *
     * @param id         ID del turno.
     * @param hospitalId ID del hospital.
     * @return Mono con el turno encontrado.
     */
    Mono<Turno> findByIdAndHospitalId(String id, String hospitalId);
    
    Flux<Turno> findByHospitalIdAndEstado(String hospitalId, String estado);
    
}
