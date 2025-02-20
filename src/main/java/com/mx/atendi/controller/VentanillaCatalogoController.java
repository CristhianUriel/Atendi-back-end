package com.mx.atendi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mx.atendi.entity.VentanillaCatalogo;
import com.mx.atendi.service.IVentanillaCatalogoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ventanillas-catalogo")
@Tag(name = "Ventanillas", description = "Catálogo de ventanillas disponibles")
public class VentanillaCatalogoController {

    private final IVentanillaCatalogoService ventanillaCatalogoService;

    public VentanillaCatalogoController(IVentanillaCatalogoService ventanillaCatalogoService) {
        this.ventanillaCatalogoService = ventanillaCatalogoService;
    }

    /**
     * Crea una nueva ventanilla.
     *
     * @param ventanilla Datos de la ventanilla a registrar.
     * @return Mono con la ventanilla creada.
     */
    @PostMapping
    @Operation(summary = "Crear una ventanilla", description = "Registra una nueva ventanilla en el catálogo")
    public Mono<VentanillaCatalogo> crearVentanilla(@RequestBody VentanillaCatalogo ventanilla,Authentication authentication) {
    	validarUsuarioAutenticado(authentication);
        return ventanillaCatalogoService.crearVentanilla(ventanilla);
    }

    /**
     * Actualiza una ventanilla existente.
     *
     * @param id ID de la ventanilla a actualizar.
     * @param ventanilla Datos nuevos de la ventanilla.
     * @return Mono con la ventanilla actualizada.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una ventanilla", description = "Modifica los datos de una ventanilla existente")
    public Mono<VentanillaCatalogo> actualizarVentanilla(@PathVariable String id, @RequestBody VentanillaCatalogo ventanilla,Authentication authentication) {
    	validarUsuarioAutenticado(authentication);
        return ventanillaCatalogoService.actualizarVentanilla(id, ventanilla);
    }

    /**
     * Elimina una ventanilla por su ID.
     *
     * @param id ID de la ventanilla a eliminar.
     * @return Mono<Void> indicando que la ventanilla fue eliminada.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una ventanilla", description = "Elimina una ventanilla del catálogo")
    public Mono<Void> eliminarVentanilla(@PathVariable String id,Authentication authentication) {
    	validarUsuarioAutenticado(authentication);
        return ventanillaCatalogoService.eliminarVentanilla(id);
    }

    /**
     * Obtiene todas las ventanillas registradas.
     *
     * @return Flux<VentanillaCatalogo> con la lista de ventanillas disponibles.
     */
    @GetMapping
    @Operation(summary = "Obtener todas las ventanillas", description = "Devuelve la lista de ventanillas registradas")
    public Flux<VentanillaCatalogo> obtenerTodasVentanillas(Authentication authentication) {
    	validarUsuarioAutenticado(authentication);
        return ventanillaCatalogoService.obtenerTodasVentanillas();
    }
    
    /**
     * Obtiene todas las ventanillas registrada atraves de una lista.
     *
     * @return Flux<VentanillaCatalogo> con la lista de ventanillas disponibles.
     */
    @GetMapping(path = "/ventanillas-ids")
    @Operation(summary = "Obtener todas las ventanillas", description = "Devuelve la lista de ventanillas registradas")
    public Flux<VentanillaCatalogo> obtenerTodasVentanillasPorListaIds(@RequestBody List<String>ids, Authentication authentication) {
    	validarUsuarioAutenticado(authentication);
        return ventanillaCatalogoService.obtenerVentanillasPorIds(ids);
    }
    /**
     * Activa o desactiva una ventanilla.
     *
     * @param id ID de la ventanilla.
     * @param activo Estado (true = activa, false = inactiva).
     * @return Mono con la ventanilla actualizada.
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar/Desactivar una ventanilla", description = "Permite cambiar el estado de una ventanilla")
    public Mono<VentanillaCatalogo> cambiarEstadoVentanilla(@PathVariable String id, @RequestParam boolean activo,Authentication authentication) {
    	validarUsuarioAutenticado(authentication);
        return ventanillaCatalogoService.cambiarEstadoVentanilla(id, activo);
    }
    
    private void validarUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Acceso no autorizado: Usuario no autenticado");
        }
    }
}


