package com.mx.atendi.service;

import reactor.core.publisher.Mono;

public interface IAudioService {

    /**
     * Genera un archivo de audio basado en el mensaje proporcionado.
     *
     * @param mensaje    Mensaje a convertir en audio.
     * @param turnoId    ID del turno asociado para nombrar el archivo.
     * @return Mono<String> con la ruta del archivo generado.
     */
    Mono<String> generarAudioParaTurno(String mensaje, String turnoId);

    /**
     * Reproduce el archivo de audio ubicado en la ruta especificada.
     *
     * @param ruta Ruta del archivo de audio.
     * @return Mono<Void> que indica el resultado de la reproducción.
     */
    Mono<Void> reproducirAudio(String ruta);
}
