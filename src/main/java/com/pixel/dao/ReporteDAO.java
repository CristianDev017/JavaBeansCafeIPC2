package com.pixel.dao;

import com.pixel.conexion.Conexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    // Flujo de caja
    public FlujoCaja calcularFlujoCaja(LocalDate fechaInicio, LocalDate fechaFin) {
        double ingresos = 0, egresosNomina = 0, egresosCompras = 0;

        String condicionFecha = " AND (? IS NULL OR fecha >= ?) AND (? IS NULL OR fecha <= ?)";

        String sqlIngresos = "SELECT COALESCE(SUM(total + propina), 0) FROM cuenta " +
                "WHERE estado = 'PAGADA'" +
                " AND (? IS NULL OR fecha_liberacion >= ?) AND (? IS NULL OR fecha_liberacion <= ?)";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sqlIngresos)) {
            setFiltrosFecha(ps, fechaInicio, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                ingresos = rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Error calculando ingresos: " + e.getMessage());
        }

        String sqlNomina = "SELECT COALESCE(SUM(monto), 0) FROM nomina " +
                "WHERE estado = 'PAGADO'" +
                " AND (? IS NULL OR fecha_emision >= ?) AND (? IS NULL OR fecha_emision <= ?)";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sqlNomina)) {
            setFiltrosFecha(ps, fechaInicio, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                egresosNomina = rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Error calculando egresos de nómina: " + e.getMessage());
        }

        String sqlCompras = "SELECT COALESCE(SUM(costo_total), 0) FROM compra_insumo " +
                "WHERE (? IS NULL OR fecha_compra >= ?) AND (? IS NULL OR fecha_compra <= ?)";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sqlCompras)) {
            ps.setObject(1, fechaInicio);
            ps.setObject(2, fechaInicio);
            ps.setObject(3, fechaFin);
            ps.setObject(4, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                egresosCompras = rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Error calculando egresos de compras: " + e.getMessage());
        }

        return new FlujoCaja(ingresos, egresosNomina, egresosCompras);
    }

    private void setFiltrosFecha(PreparedStatement ps, LocalDate inicio, LocalDate fin) throws SQLException {
        ps.setObject(1, inicio);
        ps.setObject(2, inicio);
        ps.setObject(3, fin);
        ps.setObject(4, fin);
    }

    // Productos más vendidos
    public List<ProductoVendido> productosMasVendidos(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ProductoVendido> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, SUM(d.cantidad) AS total_vendido " +
                "FROM detalle_cuenta d " +
                "JOIN producto p ON d.codigo_producto = p.codigo_producto " +
                "JOIN cuenta c ON d.id_cuenta = c.id_cuenta " +
                "WHERE c.estado = 'PAGADA'" +
                " AND (? IS NULL OR c.fecha_liberacion >= ?) AND (? IS NULL OR c.fecha_liberacion <= ?)" +
                " GROUP BY p.codigo_producto, p.nombre " +
                " ORDER BY total_vendido DESC";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setFiltrosFecha(ps, fechaInicio, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ProductoVendido(rs.getString("nombre"), rs.getInt("total_vendido")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en reporte de productos más vendidos: " + e.getMessage());
        }
        return lista;
    }

    // Insumos con bajo stock
    public List<com.pixel.modelo.Insumo> insumosBajoStock() {
        return new InsumoDAO().listarConBajoStock();
    }

    public static class FlujoCaja {
        public final double ingresos;
        public final double egresosNomina;
        public final double egresosCompras;

        public FlujoCaja(double ingresos, double egresosNomina, double egresosCompras) {
            this.ingresos = ingresos;
            this.egresosNomina = egresosNomina;
            this.egresosCompras = egresosCompras;
        }

        public double getEgresosTotales() { return egresosNomina + egresosCompras; }
        public double getBalance() { return ingresos - getEgresosTotales(); }
    }

    public static class ProductoVendido {
        public final String nombre;
        public final int cantidadVendida;

        public ProductoVendido(String nombre, int cantidadVendida) {
            this.nombre = nombre;
            this.cantidadVendida = cantidadVendida;
        }
    }
}