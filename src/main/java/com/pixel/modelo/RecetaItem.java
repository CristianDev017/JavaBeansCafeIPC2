package com.pixel.modelo;

public class RecetaItem {

    private int codigoInsumo;
    private String nombreInsumo; // solo para mostrar en la tabla, no se persiste aquí
    private double cantidadRequerida;

    public RecetaItem(int codigoInsumo, String nombreInsumo, double cantidadRequerida) {
        this.codigoInsumo = codigoInsumo;
        this.nombreInsumo = nombreInsumo;
        this.cantidadRequerida = cantidadRequerida;
    }

    public int getCodigoInsumo() { return codigoInsumo; }
    public String getNombreInsumo() { return nombreInsumo; }
    public double getCantidadRequerida() { return cantidadRequerida; }
}