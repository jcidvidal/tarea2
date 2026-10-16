package com.tarea2.tarea2.service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComprasServiceTests {
    @TempDir Path directorio;

    @Test
    void filtraConservaOrdenRedondeaYSumaLasCompras() throws Exception {
        Path csv = directorio.resolve("compras.csv");
        Files.writeString(csv, "id,producto,monto,unidad,fecha\n"
                + "3,Primero,1.005,UF,2026-09-01\n"
                + "2,Dolar,20,USD,2026-09-02\n"
                + "1,Segundo,2.005,UF,2026-09-01\n");
        var indicador = mock(IndicadorService.class);
        when(indicador.obtenerValor("uf", LocalDate.of(2026, 9, 1)))
                .thenReturn(new BigDecimal("100"));
        var service = new ComprasService(csv.toString(), indicador);

        var resultado = service.porUnidad("uf");

        assertThat(resultado.unidad()).isEqualTo("UF");
        assertThat(resultado.compras()).extracting(ComprasService.CompraConvertida::id)
                .containsExactly(3L, 1L);
        assertThat(resultado.compras()).extracting(ComprasService.CompraConvertida::monto_pesos)
                .containsExactly(101L, 201L);
        assertThat(resultado.total_pesos()).isEqualTo(302);
        verify(indicador, times(2)).obtenerValor("uf", LocalDate.of(2026, 9, 1));

        // El cambio del archivo debe verse sin reiniciar la aplicación.
        Files.writeString(csv, "id,producto,monto,unidad,fecha\n");
        assertThat(service.porUnidad("uf").compras()).isEmpty();
        assertThat(service.porUnidad("uf").total_pesos()).isZero();
    }

    @Test
    void dolarUsaLaFechaDeCadaCompra() throws Exception {
        Path csv = directorio.resolve("compras.csv");
        Files.writeString(csv, "id,producto,monto,unidad,fecha\n"
                + "2,Monitor,2,USD,2026-09-02\n4,Teclado,3,USD,2026-09-08\n");
        var indicador = mock(IndicadorService.class);
        when(indicador.obtenerValor("dolar", LocalDate.of(2026, 9, 2)))
                .thenReturn(new BigDecimal("900"));
        when(indicador.obtenerValor("dolar", LocalDate.of(2026, 9, 8)))
                .thenReturn(new BigDecimal("950"));

        var resultado = new ComprasService(csv.toString(), indicador).porUnidad("dolar");

        assertThat(resultado.unidad()).isEqualTo("USD");
        assertThat(resultado.total_pesos()).isEqualTo(4650);
        assertThat(resultado.compras()).extracting(ComprasService.CompraConvertida::monto_pesos)
                .containsExactly(1800L, 2850L);
    }

    @Test
    void rechazaUnidadDesconocidaAntesDeLeerElArchivo() {
        var service = new ComprasService("inexistente.csv", mock(IndicadorService.class));
        assertThatThrownBy(() -> service.porUnidad("euro"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void informaArchivoInexistenteOMalformado() throws Exception {
        Path csv = directorio.resolve("compras.csv");
        var service = new ComprasService(csv.toString(), mock(IndicadorService.class));
        assertThatThrownBy(() -> service.porUnidad("uf"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(500));
        Files.writeString(csv, "id,producto,monto,unidad,fecha\n1,Producto,abc,UF,2026-09-01\n");
        assertThatThrownBy(() -> service.porUnidad("uf"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(500));
    }
}
