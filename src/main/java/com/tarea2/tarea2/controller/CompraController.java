package com.tarea2.tarea2.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tarea2.tarea2.model.Compra;
import com.tarea2.tarea2.service.CompraService;

@RestController
@RequestMapping("/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable int id) {

        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "El ID debe ser positivo"));
        }

        try {
            Optional<Compra> compraOptional = compraService.buscarPorId(id);

            if (compraOptional.isPresent()) {
                return ResponseEntity.ok(compraOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No existe una compra con ID " + id));
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "La consulta fue interrumpida"));

        } catch (IOException | RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No fue posible procesar la compra"));
        }
    }
}