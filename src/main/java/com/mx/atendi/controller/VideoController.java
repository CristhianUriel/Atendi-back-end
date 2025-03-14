package com.mx.atendi.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.mx.atendi.entity.Video;
import com.mx.atendi.repository.VideoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Videos", description = "API para manejar videos en streaming y almacenamiento")
public class VideoController {

    private final VideoRepository videoRepository;

    @Value("${app.video-path}")
    private String videoPath;

    // 🔹 SUBIR VIDEO Y GUARDAR METADATA
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir un video", description = "Permite subir un video y guardar su metadata en MongoDB")
    public Mono<ResponseEntity<String>> uploadVideo(@RequestPart("file") Mono<FilePart> filePartMono, Authentication authentication) {
        return filePartMono.flatMap(filePart -> {
            Path destination = Paths.get(videoPath, filePart.filename());
            return filePart.transferTo(destination)
                    .then(videoRepository.save(new Video(
                            null,
                            filePart.filename(),
                            destination.toString(),
                            LocalDateTime.now()
                    )))
                    .thenReturn(ResponseEntity.ok("Video subido correctamente por " + authentication.getName()));
        });
    }

    // 🔹 STREAMING DE TODOS LOS VIDEOS EN LOOP
    @GetMapping(value = "/stream/all", produces = "video/mp4")
    @Operation(summary = "Reproducir todos los videos en loop", description = "Reproduce todos los videos almacenados en bucle continuo")
    public ResponseEntity<Flux<DataBuffer>> streamAllVideos() {
        Flux<DataBuffer> videoStream = videoRepository.findAll()
                .map(video -> {
                    Path path = Paths.get(videoPath, video.getNombre());
                    System.out.println("📁 Ruta del archivo: " + path); // 🔍 Depuración
                    return path;
                })
                .filter(Files::exists)
                .flatMap(path -> {
                    try {
                        FileSystemResource resource = new FileSystemResource(path);
                        return DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096)
                                .doOnError(e -> System.err.println("⚠️ Error leyendo archivo: " + path + " - " + e.getMessage()));
                    } catch (Exception e) {
                        System.err.println("❌ Excepción al acceder a archivo: " + path);
                        return Flux.empty();
                    }
                })
                .repeatWhen(flux -> flux.delayElements(Duration.ofSeconds(2))) // Evita saturación

                .onErrorResume(e -> {
                    System.err.println("🚨 Error general en streaming: " + e.getMessage());
                    return Flux.empty();
                });

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)//(MediaType.valueOf("video/mp4"))
                .body(videoStream);
    }



    // 🔹 LISTAR NOMBRES DE LOS VIDEOS
    @GetMapping("/names")
    @Operation(summary = "Obtener nombres de videos", description = "Devuelve una lista de los nombres de los videos almacenados")
    public Flux<String> getVideoNames() {
        return videoRepository.findAll().map(Video::getNombre);
    }

    // 🔹 ELIMINAR VIDEO
    @DeleteMapping("/{videoName}")
    @Operation(summary = "Eliminar un video", description = "Borra un video almacenado por su nombre, requiere autenticación")
    public Mono<ResponseEntity<String>> deleteVideo(@PathVariable String videoName, Authentication authentication) {
        Path filePath = Paths.get(videoPath, videoName);

        return Mono.fromSupplier(() -> {
            try {
                boolean fileDeleted = Files.deleteIfExists(filePath);
                return fileDeleted; // true si el archivo se eliminó, false si no existía
            } catch (IOException e) {
                throw new RuntimeException("Error eliminando el archivo", e);
            }
        })
        .flatMap(fileDeleted -> {
            if (fileDeleted) {
                return videoRepository.deleteByNombre(videoName) // Asume que tienes un método deleteByNombre en tu repositorio
                    .thenReturn(ResponseEntity.ok("Video eliminado correctamente por " + authentication.getName()));
            } else {
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video no encontrado"));
            }
        })
        .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error eliminando el video: " + e.getMessage())));
    }
}
