package com.mx.atendi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mx.atendi.entity.Usuario;
import com.mx.atendi.repository.DepartamentoRepository;
import com.mx.atendi.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementa el servicio de usuarios.
 * Se espera que los usuarios de un hospital compartan el mismo hospitalId,
 * por lo que al crear un usuario se debe proporcionar el hospitalId correspondiente.
 */
@Service
@Slf4j
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;
    
    
    public UsuarioService(UsuarioRepository usuarioRepository, DepartamentoRepository departamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.departamentoRepository = departamentoRepository;
    }

    /**
     * Crea un nuevo usuario.
     * Se asume que el hospitalId se proporciona en el objeto usuario (para que todos se vinculen al mismo hospital).
     *
     * @param usuario Datos del usuario.
     * @return Mono que emite el usuario creado.
     */
    @Override
    public Mono<Usuario> crearUsuario(Usuario usuario, String rolAdmin) {
        return usuarioRepository.save(usuario)
                .doOnNext(savedUser -> log.info("Usuario creado: {}", savedUser));
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Mono que emite el usuario encontrado.
     */
    @Override
    public Mono<Usuario> obtenerUsuario(String id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Lista todos los usuarios del sistema.
     *
     * @return Flux con la lista de usuarios.
     */
    @Override
    public Flux<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

	@Override
	public Mono<Usuario> findByUserNameAndHospitalId(String userName, String hospitalId) {
		 return usuarioRepository.findByUserNameAndHospitalId(userName, hospitalId);
	}
	
	/**
     * Asigna un departamento y una ventanilla a un usuario. Solo un administrador puede hacer esto.
     *
     * @param userId ID del usuario.
     * @param departamentoId ID del departamento.
     * @param numeroVentanilla Número de ventanilla dentro del departamento.
     * @param rolAdmin Rol del usuario que realiza la operación.
     * @return Mono con el usuario actualizado.
     */
	@Override
    public Mono<Usuario> asignarDepartamentoYVentanilla(String userId, String departamentoId, String ventanillaId, String rolAdmin) {
        return departamentoRepository.findById(departamentoId)
                .flatMap(departamento -> {
                    if (!departamento.getVentanillasIds().contains(ventanillaId)) {
                        return Mono.error(new RuntimeException("La ventanilla seleccionada no pertenece a este departamento"));
                    }

                    return usuarioRepository.findById(userId)
                            .flatMap(usuario -> {
                                usuario.setDepartamentoId(departamentoId);
                                usuario.setVentanillaId(ventanillaId);
                                return usuarioRepository.save(usuario);
                            });
                })
                .doOnSuccess(u -> log.info("Departamento y ventanilla asignados a usuario: {}", u));
    }
	
	/**
	 * Elimina un usuario por su ID. Solo los administradores pueden ejecutar esta acción.
	 *
	 * @param userId ID del usuario a eliminar.
	 * @param rolAdmin Rol del usuario autenticado que solicita la eliminación.
	 * @return Mono<Void> indicando éxito o error si el usuario no tiene permisos.
	 */
	@Override
	public Mono<Void> eliminarUsuarioPorId(String userId, String rolAdmin) {
	    return usuarioRepository.findById(userId)
	            .flatMap(usuario -> usuarioRepository.delete(usuario))
	            .doOnSuccess(v -> log.info("Usuario eliminado: {}", userId));
	}

}

