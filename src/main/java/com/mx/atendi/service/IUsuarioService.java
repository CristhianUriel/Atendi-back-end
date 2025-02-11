package com.mx.atendi.service;


import com.mx.atendi.entity.Usuario;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Define la API del servicio de usuarios.
 */
public interface IUsuarioService {
    Mono<Usuario> crearUsuario(Usuario usuario);
    Mono<Usuario> obtenerUsuario(String id);
    Flux<Usuario> listarUsuarios();
 // Agregar este método para buscar el usuario por userName y hospitalId
    Mono<Usuario> findByUserNameAndHospitalId(String userName, String hospitalId);
}

