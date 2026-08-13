package com.pixel;

import com.pixel.conexion.Conexion;
import com.pixel.ui.MainFrame;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        // Verificar conexión
        try (Connection con = Conexion.obtenerConexion()) {
            System.out.println("Conexión exitosa: " + con.isValid(2));
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}