package com.pixel.dao;

import com.pixel.conexion.Conexion;
import com.pixel.modelo.Mesa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {

    public boolean registrar(Mesa mesa) {
        String sql = "INSERT INTO mesa (numero_mesa, capacidad, estado) VALUES (?, ?, 'LIBRE')";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, mesa.getNumeroMesa());
            ps.setInt(2, mesa.getCapacidad());
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: ya existe una mesa con ese número. " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.out.println("Error al registrar mesa: " + e.getMessage());
            return false;
        }
    }

    public List<Mesa> listarTodas() {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT * FROM mesa ORDER BY numero_mesa";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMesa(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar mesas: " + e.getMessage());
        }
        return lista;
    }

    public List<Mesa> listarLibres() {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT * FROM mesa WHERE estado = 'LIBRE' ORDER BY numero_mesa";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMesa(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar mesas libres: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizar(Mesa mesa) {
        String sql = "UPDATE mesa SET capacidad = ? WHERE numero_mesa = ?";
        // El estado NO se actualiza desde aquí: cambia automáticamente
        // cuando se abre/cierra una cuenta (lo vemos en el módulo de Cuentas).

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, mesa.getCapacidad());
            ps.setInt(2, mesa.getNumeroMesa());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar mesa: " + e.getMessage());
            return false;
        }
    }

    // Usado por CuentaDAO al abrir/cerrar una cuenta. Recibe la conexión
    // para poder formar parte de la misma transacción (igual que con insumos).
    public void cambiarEstado(Connection con, int numeroMesa, String nuevoEstado) throws SQLException {
        String sql = "UPDATE mesa SET estado = ? WHERE numero_mesa = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, numeroMesa);
            ps.executeUpdate();
        }
    }

    private Mesa mapearMesa(ResultSet rs) throws SQLException {
        Mesa m = new Mesa();
        m.setNumeroMesa(rs.getInt("numero_mesa"));
        m.setCapacidad(rs.getInt("capacidad"));
        m.setEstado(rs.getString("estado"));
        return m;
    }
}