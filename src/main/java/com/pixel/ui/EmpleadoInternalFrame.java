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


    private static final Color CAFE_OSCURO = new Color(91, 58, 41);
    private static final Color CAFE_MEDIO  = new Color(121, 85, 72);
    private static final Color CAFE_CLARO  = new Color(166, 124, 82);
    private static final Color FONDO_CREMA = new Color(230, 220, 205);
    private static final Color BLANCO      = Color.WHITE;

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
        getContentPane().setBackground(FONDO_CREMA);

        JPanel panelForm = new JPanel(new GridLayout(3, 4, 8, 8));
        panelForm.setBackground(FONDO_CREMA);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CAFE_MEDIO, 1),
                        "Datos del empleado"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        ((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) panelForm.getBorder()).getOutsideBorder())
                .setTitleColor(CAFE_OSCURO);

        txtDpi = new JTextField();
        txtNombre = new JTextField();
        txtCorreo = new JTextField();
        txtSalario = new JTextField();
        cbRol = new JComboBox<>(new String[]{"MESERO", "COCINA", "BARISTA", "ADMINISTRADOR"});
        cbJornada = new JComboBox<>(new String[]{"MATUTINA", "VESPERTINA", "NOCTURNA"});

        for (JTextField campo : new JTextField[]{txtDpi, txtNombre, txtCorreo, txtSalario}) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CAFE_CLARO, 1),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        }
        cbRol.setBackground(BLANCO);
        cbJornada.setBackground(BLANCO);

        JLabel lblDpi = new JLabel("DPI:");
        JLabel lblNombre = new JLabel("Nombre completo:");
        JLabel lblCorreo = new JLabel("Correo:");
        JLabel lblSalario = new JLabel("Salario:");
        JLabel lblRol = new JLabel("Rol:");
        JLabel lblJornada = new JLabel("Jornada:");
        for (JLabel lbl : new JLabel[]{lblDpi, lblNombre, lblCorreo, lblSalario, lblRol, lblJornada}) {
            lbl.setForeground(CAFE_OSCURO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        panelForm.add(lblDpi);
        panelForm.add(txtDpi);
        panelForm.add(lblNombre);
        panelForm.add(txtNombre);

        panelForm.add(lblCorreo);
        panelForm.add(txtCorreo);
        panelForm.add(lblSalario);
        panelForm.add(txtSalario);

        panelForm.add(lblRol);
        panelForm.add(cbRol);
        panelForm.add(lblJornada);
        panelForm.add(cbJornada);

        //botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        panelBotones.setBackground(FONDO_CREMA);

        JButton btnRegistrar = crearBoton("Registrar");
        JButton btnActualizar = crearBoton("Actualizar");
        JButton btnDeshabilitar = crearBoton("Deshabilitar");
        JButton btnLimpiar = crearBoton("Limpiar");

        btnRegistrar.addActionListener(e -> registrarEmpleado());
        btnActualizar.addActionListener(e -> actualizarEmpleado());
        btnDeshabilitar.addActionListener(e -> deshabilitarEmpleado());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        JButton btnHabilitar = new JButton("Habilitar");
        btnHabilitar.addActionListener(e -> habilitarEmpleado());
        panelBotones.add(btnHabilitar);

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnDeshabilitar);
        panelBotones.add(btnLimpiar);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(FONDO_CREMA);
        panelSuperior.add(panelForm, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);


        String[] columnas = {"DPI", "Nombre", "Correo", "Rol", "Jornada", "Salario", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // la tabla es solo de lectura, se edita por el formulario
            }
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
    private void habilitarEmpleado() {
        String dpi = txtDpi.getText().trim();
        if (dpi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado de la tabla primero.");
            return;
        }
        dao.habilitar(dpi);
        JOptionPane.showMessageDialog(this, "Empleado habilitado.");
        limpiarFormulario();
        cargarTabla();
    }
}