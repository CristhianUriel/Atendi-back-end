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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.mx.atendi.entity.Video;
import com.mx.atendi.repository.VideoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Videos", description = "API para manejar videos en streaming y almacenamiento")
@Slf4j
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
    @GetMapping(value = "/stream/{index}", produces = "video/mp4")
    @Operation(summary = "Reproducir video por índice", description = "Reproduce un video específico según el índice en la lista de videos")
    public Mono<ResponseEntity<Flux<DataBuffer>>> streamVideo(@PathVariable int index, @RequestHeader(value = "Range", required = false) String range) {
        log.info("📡 Iniciando transmisión del video en índice: {}", index);
        log.info("📜 Header Range recibido: {}", range);

        return videoRepository.findAll()
                .collectList()
                .flatMap(videos -> {
                    if (videos.isEmpty() || index >= videos.size()) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }

                    Path path = Paths.get(videoPath, videos.get(index).getNombre());
                    if (!Files.exists(path)) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }

                    try {
                        FileSystemResource resource = new FileSystemResource(path);
                        long fileSize = Files.size(path);
                        long rangeStart = 0;
                        long rangeEnd = fileSize - 1;

                        if (range != null && range.startsWith("bytes=")) {
                            String[] ranges = range.replace("bytes=", "").split("-");
                            rangeStart = Long.parseLong(ranges[0]);
                            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                                rangeEnd = Long.parseLong(ranges[1]);
                            }
                        }

                        long contentLength = rangeEnd - rangeStart + 1;
                        log.info("🎯 Streaming desde {} hasta {} de un total de {} bytes", rangeStart, rangeEnd, fileSize);

                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Accept-Ranges", "bytes");
                        headers.set("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);
                        headers.setContentLength(contentLength);
                        headers.setContentType(MediaType.valueOf("video/mp4"));

                        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096)
                                .skip(rangeStart / 4096)
                                .take(contentLength / 4096 + 1);

                        return Mono.just(ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                                .headers(headers)
                                .body(dataBufferFlux));
                    } catch (Exception e) {
                        log.error("❌ Error al procesar el archivo: {}", e.getMessage());
                        return Mono.empty();
                    }
                });
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
