package gui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import modelo.*;

/**
 * Panel que muestra la estructura jerárquica de directorios y archivos
 */
public class PanelArbol extends JPanel {
    private SistemaArchivos sistema;
    private JTree arbolDirectorios;
    private JTextArea infoArea;

    public PanelArbol(SistemaArchivos sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Crear árbol
        arbolDirectorios = new JTree(construirArbol());
        arbolDirectorios.setFont(new Font("Arial", Font.PLAIN, 12));
        arbolDirectorios.addTreeSelectionListener(e -> mostrarInformacionNodo());

        JScrollPane scrollArbol = new JScrollPane(arbolDirectorios);
        scrollArbol.setPreferredSize(new Dimension(400, 500));

        // Área de información
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Courier", Font.PLAIN, 11));
        JScrollPane scrollInfo = new JScrollPane(infoArea);
        scrollInfo.setPreferredSize(new Dimension(350, 500));

        // Dividir en dos
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            scrollArbol, scrollInfo);
        split.setDividerLocation(400);

        add(split, BorderLayout.CENTER);
    }

    private DefaultMutableTreeNode construirArbol() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(
            new NodoReferencia(sistema.getRaiz(), "/", "root"));
        construirSubarbol(raiz, sistema.getRaiz());
        return raiz;
    }

    private void construirSubarbol(DefaultMutableTreeNode nodoParente, Directorio directorio) {
        // Agregar subdirectorios
        for (Directorio subdir : directorio.getSubdirectorios()) {
            if (!sistema.puedeVerDirectorio(subdir)) {
                continue;
            }
            DefaultMutableTreeNode nodoSubdir = new DefaultMutableTreeNode(
                new NodoReferencia(subdir, subdir.getNombre(), subdir.getNombre()));
            nodoParente.add(nodoSubdir);
            construirSubarbol(nodoSubdir, subdir);
        }

        // Agregar archivos
        for (Archivo archivo : directorio.getArchivos()) {
            if (!sistema.puedeVerArchivo(archivo)) {
                continue;
            }
            String etiqueta = archivo.getNombre() + " (" +
                            archivo.getCantidadBloquesAsignados() + " bloques)";
            DefaultMutableTreeNode nodoArchivo = new DefaultMutableTreeNode(
                new NodoReferencia(archivo, archivo.getNombre(), etiqueta));
            nodoParente.add(nodoArchivo);
        }
    }

    private void mostrarInformacionNodo() {
        DefaultMutableTreeNode nodo = 
            (DefaultMutableTreeNode) arbolDirectorios.getLastSelectedPathComponent();

        if (nodo == null) {
            infoArea.setText("");
            return;
        }

        infoArea.setText(nodo.toString());
    }

    public void actualizar() {
        DefaultTreeModel modelo = (DefaultTreeModel) arbolDirectorios.getModel();
        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) modelo.getRoot();
        raiz.removeAllChildren();
        construirSubarbol(raiz, sistema.getRaiz());
        modelo.reload();
    }

    /**
     * Devuelve el archivo seleccionado en el árbol o null si no hay uno.
     */
    public Archivo getArchivoSeleccionado() {
        TreePath seleccion = arbolDirectorios.getSelectionPath();
        if (seleccion == null) {
            return null;
        }

        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) seleccion.getLastPathComponent();
        Object referencia = extraerReferencia(nodo);
        if (referencia instanceof Archivo) {
            return (Archivo) referencia;
        }
        return null;
    }

    /**
     * Obtiene la ruta completa del archivo seleccionado o null si no aplica.
     */
    public String getRutaArchivoSeleccionado() {
        TreePath seleccion = arbolDirectorios.getSelectionPath();
        if (seleccion == null) {
            return null;
        }

        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) seleccion.getLastPathComponent();
        Object referencia = extraerReferencia(nodo);
        if (!(referencia instanceof Archivo)) {
            return null;
        }

        Object[] nodos = seleccion.getPath();
        StringBuilder ruta = new StringBuilder("/");
        // Saltar el nodo raíz (posición 0)
        for (int i = 1; i < nodos.length; i++) {
            DefaultMutableTreeNode actual = (DefaultMutableTreeNode) nodos[i];
            Object obj = actual.getUserObject();
            if (obj instanceof NodoReferencia) {
                String nombreRuta = ((NodoReferencia) obj).getNombreRuta();
                if (ruta.length() > 1) {
                    ruta.append("/");
                }
                ruta.append(nombreRuta);
            }
        }
        return ruta.toString();
    }

    private Object extraerReferencia(DefaultMutableTreeNode nodo) {
        Object obj = nodo.getUserObject();
        if (obj instanceof NodoReferencia) {
            return ((NodoReferencia) obj).getReferencia();
        }
        return obj;
    }

    /**
     * Wrapper para almacenar la referencia del modelo y un nombre visible.
     */
    private static class NodoReferencia {
        private final Object referencia;
        private final String nombreRuta;
        private final String nombreVisible;

        NodoReferencia(Object referencia, String nombreRuta, String nombreVisible) {
            this.referencia = referencia;
            this.nombreRuta = nombreRuta;
            this.nombreVisible = nombreVisible;
        }

        Object getReferencia() {
            return referencia;
        }

        String getNombreRuta() {
            return nombreRuta;
        }

        @Override
        public String toString() {
            return nombreVisible;
        }
    }
}
