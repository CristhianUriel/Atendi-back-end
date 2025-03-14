package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.mx.atendi.entity.Video;

import reactor.core.publisher.Mono;

@Repository
public interface VideoRepository extends ReactiveMongoRepository<Video, String> {

	Mono<Void> deleteByNombre(String nombre);
}

