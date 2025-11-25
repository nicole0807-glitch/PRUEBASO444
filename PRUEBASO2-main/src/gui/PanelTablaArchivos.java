package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedList;
import modelo.*;

/**
 * Panel que muestra la tabla de asignación de archivos
 */
public class PanelTablaArchivos extends JPanel {
    private SistemaArchivos sistema;
    private JTable tablaArchivos;
    private DefaultTableModel modeloTabla;

    public PanelTablaArchivos(SistemaArchivos sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Crear tabla
        String[] columnNames = {"Nombre", "Tamaño (bloques)", "Primer Bloque", 
                               "Bloques Asignados", "Propietario", "Público"};
        modeloTabla = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaArchivos = new JTable(modeloTabla);
        tablaArchivos.setFont(new Font("Arial", Font.PLAIN, 11));
        tablaArchivos.setRowHeight(25);
        tablaArchivos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tablaArchivos);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar datos iniciales
        actualizarTabla();
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);

        // Obtener todos los archivos recursivamente
        LinkedList<Archivo> todosLosArchivos = sistema.getRaiz().obtenerTodosLosArchivos();

        for (Archivo archivo : todosLosArchivos) {
            Object[] fila = {
                archivo.getNombre(),
                archivo.getTamañoBloques(),
                archivo.getPrimerBloque() >= 0 ? archivo.getPrimerBloque() : "-",
                archivo.getCantidadBloquesAsignados(),
                archivo.getPropietario(),
                archivo.isEsPublico() ? "Sí" : "No"
            };
            modeloTabla.addRow(fila);
        }
    }

    public void actualizar() {
        actualizarTabla();
    }
}
