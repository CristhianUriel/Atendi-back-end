package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.Departamento;

public interface DepartamentoRepository  extends ReactiveMongoRepository<Departamento, String> {

}
