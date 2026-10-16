package com.tarea2.tarea2.controller;

import com.tarea2.tarea2.service.ComprasService;
import com.tarea2.tarea2.service.ComprasService.ComprasPorUnidad;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compras")
public class ComprasController {
    private final ComprasService comprasService;

    public ComprasController(ComprasService comprasService) {
        this.comprasService = comprasService;
    }

    @GetMapping("/{unidad}")
    public ComprasPorUnidad porUnidad(@PathVariable("unidad") String unidad) {
        return comprasService.porUnidad(unidad);
    }
}
