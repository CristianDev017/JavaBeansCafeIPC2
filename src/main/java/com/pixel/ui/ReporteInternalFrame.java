package com.pixel.ui;

import com.pixel.dao.ReporteDAO;
import com.pixel.modelo.Insumo;
import com.pixel.util.HtmlExportUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReporteInternalFrame extends JInternalFrame {

    private final ReporteDAO dao = new ReporteDAO();

    private JSpinner spinnerInicio, spinnerFin;
    private JCheckBox chkSinInicio, chkSinFin;
    private JTextArea areaResultado;

    private String ultimoHtmlGenerado;
    private String ultimoNombreSugerido;

    public ReporteInternalFrame() {
        super("Reportes", true, true, true, true);
        setSize(700, 550);
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelFiltros = new JPanel();
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtro de fechas"));

        spinnerInicio = crearSpinnerFecha();
        chkSinInicio = new JCheckBox("Sin límite inferior");
        chkSinInicio.addActionListener(e -> spinnerInicio.setEnabled(!chkSinInicio.isSelected()));

        spinnerFin = crearSpinnerFecha();
        chkSinFin = new JCheckBox("Sin límite superior");
        chkSinFin.addActionListener(e -> spinnerFin.setEnabled(!chkSinFin.isSelected()));

        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(spinnerInicio);
        panelFiltros.add(chkSinInicio);
        panelFiltros.add(Box.createHorizontalStrut(15));
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(spinnerFin);
        panelFiltros.add(chkSinFin);

        JPanel panelBotones = new JPanel();
        JButton btnFlujoCaja = new JButton("Flujo de caja");
        JButton btnMasVendidos = new JButton("Productos más vendidos");
        JButton btnBajoStock = new JButton("Insumos con bajo stock");
        JButton btnExportar = new JButton("Exportar a HTML");

        btnFlujoCaja.addActionListener(e -> mostrarFlujoCaja());
        btnMasVendidos.addActionListener(e -> mostrarMasVendidos());
        btnBajoStock.addActionListener(e -> mostrarBajoStock());
        btnExportar.addActionListener(e -> exportarActual());

        panelBotones.add(btnFlujoCaja);
        panelBotones.add(btnMasVendidos);
        panelBotones.add(btnBajoStock);
        panelBotones.add(btnExportar);

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelFiltros, BorderLayout.NORTH);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(areaResultado), BorderLayout.CENTER);
    }

    private JSpinner crearSpinnerFecha() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setValue(new Date()); // fecha de hoy por defecto
        spinner.setPreferredSize(new Dimension(110, 25));
        return spinner;
    }

    private LocalDate obtenerFecha(JSpinner spinner, JCheckBox chkSinLimite) {
        if (chkSinLimite.isSelected()) return null;
        Date fecha = (Date) spinner.getValue();
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void mostrarFlujoCaja() {
        LocalDate inicio = obtenerFecha(spinnerInicio, chkSinInicio);
        LocalDate fin = obtenerFecha(spinnerFin, chkSinFin);

        ReporteDAO.FlujoCaja flujo = dao.calcularFlujoCaja(inicio, fin);

        StringBuilder sb = new StringBuilder();
        sb.append("=== FLUJO DE CAJA ===\n\n");
        sb.append(String.format("Total ingresos:        Q%.2f%n", flujo.ingresos));
        sb.append(String.format("Egresos (nómina):      Q%.2f%n", flujo.egresosNomina));
        sb.append(String.format("Egresos (compras):     Q%.2f%n", flujo.egresosCompras));
        sb.append(String.format("Total egresos:          Q%.2f%n", flujo.getEgresosTotales()));
        sb.append("\n");
        sb.append(String.format("BALANCE: Q%.2f (%s)%n", flujo.getBalance(),
                flujo.getBalance() >= 0 ? "GANANCIA" : "PÉRDIDA"));

        areaResultado.setText(sb.toString());

        ultimoHtmlGenerado = generarHtmlFlujoCaja(flujo, inicio, fin);
        ultimoNombreSugerido = "flujo_caja.html";
    }

    private void mostrarMasVendidos() {
        LocalDate inicio = obtenerFecha(spinnerInicio, chkSinInicio);
        LocalDate fin = obtenerFecha(spinnerFin, chkSinFin);

        List<ReporteDAO.ProductoVendido> lista = dao.productosMasVendidos(inicio, fin);

        StringBuilder sb = new StringBuilder();
        sb.append("=== PRODUCTOS MÁS VENDIDOS ===\n\n");
        int puesto = 1;
        for (ReporteDAO.ProductoVendido pv : lista) {
            sb.append(String.format("%d. %-30s %d unidades%n", puesto++, pv.nombre, pv.cantidadVendida));
        }
        if (lista.isEmpty()) sb.append("No hay ventas registradas en ese periodo.\n");

        areaResultado.setText(sb.toString());

        ultimoHtmlGenerado = generarHtmlMasVendidos(lista, inicio, fin);
        ultimoNombreSugerido = "productos_mas_vendidos.html";
    }

    private void mostrarBajoStock() {
        List<Insumo> lista = dao.insumosBajoStock();

        StringBuilder sb = new StringBuilder();
        sb.append("=== INSUMOS CON BAJO STOCK ===\n\n");
        for (Insumo i : lista) {
            sb.append(String.format("%-25s Stock: %.2f / Mínimo: %.2f (%s)%n",
                    i.getNombre(), i.getStockActual(), i.getStockMinimo(), i.getUnidadMedida()));
        }
        if (lista.isEmpty()) sb.append("No hay insumos con stock bajo. Todo en orden.\n");

        areaResultado.setText(sb.toString());

        ultimoHtmlGenerado = generarHtmlBajoStock(lista);
        ultimoNombreSugerido = "insumos_bajo_stock.html";
    }

    private void exportarActual() {
        if (ultimoHtmlGenerado == null) {
            JOptionPane.showMessageDialog(this, "Primero genera un reporte antes de exportarlo.");
            return;
        }
        if (HtmlExportUtil.exportarDirecto(ultimoHtmlGenerado, ultimoNombreSugerido)) {
            JOptionPane.showMessageDialog(this, "Reporte descargado en tu carpeta de Descargas.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo exportar el reporte.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Generadores de HTML

    private String generarHtmlFlujoCaja(ReporteDAO.FlujoCaja flujo, LocalDate inicio, LocalDate fin) {
        String colorBalance = flujo.getBalance() >= 0 ? "#2e7d32" : "#c62828";
        String estadoBalance = flujo.getBalance() >= 0 ? "GANANCIA" : "PÉRDIDA";

        return "<html><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial, sans-serif; margin:40px; background:#f7f4ef;}" +
                "h1{color:#5b3a29;} table{border-collapse:collapse; width:100%; max-width:500px;}" +
                "td{padding:10px; border-bottom:1px solid #ddd;}" +
                ".balance{font-size:22px; font-weight:bold; color:" + colorBalance + ";}" +
                "</style></head><body>" +
                "<h1>JavaBeans Café - Flujo de Caja</h1>" +
                "<p>Periodo: " + (inicio == null ? "Sin límite inferior" : inicio) +
                " al " + (fin == null ? "Sin límite superior" : fin) + "</p>" +
                "<table>" +
                "<tr><td>Total ingresos</td><td>Q" + String.format("%.2f", flujo.ingresos) + "</td></tr>" +
                "<tr><td>Egresos por nómina</td><td>Q" + String.format("%.2f", flujo.egresosNomina) + "</td></tr>" +
                "<tr><td>Egresos por compras</td><td>Q" + String.format("%.2f", flujo.egresosCompras) + "</td></tr>" +
                "<tr><td>Total egresos</td><td>Q" + String.format("%.2f", flujo.getEgresosTotales()) + "</td></tr>" +
                "</table>" +
                "<p class='balance'>Balance: Q" + String.format("%.2f", flujo.getBalance()) + " (" + estadoBalance + ")</p>" +
                "</body></html>";
    }

    private String generarHtmlMasVendidos(List<ReporteDAO.ProductoVendido> lista, LocalDate inicio, LocalDate fin) {
        StringBuilder filas = new StringBuilder();
        int puesto = 1;
        for (ReporteDAO.ProductoVendido pv : lista) {
            filas.append("<tr><td>").append(puesto++).append("</td><td>")
                    .append(pv.nombre).append("</td><td>")
                    .append(pv.cantidadVendida).append("</td></tr>");
        }

        return "<html><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial, sans-serif; margin:40px; background:#f7f4ef;}" +
                "h1{color:#5b3a29;} table{border-collapse:collapse; width:100%; max-width:500px;}" +
                "th,td{padding:10px; border-bottom:1px solid #ddd; text-align:left;}" +
                "th{background:#5b3a29; color:white;}" +
                "</style></head><body>" +
                "<h1>JavaBeans Café - Productos Más Vendidos</h1>" +
                "<p>Periodo: " + (inicio == null ? "Sin límite inferior" : inicio) +
                " al " + (fin == null ? "Sin límite superior" : fin) + "</p>" +
                "<table><tr><th>#</th><th>Producto</th><th>Cantidad vendida</th></tr>" +
                filas + "</table></body></html>";
    }

    private String generarHtmlBajoStock(List<Insumo> lista) {
        StringBuilder filas = new StringBuilder();
        for (Insumo i : lista) {
            filas.append("<tr><td>").append(i.getNombre()).append("</td><td>")
                    .append(i.getStockActual()).append("</td><td>")
                    .append(i.getStockMinimo()).append("</td><td>")
                    .append(i.getUnidadMedida()).append("</td></tr>");
        }

        return "<html><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial, sans-serif; margin:40px; background:#f7f4ef;}" +
                "h1{color:#c62828;} table{border-collapse:collapse; width:100%; max-width:600px;}" +
                "th,td{padding:10px; border-bottom:1px solid #ddd; text-align:left;}" +
                "th{background:#c62828; color:white;}" +
                "</style></head><body>" +
                "<h1>JavaBeans Café - Insumos con Bajo Stock</h1>" +
                "<table><tr><th>Insumo</th><th>Stock actual</th><th>Stock mínimo</th><th>Unidad</th></tr>" +
                filas + "</table></body></html>";
    }
}