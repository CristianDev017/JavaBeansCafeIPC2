package com.pixel.ui;

import com.pixel.dao.MesaDAO;
import com.pixel.modelo.Mesa;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MesaInternalFrame extends JInternalFrame {

    private final MesaDAO dao = new MesaDAO();
    private JPanel panelMesas;

    private JTextField txtNumero, txtCapacidad;

    // Colores tema café (mismos que MainFrame)
    private static final Color CAFE_OSCURO = new Color(91, 58, 41);
    private static final Color CAFE_MEDIO  = new Color(121, 85, 72);
    private static final Color CAFE_CLARO  = new Color(166, 124, 82);
    private static final Color FONDO_CREMA = new Color(230, 220, 205);
    private static final Color BLANCO      = Color.WHITE;

    // Estados de mesa (se mantienen verde/rojo porque son indicadores funcionales)
    private static final Color MESA_LIBRE = new Color(198, 239, 206);
    private static final Color MESA_OCUPADA = new Color(255, 199, 199);

    public MesaInternalFrame() {
        super("Control de Mesas", true, true, true, true);
        setSize(750, 550);
        construirUI();
        cargarMesas();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(FONDO_CREMA);

        // ---------- Formulario para registrar mesas nuevas ----------
        JPanel panelForm = new JPanel();
        panelForm.setBackground(FONDO_CREMA);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CAFE_MEDIO, 1),
                        "Registrar / actualizar mesa"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        ((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) panelForm.getBorder()).getOutsideBorder())
                .setTitleColor(CAFE_OSCURO);

        txtNumero = new JTextField(5);
        txtCapacidad = new JTextField(5);
        for (JTextField campo : new JTextField[]{txtNumero, txtCapacidad}) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CAFE_CLARO, 1),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        }

        JButton btnRegistrar = crearBoton("Registrar mesa");
        JButton btnActualizarCapacidad = crearBoton("Actualizar capacidad");

        btnRegistrar.addActionListener(e -> registrarMesa());
        btnActualizarCapacidad.addActionListener(e -> actualizarCapacidad());

        JLabel lblNumero = new JLabel("Número de mesa:");
        JLabel lblCapacidad = new JLabel("Capacidad:");
        for (JLabel lbl : new JLabel[]{lblNumero, lblCapacidad}) {
            lbl.setForeground(CAFE_OSCURO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        panelForm.add(lblNumero);
        panelForm.add(txtNumero);
        panelForm.add(lblCapacidad);
        panelForm.add(txtCapacidad);
        panelForm.add(btnRegistrar);
        panelForm.add(btnActualizarCapacidad);

        // ---------- Panel visual de mesas (tarjetas de color) ----------
        panelMesas = new JPanel(new GridLayout(0, 4, 15, 15));
        panelMesas.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelMesas.setBackground(FONDO_CREMA);

        JScrollPane scroll = new JScrollPane(panelMesas);
        scroll.getViewport().setBackground(FONDO_CREMA);
        scroll.setBorder(BorderFactory.createLineBorder(CAFE_CLARO, 1));

        JButton btnRefrescar = crearBoton("Refrescar estado");
        btnRefrescar.addActionListener(e -> cargarMesas());
        JPanel panelInferior = new JPanel();
        panelInferior.setBackground(FONDO_CREMA);
        panelInferior.add(btnRefrescar);

        add(panelForm, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(CAFE_MEDIO);
        boton.setForeground(BLANCO);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private void registrarMesa() {
        try {
            int numero = Integer.parseInt(txtNumero.getText().trim());
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());

            if (capacidad <= 0) {
                JOptionPane.showMessageDialog(this, "La capacidad debe ser mayor a 0.");
                return;
            }

            Mesa mesa = new Mesa(numero, capacidad, "LIBRE");
            if (dao.registrar(mesa)) {
                JOptionPane.showMessageDialog(this, "Mesa registrada.");
                limpiarFormulario();
                cargarMesas();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar (¿número repetido?).",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Número y capacidad deben ser enteros válidos.");
        }
    }

    private void actualizarCapacidad() {
        try {
            int numero = Integer.parseInt(txtNumero.getText().trim());
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());

            Mesa mesa = new Mesa(numero, capacidad, null);
            if (dao.actualizar(mesa)) {
                JOptionPane.showMessageDialog(this, "Capacidad actualizada.");
                limpiarFormulario();
                cargarMesas();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró esa mesa.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Número y capacidad deben ser enteros válidos.");
        }
    }

    private void limpiarFormulario() {
        txtNumero.setText("");
        txtCapacidad.setText("");
    }

    private void cargarMesas() {
        panelMesas.removeAll();

        List<Mesa> mesas = dao.listarTodas();
        for (Mesa m : mesas) {
            panelMesas.add(crearTarjetaMesa(m));
        }

        panelMesas.revalidate();
        panelMesas.repaint();
    }

    private JPanel crearTarjetaMesa(Mesa mesa) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createLineBorder(CAFE_OSCURO, 1));
        tarjeta.setPreferredSize(new Dimension(120, 100));

        // El color de estado (libre/ocupada) se mantiene igual: es información funcional, no decorativa
        Color colorFondo = mesa.getEstado().equals("LIBRE")
                ? MESA_LIBRE   // verde suave
                : MESA_OCUPADA;  // rojo suave
        tarjeta.setBackground(colorFondo);

        JLabel lblNumero = new JLabel("Mesa " + mesa.getNumeroMesa());
        lblNumero.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNumero.setForeground(CAFE_OSCURO);
        lblNumero.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblCapacidad = new JLabel("Capacidad: " + mesa.getCapacidad());
        lblCapacidad.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCapacidad.setForeground(CAFE_OSCURO);
        lblCapacidad.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEstado = new JLabel(mesa.getEstado());
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstado.setForeground(CAFE_OSCURO);
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(lblNumero);
        tarjeta.add(lblCapacidad);
        tarjeta.add(lblEstado);

        return tarjeta;
    }
}