package com.pixel.modelo;

import java.time.LocalDate;

public class Nomina {

    private int codigoNomina;
    private String dpiEmpleado;
    private String nombreEmpleado; // solo para mostrar en UI
    private LocalDate fechaEmision;
    private String tipoPago; // QUINCENA, FIN_DE_MES
    private double monto;
    private String estado; // PENDIENTE, PAGADO

    public Nomina() {
    }

    public int getCodigoNomina() { return codigoNomina; }
    public void setCodigoNomina(int codigoNomina) { this.codigoNomina = codigoNomina; }

    public String getDpiEmpleado() { return dpiEmpleado; }
    public void setDpiEmpleado(String dpiEmpleado) { this.dpiEmpleado = dpiEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}