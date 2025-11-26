package gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import modelo.*;

/**
 * Panel que visualiza el disco con los bloques ocupados y libres
 */
public class PanelDisco extends JPanel {
    private SistemaArchivos sistema;
    private int tamañoBloque;
    private int bloquesAncho;
    private JLabel lblTotal;
    private JLabel lblOcupados;
    private JLabel lblLibres;
    private JLabel lblPorcentaje;
    private JLabel lblPolítica;

    public PanelDisco(SistemaArchivos sistema) {
        this.sistema = sistema;
        this.tamañoBloque = 20;
        this.bloquesAncho = 16;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel para el disco
        JPanel panelVisualizacion = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarDisco((Graphics2D) g);
            }
        };
        panelVisualizacion.setPreferredSize(new Dimension(400, 400));

        // Panel de información
        JPanel panelInfo = crearPanelInformacion();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            panelVisualizacion, panelInfo);
        split.setDividerLocation(400);

        add(split, BorderLayout.CENTER);

        actualizarInformacionDisco();
    }

    private void dibujarDisco(Graphics2D g) {
        Disco disco = sistema.getDisco();
        Bloque[] bloques = disco.getBloques();

        g.setFont(new Font("Arial", Font.PLAIN, 8));
        int x = 10;
        int y = 10;

        for (int i = 0; i < bloques.length; i++) {
            // Dibujar bloque
            Color color;
            if (bloques[i].isOcupado()) {
                // Convertir color hexadecimal a Color
                try {
                    String colorHex = bloques[i].getColorAsignado();
                    color = Color.decode(colorHex);
                } catch (Exception ex) {
                    color = Color.BLUE;
                }
            } else {
                color = Color.WHITE;
            }

            g.setColor(color);
            g.fillRect(x, y, tamañoBloque, tamañoBloque);

            g.setColor(Color.BLACK);
            g.drawRect(x, y, tamañoBloque, tamañoBloque);

            // Siguiente posición
            x += tamañoBloque + 2;
            if ((i + 1) % bloquesAncho == 0) {
                x = 10;
                y += tamañoBloque + 2;
            }
        }
    }

    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Información del Disco"));

        lblTotal = new JLabel("Total de bloques: 0");
        lblOcupados = new JLabel("Bloques ocupados: 0");
        lblLibres = new JLabel("Bloques libres: 0");
        lblPorcentaje = new JLabel("Ocupación: 0%");
        lblPolítica = new JLabel("Política: FIFO");

        panel.add(lblTotal);
        panel.add(lblOcupados);
        panel.add(lblLibres);
        panel.add(lblPorcentaje);
        panel.add(lblPolítica);

        return panel;
    }

    private void actualizarInformacionDisco() {
        Disco disco = sistema.getDisco();
        lblTotal.setText("Total de bloques: " + disco.getTotalBloques());
        lblOcupados.setText("Bloques ocupados: " + disco.getBloquesOcupados());
        lblLibres.setText("Bloques libres: " + disco.getBloquesLibres());
        lblPorcentaje.setText(String.format("Ocupación: %.2f%%", disco.getPercentajeOcupacion()));
        lblPolítica.setText("Política: " + sistema.getPlanificador().getPoliticaActual());
    }

    public void actualizar() {
        actualizarInformacionDisco();
        repaint();
    }
}
