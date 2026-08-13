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

    private static final Color CAFE_OSCURO = new Color(91, 58, 41);
    private static final Color CAFE_MEDIO  = new Color(121, 85, 72);
    private static final Color CAFE_CLARO  = new Color(166, 124, 82);
    private static final Color FONDO_CREMA = new Color(230, 220, 205);
    private static final Color BLANCO      = Color.WHITE;

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
        getContentPane().setBackground(FONDO_CREMA);

        JPanel panelDatos = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));
        panelDatos.setBackground(FONDO_CREMA);
        panelDatos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CAFE_MEDIO, 1),
                        "Datos del producto"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        ((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) panelDatos.getBorder()).getOutsideBorder())
                .setTitleColor(CAFE_OSCURO);

        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        cbCategoria = new JComboBox<>(new String[]{"BEBIDA_CALIENTE", "BEBIDA_FRIA", "POSTRE", "COMIDA"});
        lblFoto = new JLabel("Sin imagen");
        lblFoto.setPreferredSize(new Dimension(150, 150));
        lblFoto.setMaximumSize(new Dimension(150, 150));
        lblFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setBorder(BorderFactory.createLineBorder(CAFE_CLARO, 1));
        lblFoto.setBackground(BLANCO);
        lblFoto.setOpaque(true);
        lblFoto.setForeground(CAFE_OSCURO);

        for (JTextField campo : new JTextField[]{txtNombre, txtPrecio}) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CAFE_CLARO, 1),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        cbCategoria.setBackground(BLANCO);
        cbCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSeleccionarFoto = crearBoton("Seleccionar foto...");
        btnSeleccionarFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSeleccionarFoto.addActionListener(e -> seleccionarFoto());

        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblCategoria = new JLabel("Categoría:");
        JLabel lblPrecio = new JLabel("Precio de venta:");
        for (JLabel lbl : new JLabel[]{lblNombre, lblCategoria, lblPrecio}) {
            lbl.setForeground(CAFE_OSCURO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        panelDatos.add(lblNombre);
        panelDatos.add(txtNombre);
        panelDatos.add(lblCategoria);
        panelDatos.add(cbCategoria);
        panelDatos.add(lblPrecio);
        panelDatos.add(txtPrecio);
        panelDatos.add(Box.createVerticalStrut(10));
        panelDatos.add(lblFoto);
        panelDatos.add(Box.createVerticalStrut(6));
        panelDatos.add(btnSeleccionarFoto);

        // Panel de receta
        JPanel panelReceta = new JPanel(new BorderLayout(5, 5));
        panelReceta.setBackground(FONDO_CREMA);
        panelReceta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CAFE_MEDIO, 1),
                        "Receta (insumos requeridos)"),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        ((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) panelReceta.getBorder()).getOutsideBorder())
                .setTitleColor(CAFE_OSCURO);

        JPanel panelAgregarInsumo = new JPanel();
        panelAgregarInsumo.setBackground(FONDO_CREMA);
        cbInsumoReceta = new JComboBox<>();
        cbInsumoReceta.setBackground(BLANCO);
        cargarInsumosCombo();
        txtCantidadReceta = new JTextField(6);
        txtCantidadReceta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CAFE_CLARO, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        JButton btnAgregarInsumo = crearBoton("Agregar a receta");
        btnAgregarInsumo.addActionListener(e -> agregarInsumoAReceta());

        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setForeground(CAFE_OSCURO);
        lblCantidad.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panelAgregarInsumo.add(cbInsumoReceta);
        panelAgregarInsumo.add(lblCantidad);
        panelAgregarInsumo.add(txtCantidadReceta);
        panelAgregarInsumo.add(btnAgregarInsumo);

        modeloReceta = new DefaultTableModel(new String[]{"Insumo", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaReceta = new JTable(modeloReceta);
        estilizarTabla(tablaReceta);
        JButton btnQuitarInsumo = crearBoton("Quitar seleccionado");
        btnQuitarInsumo.addActionListener(e -> quitarInsumoDeReceta());
        JPanel panelQuitar = new JPanel();
        panelQuitar.setBackground(FONDO_CREMA);
        panelQuitar.add(btnQuitarInsumo);

        JScrollPane scrollReceta = new JScrollPane(tablaReceta);
        scrollReceta.setBorder(BorderFactory.createLineBorder(CAFE_CLARO, 1));
        scrollReceta.getViewport().setBackground(BLANCO);

        panelReceta.add(panelAgregarInsumo, BorderLayout.NORTH);
        panelReceta.add(scrollReceta, BorderLayout.CENTER);
        panelReceta.add(panelQuitar, BorderLayout.SOUTH);

        JPanel panelIzquierdo = new JPanel(new GridLayout(2, 1, 5, 5));
        panelIzquierdo.setBackground(FONDO_CREMA);
        panelIzquierdo.add(panelDatos);
        panelIzquierdo.add(panelReceta);

        //Botones principales
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panelBotones.setBackground(FONDO_CREMA);
        JButton btnRegistrar = crearBoton("Registrar producto");
        JButton btnActualizar = crearBoton("Actualizar producto");
        JButton btnDeshabilitar = crearBoton("Deshabilitar");
        JButton btnLimpiar = crearBoton("Limpiar");
        JButton btnExportarMenu = crearBoton("Exportar menú a HTML");
        btnExportarMenu.addActionListener(e -> exportarMenuHtml());
        panelBotones.add(btnExportarMenu);

        btnRegistrar.addActionListener(e -> registrarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnDeshabilitar.addActionListener(e -> deshabilitarProducto());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnDeshabilitar);
        panelBotones.add(btnLimpiar);

        JPanel panelIzquierdoConBotones = new JPanel(new BorderLayout());
        panelIzquierdoConBotones.setBackground(FONDO_CREMA);
        panelIzquierdoConBotones.add(panelIzquierdo, BorderLayout.CENTER);
        panelIzquierdoConBotones.add(panelBotones, BorderLayout.SOUTH);

        //Tabla de productos
        String[] columnas = {"Código", "Nombre", "Categoría", "Precio"};
        modeloProductos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaProductos = new JTable(modeloProductos);
        estilizarTabla(tablaProductos);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.getViewport().setBackground(BLANCO);
        scrollProductos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CAFE_MEDIO, 1), "Productos del menú"));
        ((javax.swing.border.TitledBorder) scrollProductos.getBorder()).setTitleColor(CAFE_OSCURO);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdoConBotones, scrollProductos);
        splitPane.setDividerLocation(420);
        splitPane.setBackground(FONDO_CREMA);

        add(splitPane, BorderLayout.CENTER);
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

    private void exportarMenuHtml() {
        List<Producto> productos = productoDAO.listarActivos();

        StringBuilder tarjetas = new StringBuilder();
        for (Producto p : productos) {
            String imgTag = (p.getRutaFoto() != null)
                    ? "<img src='" + com.pixel.util.ImagenUtil.rutaCompleta(p.getRutaFoto()) + "'>"
                    : "<div class='sin-foto'>Sin imagen</div>";

            tarjetas.append("<div class='producto'>")
                    .append(imgTag)
                    .append("<h3>").append(p.getNombre()).append("</h3>")
                    .append("<p class='categoria'>").append(p.getCategoria().replace("_", " ")).append("</p>")
                    .append("<p class='precio'>Q").append(String.format("%.2f", p.getPrecioVenta())).append("</p>")
                    .append("</div>");
        }

        String html = "<html><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial, sans-serif; background:#f7f4ef; margin:40px;}" +
                "h1{color:#5b3a29; text-align:center;}" +
                ".contenedor{display:flex; flex-wrap:wrap; gap:20px; justify-content:center;}" +
                ".producto{background:white; border-radius:10px; box-shadow:0 2px 6px rgba(0,0,0,0.1); " +
                "padding:15px; width:220px; text-align:center;}" +
                ".producto img{width:180px; height:180px; object-fit:cover; border-radius:8px;}" +
                ".sin-foto{width:180px; height:180px; background:#eee; display:flex; align-items:center; " +
                "justify-content:center; border-radius:8px; color:#999;}" +
                ".categoria{color:#888; font-size:13px;}" +
                ".precio{font-weight:bold; color:#5b3a29; font-size:16px;}" +
                "</style></head><body>" +
                "<h1>JavaBeans Café - Nuestro Menú</h1>" +
                "<div class='contenedor'>" + tarjetas + "</div>" +
                "</body></html>";

        if (com.pixel.util.HtmlExportUtil.exportarDirecto(html, "menu_javabeans.html")) {
            JOptionPane.showMessageDialog(this, "Menú descargado en tu carpeta de Descargas.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo exportar el menú.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}