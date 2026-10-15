package com.tarea2.tarea2.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tarea2.tarea2.model.Compra;

@Service
public class CompraService {

    @Value("${CSV_PATH:compras.csv}")
    private String csvPath;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Optional<Compra> buscarPorId(int id) throws IOException, InterruptedException {
        Path path = Path.of(csvPath);
        if (!Files.exists(path)) {
            throw new IOException("El archivo CSV no existe en la ruta: " + csvPath);
        }

        List<String> lineas = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isBlank()) {
                continue;
            }

            String[] columnas = linea.split(",", -1);

            if (columnas.length != 5) {
                throw new IOException("Formato CSV incorrecto en la línea " + (i + 1));
            }

            int idCsv = Integer.parseInt(columnas[0].trim());

            if (idCsv == id) {
                Compra compra = new Compra(
                        idCsv,
                        columnas[1].trim(),
                        new BigDecimal(columnas[2].trim()),
                        normalizarUnidad(columnas[3]),
                        LocalDate.parse(columnas[4].trim())
                );

                BigDecimal valor = obtenerValorHistorico(compra.getUnidad(), compra.getFecha());
                compra.setValor_unidad(valor);

                long montoPesos = compra.getMonto()
                        .multiply(valor)
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValueExact();

                compra.setMonto_pesos(montoPesos);

                return Optional.of(compra);
            }
        }

        return Optional.empty();
    }

    private String normalizarUnidad(String unidad) throws IOException {
        if (unidad == null) {
            throw new IOException("Unidad nula");
        }
        String valor = unidad.trim().toUpperCase();

        if (valor.equals("UF")) {
            return "UF";
        }

        if (valor.equals("USD") || valor.equals("DOLAR") || valor.equals("DÓLAR")) {
            return "USD";
        }

        throw new IOException("Unidad no soportada: " + unidad);
    }

    private BigDecimal obtenerValorHistorico(String unidad, LocalDate fecha) throws IOException, InterruptedException {
        String indicador = unidad.equals("UF") ? "uf" : "dolar";
        String url = "https://mindicador.cl/api/" + indicador + "/" + fecha.getYear();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error consultando mindicador.cl: HTTP " + response.statusCode());
        }

        String json = response.body();

        Pattern patron = Pattern.compile("\"fecha\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"valor\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
        Matcher matcher = patron.matcher(json);

        BigDecimal valorCercano = null;
        LocalDate fechaCercana = null;

        while (matcher.find()) {
            LocalDate fechaIndicador = OffsetDateTime.parse(matcher.group(1)).toLocalDate();
            BigDecimal valor = new BigDecimal(matcher.group(2));

            if (fechaIndicador.equals(fecha)) {
                return valor;
            }

            if (fechaIndicador.isBefore(fecha)) {
                if (fechaCercana == null || fechaIndicador.isAfter(fechaCercana)) {
                    fechaCercana = fechaIndicador;
                    valorCercano = valor;
                }
            }
        }

        if (valorCercano != null) {
            return valorCercano;
        }

        throw new IOException("No existe valor de " + unidad + " para " + fecha);
    }
}