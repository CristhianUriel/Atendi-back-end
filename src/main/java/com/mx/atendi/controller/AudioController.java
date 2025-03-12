package com.mx.atendi.controller;

import com.mx.atendi.service.IAudioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/audios")
@Tag(name = "Audios", description = "Gestión de audios para los turnos")
public class AudioController {

    private final IAudioService audioService;

    @Autowired
    public AudioController(IAudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/generar")
    @Operation(summary = "Generar audio para un turno", description = "Convierte un mensaje en audio y lo guarda")
    public Mono<ResponseEntity<String>> generarAudio(@RequestParam String mensaje, @RequestParam String turnoId) {
        return audioService.generarAudioParaTurno(mensaje, turnoId)
                .map(ruta -> ResponseEntity.ok("Audio generado en: " + ruta))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al generar audio: " + e.getMessage())));
    }

    @PostMapping("/reproducir")
    @Operation(summary = "Reproducir audio", description = "Reproduce un archivo de audio por su ruta")
    public Mono<ResponseEntity<String>> reproducirAudio(@RequestParam String ruta) {
        return audioService.reproducirAudio(ruta)
                .then(Mono.just(ResponseEntity.ok("Reproducción iniciada para: " + ruta)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al reproducir audio: " + e.getMessage())));
    }
}
