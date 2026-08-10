package com.pixel.modelo;

public class Producto {

    private int codigoProducto;
    private String nombre;
    private String categoria; // BEBIDA_CALIENTE, BEBIDA_FRIA, POSTRE, COMIDA
    private double precioVenta;
    private String rutaFoto;
    private boolean activo;

    public Producto() {
    }

    public Producto(int codigoProducto, String nombre, String categoria,
                    double precioVenta, String rutaFoto, boolean activo) {
        this.codigoProducto = codigoProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioVenta = precioVenta;
        this.rutaFoto = rutaFoto;
        this.activo = activo;
    }

    public int getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(int codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public String getRutaFoto() { return rutaFoto; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre;
    }
}