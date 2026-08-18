package com.pixel.dao;

import com.pixel.conexion.Conexion;
import com.pixel.modelo.Empleado;
import com.pixel.modelo.Nomina;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class NominaDAO {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    public void generarNominasSiCorresponde() {
        LocalDate hoy = LocalDate.of(2026, 8, 10);
        YearMonth mesActual = YearMonth.from(hoy);

        LocalDate fechaCorteQuincena = mesActual.atDay(15);
        LocalDate fechaCorteFinDeMes = mesActual.atEndOfMonth();

        LocalDate fechaEmisionQuincena = fechaCorteQuincena.minusDays(5); // día 10
        LocalDate fechaEmisionFinDeMes = fechaCorteFinDeMes.minusDays(5);

        if (hoy.isEqual(fechaEmisionQuincena)) {
            generarNominasDelPeriodo("QUINCENA", fechaCorteQuincena);
        }
        if (hoy.isEqual(fechaEmisionFinDeMes)) {
            generarNominasDelPeriodo("FIN_DE_MES", fechaCorteFinDeMes);
        }
    }

    private void generarNominasDelPeriodo(String tipoPago, LocalDate fechaCorte) {
        List<Empleado> activos = empleadoDAO.listarActivos();

        for (Empleado emp : activos) {
            if (yaExisteNominaDelPeriodo(emp.getDpi(), tipoPago, fechaCorte)) {
                continue; // evita duplicar si el sistema se abre varias veces ese día
            }

            double monto;
            if (tipoPago.equals("QUINCENA")) {
                monto = emp.getSalario() * 0.30;
            } else {
                double propinasDelMes = obtenerPropinasDelMes(emp.getDpi(), fechaCorte);
                monto = (emp.getSalario() * 0.70) + propinasDelMes;
            }

            registrarNomina(emp.getDpi(), tipoPago, monto);
        }
    }

    private boolean yaExisteNominaDelPeriodo(String dpi, String tipoPago, LocalDate fechaCorte) {
        String sql = "SELECT COUNT(*) FROM nomina WHERE dpi_empleado = ? AND tipo_pago = ? " +
                "AND MONTH(fecha_emision) = ? AND YEAR(fecha_emision) = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dpi);
            ps.setString(2, tipoPago);
            ps.setInt(3, fechaCorte.getMonthValue());
            ps.setInt(4, fechaCorte.getYear());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error al verificar nómina existente: " + e.getMessage());
            return true; // por seguridad, si falla la validación, no generamos duplicados
        }
    }

    private double obtenerPropinasDelMes(String dpiMesero, LocalDate fechaCorte) {
        String sql = "SELECT COALESCE(SUM(propina), 0) FROM cuenta " +
                "WHERE dpi_mesero = ? AND estado = 'PAGADA' " +
                "AND MONTH(fecha_liberacion) = ? AND YEAR(fecha_liberacion) = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dpiMesero);
            ps.setInt(2, fechaCorte.getMonthValue());
            ps.setInt(3, fechaCorte.getYear());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            System.out.println("Error al calcular propinas: " + e.getMessage());
            return 0;
        }
    }

    private void registrarNomina(String dpi, String tipoPago, double monto) {
        String sql = "INSERT INTO nomina (dpi_empleado, fecha_emision, tipo_pago, monto, estado) " +
                "VALUES (?, CURDATE(), ?, ?, 'PENDIENTE')";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dpi);
            ps.setString(2, tipoPago);
            ps.setDouble(3, monto);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al registrar nómina: " + e.getMessage());
        }
    }

    public boolean marcarComoPagado(int codigoNomina) {
        String sql = "UPDATE nomina SET estado = 'PAGADO' WHERE codigo_nomina = ?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigoNomina);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al marcar nómina como pagada: " + e.getMessage());
            return false;
        }
    }

    public List<Nomina> listarTodas() {
        List<Nomina> lista = new ArrayList<>();
        String sql = "SELECT n.*, e.nombre_completo FROM nomina n " +
                "JOIN empleado e ON n.dpi_empleado = e.dpi " +
                "ORDER BY n.fecha_emision DESC";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearNomina(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar nóminas: " + e.getMessage());
        }
        return lista;
    }

    private Nomina mapearNomina(ResultSet rs) throws SQLException {
        Nomina n = new Nomina();
        n.setCodigoNomina(rs.getInt("codigo_nomina"));
        n.setDpiEmpleado(rs.getString("dpi_empleado"));
        n.setNombreEmpleado(rs.getString("nombre_completo"));
        n.setFechaEmision(rs.getObject("fecha_emision", LocalDate.class));
        n.setTipoPago(rs.getString("tipo_pago"));
        n.setMonto(rs.getDouble("monto"));
        n.setEstado(rs.getString("estado"));
        return n;
    }
}