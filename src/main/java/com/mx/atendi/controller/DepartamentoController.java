package com.mx.atendi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mx.atendi.entity.Departamento;
import com.mx.atendi.service.IDepartamentoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/departamentos")
@Tag(name = "Departamentos", description = "Gestión de departamentos y sus operaciones")
public class DepartamentoController {

    private final IDepartamentoService departamentoService;

    public DepartamentoController(IDepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @PostMapping
    @Operation(summary = "Crear un departamento", description = "Registra un nuevo departamento con ventanillas y operaciones")
    public Mono<Departamento> crearDepartamento(@RequestBody Departamento departamento,Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return departamentoService.crearDepartamento(departamento);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un departamento", description = "Modifica los datos de un departamento existente")
    public Mono<Departamento> actualizarDepartamento(@PathVariable String id, @RequestBody Departamento departamento,Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return departamentoService.actualizarDepartamento(id, departamento);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un departamento", description = "Elimina un departamento por ID")
    public Mono<Void> eliminarDepartamento(@PathVariable String id,Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return departamentoService.eliminarDepartamento(id);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los departamentos", description = "Devuelve la lista de todos los departamentos")
    public Flux<Departamento> obtenerDepartamentos(Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return departamentoService.obtenerDepartamentos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un departamento por ID", description = "Busca un departamento por su ID")
    public Mono<Departamento> obtenerDepartamentoPorId(@PathVariable String id,Authentication authentication) {
    	 validarUsuarioAutenticado(authentication);
        return departamentoService.obtenerDepartamentoPorId(id);
    }
    private void validarUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Acceso no autorizado: Usuario no autenticado");
        }
    }
}

