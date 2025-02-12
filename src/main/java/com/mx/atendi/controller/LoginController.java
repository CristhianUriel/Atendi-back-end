package com.mx.atendi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.mx.atendi.security.JwtUtil;
import com.mx.atendi.service.IHospitalService;
import com.mx.atendi.service.IUsuarioService;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controlador para autenticación y generación de tokens JWT.
 */
@Tag(name = "Auth API", description = "Endpoints para autenticación")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configura CORS según tus necesidades de producción
public class LoginController {

    private final JwtUtil jwtUtil;
    private final IUsuarioService usuarioService;
    private final IHospitalService hospitalService;

    /**
     * Autentica al usuario validando las credenciales contra la base de datos y verifica que el hospital exista.
     * Si la autenticación es exitosa, genera y retorna un token JWT.
     *
     * @param loginRequest Objeto con userName y password.
     * @param hospitalId   ID del hospital (en header).
     * @return Mono que emite un mapa con el token JWT.
     */
    @Operation(summary = "Login", description = "Autentica al usuario y devuelve un token JWT")
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> login(@RequestBody LoginRequest loginRequest,
                                             @RequestHeader("Hospital-Id") String hospitalId) {
        // Primero, verificar que el hospital exista
        return hospitalService.buscarHospitalPorId(hospitalId)
                .switchIfEmpty(Mono.error(new RuntimeException("Hospital no encontrado")))
                .flatMap(hospital ->
                    // Buscar el usuario por userName y hospitalId.
                    usuarioService.findByUserNameAndHospitalId(loginRequest.getUserName(), hospitalId)
                )
                .flatMap(usuario -> {
                    // Validar la contraseña (en producción se debe usar un hash, como BCrypt)
                    if (!usuario.getPassword().equals(loginRequest.getPassword())) {
                        return Mono.error(new RuntimeException("Credenciales inválidas"));
                    }
                    // Generar el token JWT con los datos del usuario
                    String token = jwtUtil.generateToken(loginRequest.getUserName(), usuario.getRol(), hospitalId);
                    return Mono.just(Map.of("token", token));
                });
    }

    /**
     * DTO para el login.
     */
    @Data
    public static class LoginRequest {
        private String userName;
        private String password;
    }
}


