package com.pixel.ui;

import com.pixel.dao.NominaDAO;
import com.pixel.modelo.Nomina;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NominaInternalFrame extends JInternalFrame {

    private final NominaDAO dao = new NominaDAO();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private int codigoSeleccionado = -1;

    public NominaInternalFrame() {
        super("Gestión de Nóminas", true, true, true, true);
        setSize(750, 500);
        construirUI();
        cargarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        String[] columnas = {"Código", "Empleado", "Fecha emisión", "Tipo", "Monto", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            codigoSeleccionado = (fila == -1) ? -1
                    : Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
        });

        JButton btnMarcarPagado = new JButton("Marcar como PAGADO");
        btnMarcarPagado.addActionListener(e -> marcarPagado());

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarTabla());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnMarcarPagado);
        panelBotones.add(btnRefrescar);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Nomina> nominas = dao.listarTodas();
        for (Nomina n : nominas) {
            modeloTabla.addRow(new Object[]{
                    n.getCodigoNomina(), n.getNombreEmpleado(), n.getFechaEmision(),
                    n.getTipoPago(), n.getMonto(), n.getEstado()
            });
        }
    }

    private void marcarPagado() {
        if (codigoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una nómina de la tabla primero.");
            return;
        }
        if (dao.marcarComoPagado(codigoSeleccionado)) {
            JOptionPane.showMessageDialog(this, "Nómina marcada como PAGADA.");
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar la nómina.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}