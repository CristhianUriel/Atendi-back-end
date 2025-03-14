package com.mx.atendi.service;

import com.mx.atendi.entity.Turno;
import lombok.extern.slf4j.Slf4j;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;
import org.cups4j.PrintRequestResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.print.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImpresionService implements IImpresionService {

	private final IDepartamentoService departamentoService;
	private final IOperacionService operacionService;

	public ImpresionService(DepartamentoService departamentoService, OperacionService operacionService) {
		this.departamentoService = departamentoService;
		this.operacionService = operacionService;
	}

	@Override
	public Mono<String> imprimirTicket(Turno turno, String impresora) {
		return imprimirVariosTickets(List.of(turno), impresora);
	}

	@Override
	public Mono<String> imprimirVariosTickets(List<Turno> turnos, String impresora) {
		return Flux.fromIterable(turnos).flatMap(turno -> generarFormatoTicket(turno, esImpresoraTermica(impresora)))
				.collectList().flatMap(tickets -> Mono.fromCallable(() -> {
					String contenido = String.join("\n\n", tickets);
					ByteArrayInputStream bais = new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8));
					Doc doc = new SimpleDoc(bais, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

					// 🔹 1️⃣ Intentamos imprimir en local (Windows/Linux)
					PrintService printService = seleccionarImpresoraLocal(impresora);
					if (printService != null) {
						try {
							DocPrintJob printJob = printService.createPrintJob();
							printJob.print(doc, null);
							log.info("✅ Tickets impresos en el sistema local: {}", printService.getName());
							return "Tickets impresos correctamente en local.";
						} catch (Exception e) {
							log.error("❌ Error imprimiendo en local: ", e);
						}
					}

					// 🔹 2️⃣ Si no está en local, intentamos en CUPS (WSL2)
					String cupsIP = obtenerIPWSL();
					if (cupsIP != null) {
						try {
							// 🔥 Creamos un archivo temporal con el contenido a imprimir
							Path tempFile = Files.createTempFile("ticket", ".txt");
							Files.write(tempFile, contenido.getBytes(StandardCharsets.UTF_8));

							// 🔥 Conectar con CUPS
							CupsClient cupsClient = new CupsClient(cupsIP, 631);
							CupsPrinter cupsPrinter = cupsClient
									.getPrinter(new URL("http://" + cupsIP + ":631/printers/" + impresora));

							// 🔥 Crear un `PrintJob`
							InputStream inputStream = new FileInputStream(tempFile.toFile());
							PrintJob job = new PrintJob.Builder(inputStream).jobName("Ticket de impresión").copies(1)
									.build();

							// 🔥 Enviar la impresión a CUPS
							PrintRequestResult result = cupsPrinter.print(job);

							log.info("✅ Impresión enviada a CUPS: {} - Éxito: {}", cupsPrinter.getName(),
									result.isSuccessfulResult());

							// 🔥 Eliminamos el archivo temporal después de la impresión
							Files.deleteIfExists(tempFile);

							return "Impresión enviada a CUPS.";
						} catch (Exception e) {
							log.error("❌ Error imprimiendo en CUPS: ", e);
						}
					}

					log.error("❌ No se pudo imprimir ni en local ni en CUPS.");
					return "No se pudo imprimir.";
				}));
	}

	// 🔥 Ajusta el formato para A4 (Columnas)
	private String ajustarFormatoA4(List<String> tickets) {
		StringBuilder sb = new StringBuilder();
		int numTickets = tickets.size();

		if (numTickets <= 3) {
			// 🔥 1-3 tickets en una columna
			tickets.forEach(ticket -> sb.append(ticket).append("\n\n"));
		} else {
			// 🔥 4-6 tickets en dos columnas
			for (int i = 0; i < numTickets; i += 2) {
				String ticketIzq = tickets.get(i);
				String ticketDer = (i + 1 < numTickets) ? tickets.get(i + 1) : "";
				sb.append(String.format("%-45s%-45s\n\n", ticketIzq, ticketDer));
			}
		}

		return sb.toString();
	}

	// 🔥 Detecta si es una impresora térmica
	private boolean esImpresoraTermica(String impresora) {
		return impresora != null
				&& (impresora.toLowerCase().contains("thermal") || impresora.toLowerCase().contains("escpos"));
	}

	// 🔥 Formato del ticket según el tipo de impresora
	private Mono<String> generarFormatoTicket(Turno turno, boolean impresoraTermica) {
		Mono<String> nombreDepartamento = departamentoService.obtenerNombrePorId(turno.getDepartamentoId());
		Mono<String> nombreOperacion = operacionService.obtenerNombrePorId(turno.getTipoOperacion());

		return Mono.zip(nombreDepartamento, nombreOperacion).map(tuple -> {
			String depto = tuple.getT1();
			String operacion = tuple.getT2();

			if (impresoraTermica) {
				log.info("Tipo Impresion Impresora Térmica");
				return "\u001B@" + // Reset de la impresora
						"\u001B!\u0000" + // Fuente estándar (48 caracteres por línea)
						"================================================\n"
						+ "                TICKET DE TURNO                \n"
						+ "        SUBDELEGACION DEL IMMS TEHUACAN        \n"
						+ "================================================\n"
						+ String.format("%-15s %s\n", "Turno:", turno.getNumeroTurno())
						+ String.format("%-15s %s\n", "Departamento:", depto)
						+ String.format("%-15s %s\n", "Operacion:", operacion)
						+ String.format("%-15s %s\n", "Fecha:", turno.getHoraCreacion())
						+ String.format("%-15s %s\n", "Estado:", turno.getEstado())
						+ "================================================\n"
						+ "     ¡Gracias por su espera!     \n\n\n\n" + "\u001DVA\u0003"; // Comando de corte de papel
			} else {
				log.info("Tipo Impresion Impresora Normal");

				return "================================================\n"
						+ "                TICKET DE TURNO                \n"
						+ "        SUBDELEGACION DEL IMMS TEHUACAN        \n"
						+ "================================================\n"
						+ String.format("%-20s %s\n", "Turno:", turno.getNumeroTurno())
						+ String.format("%-20s %s\n", "Departamento:", depto)
						+ String.format("%-20s %s\n", "Operación:", operacion)
						+ String.format("%-20s %s\n", "Fecha:", turno.getHoraCreacion())
						+ String.format("%-20s %s\n", "Estado:", turno.getEstado())
						+ "================================================\n"
						+ "            ¡Gracias por su espera!            \n\n\n";
			}
		});
	}

	private CupsPrinter seleccionarImpresoraCUPS(String impresora) {
		try {
			String cupsIP = obtenerIPWSL();
			if (cupsIP != null) {
				CupsClient cupsClient = new CupsClient(cupsIP, 631);
				String printerUrl = "http://" + cupsIP + ":631/printers/" + impresora;
				CupsPrinter printer = cupsClient.getPrinter(new URL(printerUrl));

				log.info("✅ Impresora encontrada en CUPS: " + printer.getName());
				return printer;
			}
		} catch (Exception e) {
			log.error("❌ No se pudo conectar a CUPS.", e);
		}
		return null;
	}

	private PrintService seleccionarImpresoraLocal(String impresora) {
		PrintService[] servicios = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService servicio : servicios) {
			if (impresora == null || servicio.getName().equalsIgnoreCase(impresora)) {
				log.info("✅ Impresora encontrada en el sistema local: " + servicio.getName());
				return servicio;
			}
		}
		log.warn("⚠ No se encontró la impresora en el sistema local.");
		return null;
	}

	private String obtenerIPWSL() {
		String ipWSL = "172.26.182.117";
		return ipWSL;
	}

	@Override
	public Mono<List<String>> listarImpresorasDisponibles() {
		return Mono.fromCallable(() -> {
			// 🔹 1️⃣ Obtener impresoras locales de Windows
			PrintService[] serviciosDeImpresion = PrintServiceLookup.lookupPrintServices(null, null);
			List<String> impresorasLocales = Arrays.stream(serviciosDeImpresion).map(PrintService::getName)
					.collect(Collectors.toList());

			// 🔹 2️⃣ Obtener impresoras desde CUPS en WSL2
			String cupsIP = obtenerIPWSL();
			if (cupsIP != null) {
				try {
					CupsClient cupsClient = new CupsClient(cupsIP, 631);
					List<CupsPrinter> impresorasCUPS = cupsClient.getPrinters();
					List<String> nombresImpresorasCUPS = impresorasCUPS.stream().map(CupsPrinter::getName)
							.collect(Collectors.toList());
					// Combinar listas de impresoras
					impresorasLocales.addAll(nombresImpresorasCUPS);
				} catch (Exception e) {
					log.error("❌ Error al obtener impresoras de CUPS: ", e);
				}
			} else {
				log.warn("⚠️ No se pudo obtener la IP de WSL2; solo se listarán impresoras locales.");
			}

			return impresorasLocales;
		});
	}

}
