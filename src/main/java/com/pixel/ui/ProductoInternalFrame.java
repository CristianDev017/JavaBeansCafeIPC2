package com.pixel.ui;

import com.pixel.dao.InsumoDAO;
import com.pixel.dao.ProductoDAO;
import com.pixel.modelo.Insumo;
import com.pixel.modelo.Producto;
import com.pixel.modelo.RecetaItem;
import com.pixel.util.ImagenUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ProductoInternalFrame extends JInternalFrame {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final InsumoDAO insumoDAO = new InsumoDAO();

    private JTextField txtNombre, txtPrecio;
    private JComboBox<String> cbCategoria;
    private JLabel lblFoto;
    private String rutaFotoSeleccionada; // nombre de archivo que se va a guardar en BD

    private JComboBox<Insumo> cbInsumoReceta;
    private JTextField txtCantidadReceta;
    private JTable tablaReceta;
    private DefaultTableModel modeloReceta;
    private List<RecetaItem> recetaActual = new ArrayList<>();

    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    private int codigoSeleccionado = -1;

    public ProductoInternalFrame() {
        super("Gestión de Menú", true, true, true, true);
        setSize(950, 650);
        construirUI();
        cargarTablaProductos();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        // ---------- Panel izquierdo: datos del producto + foto ----------
        JPanel panelDatos = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos del producto"));

        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        cbCategoria = new JComboBox<>(new String[]{"BEBIDA_CALIENTE", "BEBIDA_FRIA", "POSTRE", "COMIDA"});
        lblFoto = new JLabel("Sin imagen");
        lblFoto.setPreferredSize(new Dimension(150, 150));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setBorder(BorderFactory.createEtchedBorder());

        JButton btnSeleccionarFoto = new JButton("Seleccionar foto...");
        btnSeleccionarFoto.addActionListener(e -> seleccionarFoto());

        panelDatos.add(new JLabel("Nombre:"));
        panelDatos.add(txtNombre);
        panelDatos.add(new JLabel("Categoría:"));
        panelDatos.add(cbCategoria);
        panelDatos.add(new JLabel("Precio de venta:"));
        panelDatos.add(txtPrecio);
        panelDatos.add(Box.createVerticalStrut(10));
        panelDatos.add(lblFoto);
        panelDatos.add(btnSeleccionarFoto);

        // ---------- Panel de receta ----------
        JPanel panelReceta = new JPanel(new BorderLayout(5, 5));
        panelReceta.setBorder(BorderFactory.createTitledBorder("Receta (insumos requeridos)"));

        JPanel panelAgregarInsumo = new JPanel();
        cbInsumoReceta = new JComboBox<>();
        cargarInsumosCombo();
        txtCantidadReceta = new JTextField(6);
        JButton btnAgregarInsumo = new JButton("Agregar a receta");
        btnAgregarInsumo.addActionListener(e -> agregarInsumoAReceta());

        panelAgregarInsumo.add(cbInsumoReceta);
        panelAgregarInsumo.add(new JLabel("Cantidad:"));
        panelAgregarInsumo.add(txtCantidadReceta);
        panelAgregarInsumo.add(btnAgregarInsumo);

        modeloReceta = new DefaultTableModel(new String[]{"Insumo", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaReceta = new JTable(modeloReceta);
        JButton btnQuitarInsumo = new JButton("Quitar seleccionado");
        btnQuitarInsumo.addActionListener(e -> quitarInsumoDeReceta());

        panelReceta.add(panelAgregarInsumo, BorderLayout.NORTH);
        panelReceta.add(new JScrollPane(tablaReceta), BorderLayout.CENTER);
        panelReceta.add(btnQuitarInsumo, BorderLayout.SOUTH);

        JPanel panelIzquierdo = new JPanel(new GridLayout(2, 1, 5, 5));
        panelIzquierdo.add(panelDatos);
        panelIzquierdo.add(panelReceta);

        // ---------- Botones principales ----------
        JPanel panelBotones = new JPanel();
        JButton btnRegistrar = new JButton("Registrar producto");
        JButton btnActualizar = new JButton("Actualizar producto");
        JButton btnDeshabilitar = new JButton("Deshabilitar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnRegistrar.addActionListener(e -> registrarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnDeshabilitar.addActionListener(e -> deshabilitarProducto());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnDeshabilitar);
        panelBotones.add(btnLimpiar);

        JPanel panelIzquierdoConBotones = new JPanel(new BorderLayout());
        panelIzquierdoConBotones.add(panelIzquierdo, BorderLayout.CENTER);
        panelIzquierdoConBotones.add(panelBotones, BorderLayout.SOUTH);

        // ---------- Tabla de productos (derecha) ----------
        String[] columnas = {"Código", "Nombre", "Categoría", "Precio"};
        modeloProductos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaProductos = new JTable(modeloProductos);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.setBorder(BorderFactory.createTitledBorder("Productos del menú"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdoConBotones, scrollProductos);
        splitPane.setDividerLocation(420);

        add(splitPane, BorderLayout.CENTER);
    }

    private void cargarInsumosCombo() {
        cbInsumoReceta.removeAllItems();
        for (Insumo i : insumoDAO.listarTodos()) {
            cbInsumoReceta.addItem(i);
        }
    }

    private void seleccionarFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                rutaFotoSeleccionada = ImagenUtil.guardarImagen(archivo);
                mostrarFotoEnLabel(ImagenUtil.rutaCompleta(rutaFotoSeleccionada));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar la imagen: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarFotoEnLabel(String ruta) {
        try {
            BufferedImage img = ImageIO.read(new File(ruta));
            if (img != null) {
                Image escalada = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblFoto.setIcon(new ImageIcon(escalada));
                lblFoto.setText("");
            }
        } catch (IOException e) {
            lblFoto.setIcon(null);
            lblFoto.setText("Sin imagen");
        }
    }

    private void agregarInsumoAReceta() {
        Insumo seleccionado = (Insumo) cbInsumoReceta.getSelectedItem();
        if (seleccionado == null) return;

        String cantStr = txtCantidadReceta.getText().trim();
        if (cantStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa la cantidad requerida.");
            return;
        }

        try {
            double cantidad = Double.parseDouble(cantStr);
            recetaActual.add(new RecetaItem(seleccionado.getCodigoInsumo(), seleccionado.getNombre(), cantidad));
            modeloReceta.addRow(new Object[]{seleccionado.getNombre(), cantidad});
            txtCantidadReceta.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido.");
        }
    }

    private void quitarInsumoDeReceta() {
        int fila = tablaReceta.getSelectedRow();
        if (fila == -1) return;
        recetaActual.remove(fila);
        modeloReceta.removeRow(fila);
    }

    private void registrarProducto() {
        if (!validarFormulario()) return;

        Producto p = new Producto(0, txtNombre.getText().trim(),
                (String) cbCategoria.getSelectedItem(),
                Double.parseDouble(txtPrecio.getText().trim()),
                rutaFotoSeleccionada, true);

        if (productoDAO.registrar(p, recetaActual)) {
            JOptionPane.showMessageDialog(this, "Producto registrado correctamente.");
            limpiarFormulario();
            cargarTablaProductos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el producto.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProducto() {
        if (codigoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla primero.");
            return;
        }
        if (!validarFormulario()) return;

        Producto p = new Producto(codigoSeleccionado, txtNombre.getText().trim(),
                (String) cbCategoria.getSelectedItem(),
                Double.parseDouble(txtPrecio.getText().trim()),
                rutaFotoSeleccionada, true);

        if (productoDAO.actualizar(p, recetaActual)) {
            JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
            limpiarFormulario();
            cargarTablaProductos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el producto.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deshabilitarProducto() {
        if (codigoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto primero.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Deshabilitar este producto del menú?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            productoDAO.deshabilitar(codigoSeleccionado);
            limpiarFormulario();
            cargarTablaProductos();
        }
    }

    private void cargarTablaProductos() {
        modeloProductos.setRowCount(0);
        for (Producto p : productoDAO.listarActivos()) {
            modeloProductos.addRow(new Object[]{
                    p.getCodigoProducto(), p.getNombre(), p.getCategoria(), p.getPrecioVenta()
            });
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) return;

        codigoSeleccionado = Integer.parseInt(modeloProductos.getValueAt(fila, 0).toString());
        txtNombre.setText(modeloProductos.getValueAt(fila, 1).toString());
        cbCategoria.setSelectedItem(modeloProductos.getValueAt(fila, 2).toString());
        txtPrecio.setText(modeloProductos.getValueAt(fila, 3).toString());

        // Cargar receta existente
        recetaActual.clear();
        modeloReceta.setRowCount(0);
        for (RecetaItem item : productoDAO.obtenerReceta(codigoSeleccionado)) {
            recetaActual.add(item);
            modeloReceta.addRow(new Object[]{item.getNombreInsumo(), item.getCantidadRequerida()});
        }
    }

    private void limpiarFormulario() {
        codigoSeleccionado = -1;
        txtNombre.setText("");
        txtPrecio.setText("");
        cbCategoria.setSelectedIndex(0);
        rutaFotoSeleccionada = null;
        lblFoto.setIcon(null);
        lblFoto.setText("Sin imagen");
        recetaActual.clear();
        modeloReceta.setRowCount(0);
        tablaProductos.clearSelection();
        cargarInsumosCombo();
    }

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y precio son obligatorios.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser mayor a 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.");
            return false;
        }
        if (recetaActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El producto debe tener al menos un insumo en la receta.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}