package com.pixel.dao;

import com.pixel.conexion.Conexion;
import com.pixel.modelo.Producto;
import com.pixel.modelo.RecetaItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public boolean registrar(Producto producto, List<RecetaItem> receta) {
        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false);

            String sqlProducto = "INSERT INTO producto (nombre, categoria, precio_venta, ruta_foto, activo) " +
                    "VALUES (?, ?, ?, ?, ?)";
            int codigoGenerado;
            try (PreparedStatement ps = con.prepareStatement(sqlProducto, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, producto.getNombre());
                ps.setString(2, producto.getCategoria());
                ps.setDouble(3, producto.getPrecioVenta());
                ps.setString(4, producto.getRutaFoto());
                ps.setBoolean(5, true);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    codigoGenerado = rs.getInt(1);
                }
            }

            insertarReceta(con, codigoGenerado, receta);

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Error al registrar producto: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean actualizar(Producto producto, List<RecetaItem> receta) {
        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false);

            String sqlProducto = "UPDATE producto SET nombre=?, categoria=?, precio_venta=?, ruta_foto=? " +
                    "WHERE codigo_producto=?";
            try (PreparedStatement ps = con.prepareStatement(sqlProducto)) {
                ps.setString(1, producto.getNombre());
                ps.setString(2, producto.getCategoria());
                ps.setDouble(3, producto.getPrecioVenta());
                ps.setString(4, producto.getRutaFoto());
                ps.setInt(5, producto.getCodigoProducto());
                ps.executeUpdate();
            }

            // Borramos la receta anterior y la volvemos a insertar completa.
            // Es más simple y seguro que intentar calcular diferencias línea por línea.
            String sqlBorrarReceta = "DELETE FROM receta WHERE codigo_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlBorrarReceta)) {
                ps.setInt(1, producto.getCodigoProducto());
                ps.executeUpdate();
            }

            insertarReceta(con, producto.getCodigoProducto(), receta);

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Error al actualizar producto: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void insertarReceta(Connection con, int codigoProducto, List<RecetaItem> receta) throws SQLException {
        String sqlReceta = "INSERT INTO receta (codigo_producto, codigo_insumo, cantidad_requerida) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlReceta)) {
            for (RecetaItem item : receta) {
                ps.setInt(1, codigoProducto);
                ps.setInt(2, item.getCodigoInsumo());
                ps.setDouble(3, item.getCantidadRequerida());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Producto> listarActivos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE activo = TRUE ORDER BY categoria, nombre";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    public boolean deshabilitar(int codigoProducto) {
        String sql = "UPDATE producto SET activo = FALSE WHERE codigo_producto = ?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigoProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al deshabilitar producto: " + e.getMessage());
            return false;
        }
    }

    // Trae la receta de un producto, con el nombre del insumo incluido (JOIN)
    public List<RecetaItem> obtenerReceta(int codigoProducto) {
        List<RecetaItem> lista = new ArrayList<>();
        String sql = "SELECT r.codigo_insumo, i.nombre, r.cantidad_requerida " +
                "FROM receta r JOIN insumo i ON r.codigo_insumo = i.codigo_insumo " +
                "WHERE r.codigo_producto = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigoProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RecetaItem(
                            rs.getInt("codigo_insumo"),
                            rs.getString("nombre"),
                            rs.getDouble("cantidad_requerida")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener receta: " + e.getMessage());
        }
        return lista;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setCodigoProducto(rs.getInt("codigo_producto"));
        p.setNombre(rs.getString("nombre"));
        p.setCategoria(rs.getString("categoria"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setRutaFoto(rs.getString("ruta_foto"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}