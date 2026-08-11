package com.pixel.modelo;

import java.time.LocalDateTime;

public class Cuenta {

    private int idCuenta;
    private int numeroMesa;
    private String dpiMesero;
    private String nombreMesero; // solo para mostrar en UI
    private LocalDateTime fechaOcupacion;
    private LocalDateTime fechaLiberacion;
    private String estado; // ABIERTA, PAGADA
    private double total;
    private double propina;

    public Cuenta() {
    }

    public int getIdCuenta() { return idCuenta; }
    public void setIdCuenta(int idCuenta) { this.idCuenta = idCuenta; }

    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }

    public String getDpiMesero() { return dpiMesero; }
    public void setDpiMesero(String dpiMesero) { this.dpiMesero = dpiMesero; }

    public String getNombreMesero() { return nombreMesero; }
    public void setNombreMesero(String nombreMesero) { this.nombreMesero = nombreMesero; }

    public LocalDateTime getFechaOcupacion() { return fechaOcupacion; }
    public void setFechaOcupacion(LocalDateTime fechaOcupacion) { this.fechaOcupacion = fechaOcupacion; }

    public LocalDateTime getFechaLiberacion() { return fechaLiberacion; }
    public void setFechaLiberacion(LocalDateTime fechaLiberacion) { this.fechaLiberacion = fechaLiberacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getPropina() { return propina; }
    public void setPropina(double propina) { this.propina = propina; }
}