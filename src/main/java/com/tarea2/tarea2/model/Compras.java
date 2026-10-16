package com.tarea2.tarea2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// Un record genera el constructor y los accesores de estos datos.
public record Compras(long id, String producto, BigDecimal monto, String unidad, LocalDate fecha) { }
