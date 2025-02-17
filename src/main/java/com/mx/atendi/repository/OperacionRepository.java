package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.Operacion;

public interface OperacionRepository extends ReactiveMongoRepository<Operacion, String> {
}
