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

    public MesaInternalFrame() {
        super("Control de Mesas", true, true, true, true);
        setSize(750, 550);
        construirUI();
        cargarMesas();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        // ---------- Formulario para registrar mesas nuevas ----------
        JPanel panelForm = new JPanel();
        panelForm.setBorder(BorderFactory.createTitledBorder("Registrar / actualizar mesa"));

        txtNumero = new JTextField(5);
        txtCapacidad = new JTextField(5);
        JButton btnRegistrar = new JButton("Registrar mesa");
        JButton btnActualizarCapacidad = new JButton("Actualizar capacidad");

        btnRegistrar.addActionListener(e -> registrarMesa());
        btnActualizarCapacidad.addActionListener(e -> actualizarCapacidad());

        panelForm.add(new JLabel("Número de mesa:"));
        panelForm.add(txtNumero);
        panelForm.add(new JLabel("Capacidad:"));
        panelForm.add(txtCapacidad);
        panelForm.add(btnRegistrar);
        panelForm.add(btnActualizarCapacidad);

        // ---------- Panel visual de mesas (tarjetas de color) ----------
        panelMesas = new JPanel(new GridLayout(0, 4, 15, 15));
        panelMesas.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(panelMesas);

        JButton btnRefrescar = new JButton("Refrescar estado");
        btnRefrescar.addActionListener(e -> cargarMesas());

        add(panelForm, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(btnRefrescar, BorderLayout.SOUTH);
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
        tarjeta.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        tarjeta.setPreferredSize(new Dimension(120, 100));

        Color colorFondo = mesa.getEstado().equals("LIBRE")
                ? new Color(198, 239, 206)   // verde suave
                : new Color(255, 199, 199);  // rojo suave
        tarjeta.setBackground(colorFondo);

        JLabel lblNumero = new JLabel("Mesa " + mesa.getNumeroMesa());
        lblNumero.setFont(lblNumero.getFont().deriveFont(Font.BOLD, 16f));
        lblNumero.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblCapacidad = new JLabel("Capacidad: " + mesa.getCapacidad());
        lblCapacidad.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEstado = new JLabel(mesa.getEstado());
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD));
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(lblNumero);
        tarjeta.add(lblCapacidad);
        tarjeta.add(lblEstado);

        return tarjeta;
    }
}