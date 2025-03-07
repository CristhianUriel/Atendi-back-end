package com.mx.atendi.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.Operacion;

import reactor.core.publisher.Flux;

public interface OperacionRepository extends ReactiveMongoRepository<Operacion, String> {
	Flux<Operacion> findByIdIn(List<String>  ids);
}
