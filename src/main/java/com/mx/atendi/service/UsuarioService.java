package com.mx.atendi.service;

import org.springframework.stereotype.Service;

import com.mx.atendi.entity.Usuario;
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

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea un nuevo usuario.
     * Se asume que el hospitalId se proporciona en el objeto usuario (para que todos se vinculen al mismo hospital).
     *
     * @param usuario Datos del usuario.
     * @return Mono que emite el usuario creado.
     */
    @Override
    public Mono<Usuario> crearUsuario(Usuario usuario) {
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
}

