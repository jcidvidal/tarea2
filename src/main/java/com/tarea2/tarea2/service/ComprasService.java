package com.tarea2.tarea2.service;

import com.tarea2.tarea2.model.Compras;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComprasService {
    private final String csvPath;
    private final IndicadorService indicadorService;

    public ComprasService(@Value("${compras.csv-path}") String csvPath,
                         IndicadorService indicadorService) {
        this.csvPath = csvPath;
        this.indicadorService = indicadorService;
    }

    public ComprasPorUnidad porUnidad(String unidad) {
        String indicador = unidad.toLowerCase(Locale.ROOT);
        String unidadCsv = switch (indicador) {
            case "uf" -> "UF";
            case "dolar" -> "USD";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use uf o dolar.");
        };

        List<CompraConvertida> resultado = new ArrayList<>();
        long total = 0;

        for (Compras compra : leerCompras()) {
            if (!compra.unidad().equalsIgnoreCase(unidadCsv)) {
                continue;
            }
            BigDecimal valor = indicadorService.obtenerValor(indicador, compra.fecha());
            long pesos = compra.monto().multiply(valor).setScale(0, RoundingMode.HALF_UP).longValueExact();

            resultado.add(new CompraConvertida(compra.id(), compra.producto(),
                    compra.monto(), compra.fecha(), valor, pesos));
            total += pesos;
        }
        return new ComprasPorUnidad(unidadCsv, resultado, total);
    }

    private List<Compras> leerCompras() {
        List<Compras> compras = new ArrayList<>();
        try {
            List<String> lineas = Files.readAllLines(Path.of(csvPath));
            if (lineas.isEmpty() || !lineas.get(0).replace("\uFEFF", "")
                    .equals("id,producto,monto,unidad,fecha")) {
                throw new IllegalArgumentException("Cabecera incorrecta.");
            }

            // La primera línea contiene los nombres de las columnas.
            for (int i = 1; i < lineas.size(); i++) {
                if (lineas.get(i).isBlank()) {
                    continue;
                }
                String[] campos = lineas.get(i).split(",", -1);
                if (campos.length != 5) {
                    throw new IllegalArgumentException("Cada compra debe tener cinco columnas.");
                }
                compras.add(new Compras(Long.parseLong(campos[0].trim()), campos[1].trim(),
                        new BigDecimal(campos[2].trim()), campos[3].trim(),
                        LocalDate.parse(campos[4].trim())));
            }
        } catch (IOException | IllegalArgumentException | DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo leer el archivo de compras.", ex);
        }
        return compras;
    }

    // Estos campos definen el JSON solicitado en el enunciado.
    public record CompraConvertida(long id, String producto, BigDecimal monto, LocalDate fecha,
                                   BigDecimal valor_unidad, long monto_pesos) { }

    public record ComprasPorUnidad(String unidad, List<CompraConvertida> compras, long total_pesos) { }
}
