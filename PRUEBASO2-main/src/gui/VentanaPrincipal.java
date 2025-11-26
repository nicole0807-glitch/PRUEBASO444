package gui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import modelo.*;

/**
 * Ventana principal de la aplicación del simulador de sistema de archivos
 */
public class VentanaPrincipal extends JFrame {
    private final SistemaArchivos sistema;
    private final JTabbedPane tabbedPane;
    private final PanelArbol panelArbol;
    private final PanelDisco panelDisco;
    private final PanelTablaArchivos panelTabla;
    private final PanelProcesos panelProcesos;
    private JLabel estadoLabel;
    private JLabel usuarioLabel;

    public VentanaPrincipal(int numBloques) {
        // Inicializar sistema (256 bloques, SIN buffer - es opcional)
        sistema = new SistemaArchivos(numBloques, false);

        // Configuración básica de la ventana
        setTitle("Simulador Virtual de Sistema de Archivos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        // Crear menú
        crearMenuBar();

        // Crear panel superior con controles
        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);

        // Crear panel con pestañas
        tabbedPane = new JTabbedPane();

        panelArbol = new PanelArbol(sistema);
        panelDisco = new PanelDisco(sistema);
        panelTabla = new PanelTablaArchivos(sistema);
        panelProcesos = new PanelProcesos(sistema);

        tabbedPane.addTab("Estructura", panelArbol);
        tabbedPane.addTab("Disco", panelDisco);
        tabbedPane.addTab("Archivos", panelTabla);
        tabbedPane.addTab("Procesos", panelProcesos);

        add(tabbedPane, BorderLayout.CENTER);

        // Crear barra de estado
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);

        actualizarPantalla();

        setVisible(true);
    }

    private void crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menú Archivo
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem guardar = new JMenuItem("Guardar");
        JMenuItem cargar = new JMenuItem("Cargar");
        JMenuItem salir = new JMenuItem("Salir");

        guardar.addActionListener(e -> guardarSistema());
        cargar.addActionListener(e -> cargarSistema());
        salir.addActionListener(e -> System.exit(0));

        menuArchivo.add(guardar);
        menuArchivo.add(cargar);
        menuArchivo.addSeparator();
        menuArchivo.add(salir);

        // Menú Edición
        JMenu menuEdicion = new JMenu("Edición");
        JMenuItem crearDirectorio = new JMenuItem("Crear Directorio");
        JMenuItem crearArchivo = new JMenuItem("Crear Archivo");
        JMenuItem eliminarArchivo = new JMenuItem("Eliminar Archivo");

        crearDirectorio.addActionListener(e -> mostrarDialogoCrearDirectorio());
        crearArchivo.addActionListener(e -> mostrarDialogoCrearArchivo());
        eliminarArchivo.addActionListener(e -> eliminarArchivoSeleccionado());

        menuEdicion.add(crearDirectorio);
        menuEdicion.add(crearArchivo);
        menuEdicion.add(eliminarArchivo);

        // Menú Configuración
        JMenu menuConfiguracion = new JMenu("Configuración");
        JMenuItem cambiarModo = new JMenuItem("Cambiar Modo");
        JMenuItem planificacion = new JMenuItem("Política de Planificación");

        cambiarModo.addActionListener(e -> mostrarDialogoCambiarModo());
        planificacion.addActionListener(e -> mostrarDialogoPlanificacion());

        menuConfiguracion.add(cambiarModo);
        menuConfiguracion.add(planificacion);

        // Agregar menu's
        menuBar.add(menuArchivo);
        menuBar.add(menuEdicion);
        menuBar.add(menuConfiguracion);

        setJMenuBar(menuBar);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel izquierdo con información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usuarioLabel = new JLabel("Usuario: admin | Modo: ADMIN");
        usuarioLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfo.add(usuarioLabel);

        // Panel derecho con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProcesar = new JButton("Procesar E/S");
        JButton btnActualizar = new JButton("Actualizar");

        btnProcesar.addActionListener(e -> {
            sistema.procesarSiguienteSolicitud();
            actualizarPantalla();
        });

        btnActualizar.addActionListener(e -> actualizarPantalla());

        panelBotones.add(btnProcesar);
        panelBotones.add(btnActualizar);

        panel.add(panelInfo, BorderLayout.WEST);
        panel.add(panelBotones, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        estadoLabel = new JLabel("Estado: Sistema inicializado");
        estadoLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        panel.add(estadoLabel, BorderLayout.CENTER);

        return panel;
    }

    private void mostrarDialogoCrearDirectorio() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del directorio:");
        if (nombre != null && !nombre.isEmpty()) {
            if (!sistema.puedeOperarEnDirectorio(sistema.getDirectorioActual())) {
                JOptionPane.showMessageDialog(this,
                    "No tiene permisos para crear en este directorio.",
                    "Permiso denegado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (sistema.crearDirectorio(nombre)) {
                JOptionPane.showMessageDialog(this, "Directorio creado exitosamente");
                actualizarPantalla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear directorio", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarDialogoCrearArchivo() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField nombreField = new JTextField();
        JTextField tamañoField = new JTextField("4");
        JCheckBox publico = new JCheckBox("Público");

        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Tamaño (bloques):"));
        panel.add(tamañoField);
        panel.add(new JLabel(""));
        panel.add(publico);

        int resultado = JOptionPane.showConfirmDialog(this, panel, 
            "Crear Archivo", JOptionPane.OK_CANCEL_OPTION);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText();
                int tamaño = Integer.parseInt(tamañoField.getText());
                boolean esPublico = publico.isSelected();

                if (!sistema.puedeOperarEnDirectorio(sistema.getDirectorioActual())) {
                    JOptionPane.showMessageDialog(this,
                        "No tiene permisos para crear en este directorio.",
                        "Permiso denegado", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (sistema.crearArchivo(nombre, tamaño, esPublico)) {
                    JOptionPane.showMessageDialog(this, "Archivo creado exitosamente");
                    actualizarPantalla();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Error al crear archivo. Verifique el espacio disponible.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tamaño inválido", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Elimina el archivo actualmente seleccionado en la interfaz (árbol o tabla).
     */
    private void eliminarArchivoSeleccionado() {
        Archivo archivoSeleccionado = panelArbol.getArchivoSeleccionado();
        String rutaSeleccionada = panelArbol.getRutaArchivoSeleccionado();

        if (archivoSeleccionado == null || rutaSeleccionada == null) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un archivo en el árbol para eliminarlo.");
            return;
        }

        if (!sistema.puedeOperarArchivo(archivoSeleccionado)) {
            JOptionPane.showMessageDialog(this,
                "No tiene permisos para operar sobre este archivo.",
                "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean eliminado = sistema.eliminarArchivoPorRuta(rutaSeleccionada);
        if (eliminado) {
            JOptionPane.showMessageDialog(this, "Archivo eliminado exitosamente");
            actualizarPantalla();
        } else {
            JOptionPane.showMessageDialog(this, "Error al eliminar archivo",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoCambiarModo() {
        String[] opciones = {"ADMINISTRADOR", "USUARIO"};
        int opcion = JOptionPane.showOptionDialog(this, 
            "Seleccione el modo:", "Cambiar Modo",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
            null, opciones, opciones[0]);

        if (opcion == 0) {
            sistema.setModoAdministrador(true);
            sistema.setUsuarioActual("admin");
        } else if (opcion == 1) {
            String usuario = JOptionPane.showInputDialog(this,
                "Ingrese el nombre de usuario:", sistema.getUsuarioActual());
            if (usuario == null || usuario.trim().isEmpty()) {
                return;
            }
            sistema.setModoAdministrador(false);
            sistema.setUsuarioActual(usuario.trim());
        }

        actualizarPantalla();
    }

    private void mostrarDialogoPlanificacion() {
        String[] politicas = {"FIFO", "SSTF", "SCAN", "CSCAN"};
        int opcion = JOptionPane.showOptionDialog(this,
            "Seleccione la política de planificación:",
            "Planificación de Disco",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, politicas, politicas[0]);

        if (opcion >= 0) {
            Planificador.PoliticaplanificacionDisco politica = 
                Planificador.PoliticaplanificacionDisco.values()[opcion];
            sistema.setPoliticaPlanificacion(politica);
            JOptionPane.showMessageDialog(this, 
                "Política cambiada a: " + politica.name());
            actualizarPantalla();
        }
    }

    private void actualizarPantalla() {
        panelArbol.actualizar();
        panelDisco.actualizar();
        panelTabla.actualizar();
        panelProcesos.actualizar();

        usuarioLabel.setText("Usuario: " + sistema.getUsuarioActual() +
            " | Modo: " + (sistema.isModoAdministrador() ? "ADMIN" : "USER"));
        estadoLabel.setText("Estado: " + sistema.toString());
    }

    private void guardarSistema() {
        // Permite elegir un archivo y delegar el guardado del estado del sistema
        JFileChooser selector = new JFileChooser();
        int resultado = selector.showSaveDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = selector.getSelectedFile();
            try {
                sistema.guardarEstado(archivoSeleccionado);
                JOptionPane.showMessageDialog(this, "Estado guardado correctamente.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al guardar el sistema: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarSistema() {
        // Permite elegir un archivo previamente guardado y recargar el estado
        JFileChooser selector = new JFileChooser();
        int resultado = selector.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = selector.getSelectedFile();
            try {
                sistema.cargarEstado(archivoSeleccionado);
                panelArbol.actualizar();
                panelDisco.actualizar();
                panelTabla.actualizar();
                panelProcesos.actualizar();
                actualizarPantalla();
                JOptionPane.showMessageDialog(this, "Estado cargado correctamente.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al cargar el sistema: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}