package com.tarea2.tarea2.model;

import java.time.format.DateTimeFormatter;

public class Compras {
    private Long id;
    private String producto;
    private double monto;
    private String unidad;
    private DateTimeFormatter fecha;

    public Compras(Long id, String producto, double monto, String unidad, DateTimeFormatter fecha) {
        this.id = id;
        this.producto = producto;
        this.monto = monto;
        this.unidad = unidad;
        this.fecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public DateTimeFormatter getFecha() {
        return fecha;
    }

    public void setFecha(DateTimeFormatter formatter) {
        this.fecha = formatter;
    }
}
