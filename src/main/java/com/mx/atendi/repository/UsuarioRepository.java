package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mx.atendi.entity.Usuario;

import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad Usuario.
 */
public interface UsuarioRepository extends ReactiveMongoRepository<Usuario, String> {
	   // Agregar este método para buscar el usuario por userName y hospitalId
    Mono<Usuario> findByUserNameAndHospitalId(String userName, String hospitalId);
}
