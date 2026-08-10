package com.pixel.ui;

import com.pixel.dao.InsumoDAO;
import com.pixel.modelo.Insumo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InsumoInternalFrame extends JInternalFrame {

    private final InsumoDAO dao = new InsumoDAO();

    private JTextField txtNombre, txtUnidad, txtStockActual, txtStockMinimo, txtCosto;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private int codigoSeleccionado = -1;

    public InsumoInternalFrame() {
        super("Gestión de Inventario", true, true, true, true);
        setSize(750, 500);
        construirUI();
        cargarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelForm = new JPanel(new GridLayout(3, 4, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del insumo"));

        txtNombre = new JTextField();
        txtUnidad = new JTextField();
        txtStockActual = new JTextField();
        txtStockMinimo = new JTextField();
        txtCosto = new JTextField();

        panelForm.add(new JLabel("Nombre:"));
        panelForm.add(txtNombre);
        panelForm.add(new JLabel("Unidad de medida:"));
        panelForm.add(txtUnidad);

        panelForm.add(new JLabel("Stock inicial:"));
        panelForm.add(txtStockActual);
        panelForm.add(new JLabel("Stock mínimo:"));
        panelForm.add(txtStockMinimo);

        panelForm.add(new JLabel("Costo:"));
        panelForm.add(txtCosto);
        panelForm.add(new JLabel(""));
        panelForm.add(new JLabel(""));

        JPanel panelBotones = new JPanel();
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnRegistrarCompra = new JButton("Registrar compra (+stock)");
        JButton btnLimpiar = new JButton("Limpiar");

        btnRegistrar.addActionListener(e -> registrarInsumo());
        btnActualizar.addActionListener(e -> actualizarInsumo());
        btnRegistrarCompra.addActionListener(e -> registrarCompra());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnRegistrarCompra);
        panelBotones.add(btnLimpiar);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelForm, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        String[] columnas = {"Código", "Nombre", "Unidad", "Stock actual", "Stock mínimo", "Costo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());

        // ---------- Alerta visual: fila roja si el stock está bajo ----------
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                double stockActual = Double.parseDouble(table.getValueAt(row, 3).toString());
                double stockMinimo = Double.parseDouble(table.getValueAt(row, 4).toString());

                if (!isSelected) {
                    if (stockActual <= stockMinimo) {
                        c.setBackground(new Color(255, 205, 205)); // rojo suave: alerta
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Insumo> insumos = dao.listarTodos();
        for (Insumo i : insumos) {
            modeloTabla.addRow(new Object[]{
                    i.getCodigoInsumo(), i.getNombre(), i.getUnidadMedida(),
                    i.getStockActual(), i.getStockMinimo(), i.getCosto()
            });
        }
    }

    private void registrarInsumo() {
        if (!validarFormulario()) return;

        Insumo insumo = new Insumo(0, txtNombre.getText().trim(), txtUnidad.getText().trim(),
                Double.parseDouble(txtStockActual.getText().trim()),
                Double.parseDouble(txtStockMinimo.getText().trim()),
                Double.parseDouble(txtCosto.getText().trim()));

        if (dao.registrar(insumo)) {
            JOptionPane.showMessageDialog(this, "Insumo registrado correctamente.");
            limpiarFormulario();
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el insumo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarInsumo() {
        if (codigoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un insumo de la tabla primero.");
            return;
        }
        if (!validarFormulario()) return;

        Insumo insumo = new Insumo(codigoSeleccionado, txtNombre.getText().trim(), txtUnidad.getText().trim(),
                0, Double.parseDouble(txtStockMinimo.getText().trim()),
                Double.parseDouble(txtCosto.getText().trim()));

        if (dao.actualizar(insumo)) {
            JOptionPane.showMessageDialog(this, "Insumo actualizado correctamente.");
            limpiarFormulario();
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el insumo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarCompra() {
        if (codigoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un insumo de la tabla primero.");
            return;
        }

        String cantidadStr = JOptionPane.showInputDialog(this, "Cantidad comprada:");
        if (cantidadStr == null || cantidadStr.trim().isEmpty()) return;

        String costoStr = JOptionPane.showInputDialog(this, "Costo total de la compra:");
        if (costoStr == null || costoStr.trim().isEmpty()) return;

        try {
            double cantidad = Double.parseDouble(cantidadStr.trim());
            double costo = Double.parseDouble(costoStr.trim());

            if (dao.registrarCompra(codigoSeleccionado, cantidad, costo)) {
                JOptionPane.showMessageDialog(this, "Compra registrada y stock actualizado.");
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar la compra.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad y costo deben ser números válidos.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;

        codigoSeleccionado = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
        txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
        txtUnidad.setText(modeloTabla.getValueAt(fila, 2).toString());
        txtStockActual.setText(modeloTabla.getValueAt(fila, 3).toString());
        txtStockActual.setEditable(false); // el stock actual solo cambia por compras
        txtStockMinimo.setText(modeloTabla.getValueAt(fila, 4).toString());
        txtCosto.setText(modeloTabla.getValueAt(fila, 5).toString());
    }

    private void limpiarFormulario() {
        codigoSeleccionado = -1;
        txtNombre.setText("");
        txtUnidad.setText("");
        txtStockActual.setText("");
        txtStockActual.setEditable(true);
        txtStockMinimo.setText("");
        txtCosto.setText("");
        tabla.clearSelection();
    }

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty() || txtUnidad.getText().trim().isEmpty() ||
                txtStockMinimo.getText().trim().isEmpty() || txtCosto.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            if (codigoSeleccionado == -1) {
                Double.parseDouble(txtStockActual.getText().trim());
            }
            Double.parseDouble(txtStockMinimo.getText().trim());
            Double.parseDouble(txtCosto.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Los valores numéricos no son válidos.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}