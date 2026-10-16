package com.tarea2.tarea2.service;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class IndicadorServiceTests {
    private MockRestServiceServer server;
    private IndicadorService service;

    @BeforeEach
    void configurarCliente() {
        var builder = RestClient.builder().baseUrl("https://mindicador.cl/api");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new IndicadorService(builder.build());
    }

    @AfterEach
    void verificarConsultas() {
        server.verify();
    }

    @Test
    void consultaFechaHistoricaYLeeElValor() {
        server.expect(requestTo("https://mindicador.cl/api/uf/01-09-2026"))
                .andRespond(withSuccess("{\"codigo\":\"uf\",\"serie\":[{\"fecha\":\"2026-09-01T04:00:00.000Z\",\"valor\":40000.25}]}", MediaType.APPLICATION_JSON));
        assertThat(service.obtenerValor("uf", LocalDate.of(2026, 9, 1)))
                .isEqualByComparingTo("40000.25");
    }

    @Test
    void noUsaUnValorDeOtraFecha() {
        server.expect(requestTo("https://mindicador.cl/api/dolar/01-09-2026"))
                .andRespond(withSuccess("{\"serie\":[{\"fecha\":\"2026-09-02T04:00:00.000Z\",\"valor\":900}]}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> service.obtenerValor("dolar", LocalDate.of(2026, 9, 1)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(422));
    }

    @Test
    void informaFechaSinCotizacion() {
        server.expect(requestTo("https://mindicador.cl/api/dolar/01-09-2026"))
                .andRespond(withSuccess("{\"serie\":[]}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> service.obtenerValor("dolar", LocalDate.of(2026, 9, 1)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(422));
    }

    @Test
    void informaFallaDeLaApi() {
        server.expect(requestTo("https://mindicador.cl/api/uf/01-09-2026"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThatThrownBy(() -> service.obtenerValor("uf", LocalDate.of(2026, 9, 1)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(502));
    }
}
