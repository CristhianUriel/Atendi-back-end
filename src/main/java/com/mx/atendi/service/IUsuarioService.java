package com.mx.atendi.service;


import java.util.List;

import com.mx.atendi.entity.Usuario;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Define la API del servicio de usuarios.
 */
public interface IUsuarioService {
    Mono<Usuario> crearUsuario(Usuario usuario, String rolAdmin);
    Mono<Usuario> obtenerUsuario(String id);
    Flux<Usuario> listarUsuarios();
 // Agregar este método para buscar el usuario por userName y hospitalId
    Mono<Usuario> findByUserNameAndHospitalId(String userName, String hospitalId);
	
	/**
	 * Elimina un usuario por su ID. Solo los administradores pueden ejecutar esta acción.
	 *
	 * @param userId ID del usuario a eliminar.
	 * @param rolAdmin Rol del usuario autenticado que solicita la eliminación.
	 * @return Mono<Void> indicando éxito o error si el usuario no tiene permisos.
	 */
	Mono<Void> eliminarUsuarioPorId(String userId, String rolAdmin);
	/**
	 * Asigna un departamento y una ventanilla a un usuario. Solo un administrador puede hacer esto.
	 *
	 * @param userId ID del usuario.
	 * @param departamentoId ID del departamento.
	 * @param numeroVentanilla Número de ventanilla dentro del departamento.
	 * @param rolAdmin Rol del usuario que realiza la operación.
	 * @return Mono con el usuario actualizado.
	 */
	Mono<Usuario> asignarDepartamentoYVentanilla(String userId, String departamentoId, String numeroVentanilla,
			String rolAdmin);
	Mono<Usuario> actualizarUsuario(String userId,  Usuario usuario, String rolAdmin );
}

