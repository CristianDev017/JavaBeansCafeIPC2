package com.pixel.dao;

import com.pixel.conexion.Conexion;
import com.pixel.modelo.Insumo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsumoDAO {

    public boolean registrar(Insumo insumo) {
        String sql = "INSERT INTO insumo (nombre, unidad_medida, stock_actual, stock_minimo, costo) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getUnidadMedida());
            ps.setDouble(3, insumo.getStockActual());
            ps.setDouble(4, insumo.getStockMinimo());
            ps.setDouble(5, insumo.getCosto());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al registrar insumo: " + e.getMessage());
            return false;
        }
    }

    public List<Insumo> listarTodos() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumo ORDER BY nombre";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearInsumo(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar insumos: " + e.getMessage());
        }
        return lista;
    }

    public List<Insumo> listarConBajoStock() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumo WHERE stock_actual <= stock_minimo ORDER BY nombre";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearInsumo(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar insumos con bajo stock: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizar(Insumo insumo) {
        String sql = "UPDATE insumo SET nombre=?, unidad_medida=?, stock_minimo=?, costo=? " +
                "WHERE codigo_insumo=?";
        // Nota: stock_actual NO se edita aquí directamente, solo mediante
        // registrarCompra() (entradas) o el descuento automático en Cuentas (salidas).
        // Esto evita que alguien "cuadre" el inventario a mano sin dejar rastro.

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getUnidadMedida());
            ps.setDouble(3, insumo.getStockMinimo());
            ps.setDouble(4, insumo.getCosto());
            ps.setInt(5, insumo.getCodigoInsumo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar insumo: " + e.getMessage());
            return false;
        }
    }

    // Registra una compra Y suma el stock, en una sola transacción
    public boolean registrarCompra(int codigoInsumo, double cantidad, double costoTotal) {
        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false);

            String sqlCompra = "INSERT INTO compra_insumo (codigo_insumo, cantidad, costo_total, fecha_compra) " +
                    "VALUES (?, ?, ?, CURDATE())";
            try (PreparedStatement ps = con.prepareStatement(sqlCompra)) {
                ps.setInt(1, codigoInsumo);
                ps.setDouble(2, cantidad);
                ps.setDouble(3, costoTotal);
                ps.executeUpdate();
            }

            String sqlStock = "UPDATE insumo SET stock_actual = stock_actual + ? WHERE codigo_insumo = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                ps.setDouble(1, cantidad);
                ps.setInt(2, codigoInsumo);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Error al registrar compra: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Usado internamente por Cuentas para descontar stock al confirmar un pedido.
    // Retorna false si no hay suficiente stock (y no descuenta nada).
    public boolean descontarStock(Connection con, int codigoInsumo, double cantidad) throws SQLException {
        String sqlVerificar = "SELECT stock_actual FROM insumo WHERE codigo_insumo = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
            ps.setInt(1, codigoInsumo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double stockActual = rs.getDouble("stock_actual");
                    if (stockActual < cantidad) {
                        return false; // no hay suficiente inventario
                    }
                }
            }
        }

        String sqlDescontar = "UPDATE insumo SET stock_actual = stock_actual - ? WHERE codigo_insumo = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlDescontar)) {
            ps.setDouble(1, cantidad);
            ps.setInt(2, codigoInsumo);
            ps.executeUpdate();
        }
        return true;
    }

    private Insumo mapearInsumo(ResultSet rs) throws SQLException {
        Insumo i = new Insumo();
        i.setCodigoInsumo(rs.getInt("codigo_insumo"));
        i.setNombre(rs.getString("nombre"));
        i.setUnidadMedida(rs.getString("unidad_medida"));
        i.setStockActual(rs.getDouble("stock_actual"));
        i.setStockMinimo(rs.getDouble("stock_minimo"));
        i.setCosto(rs.getDouble("costo"));
        return i;
    }
}