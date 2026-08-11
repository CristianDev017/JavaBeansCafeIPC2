package com.pixel.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JDesktopPane escritorio;

    public MainFrame() {
        setTitle("JavaBeans Café - Sistema de Administración");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // centra la ventana

        escritorio = new JDesktopPane();
        add(escritorio, BorderLayout.CENTER);

        setJMenuBar(construirMenu());
    }

    private JMenuBar construirMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuPersonal = new JMenu("Personal");
        JMenuItem itemGestionPersonal = new JMenuItem("Gestión de Personal");
        itemGestionPersonal.addActionListener(e -> abrirGestionPersonal());
        menuPersonal.add(itemGestionPersonal);

        JMenu menuInventario = new JMenu("Inventario");
        JMenuItem itemGestionInventario = new JMenuItem("Gestión de Inventario");
        itemGestionInventario.addActionListener(e -> abrirGestionInventario());
        menuInventario.add(itemGestionInventario);

        JMenu menuMenu = new JMenu("Menú");
        JMenuItem itemGestionMenu = new JMenuItem("Gestión de Menú");
        itemGestionMenu.addActionListener(e -> abrirGestionMenu());
        menuMenu.add(itemGestionMenu);

        JMenu menuMesas = new JMenu("Mesas");
        JMenuItem itemControlMesas = new JMenuItem("Control de Mesas");
        itemControlMesas.addActionListener(e -> abrirControlMesas());
        menuMesas.add(itemControlMesas);

        JMenu menuCuentas = new JMenu("Cuentas");
        JMenu menuReportes = new JMenu("Reportes");

        menuBar.add(menuPersonal);
        menuBar.add(menuInventario);
        menuBar.add(menuMenu);
        menuBar.add(menuMesas);
        menuBar.add(menuCuentas);
        menuBar.add(menuReportes);

        return menuBar;
    }

    private void abrirGestionPersonal() {
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame instanceof EmpleadoInternalFrame) {
                try {
                    frame.setSelected(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }

        EmpleadoInternalFrame internal = new EmpleadoInternalFrame();
        escritorio.add(internal);
        internal.setVisible(true);
        try {
            internal.setSelected(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void abrirGestionInventario() {
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame instanceof InsumoInternalFrame) {
                try {
                    frame.setSelected(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }

        InsumoInternalFrame internal = new InsumoInternalFrame();
        escritorio.add(internal);
        internal.setVisible(true);
        try {
            internal.setSelected(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void abrirGestionMenu() {
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame instanceof ProductoInternalFrame) {
                try { frame.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
                return;
            }
        }
        ProductoInternalFrame internal = new ProductoInternalFrame();
        escritorio.add(internal);
        internal.setVisible(true);
        try { internal.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void abrirControlMesas() {
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame instanceof MesaInternalFrame) {
                try { frame.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
                return;
            }
        }
        MesaInternalFrame internal = new MesaInternalFrame();
        escritorio.add(internal);
        internal.setVisible(true);
        try { internal.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
    }
}