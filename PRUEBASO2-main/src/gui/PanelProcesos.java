package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import modelo.*;

/**
 * Panel que muestra el estado de los procesos y la cola de E/S
 */
public class PanelProcesos extends JPanel {
    private SistemaArchivos sistema;
    private JTable tablaProcesos;
    private JTable tablaColaIO;
    private DefaultTableModel modeloProcesos;
    private DefaultTableModel modeloColaIO;
    private SimpleDateFormat formato;

    public PanelProcesos(SistemaArchivos sistema) {
        this.sistema = sistema;
        this.formato = new SimpleDateFormat("HH:mm:ss");
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel superior - Procesos
        JPanel panelProcesos = crearPanelProcesos();

        // Panel inferior - Cola E/S
        JPanel panelColaIO = crearPanelColaIO();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            panelProcesos, panelColaIO);
        split.setDividerLocation(250);

        add(split, BorderLayout.CENTER);
    }

    private JPanel crearPanelProcesos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Procesos"));

        String[] columnNames = {"ID", "Usuario", "Estado", "Operación", "Tiempo Transcurrido"};
        modeloProcesos = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProcesos = new JTable(modeloProcesos);
        tablaProcesos.setFont(new Font("Arial", Font.PLAIN, 11));
        tablaProcesos.setRowHeight(20);

        JScrollPane scrollPane = new JScrollPane(tablaProcesos);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelColaIO() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Cola de E/S"));

        String[] columnNames = {"ID Solicitud", "ID Proceso", "Tipo", "Archivo", 
                               "Completada", "Tiempo Espera (ms)"};
        modeloColaIO = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaColaIO = new JTable(modeloColaIO);
        tablaColaIO.setFont(new Font("Arial", Font.PLAIN, 11));
        tablaColaIO.setRowHeight(20);

        JScrollPane scrollPane = new JScrollPane(tablaColaIO);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void actualizar() {
        actualizarTablaProcesos();
        actualizarTablaColaIO();
    }

    private void actualizarTablaProcesos() {
        modeloProcesos.setRowCount(0);

        LinkedList<Proceso> procesos = sistema.getProcesos();
        for (Proceso proceso : procesos) {
            long tiempoTranscurrido = proceso.getTiempoTranscurrido();
            Object[] fila = {
                proceso.getIdProceso(),
                proceso.getNombreUsuario(),
                proceso.getEstado().name(),
                proceso.getTipoOperacion(),
                tiempoTranscurrido + " ms"
            };
            modeloProcesos.addRow(fila);
        }
    }

    private void actualizarTablaColaIO() {
        modeloColaIO.setRowCount(0);

        ColaIO colaIO = sistema.getColaIO();
        LinkedList<SolicitudIO> solicitudes = colaIO.obtenerTodas();

        for (SolicitudIO solicitud : solicitudes) {
            Object[] fila = {
                solicitud.getIdSolicitud(),
                solicitud.getIdProceso(),
                solicitud.getTipo().name(),
                solicitud.getArchivoAfectado() != null ? 
                    solicitud.getArchivoAfectado().getNombre() : "-",
                solicitud.isCompletada() ? "Sí" : "No",
                solicitud.getTiempoEspera()
            };
            modeloColaIO.addRow(fila);
        }
    }
}
