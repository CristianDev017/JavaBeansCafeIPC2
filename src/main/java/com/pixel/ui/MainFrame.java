package com.pixel.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JDesktopPane escritorio;

    public MainFrame() {
        setTitle("JavaBeans Café — Sistema de Administración");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        add(construirPanelBotones(), BorderLayout.NORTH);

        escritorio = new JDesktopPane();
        escritorio.setBackground(new Color(230, 220, 205));
        add(escritorio, BorderLayout.CENTER);

        setJMenuBar(construirMenu());

        new com.pixel.dao.NominaDAO().generarNominasSiCorresponde();
    }

    // ---------- Panel de botones grandes tipo dashboard ----------
    private JPanel construirPanelBotones() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(91, 58, 41)); // café oscuro de fondo

        panel.add(crearBotonModulo("Personal",  new Color(121, 85, 72), e -> abrirGestionPersonal()));
        panel.add(crearBotonModulo("Inventario",  new Color(121, 85, 72), e -> abrirGestionInventario()));
        panel.add(crearBotonModulo("Menú", new Color(121, 85, 72), e -> abrirGestionMenu()));
        panel.add(crearBotonModulo("Mesas",  new Color(121, 85, 72), e -> abrirControlMesas()));
        panel.add(crearBotonModulo("Cuentas",  new Color(121, 85, 72), e -> abrirGestionCuentas()));
        panel.add(crearBotonModulo("Reportes",  new Color(121, 85, 72), e -> abrirReportes()));

        return panel;
    }

    private JButton crearBotonModulo(String texto, Color color, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(150, 70));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(accion);
        return boton;
    }

    // ---------- Barra de menú (obligatoria por especificación, se mantiene arriba de todo) ----------
    private JMenuBar construirMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuPersonal = new JMenu("Personal");
        JMenuItem itemGestionPersonal = new JMenuItem("Gestión de Personal");
        itemGestionPersonal.addActionListener(e -> abrirGestionPersonal());
        JMenuItem itemGestionNominas = new JMenuItem("Gestión de Nóminas");
        itemGestionNominas.addActionListener(e -> abrirGestionNominas());
        menuPersonal.add(itemGestionPersonal);
        menuPersonal.add(itemGestionNominas);

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
        JMenuItem itemGestionCuentas = new JMenuItem("Gestión de Cuentas");
        itemGestionCuentas.addActionListener(e -> abrirGestionCuentas());
        menuCuentas.add(itemGestionCuentas);

        JMenu menuReportes = new JMenu("Reportes");
        JMenuItem itemReportes = new JMenuItem("Ver Reportes");
        itemReportes.addActionListener(e -> abrirReportes());
        menuReportes.add(itemReportes);

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

    private void abrirGestionCuentas() {
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame instanceof CuentaInternalFrame) {
                try { frame.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
                return;
            }
        }
        CuentaInternalFrame internal = new CuentaInternalFrame();
        escritorio.add(internal);
        internal.setVisible(true);
        try { internal.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void abrirGestionNominas() {
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame instanceof NominaInternalFrame) {
                try { frame.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
                return;
            }
        }
        NominaInternalFrame internal = new NominaInternalFrame();
        escritorio.add(internal);
        internal.setVisible(true);
        try { internal.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void abrirReportes() {
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame instanceof ReporteInternalFrame) {
                try { frame.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
                return;
            }
        }
        ReporteInternalFrame internal = new ReporteInternalFrame();
        escritorio.add(internal);
        internal.setVisible(true);
        try { internal.setSelected(true); } catch (Exception ex) { ex.printStackTrace(); }
    }
}