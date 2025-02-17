package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.VentanillaCatalogo;



public interface VentanillaCatalogoRepository extends ReactiveMongoRepository<VentanillaCatalogo, String> {

}

