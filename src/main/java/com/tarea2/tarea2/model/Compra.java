package com.tarea2.tarea2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Compra {

    private int id;
    private String producto;
    private BigDecimal monto;
    private String unidad;
    private LocalDate fecha;
    private BigDecimal valor_unidad;
    private long monto_pesos;

    public Compra() {
    }

    public Compra(int id, String producto, BigDecimal monto, String unidad, LocalDate fecha) {
        this.id = id;
        this.producto = producto;
        this.monto = monto;
        this.unidad = unidad;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getValor_unidad() {
        return valor_unidad;
    }

    public void setValor_unidad(BigDecimal valor_unidad) {
        this.valor_unidad = valor_unidad;
    }

    public long getMonto_pesos() {
        return monto_pesos;
    }

    public void setMonto_pesos(long monto_pesos) {
        this.monto_pesos = monto_pesos;
    }
}