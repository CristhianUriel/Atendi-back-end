package com.mx.atendi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mx.atendi.dto.TurnoDTO;
import com.mx.atendi.entity.ContadorTurnos;
import com.mx.atendi.entity.HistorialTurnos;
import com.mx.atendi.entity.Turno;
import com.mx.atendi.entity.VentanillaCatalogo;
import com.mx.atendi.repository.ContadorTurnosRepository;
import com.mx.atendi.repository.HistorialTurnosRepository;
import com.mx.atendi.repository.OperacionRepository;
import com.mx.atendi.repository.TurnoRepository;
import com.mx.atendi.repository.UsuarioRepository;
import com.mx.atendi.repository.VentanillaCatalogoRepository;
import com.mx.atendi.utils.EstatusTurno;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class TurnoService implements ITurnoService {

	private final TurnoRepository turnoRepository;
	private final ContadorTurnosRepository contadorTurnosRepository;
	private final HistorialTurnosRepository historialTurnosRepository;
	private final OperacionRepository operacionRepository;
	private final UsuarioRepository usuarioRepository;
	private final VentanillaCatalogoRepository ventanillaCatalogoRepository;
	private final Sinks.Many<TurnoDTO> sink;

	public TurnoService(TurnoRepository turnoRepository, ContadorTurnosRepository contadorTurnosRepository,
			HistorialTurnosRepository historialTurnosRepository, OperacionRepository operacionRepository,
			VentanillaCatalogoRepository ventanillaCatalogoRepository, UsuarioRepository usuarioRepository) {
		this.turnoRepository = turnoRepository;
		this.contadorTurnosRepository = contadorTurnosRepository;
		this.historialTurnosRepository = historialTurnosRepository;
		this.operacionRepository = operacionRepository;
		this.ventanillaCatalogoRepository = ventanillaCatalogoRepository;
		this.usuarioRepository = usuarioRepository;
		this.sink = Sinks.many().replay().limit(100);
	}

	@Override
	public Mono<TurnoDTO> crearTurno(Turno turno, String rolUsuario) {
		if ("VENTANILLA".equalsIgnoreCase(rolUsuario)) {
			return Mono.error(new RuntimeException("Los usuarios de ventanilla no pueden crear turnos"));
		}

		turno.setHoraCreacion(LocalDateTime.now());
		turno.setEstado(EstatusTurno.PENDIENTE.getValor());

		return generarNumeroTurno(turno.getHospitalId(), turno.getDepartamentoId()).flatMap(numeroTurno -> {
			turno.setNumeroTurno(numeroTurno);
			return turnoRepository.save(turno).flatMap(
					savedTurno -> operacionRepository.findById(savedTurno.getTipoOperacion()).map(operacion -> {
						TurnoDTO turnoDTO = convertirADTO(savedTurno, operacion.getNombre());
						sink.tryEmitNext(turnoDTO);
						log.info("Turno creado y emitido: {}", turnoDTO);
						return turnoDTO;
					}));
		});
	}

	@Override
	public Mono<TurnoDTO> tomarTurno(String turnoId, String usuarioId, String departamentoId) {
		return turnoRepository.findById(turnoId).switchIfEmpty(Mono.error(new RuntimeException("Turno no encontrado")))
				.flatMap(turno -> {
					if (!turno.getDepartamentoId().equals(departamentoId)) {
						return Mono.error(new RuntimeException("No puedes tomar turnos de otro departamento"));
					}
					if (turno.getAtendidoPor() != null) {
						return Mono.error(new RuntimeException("Este turno ya fue tomado por otro usuario"));
					}

					turno.setAtendidoPor(usuarioId);
					turno.setEstado(EstatusTurno.EN_PROCESO.getValor());
					turno.setHoraAtencion(LocalDateTime.now());

					return turnoRepository.save(turno)
							.flatMap(savedTurno -> Mono.zip(operacionRepository.findById(savedTurno.getTipoOperacion()),
									obtenerVentanillaPorUsuario(usuarioId)).map(tuple -> {
										var operacion = tuple.getT1();
										var ventanilla = tuple.getT2();

										// Nuevo método convertirADTO que recibe operación y ventanilla
										TurnoDTO turnoDTO = convertirADTO(savedTurno, operacion.getNombre(),
												ventanilla.getNombre());

										// Emitir DTO actualizado
										sink.tryEmitNext(turnoDTO);

										// Emitir eliminado (visual)
										TurnoDTO eliminado = new TurnoDTO();
										eliminado.setHospitalId(turnoDTO.getHospitalId());
										eliminado.setDepartamentoId(turnoDTO.getDepartamentoId());
										eliminado.setId(turnoId);
										eliminado.setEstado(EstatusTurno.ELIMINADO.getValor());
										sink.tryEmitNext(eliminado);

										log.info("✅ Turno tomado por usuario {}: {}", usuarioId, turnoDTO);
										log.info("🗑️ Turno eliminado y emitido: {}", eliminado);

										return turnoDTO;
									}));
				});
	}

	@Override
	public Flux<TurnoDTO> streamTurnos(String hospitalId, String departamentoId, boolean esMonitor) {
		Flux<TurnoDTO> turnosPendientes = Flux.defer(() -> esMonitor ? turnoRepository
				.findByHospitalIdAndEstado(hospitalId, EstatusTurno.PENDIENTE.getValor()).flatMap(turno -> {
					if (turno.getTipoOperacion() == null) {
						return Mono.just(convertirADTO(turno, "Sin operación"));
					}
					return operacionRepository.findById(turno.getTipoOperacion())
							.map(operacion -> convertirADTO(turno, operacion.getNombre()))
							.defaultIfEmpty(convertirADTO(turno, "Operación no encontrada"));
				})
				: turnoRepository.findByHospitalIdAndDepartamentoIdAndEstado(hospitalId, departamentoId,
						EstatusTurno.PENDIENTE.getValor()).flatMap(turno -> {
							if (turno.getTipoOperacion() == null) {
								return Mono.just(convertirADTO(turno, "Sin operación"));
							}
							return operacionRepository.findById(turno.getTipoOperacion())
									.map(operacion -> convertirADTO(turno, operacion.getNombre()))
									.defaultIfEmpty(convertirADTO(turno, "Operación no encontrada"));
						}));

		Flux<TurnoDTO> turnosNuevos = sink.asFlux().filter(turno -> Objects.equals(turno.getHospitalId(), hospitalId)
				&& (esMonitor || Objects.equals(turno.getDepartamentoId(), departamentoId)));

		return Flux.merge(turnosPendientes, turnosNuevos)
				.filter(turno -> turno.getEstado().equals(EstatusTurno.PENDIENTE.getValor())
						|| turno.getEstado().equals(EstatusTurno.ELIMINADO.getValor()));
	}

	@Override
	public Flux<HistorialTurnos> obtenerTurnosUltimosAtendidos(String hospitalId, int cantidad) {
		return historialTurnosRepository.findAll().filter(turno -> turno.getHospitalId().equals(hospitalId))
				.sort((t1, t2) -> t2.getHoraFinalizacion().compareTo(t1.getHoraFinalizacion())).take(cantidad);
	}

	@Override
	public Mono<TurnoDTO> finalizarTurno(String turnoId, String usuarioId, String estadoFinal) {
		return turnoRepository.findById(turnoId).flatMap(turno -> {
			if (turno.getAtendidoPor() == null || !turno.getAtendidoPor().equals(usuarioId)) {
				return Mono.error(new RuntimeException("No puedes finalizar un turno que no tomaste"));
			}

			turno.setEstado(estadoFinal);
			turno.setHoraAtencion(LocalDateTime.now());

			HistorialTurnos historial = new HistorialTurnos();
			historial.setNumeroTurno(turno.getNumeroTurno());
			historial.setHospitalId(turno.getHospitalId());
			historial.setDepartamentoId(turno.getDepartamentoId());
			historial.setTipoOperacion(turno.getTipoOperacion());
			historial.setEstado(estadoFinal);
			historial.setAtendidoPor(turno.getAtendidoPor());
			historial.setHoraCreacion(turno.getHoraCreacion());
			historial.setHoraAtencion(turno.getHoraAtencion());
			historial.setHoraFinalizacion(LocalDateTime.now());

			return historialTurnosRepository.save(historial).then(turnoRepository.delete(turno))
					.then(operacionRepository.findById(turno.getTipoOperacion()).map(operacion -> {
						// 🔥 Emitir evento de eliminación del turno
						TurnoDTO eliminado = new TurnoDTO();
						eliminado.setId(turnoId);
						eliminado.setEstado(EstatusTurno.ELIMINADO.getValor());
						sink.tryEmitNext(eliminado);

						return convertirADTO(turno, operacion.getNombre());
					}));
		});
	}

	@Override
	public Mono<Turno> buscarTurnoPorId(String turnoId) {
		log.info("buscarTurnoPorId [{}]", turnoId);
		return turnoRepository.findById(turnoId)
				.switchIfEmpty(Mono.error(new RuntimeException("Turno no encontrado: " + turnoId)));
	}

	@Override
	public Flux<Turno> buscarVariosTurnos(List<String> turnosIds) {
		return turnoRepository.findAllById(turnosIds)
				.switchIfEmpty(Mono.error(new RuntimeException("No se encontraron los turnos especificados")));
	}

	private TurnoDTO convertirADTO(Turno turno, String tipoOperacionNombre) {
		return new TurnoDTO(turno.getId(), turno.getNumeroTurno(), turno.getHospitalId(), turno.getDepartamentoId(),
				turno.getTipoOperacion(), tipoOperacionNombre, turno.getAtendidoPor(), turno.getEstado(),
				turno.getHoraCreacion(), turno.getHoraAtencion());
	}

	private TurnoDTO convertirADTO(Turno turno, String tipoOperacionNombre, String nombreVentanilla) {
		return new TurnoDTO(turno.getId(), turno.getNumeroTurno(), turno.getHospitalId(), turno.getDepartamentoId(),
				turno.getTipoOperacion(), tipoOperacionNombre, nombreVentanilla, turno.getEstado(),
				turno.getHoraCreacion(), turno.getHoraAtencion());
	}

	private Mono<VentanillaCatalogo> obtenerVentanillaPorUsuario(String usuarioId) {
		return usuarioRepository.findByUserName(usuarioId)
				.flatMap(usuario -> ventanillaCatalogoRepository.findById(usuario.getVentanillaId()));
	}

	private Mono<String> generarNumeroTurno(String hospitalId, String departamentoId) {
		LocalDate hoy = LocalDate.now();

		return contadorTurnosRepository.findByHospitalIdAndDepartamentoIdAndFecha(hospitalId, departamentoId, hoy)
				.switchIfEmpty(Mono.defer(() -> {
					ContadorTurnos nuevoContador = new ContadorTurnos();
					nuevoContador.setHospitalId(hospitalId);
					nuevoContador.setDepartamentoId(departamentoId);
					nuevoContador.setFecha(hoy);
					nuevoContador.setUltimoTurno("A000");
					return contadorTurnosRepository.save(nuevoContador);
				})).flatMap(contador -> {
					String nuevoNumero = generarSiguienteNumero(contador.getUltimoTurno());
					contador.setUltimoTurno(nuevoNumero);
					return contadorTurnosRepository.save(contador).map(c -> nuevoNumero);
				});
	}

	private String generarSiguienteNumero(String ultimo) {
		int numero = Integer.parseInt(ultimo.replaceAll("[^0-9]", "")); // Extraer números
		numero++; // Incrementar número

		String nuevaLetra = obtenerLetraPorNumero(numero); // Asignar letra basada en el número

		return nuevaLetra + String.format("%03d", numero);
	}

	private String obtenerLetraPorNumero(int numero) {
		int index = (numero - 1) % 26; // Cicla entre 0 y 25 (A-Z)
		return String.valueOf((char) ('A' + index)); // Convierte el índice a una letra (A-Z)
	}

}
