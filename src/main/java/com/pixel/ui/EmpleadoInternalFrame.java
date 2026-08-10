package com.pixel.ui;

import com.pixel.dao.EmpleadoDAO;
import com.pixel.modelo.Empleado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EmpleadoInternalFrame extends JInternalFrame {

    private final EmpleadoDAO dao = new EmpleadoDAO();

    // Componentes del formulario
    private JTextField txtDpi, txtNombre, txtCorreo, txtSalario;
    private JComboBox<String> cbRol, cbJornada;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public EmpleadoInternalFrame() {
        super("Gestión de Personal", true, true, true, true);
        setSize(700, 500);

        construirUI();
        cargarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        // ---------- Panel de formulario (arriba) ----------
        JPanel panelForm = new JPanel(new GridLayout(3, 4, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));

        txtDpi = new JTextField();
        txtNombre = new JTextField();
        txtCorreo = new JTextField();
        txtSalario = new JTextField();
        cbRol = new JComboBox<>(new String[]{"MESERO", "COCINA", "BARISTA", "ADMINISTRADOR"});
        cbJornada = new JComboBox<>(new String[]{"MATUTINA", "VESPERTINA", "NOCTURNA"});

        panelForm.add(new JLabel("DPI:"));
        panelForm.add(txtDpi);
        panelForm.add(new JLabel("Nombre completo:"));
        panelForm.add(txtNombre);

        panelForm.add(new JLabel("Correo:"));
        panelForm.add(txtCorreo);
        panelForm.add(new JLabel("Salario:"));
        panelForm.add(txtSalario);

        panelForm.add(new JLabel("Rol:"));
        panelForm.add(cbRol);
        panelForm.add(new JLabel("Jornada:"));
        panelForm.add(cbJornada);

        // ---------- Panel de botones ----------
        JPanel panelBotones = new JPanel();
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnDeshabilitar = new JButton("Deshabilitar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnRegistrar.addActionListener(e -> registrarEmpleado());
        btnActualizar.addActionListener(e -> actualizarEmpleado());
        btnDeshabilitar.addActionListener(e -> deshabilitarEmpleado());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnDeshabilitar);
        panelBotones.add(btnLimpiar);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelForm, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        // ---------- Tabla (abajo) ----------
        String[] columnas = {"DPI", "Nombre", "Correo", "Rol", "Jornada", "Salario", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // la tabla es solo de lectura, se edita por el formulario
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());
        JScrollPane scroll = new JScrollPane(tabla);

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0); // limpia la tabla
        List<Empleado> empleados = dao.listarTodos();
        for (Empleado e : empleados) {
            modeloTabla.addRow(new Object[]{
                    e.getDpi(), e.getNombreCompleto(), e.getCorreo(),
                    e.getRol(), e.getJornada(), e.getSalario(),
                    e.isActivo() ? "SI" : "NO"
            });
        }
    }

    private void registrarEmpleado() {
        if (!validarFormulario()) return;

        Empleado emp = new Empleado(
                txtDpi.getText().trim(),
                txtNombre.getText().trim(),
                txtCorreo.getText().trim(),
                (String) cbRol.getSelectedItem(),
                (String) cbJornada.getSelectedItem(),
                Double.parseDouble(txtSalario.getText().trim()),
                LocalDate.now(),
                true
        );

        boolean exito = dao.registrar(emp);
        if (exito) {
            JOptionPane.showMessageDialog(this, "Empleado registrado correctamente.");
            limpiarFormulario();
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar. Verifica que el DPI o correo no estén repetidos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEmpleado() {
        if (!validarFormulario()) return;

        Empleado emp = new Empleado(
                txtDpi.getText().trim(),
                txtNombre.getText().trim(),
                txtCorreo.getText().trim(),
                (String) cbRol.getSelectedItem(),
                (String) cbJornada.getSelectedItem(),
                Double.parseDouble(txtSalario.getText().trim()),
                null, // no se actualiza la fecha de contratación
                true
        );

        boolean exito = dao.actualizar(emp);
        if (exito) {
            JOptionPane.showMessageDialog(this, "Empleado actualizado correctamente.");
            limpiarFormulario();
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar. Verifica el DPI.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deshabilitarEmpleado() {
        String dpi = txtDpi.getText().trim();
        if (dpi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado de la tabla primero.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas deshabilitar a este empleado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            dao.deshabilitar(dpi);
            JOptionPane.showMessageDialog(this, "Empleado deshabilitado.");
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;

        txtDpi.setText(modeloTabla.getValueAt(fila, 0).toString());
        txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
        txtCorreo.setText(modeloTabla.getValueAt(fila, 2).toString());
        cbRol.setSelectedItem(modeloTabla.getValueAt(fila, 3).toString());
        cbJornada.setSelectedItem(modeloTabla.getValueAt(fila, 4).toString());
        txtSalario.setText(modeloTabla.getValueAt(fila, 5).toString());

        txtDpi.setEditable(false); // el DPI no se debe poder cambiar una vez creado
    }

    private void limpiarFormulario() {
        txtDpi.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");
        txtSalario.setText("");
        cbRol.setSelectedIndex(0);
        cbJornada.setSelectedIndex(0);
        txtDpi.setEditable(true);
        tabla.clearSelection();
    }

    private boolean validarFormulario() {
        if (txtDpi.getText().trim().isEmpty() ||
                txtNombre.getText().trim().isEmpty() ||
                txtCorreo.getText().trim().isEmpty() ||
                txtSalario.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double salario = Double.parseDouble(txtSalario.getText().trim());
            if (salario <= 0) {
                JOptionPane.showMessageDialog(this, "El salario debe ser mayor a 0.",
                        "Error de validación", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El salario debe ser un número válido.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!txtCorreo.getText().contains("@")) {
            JOptionPane.showMessageDialog(this, "El correo no parece válido.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}