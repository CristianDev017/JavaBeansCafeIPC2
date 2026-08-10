package com.pixel.modelo;

public class Insumo {

    private int codigoInsumo;
    private String nombre;
    private String unidadMedida;
    private double stockActual;
    private double stockMinimo;
    private double costo;

    public Insumo() {
    }

    public Insumo(int codigoInsumo, String nombre, String unidadMedida,
                  double stockActual, double stockMinimo, double costo) {
        this.codigoInsumo = codigoInsumo;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.costo = costo;
    }

    public int getCodigoInsumo() { return codigoInsumo; }
    public void setCodigoInsumo(int codigoInsumo) { this.codigoInsumo = codigoInsumo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public double getStockActual() { return stockActual; }
    public void setStockActual(double stockActual) { this.stockActual = stockActual; }

    public double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(double stockMinimo) { this.stockMinimo = stockMinimo; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public boolean isStockBajo() {
        return stockActual <= stockMinimo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}