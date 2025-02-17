package com.mx.atendi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mx.atendi.entity.Operacion;
import com.mx.atendi.service.IOperacionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/operaciones")
@Tag(name = "Operaciones", description = "Catálogo de operaciones disponibles")
public class OperacionController {

    private final IOperacionService operacionService;

    public OperacionController(IOperacionService operacionService) {
        this.operacionService = operacionService;
    }

    /**
     * Crea una nueva operación.
     *
     * @param operacion Datos de la operación a registrar.
     * @return Mono con la operación creada.
     */
    @PostMapping
    @Operation(summary = "Crear una operación", description = "Registra una nueva operación en el catálogo")
    public Mono<Operacion> crearOperacion(@RequestBody Operacion operacion,Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return operacionService.crearOperacion(operacion);
    }

    /**
     * Actualiza una operación existente.
     *
     * @param id ID de la operación a actualizar.
     * @param operacion Datos nuevos de la operación.
     * @return Mono con la operación actualizada.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una operación", description = "Modifica una operación existente")
    public Mono<Operacion> actualizarOperacion(@PathVariable String id, @RequestBody Operacion operacion,Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return operacionService.actualizarOperacion(id, operacion);
    }

    /**
     * Elimina una operación por su ID.
     *
     * @param id ID de la operación a eliminar.
     * @return Mono<Void> indicando que la operación fue eliminada.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una operación", description = "Elimina una operación por su ID")
    public Mono<Void> eliminarOperacion(@PathVariable String id,Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return operacionService.eliminarOperacion(id);
    }

    /**
     * Obtiene todas las operaciones del catálogo.
     *
     * @return Flux<Operacion> con todas las operaciones registradas.
     */
    @GetMapping
    @Operation(summary = "Obtener todas las operaciones", description = "Devuelve la lista de todas las operaciones")
    public Flux<Operacion> obtenerTodasOperaciones(Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return operacionService.obtenerTodasOperaciones();
    }
    
    private void validarUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Acceso no autorizado: Usuario no autenticado");
        }
    }
}

