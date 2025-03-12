package com.mx.atendi.service;

import com.mx.atendi.entity.Turno;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.print.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
        boolean impresoraTermica = esImpresoraTermica(impresora);  // 🔥 Detecta si es térmica

        return Flux.fromIterable(turnos)
                .flatMap(turno -> generarFormatoTicket(turno, impresoraTermica))  // 🔥 Genera formato según el tipo
                .collectList()
                .flatMap(tickets -> Mono.fromCallable(() -> {
                    String contenido;
                    if (impresoraTermica) {
                        // 🔥 Concatenar tickets sin columnas (térmica)
                        contenido = String.join("\n\n", tickets);
                    } else {
                        // 🔥 Ajustar formato para A4 (columnas)
                        contenido = ajustarFormatoA4(tickets);
                    }

                    ByteArrayInputStream bais = new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8));
                    Doc doc = new SimpleDoc(bais, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

                    PrintService printService = seleccionarImpresora(impresora);
                    if (printService == null) {
                        log.error("Impresora no encontrada: {}", impresora);
                        return "Impresora no encontrada.";
                    }

                    DocPrintJob printJob = printService.createPrintJob();
                    printJob.print(doc, null);

                    log.info("Tickets impresos en la impresora: {}", printService.getName());
                    return "Tickets impresos correctamente.";
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
        return impresora != null && (impresora.toLowerCase().contains("thermal") || impresora.toLowerCase().contains("escpos"));
    }

    // 🔥 Formato del ticket según el tipo de impresora
    private Mono<String> generarFormatoTicket(Turno turno, boolean impresoraTermica) {
        Mono<String> nombreDepartamento = departamentoService.obtenerNombrePorId(turno.getDepartamentoId());
        Mono<String> nombreOperacion = operacionService.obtenerNombrePorId(turno.getTipoOperacion());

        return Mono.zip(nombreDepartamento, nombreOperacion)
                .map(tuple -> {
                    String depto = tuple.getT1();
                    String operacion = tuple.getT2();

                    if (impresoraTermica) {
                        // 🔥 Formato para impresora térmica
                        return "\u001B@" +                          // Reset
                                "\u001B!\u0011" +                   // Fuente grande
                                "===============================\n" +
                                "          TICKET DE TURNO       \n" +
                                "===============================\n" +
                                "Turno: " + turno.getNumeroTurno() + "\n" +
                                "Departamento: " + depto + "\n" +
                                "Operación: " + operacion + "\n" +
                                "Fecha: " + turno.getHoraCreacion() + "\n" +
                                "Estado: " + turno.getEstado() + "\n" +
                                "===============================\n" +
                                "Gracias por su espera\n\n\n\n" +
                                "\u001DVA\u0003";                  // Corte de papel
                    } else {
                        // 🔥 Formato para impresora A4
                        return "===============================\n" +
                                "          TICKET DE TURNO       \n" +
                                "===============================\n" +
                                "Turno: " + turno.getNumeroTurno() + "\n" +
                                "Departamento: " + depto + "\n" +
                                "Operación: " + operacion + "\n" +
                                "Fecha: " + turno.getHoraCreacion() + "\n" +
                                "Estado: " + turno.getEstado() + "\n" +
                                "===============================\n" +
                                "Gracias por su espera\n";
                    }
                });
    }


    private PrintService seleccionarImpresora(String impresora) {
        PrintService[] servicios = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService servicio : servicios) {
            if (impresora == null || servicio.getName().equalsIgnoreCase(impresora)) {
                return servicio;
            }
        }
        return PrintServiceLookup.lookupDefaultPrintService();  // 🔥 Usa la predeterminada si no se especifica
    }

    
    @Override
    public Mono<List<String>> listarImpresorasDisponibles() {
        return Mono.fromCallable(() -> {
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            return Arrays.stream(printServices)
                         .map(PrintService::getName)
                         .collect(Collectors.toList());
        });
    }
    
   
}
