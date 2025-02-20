package com.mx.atendi.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.VentanillaCatalogo;

import reactor.core.publisher.Flux;



public interface VentanillaCatalogoRepository extends ReactiveMongoRepository<VentanillaCatalogo, String> {
	
	Flux<VentanillaCatalogo> findByIdInAndActivoTrue(List<String> ids);
}

