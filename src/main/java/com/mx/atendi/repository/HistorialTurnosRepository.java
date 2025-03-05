package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.HistorialTurnos;

public interface HistorialTurnosRepository extends ReactiveMongoRepository<HistorialTurnos, String> {

}
