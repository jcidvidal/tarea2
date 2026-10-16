package com.tarea2.tarea2.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IndicadorService {
    private static final Logger log = LoggerFactory.getLogger(IndicadorService.class);
    private static final DateTimeFormatter FORMATO_API = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final RestClient client;

    public IndicadorService(RestClient client) {
        this.client = client;
    }

    public BigDecimal obtenerValor(String indicador, LocalDate fecha) {
        RespuestaIndicador respuesta;
        try {
            respuesta = client.get().uri("/{indicador}/{fecha}", indicador, fecha.format(FORMATO_API))
                    .retrieve().body(RespuestaIndicador.class);
        } catch (RestClientException ex) {
            log.warn("Falló la consulta a mindicador.cl: indicador={}, fecha={}", indicador, fecha, ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo conectar con mindicador.cl o la API devolvió un error. "
                            + "Intente nuevamente más tarde; revise la consola para ver la causa.", ex);
        }
        if (respuesta == null || respuesta.serie() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Respuesta inválida de mindicador.cl.");
        }
        for (DatoIndicador dato : respuesta.serie()) {
            if (dato != null && dato.fecha() != null
                    && dato.fecha().startsWith(fecha.toString() + "T")
                    && dato.valor() != null && dato.valor().signum() > 0) {
                return dato.valor();
            }
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                "No hay valor de " + indicador + " para la fecha " + fecha + ".");
    }

    public record RespuestaIndicador(List<DatoIndicador> serie) { }
    public record DatoIndicador(String fecha, BigDecimal valor) { }
}
