package com.tarea2.tarea2.controller;

import com.tarea2.tarea2.service.ComprasService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ComprasControllerTests {
    @Test
    void entregaElContratoJsonDelEnunciado() throws Exception {
        var service = mock(ComprasService.class);
        when(service.porUnidad("uf")).thenReturn(new ComprasService.ComprasPorUnidad("UF",
                List.of(new ComprasService.CompraConvertida(1, "Notebook", new BigDecimal("25.5"),
                        LocalDate.of(2026, 9, 1), new BigDecimal("40000"), 1020000)), 1020000));
        var mvc = MockMvcBuilders.standaloneSetup(new ComprasController(service)).build();
        mvc.perform(get("/compras/uf"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"unidad":"UF","compras":[{"id":1,"producto":"Notebook","monto":25.5,
                        "fecha":"2026-09-01","valor_unidad":40000,"monto_pesos":1020000}],
                        "total_pesos":1020000}
                        """, org.springframework.test.json.JsonCompareMode.STRICT));
    }

    @Test
    void unidadInvalidaResponde400() throws Exception {
        var service = mock(ComprasService.class);
        when(service.porUnidad("euro"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unidad no válida"));
        MockMvcBuilders.standaloneSetup(new ComprasController(service)).build()
                .perform(get("/compras/euro")).andExpect(status().isBadRequest());
    }
}
