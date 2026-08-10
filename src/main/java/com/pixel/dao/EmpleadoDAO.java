package com.pixel.dao;

import com.pixel.conexion.Conexion;
import com.pixel.modelo.Empleado;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    // ---------- CREAR ----------
    public boolean registrar(Empleado emp) {
        String sql = "INSERT INTO empleado (dpi, nombre_completo, correo, rol, jornada, salario, fecha_contratacion, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getDpi());
            ps.setString(2, emp.getNombreCompleto());
            ps.setString(3, emp.getCorreo());
            ps.setString(4, emp.getRol());
            ps.setString(5, emp.getJornada());
            ps.setDouble(6, emp.getSalario());
            ps.setObject(7, emp.getFechaContratacion());
            ps.setBoolean(8, emp.isActivo());

            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Esto salta si el DPI ya existe (PK duplicada) o el correo ya existe (UNIQUE)
            System.out.println("Error: DPI o correo ya registrado. " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.out.println("Error al registrar empleado: " + e.getMessage());
            return false;
        }
    }

    // ---------- LISTAR (todos, incluyendo inactivos) ----------
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleado ORDER BY nombre_completo";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpleado(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar empleados: " + e.getMessage());
        }
        return lista;
    }

    // ---------- LISTAR SOLO ACTIVOS (para asignar meseros, calcular nómina, etc.) ----------
    public List<Empleado> listarActivos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleado WHERE activo = TRUE ORDER BY nombre_completo";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpleado(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar empleados activos: " + e.getMessage());
        }
        return lista;
    }

    // ---------- ACTUALIZAR ----------
    public boolean actualizar(Empleado emp) {
        String sql = "UPDATE empleado SET nombre_completo=?, correo=?, rol=?, jornada=?, salario=? " +
                "WHERE dpi=?";
        // Nota: fecha_contratacion y dpi normalmente no se editan una vez creado el registro.

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getNombreCompleto());
            ps.setString(2, emp.getCorreo());
            ps.setString(3, emp.getRol());
            ps.setString(4, emp.getJornada());
            ps.setDouble(5, emp.getSalario());
            ps.setString(6, emp.getDpi());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar empleado: " + e.getMessage());
            return false;
        }
    }

    // ---------- DESHABILITAR (no se borra, por las FK con nomina/cuenta) ----------
    public boolean deshabilitar(String dpi) {
        String sql = "UPDATE empleado SET activo = FALSE WHERE dpi = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dpi);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al deshabilitar empleado: " + e.getMessage());
            return false;
        }
    }

    // ---------- Método privado auxiliar: convierte una fila del ResultSet en un objeto Empleado ----------
    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        Empleado emp = new Empleado();
        emp.setDpi(rs.getString("dpi"));
        emp.setNombreCompleto(rs.getString("nombre_completo"));
        emp.setCorreo(rs.getString("correo"));
        emp.setRol(rs.getString("rol"));
        emp.setJornada(rs.getString("jornada"));
        emp.setSalario(rs.getDouble("salario"));
        emp.setFechaContratacion(rs.getObject("fecha_contratacion", LocalDate.class));
        emp.setActivo(rs.getBoolean("activo"));
        return emp;
    }
}