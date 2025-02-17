package com.mx.atendi.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.atendi.entity.Usuario;
import com.mx.atendi.service.IUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    	 
        return usuarioService.crearUsuario(usuario,usuario.getRol());
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
    
    /**
     * Asigna una ventanilla y tipos de operación a un usuario. Solo administradores pueden hacer esto.
     *
     * @param userId ID del usuario.
     * @param ventanillaId ID de la ventanilla.
     * @param numeroVentanilla Número de ventanilla asignado.
     * @param tiposOperacion Tipos de operación permitidos.
     * @param authentication Datos del usuario autenticado.
     * @return Mono con el usuario actualizado.
     */
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios")
    @PutMapping("/{userId}/asignar-ventanilla")
    public Mono<Usuario> asignarDepartamentoYVentanilla(
            @Parameter(description = "ID del usuario", required = true) @PathVariable String userId,
            @Parameter(description = "ID del departamento", required = true) @RequestParam String departamentoId,
            @Parameter(description = "Número de ventanilla asignado", required = true) @RequestParam String numeroVentanilla,
            Authentication authentication
        ) {
            String rolAdmin = authentication.getAuthorities().iterator().next().getAuthority();
            return usuarioService.asignarDepartamentoYVentanilla(userId, departamentoId, numeroVentanilla, rolAdmin);
        }
    
    /**
     * Elimina un usuario por su ID. Solo los administradores pueden eliminar usuarios.
     *
     * @param userId ID del usuario a eliminar.
     * @param authentication Datos del usuario autenticado que realiza la solicitud.
     * @return Mono<Void> si la eliminación fue exitosa o error si no tiene permisos.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Eliminar usuario por ID", description = "Elimina un usuario en base a su ID. Solo los administradores pueden ejecutar esta acción.")
    public Mono<Void> eliminarUsuario(@PathVariable String userId,
        Authentication authentication
    ) {
        String rolAdmin = authentication.getAuthorities().iterator().next().getAuthority();
        return usuarioService.eliminarUsuarioPorId(userId, rolAdmin);
    }

}

