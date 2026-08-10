package com.pixel;

import com.pixel.conexion.Conexion;
import com.pixel.dao.EmpleadoDAO;
import com.pixel.modelo.Empleado;
import com.pixel.ui.MainFrame;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class Main {
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });

        // Prueba 1: verificar conexión
        try (Connection con = Conexion.obtenerConexion()) {
            System.out.println("Conexión exitosa: " + con.isValid(2));
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }

        // Prueba 2: registrar y listar empleados usando el DAO
        EmpleadoDAO dao = new EmpleadoDAO();

        Empleado nuevo = new Empleado("1234567890101", "Juan Pérez", "juan@correo.com",
                "MESERO", "MATUTINA", 3500.0, LocalDate.now(), true);
        dao.registrar(nuevo);

        for (Empleado e : dao.listarTodos()) {
            System.out.println(e);
        }
    }
}