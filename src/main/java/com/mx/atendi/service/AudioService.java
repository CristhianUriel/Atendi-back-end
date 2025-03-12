package com.mx.atendi.service;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AudioService implements IAudioService {

//    private final MaryInterface maryTTS;
//    private final Path carpetaAudios;
//    
//    public AudioService() throws MaryConfigurationException {
//        maryTTS = new LocalMaryInterface();
//       // maryTTS.setLocale(new Locale("es"));  // Ajuste del locale a español
//        maryTTS.setVoice("cmu-slt-hsmm");    // 🔥 Usar exclusivamente cmu-slt-hsmm
//
//        // 🔥 Usar ruta absoluta para la carpeta "audios"
//        carpetaAudios = Paths.get(System.getProperty("user.dir"), "audios");
//
//        // 🔥 Crear la carpeta "audios" si no existe
//        if (Files.notExists(carpetaAudios)) {
//            try {
//                Files.createDirectories(carpetaAudios);
//                log.info("✅ Carpeta 'audios' creada exitosamente en: {}", carpetaAudios.toAbsolutePath());
//            } catch (IOException e) {
//                log.error("❌ No se pudo crear la carpeta 'audios': {}", e.getMessage());
//                throw new RuntimeException("Error al crear la carpeta 'audios'", e);
//            }
//        }
//
//        // 🔥 Listar voces disponibles para confirmar que cmu-slt-hsmm está presente
//        log.info("Voces disponibles:");
//        maryTTS.getAvailableVoices().forEach(voice -> log.info("🔹 " + voice));
//    }
//
//    /**
//     * Genera y guarda un archivo de audio a partir de un texto utilizando MaryTTS.
//     * @param mensaje El mensaje a convertir en audio.
//     * @param turnoId El ID del turno para nombrar el archivo.
//     * @return Mono<String> Ruta del archivo generado.
//     */
//    @Override
//    public Mono<String> generarAudioParaTurno(String mensaje, String turnoId) {
//        return Mono.fromCallable(() -> {
//            try {
//                AudioInputStream audio = maryTTS.generateAudio(mensaje);
//                File archivoSalida = carpetaAudios.resolve("turno_" + turnoId + ".wav").toFile();
//
//                // 🔥 Asegurar permisos de escritura
//                if (!archivoSalida.getParentFile().canWrite()) {
//                    throw new IOException("No hay permisos de escritura en: " + archivoSalida.getParentFile().getAbsolutePath());
//                }
//
//                //AudioSystem.write(audio, AudioSystem.getAudioFileFormat(archivoSalida).getType(), archivoSalida);
//                AudioSystem.write(audio, AudioFileFormat.Type.WAVE, archivoSalida);
//                log.info("✅ Audio generado para el turno {}: {}", turnoId, archivoSalida.getAbsolutePath());
//                return archivoSalida.getAbsolutePath();
//            } catch (SynthesisException | IOException e) {
//                log.error("❌ Error al generar el audio para el turno {}: {}", turnoId, e.getMessage());
//                throw new RuntimeException("Error al generar el audio", e);
//            }
//        });
//    }
//
//    /**
//     * Reproduce el archivo de audio especificado.
//     * @param ruta Ruta del archivo de audio.
//     * @return Mono<Void> indicando la finalización de la reproducción.
//     */
//    @Override
//    public Mono<Void> reproducirAudio(String ruta) {
//        return Mono.fromRunnable(() -> {
//            try {
//                File archivoAudio = new File(ruta);
//                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(archivoAudio);
//                Clip clip = AudioSystem.getClip();
//                clip.open(audioInputStream);
//                clip.start();
//                log.info("▶ Reproduciendo audio: {}", ruta);
//                Thread.sleep(clip.getMicrosecondLength() / 1000);  // Esperar a que termine la reproducción
//            } catch (Exception e) {
//                log.error("❌ Error al reproducir el audio: {}", e.getMessage());
//                throw new RuntimeException("Error al reproducir el audio", e);
//            }
//        });
//    }

	 private final Path carpetaAudios;

	    public AudioService() {
	        carpetaAudios = Paths.get(System.getProperty("user.dir"), "audios");

	        // Crear la carpeta "audios" si no existe
	        if (Files.notExists(carpetaAudios)) {
	            try {
	                Files.createDirectories(carpetaAudios);
	                log.info("✅ Carpeta 'audios' creada exitosamente en: {}", carpetaAudios.toAbsolutePath());
	            } catch (IOException e) {
	                log.error("❌ No se pudo crear la carpeta 'audios': {}", e.getMessage());
	                throw new RuntimeException("Error al crear la carpeta 'audios'", e);
	            }
	        }
	    }

	    @Override
	    public Mono<String> generarAudioParaTurno(String mensaje, String turnoId) {
	        return Mono.fromCallable(() -> {
	            try {
	                // Ruta del archivo WAV
	                String archivoWav = carpetaAudios.resolve("turno_" + turnoId + ".wav").toString();

	                // Comando para generar el archivo WAV con eSpeak estándar en CMD
	                // Cambié la voz a 'es' para español estándar
	                //String comando = String.format("cmd /c \"\"C:\\Program Files (x86)\\eSpeak\\command_line\\espeak.exe\" -v es \"%s\" --stdout > \"%s\"\"", mensaje, archivoWav);
	                String comando = String.format("cmd /c \"\"C:\\Program Files (x86)\\eSpeak\\command_line\\espeak.exe\" -v es \"%s\" --wave \"%s\"\"", mensaje, archivoWav);

	                Process proceso = Runtime.getRuntime().exec(comando);

	                // Capturar salida de error
	                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getErrorStream()))) {
	                    String linea;
	                    while ((linea = reader.readLine()) != null) {
	                        log.error("❌ Error CMD: " + linea);
	                    }
	                }

	                proceso.waitFor();

	                if (proceso.exitValue() != 0) {
	                    log.error("❌ Error al generar WAV con eSpeak: {}", proceso.exitValue());
	                    throw new RuntimeException("Error en la síntesis de voz.");
	                }

	                log.info("✅ Audio WAV generado para el turno {}: {}", turnoId, archivoWav);
	                return archivoWav;
	            } catch (IOException | InterruptedException e) {
	                log.error("❌ Error al generar el audio para el turno {}: {}", turnoId, e.getMessage());
	                throw new RuntimeException("Error al generar el audio", e);
	            }
	        });
	    }

	    @Override
	    public Mono<Void> reproducirAudio(String ruta) {
	        return Mono.fromRunnable(() -> {
	            try {
	                // Comando para reproducir el archivo WAV en Windows
	                String comando = String.format("cmd /c start %s", ruta);
	                Process proceso = Runtime.getRuntime().exec(comando);
	                proceso.waitFor();

	                if (proceso.exitValue() != 0) {
	                    log.error("❌ Error al reproducir el audio WAV: {}", proceso.exitValue());
	                    throw new RuntimeException("Error en la reproducción de audio.");
	                }

	                log.info("▶ Reproduciendo audio: {}", ruta);
	            } catch (IOException | InterruptedException e) {
	                log.error("❌ Error al reproducir el audio: {}", e.getMessage());
	                throw new RuntimeException("Error al reproducir el audio", e);
	            }
	        });
	    }
}
