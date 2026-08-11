package com.pixel.modelo;

public class Mesa {

    private int numeroMesa;
    private int capacidad;
    private String estado; // LIBRE, OCUPADA

    public Mesa() {
    }

    public Mesa(int numeroMesa, int capacidad, String estado) {
        this.numeroMesa = numeroMesa;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Mesa " + numeroMesa;
    }
}