package com.pixel.modelo;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Empleado {

    private String dpi;
    private String nombreCompleto;
    private String correo;
    private String rol;        // MESERO, COCINA, BARISTA, ADMINISTRADOR
    private String jornada;    // MATUTINA, VESPERTINA, NOCTURNA
    private double salario;
    private LocalDate fechaContratacion;
    private boolean activo;

    public Empleado() {
    }

    public Empleado(String dpi, String nombreCompleto, String correo, String rol,
                    String jornada, double salario, LocalDate fechaContratacion, boolean activo) {
        this.dpi = dpi;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.rol = rol;
        this.jornada = jornada;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }

    // Getters y setters
    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getJornada() { return jornada; }
    public void setJornada(String jornada) { this.jornada = jornada; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombreCompleto + " (" + dpi + ")";
    }

    public List<Empleado> listarMeserosActivos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleado WHERE activo = TRUE AND rol = 'MESERO' ORDER BY nombre_completo";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpleado(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar meseros: " + e.getMessage());
        }
        return lista;
    }
}