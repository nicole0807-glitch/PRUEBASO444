package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedList;
import modelo.*;

/**
 * Panel que muestra el estado del buffer (caché de memoria)
 */
public class PanelBuffer extends JPanel {
    private SistemaArchivos sistema;
    private JTable tablaBuffer;
    private DefaultTableModel modeloBuffer;
    private JLabel lblCapacidad;
    private JLabel lblOcupado;
    private JLabel lblLibre;
    private JProgressBar barraOcupacion;
    private JLabel lblPolitica;

    public PanelBuffer(SistemaArchivos sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel superior - Información
        JPanel panelInfo = crearPanelInformacion();

        // Panel central - Tabla
        JPanel panelTabla = crearPanelTabla();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            panelInfo, panelTabla);
        split.setDividerLocation(120);

        add(split, BorderLayout.CENTER);
    }

    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Información del Buffer"));

        lblCapacidad = new JLabel("Capacidad: - bloques");
        lblOcupado = new JLabel("Espacio ocupado: - bloques");
        lblLibre = new JLabel("Espacio libre: - bloques");
        lblPolitica = new JLabel("Política de reemplazo: -");

        barraOcupacion = new JProgressBar(0, 100);
        barraOcupacion.setStringPainted(true);
        barraOcupacion.setString("0%");

        panel.add(lblCapacidad);
        panel.add(lblOcupado);
        panel.add(lblLibre);
        panel.add(barraOcupacion);

        actualizarInfo();
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Bloques en el Buffer"));

        String[] columnNames = {"Número de Bloque", "Accesos", "Tiempo Último Acceso"};
        modeloBuffer = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaBuffer = new JTable(modeloBuffer);
        tablaBuffer.setFont(new Font("Arial", Font.PLAIN, 11));
        tablaBuffer.setRowHeight(20);

        JScrollPane scrollPane = new JScrollPane(tablaBuffer);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarInfo() {
        if (sistema.getBuffer() == null) {
            lblCapacidad.setText("Buffer no disponible");
            return;
        }

        Buffer buffer = sistema.getBuffer();
        lblCapacidad.setText("Capacidad: " + buffer.getCapacidadMaxima() + " bloques");
        lblOcupado.setText("Espacio ocupado: " + buffer.getEspacioOcupado() + " bloques");
        lblLibre.setText("Espacio libre: " + buffer.getEspacioDisponible() + " bloques");
        lblPolitica.setText("Política de reemplazo: " + buffer.getPoliticaActual().name());

        int porcentaje = (int) buffer.getPercentajeOcupacion();
        barraOcupacion.setValue(porcentaje);
        barraOcupacion.setString(porcentaje + "%");
    }

    public void actualizar() {
        if (sistema.getBuffer() == null) {
            return;
        }

        actualizarInfo();
        actualizarTabla();
    }

    private void actualizarTabla() {
        modeloBuffer.setRowCount(0);

        if (sistema.getBuffer() == null) {
            return;
        }

        LinkedList<Buffer.BloqueBlog> bloques = sistema.getBuffer().obtenerBloques();

        for (Buffer.BloqueBlog bloque : bloques) {
            long tiempoAcceso = bloque.getTiempoUltimoAcceso();
            java.text.SimpleDateFormat sdf = 
                new java.text.SimpleDateFormat("HH:mm:ss.SSS");
            String tiempoFormato = sdf.format(new java.util.Date(tiempoAcceso));

            Object[] fila = {
                bloque.getNumeroBloque(),
                bloque.getContadorAccesos(),
                tiempoFormato
            };
            modeloBuffer.addRow(fila);
        }
    }
}
