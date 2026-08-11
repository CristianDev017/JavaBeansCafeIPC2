package com.pixel.dao;

import com.pixel.conexion.Conexion;
import com.pixel.modelo.Cuenta;
import com.pixel.modelo.DetalleCuenta;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO {

    private final InsumoDAO insumoDAO = new InsumoDAO();
    private final MesaDAO mesaDAO = new MesaDAO();

    // ---------- Abrir una cuenta nueva ----------
    // Devuelve el id_cuenta generado, o -1 si falló (ej. mesa ya ocupada).
    public int abrirCuenta(int numeroMesa, String dpiMesero) {
        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false);

            // Verificamos que la mesa siga LIBRE en este momento (evita condición de carrera
            // si dos meseros intentan abrir la misma mesa casi al mismo tiempo).
            String sqlVerificar = "SELECT estado FROM mesa WHERE numero_mesa = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                ps.setInt(1, numeroMesa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"LIBRE".equals(rs.getString("estado"))) {
                        con.rollback();
                        return -1; // la mesa no existe o ya está ocupada
                    }
                }
            }

            String sqlInsert = "INSERT INTO cuenta (numero_mesa, dpi_mesero, fecha_ocupacion, estado, total, propina) " +
                    "VALUES (?, ?, NOW(), 'ABIERTA', 0, 0)";
            int idGenerado;
            try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, numeroMesa);
                ps.setString(2, dpiMesero);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idGenerado = rs.getInt(1);
                }
            }

            mesaDAO.cambiarEstado(con, numeroMesa, "OCUPADA");

            con.commit();
            return idGenerado;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Error al abrir cuenta: " + e.getMessage());
            return -1;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ---------- Agregar un producto a la cuenta (descuenta inventario) ----------
    // Devuelve un mensaje: null si tuvo éxito, o el motivo del error si falló.
    public String agregarProducto(int idCuenta, int codigoProducto, int cantidad) {
        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false);

            // 1. Traer precio y receta del producto
            double precioUnitario;
            String sqlPrecio = "SELECT precio_venta FROM producto WHERE codigo_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPrecio)) {
                ps.setInt(1, codigoProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return "El producto no existe.";
                    }
                    precioUnitario = rs.getDouble("precio_venta");
                }
            }

            List<int[]> insumosNecesarios = new ArrayList<>(); // [codigoInsumo] paralelo a cantidadesRequeridas
            List<Double> cantidadesRequeridas = new ArrayList<>();
            String sqlReceta = "SELECT codigo_insumo, cantidad_requerida FROM receta WHERE codigo_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlReceta)) {
                ps.setInt(1, codigoProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        insumosNecesarios.add(new int[]{rs.getInt("codigo_insumo")});
                        cantidadesRequeridas.add(rs.getDouble("cantidad_requerida") * cantidad);
                    }
                }
            }

            // 2. Descontar cada insumo de la receta (multiplicado por la cantidad pedida)
            for (int i = 0; i < insumosNecesarios.size(); i++) {
                int codigoInsumo = insumosNecesarios.get(i)[0];
                double cantidadADescontar = cantidadesRequeridas.get(i);

                boolean exito = insumoDAO.descontarStock(con, codigoInsumo, cantidadADescontar);
                if (!exito) {
                    con.rollback();
                    return "Inventario insuficiente para preparar este producto.";
                }
            }

            // 3. Insertar el detalle de cuenta
            double subtotal = precioUnitario * cantidad;
            String sqlDetalle = "INSERT INTO detalle_cuenta (id_cuenta, codigo_producto, cantidad, subtotal) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idCuenta);
                ps.setInt(2, codigoProducto);
                ps.setInt(3, cantidad);
                ps.setDouble(4, subtotal);
                ps.executeUpdate();
            }

            // 4. Actualizar el total acumulado de la cuenta
            String sqlActualizarTotal = "UPDATE cuenta SET total = total + ? WHERE id_cuenta = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlActualizarTotal)) {
                ps.setDouble(1, subtotal);
                ps.setInt(2, idCuenta);
                ps.executeUpdate();
            }

            con.commit();
            return null; // éxito

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Error al agregar producto: " + e.getMessage());
            return "Ocurrió un error inesperado al procesar el pedido.";
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ---------- Agregar propina ----------
    public boolean agregarPropina(int idCuenta, double propina) {
        String sql = "UPDATE cuenta SET propina = propina + ? WHERE id_cuenta = ?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, propina);
            ps.setInt(2, idCuenta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al agregar propina: " + e.getMessage());
            return false;
        }
    }

    // ---------- Cerrar / cobrar la cuenta ----------
    public boolean cerrarCuenta(int idCuenta) {
        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false);

            int numeroMesa;
            String sqlDatos = "SELECT numero_mesa FROM cuenta WHERE id_cuenta = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDatos)) {
                ps.setInt(1, idCuenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    numeroMesa = rs.getInt("numero_mesa");
                }
            }

            String sqlCerrar = "UPDATE cuenta SET estado = 'PAGADA', fecha_liberacion = NOW() WHERE id_cuenta = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlCerrar)) {
                ps.setInt(1, idCuenta);
                ps.executeUpdate();
            }

            mesaDAO.cambiarEstado(con, numeroMesa, "LIBRE");

            // NOTA: la propina se carga al pago del mes del mesero (NominaDAO)
            // en el siguiente módulo que vamos a construir: Nóminas.
            // Por ahora la propina queda registrada en la cuenta y la
            // recuperaremos desde ahí al generar la nómina de fin de mes.

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Error al cerrar cuenta: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ---------- Listar cuentas abiertas ----------
    public List<Cuenta> listarAbiertas() {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT c.*, e.nombre_completo FROM cuenta c " +
                "JOIN empleado e ON c.dpi_mesero = e.dpi " +
                "WHERE c.estado = 'ABIERTA' ORDER BY c.fecha_ocupacion";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCuenta(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar cuentas abiertas: " + e.getMessage());
        }
        return lista;
    }

    // ---------- Listar el detalle (productos) de una cuenta ----------
    public List<DetalleCuenta> listarDetalle(int idCuenta) {
        List<DetalleCuenta> lista = new ArrayList<>();
        String sql = "SELECT d.*, p.nombre FROM detalle_cuenta d " +
                "JOIN producto p ON d.codigo_producto = p.codigo_producto " +
                "WHERE d.id_cuenta = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCuenta d = new DetalleCuenta();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdCuenta(rs.getInt("id_cuenta"));
                    d.setCodigoProducto(rs.getInt("codigo_producto"));
                    d.setNombreProducto(rs.getString("nombre"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setSubtotal(rs.getDouble("subtotal"));
                    lista.add(d);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar detalle de cuenta: " + e.getMessage());
        }
        return lista;
    }

    private Cuenta mapearCuenta(ResultSet rs) throws SQLException {
        Cuenta c = new Cuenta();
        c.setIdCuenta(rs.getInt("id_cuenta"));
        c.setNumeroMesa(rs.getInt("numero_mesa"));
        c.setDpiMesero(rs.getString("dpi_mesero"));
        c.setNombreMesero(rs.getString("nombre_completo"));
        c.setFechaOcupacion(rs.getObject("fecha_ocupacion", LocalDateTime.class));
        Timestamp liberacion = rs.getTimestamp("fecha_liberacion");
        if (liberacion != null) c.setFechaLiberacion(liberacion.toLocalDateTime());
        c.setEstado(rs.getString("estado"));
        c.setTotal(rs.getDouble("total"));
        c.setPropina(rs.getDouble("propina"));
        return c;
    }
}