package com.mx.atendi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mx.atendi.entity.Usuario;
import com.mx.atendi.service.IUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para la gestión de usuarios.
 */
@Tag(name = "Usuarios API", description = "Endpoints para la gestión de usuarios")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Crea un nuevo usuario.
     * Se espera que al crear un usuario, se proporcione el hospitalId existente al que pertenece.
     *
     * @param usuario Datos del usuario.
     * @return Mono que emite el usuario creado.
     */
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario y asigna tipos de operación y ventanillas")
    @PostMapping
    public Mono<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        return usuarioService.crearUsuario(usuario);
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Mono que emite el usuario encontrado.
     */
    @Operation(summary = "Obtener usuario", description = "Obtiene un usuario por su ID")
    @GetMapping("/{id}")
    public Mono<Usuario> obtenerUsuario(@PathVariable String id) {
        return usuarioService.obtenerUsuario(id);
    }

    /**
     * Lista todos los usuarios.
     *
     * @return Flux con la lista de usuarios.
     */
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios")
    @GetMapping
    public Flux<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }
}

