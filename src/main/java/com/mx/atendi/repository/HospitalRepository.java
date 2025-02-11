package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.Hospital;

import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad Hospital.
 */
public interface HospitalRepository extends ReactiveMongoRepository<Hospital, String> {
	
	Mono<Hospital> findByNombre(String nombre);
}

