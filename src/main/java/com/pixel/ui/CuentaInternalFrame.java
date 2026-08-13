
package com.pixel.ui;

import com.pixel.dao.CuentaDAO;
import com.pixel.dao.EmpleadoDAO;
import com.pixel.dao.MesaDAO;
import com.pixel.dao.ProductoDAO;
import com.pixel.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CuentaInternalFrame extends JInternalFrame {

    private final CuentaDAO cuentaDAO = new CuentaDAO();
    private final MesaDAO mesaDAO = new MesaDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    // Colores tema café (mismos que MainFrame)
    private static final Color CAFE_OSCURO = new Color(91, 58, 41);
    private static final Color CAFE_MEDIO  = new Color(121, 85, 72);
    private static final Color CAFE_CLARO  = new Color(166, 124, 82);
    private static final Color FONDO_CREMA = new Color(230, 220, 205);
    private static final Color BLANCO      = Color.WHITE;

    private JComboBox<Mesa> cbMesaLibre;
    private JComboBox<Empleado> cbMesero;

    private JTable tablaCuentasAbiertas;
    private DefaultTableModel modeloCuentasAbiertas;
    private int idCuentaSeleccionada = -1;

    private JComboBox<Producto> cbProducto;
    private JTextField txtCantidadProducto;
    private JTable tablaDetalle;
    private DefaultTableModel modeloDetalle;
    private JLabel lblTotal;

    private JTextField txtPropina;

    public CuentaInternalFrame() {
        super("Gestión de Cuentas", true, true, true, true);
        setSize(1000, 650);
        construirUI();
        cargarCuentasAbiertas();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(FONDO_CREMA);

        // ---------- Panel superior: abrir cuenta nueva ----------
        JPanel panelAbrir = new JPanel();
        panelAbrir.setBackground(FONDO_CREMA);
        panelAbrir.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CAFE_MEDIO, 1),
                        "Abrir nueva cuenta"),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        ((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) panelAbrir.getBorder()).getOutsideBorder())
                .setTitleColor(CAFE_OSCURO);

        cbMesaLibre = new JComboBox<>();
        cbMesero = new JComboBox<>();
        cbMesaLibre.setBackground(BLANCO);
        cbMesero.setBackground(BLANCO);
        recargarCombosAbrir();

        JButton btnAbrir = crearBoton("Abrir cuenta");
        btnAbrir.addActionListener(e -> abrirCuenta());

        JLabel lblMesaLibre = new JLabel("Mesa libre:");
        JLabel lblMesero = new JLabel("Mesero:");
        for (JLabel lbl : new JLabel[]{lblMesaLibre, lblMesero}) {
            lbl.setForeground(CAFE_OSCURO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        panelAbrir.add(lblMesaLibre);
        panelAbrir.add(cbMesaLibre);
        panelAbrir.add(lblMesero);
        panelAbrir.add(cbMesero);
        panelAbrir.add(btnAbrir);

        // ---------- Panel central: cuentas abiertas + detalle ----------
        String[] colsCuentas = {"ID", "Mesa", "Mesero", "Total", "Propina"};
        modeloCuentasAbiertas = new DefaultTableModel(colsCuentas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaCuentasAbiertas = new JTable(modeloCuentasAbiertas);
        estilizarTabla(tablaCuentasAbiertas);
        tablaCuentasAbiertas.getSelectionModel().addListSelectionListener(e -> seleccionarCuenta());
        JScrollPane scrollCuentas = new JScrollPane(tablaCuentasAbiertas);
        scrollCuentas.getViewport().setBackground(BLANCO);
        scrollCuentas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CAFE_MEDIO, 1), "Cuentas abiertas"));
        ((javax.swing.border.TitledBorder) scrollCuentas.getBorder()).setTitleColor(CAFE_OSCURO);
        scrollCuentas.setPreferredSize(new Dimension(1000, 150));

        // ---------- Panel de detalle de la cuenta seleccionada ----------
        JPanel panelDetalle = new JPanel(new BorderLayout(5, 5));
        panelDetalle.setBackground(FONDO_CREMA);
        panelDetalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CAFE_MEDIO, 1),
                        "Detalle de la cuenta seleccionada"),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        ((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) panelDetalle.getBorder()).getOutsideBorder())
                .setTitleColor(CAFE_OSCURO);

        JPanel panelAgregarProducto = new JPanel();
        panelAgregarProducto.setBackground(FONDO_CREMA);
        cbProducto = new JComboBox<>();
        cbProducto.setBackground(BLANCO);
        recargarProductos();
        txtCantidadProducto = new JTextField(5);
        txtCantidadProducto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CAFE_CLARO, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        JButton btnAgregarProducto = crearBoton("Agregar producto");
        btnAgregarProducto.addActionListener(e -> agregarProducto());

        JLabel lblProducto = new JLabel("Producto:");
        JLabel lblCantidad = new JLabel("Cantidad:");
        for (JLabel lbl : new JLabel[]{lblProducto, lblCantidad}) {
            lbl.setForeground(CAFE_OSCURO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        panelAgregarProducto.add(lblProducto);
        panelAgregarProducto.add(cbProducto);
        panelAgregarProducto.add(lblCantidad);
        panelAgregarProducto.add(txtCantidadProducto);
        panelAgregarProducto.add(btnAgregarProducto);

        String[] colsDetalle = {"Producto", "Cantidad", "Subtotal"};
        modeloDetalle = new DefaultTableModel(colsDetalle, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaDetalle = new JTable(modeloDetalle);
        estilizarTabla(tablaDetalle);

        lblTotal = new JLabel("Total: Q0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(CAFE_OSCURO);

        JPanel panelCobro = new JPanel();
        panelCobro.setBackground(FONDO_CREMA);
        txtPropina = new JTextField(6);
        txtPropina.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CAFE_CLARO, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        JButton btnAgregarPropina = crearBoton("Agregar propina");
        JButton btnCobrar = crearBoton("Cobrar cuenta");
        btnAgregarPropina.addActionListener(e -> agregarPropina());
        btnCobrar.addActionListener(e -> cobrarCuenta());

        JLabel lblPropina = new JLabel("Propina:");
        lblPropina.setForeground(CAFE_OSCURO);
        lblPropina.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panelCobro.add(lblTotal);
        panelCobro.add(lblPropina);
        panelCobro.add(txtPropina);
        panelCobro.add(btnAgregarPropina);
        panelCobro.add(btnCobrar);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.getViewport().setBackground(BLANCO);
        scrollDetalle.setBorder(BorderFactory.createLineBorder(CAFE_CLARO, 1));

        panelDetalle.add(panelAgregarProducto, BorderLayout.NORTH);
        panelDetalle.add(scrollDetalle, BorderLayout.CENTER);
        panelDetalle.add(panelCobro, BorderLayout.SOUTH);

        JPanel panelCentral = new JPanel(new BorderLayout(5, 5));
        panelCentral.setBackground(FONDO_CREMA);
        panelCentral.add(scrollCuentas, BorderLayout.NORTH);
        panelCentral.add(panelDetalle, BorderLayout.CENTER);

        add(panelAbrir, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
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

    private void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(24);
        tabla.setGridColor(CAFE_CLARO);
        tabla.setSelectionBackground(CAFE_CLARO);
        tabla.setSelectionForeground(BLANCO);
        tabla.getTableHeader().setBackground(CAFE_MEDIO);
        tabla.getTableHeader().setForeground(BLANCO);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private void recargarCombosAbrir() {
        cbMesaLibre.removeAllItems();
        for (Mesa m : mesaDAO.listarLibres()) {
            cbMesaLibre.addItem(m);
        }
        cbMesero.removeAllItems();
        for (Empleado emp : empleadoDAO.listarMeserosActivos()) {
            cbMesero.addItem(emp);
        }
    }

    private void recargarProductos() {
        cbProducto.removeAllItems();
        for (Producto p : productoDAO.listarActivos()) {
            cbProducto.addItem(p);
        }
    }

    private void abrirCuenta() {
        Mesa mesa = (Mesa) cbMesaLibre.getSelectedItem();
        Empleado mesero = (Empleado) cbMesero.getSelectedItem();

        if (mesa == null || mesero == null) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar una mesa libre y un mesero.");
            return;
        }

        int idCuenta = cuentaDAO.abrirCuenta(mesa.getNumeroMesa(), mesero.getDpi());
        if (idCuenta != -1) {
            JOptionPane.showMessageDialog(this, "Cuenta abierta correctamente en Mesa " + mesa.getNumeroMesa());
            recargarCombosAbrir();
            cargarCuentasAbiertas();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la cuenta. La mesa pudo haber sido ocupada.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCuentasAbiertas() {
        modeloCuentasAbiertas.setRowCount(0);
        List<Cuenta> cuentas = cuentaDAO.listarAbiertas();
        for (Cuenta c : cuentas) {
            modeloCuentasAbiertas.addRow(new Object[]{
                    c.getIdCuenta(), c.getNumeroMesa(), c.getNombreMesero(), c.getTotal(), c.getPropina()
            });
        }
        limpiarDetalle();
    }

    private void seleccionarCuenta() {
        int fila = tablaCuentasAbiertas.getSelectedRow();
        if (fila == -1) return;

        idCuentaSeleccionada = Integer.parseInt(modeloCuentasAbiertas.getValueAt(fila, 0).toString());
        cargarDetalle();
    }

    private void cargarDetalle() {
        modeloDetalle.setRowCount(0);
        double total = 0;
        for (DetalleCuenta d : cuentaDAO.listarDetalle(idCuentaSeleccionada)) {
            modeloDetalle.addRow(new Object[]{d.getNombreProducto(), d.getCantidad(), d.getSubtotal()});
            total += d.getSubtotal();
        }
        lblTotal.setText(String.format("Total: Q%.2f", total));
    }

    private void agregarProducto() {
        if (idCuentaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cuenta abierta primero.");
            return;
        }

        Producto producto = (Producto) cbProducto.getSelectedItem();
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "No hay productos disponibles en el menú.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantidadProducto.getText().trim());
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.");
                return;
            }

            String error = cuentaDAO.agregarProducto(idCuentaSeleccionada, producto.getCodigoProducto(), cantidad);
            if (error == null) {
                txtCantidadProducto.setText("");
                cargarDetalle();
                cargarCuentasAbiertas(); // refresca totales en la tabla superior
                seleccionarFilaCuenta(idCuentaSeleccionada); // mantiene la selección
            } else {
                JOptionPane.showMessageDialog(this, error, "No se pudo agregar", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.");
        }
    }

    private void seleccionarFilaCuenta(int idCuenta) {
        for (int i = 0; i < modeloCuentasAbiertas.getRowCount(); i++) {
            if (Integer.parseInt(modeloCuentasAbiertas.getValueAt(i, 0).toString()) == idCuenta) {
                tablaCuentasAbiertas.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void agregarPropina() {
        if (idCuentaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cuenta abierta primero.");
            return;
        }
        try {
            double propina = Double.parseDouble(txtPropina.getText().trim());
            if (propina <= 0) {
                JOptionPane.showMessageDialog(this, "La propina debe ser mayor a 0.");
                return;
            }
            cuentaDAO.agregarPropina(idCuentaSeleccionada, propina);
            txtPropina.setText("");
            cargarCuentasAbiertas();
            seleccionarFilaCuenta(idCuentaSeleccionada);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La propina debe ser un número válido.");
        }
    }

    private void cobrarCuenta() {
        if (idCuentaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cuenta abierta primero.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Confirmar cobro de esta cuenta? La mesa quedará LIBRE.",
                "Confirmar cobro", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (cuentaDAO.cerrarCuenta(idCuentaSeleccionada)) {
                JOptionPane.showMessageDialog(this, "Cuenta cobrada correctamente.");
                idCuentaSeleccionada = -1;
                recargarCombosAbrir();
                cargarCuentasAbiertas();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo cerrar la cuenta.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarDetalle() {
        idCuentaSeleccionada = -1;
        modeloDetalle.setRowCount(0);
        lblTotal.setText("Total: Q0.00");
    }
}