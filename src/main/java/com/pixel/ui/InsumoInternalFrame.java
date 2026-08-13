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


    private static final Color CAFE_OSCURO = new Color(91, 58, 41);
    private static final Color CAFE_MEDIO  = new Color(121, 85, 72);
    private static final Color CAFE_CLARO  = new Color(166, 124, 82);
    private static final Color FONDO_CREMA = new Color(230, 220, 205);
    private static final Color BLANCO      = Color.WHITE;
    private static final Color ALERTA_ROJA = new Color(255, 205, 205);

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
        getContentPane().setBackground(FONDO_CREMA);

        JPanel panelForm = new JPanel(new GridLayout(3, 4, 8, 8));
        panelForm.setBackground(FONDO_CREMA);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CAFE_MEDIO, 1),
                        "Datos del insumo"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        ((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) panelForm.getBorder()).getOutsideBorder())
                .setTitleColor(CAFE_OSCURO);

        txtNombre = new JTextField();
        txtUnidad = new JTextField();
        txtStockActual = new JTextField();
        txtStockMinimo = new JTextField();
        txtCosto = new JTextField();

        for (JTextField campo : new JTextField[]{txtNombre, txtUnidad, txtStockActual, txtStockMinimo, txtCosto}) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CAFE_CLARO, 1),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        }

        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblUnidad = new JLabel("Unidad de medida:");
        JLabel lblStockActual = new JLabel("Stock inicial:");
        JLabel lblStockMinimo = new JLabel("Stock mínimo:");
        JLabel lblCosto = new JLabel("Costo:");
        for (JLabel lbl : new JLabel[]{lblNombre, lblUnidad, lblStockActual, lblStockMinimo, lblCosto}) {
            lbl.setForeground(CAFE_OSCURO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        panelForm.add(lblNombre);
        panelForm.add(txtNombre);
        panelForm.add(lblUnidad);
        panelForm.add(txtUnidad);

        panelForm.add(lblStockActual);
        panelForm.add(txtStockActual);
        panelForm.add(lblStockMinimo);
        panelForm.add(txtStockMinimo);

        panelForm.add(lblCosto);
        panelForm.add(txtCosto);
        panelForm.add(new JLabel(""));
        panelForm.add(new JLabel(""));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        panelBotones.setBackground(FONDO_CREMA);

        JButton btnRegistrar = crearBoton("Registrar");
        JButton btnActualizar = crearBoton("Actualizar");
        JButton btnRegistrarCompra = crearBoton("Registrar compra (+stock)");
        JButton btnLimpiar = crearBoton("Limpiar");

        btnRegistrar.addActionListener(e -> registrarInsumo());
        btnActualizar.addActionListener(e -> actualizarInsumo());
        btnRegistrarCompra.addActionListener(e -> registrarCompra());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnRegistrarCompra);
        panelBotones.add(btnLimpiar);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(FONDO_CREMA);
        panelSuperior.add(panelForm, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        String[] columnas = {"Código", "Nombre", "Unidad", "Stock actual", "Stock mínimo", "Costo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(24);
        tabla.setGridColor(CAFE_CLARO);
        tabla.setSelectionBackground(CAFE_CLARO);
        tabla.setSelectionForeground(BLANCO);
        tabla.getTableHeader().setBackground(CAFE_MEDIO);
        tabla.getTableHeader().setForeground(BLANCO);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                double stockActual = Double.parseDouble(table.getValueAt(row, 3).toString());
                double stockMinimo = Double.parseDouble(table.getValueAt(row, 4).toString());

                if (!isSelected) {
                    if (stockActual <= stockMinimo) {
                        c.setBackground(ALERTA_ROJA); // rojo suave: alerta
                    } else {
                        c.setBackground(BLANCO);
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(CAFE_CLARO, 1));
        scroll.getViewport().setBackground(BLANCO);

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
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