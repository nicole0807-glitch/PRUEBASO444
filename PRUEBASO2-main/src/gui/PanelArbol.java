package gui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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
    private Archivo archivoSeleccionado;
    private Directorio directorioSeleccionado;

    public PanelArbol(SistemaArchivos sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Crear árbol
        arbolDirectorios = new JTree(construirArbol());
        arbolDirectorios.setFont(new Font("Arial", Font.PLAIN, 12));
        arbolDirectorios.addTreeSelectionListener(crearListenerSeleccion());

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

        // Intentar reseleccionar el directorio actual para mantener la navegación
        seleccionarDirectorio(sistema.getDirectorioActual());
    }

    /**
     * Devuelve el archivo seleccionado en el árbol o null si no hay uno.
     */
    public Archivo getArchivoSeleccionado() {
        return archivoSeleccionado;
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
     * Devuelve el directorio seleccionado o, si hay un archivo seleccionado,
     * su directorio padre. Si no hay selección, devuelve la raíz.
     */
    public Directorio getDirectorioSeleccionado() {
        TreePath seleccion = arbolDirectorios.getSelectionPath();
        if (seleccion == null) {
            return sistema.getRaiz();
        }

        DefaultMutableTreeNode nodoSeleccionado =
            (DefaultMutableTreeNode) seleccion.getLastPathComponent();
        Object referencia = extraerReferencia(nodoSeleccionado);

        if (referencia instanceof Directorio) {
            return (Directorio) referencia;
        }

        // Si es un archivo, el directorio es el nodo padre
        DefaultMutableTreeNode nodoPadre =
            (DefaultMutableTreeNode) nodoSeleccionado.getParent();
        if (nodoPadre != null) {
            Object refPadre = extraerReferencia(nodoPadre);
            if (refPadre instanceof Directorio) {
                return (Directorio) refPadre;
            }
        }
        return sistema.getRaiz();
    }

    /**
     * Listener que mantiene sincronizado el directorio actual con la selección del árbol.
     */
    private TreeSelectionListener crearListenerSeleccion() {
        return new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath seleccion = e.getPath();
                if (seleccion == null) {
                    archivoSeleccionado = null;
                    directorioSeleccionado = sistema.getRaiz();
                    sistema.setDirectorioActual(directorioSeleccionado);
                    infoArea.setText("");
                    return;
                }

                DefaultMutableTreeNode nodo =
                    (DefaultMutableTreeNode) seleccion.getLastPathComponent();
                Object referencia = extraerReferencia(nodo);

                if (referencia instanceof Directorio) {
                    directorioSeleccionado = (Directorio) referencia;
                    archivoSeleccionado = null;
                    sistema.setDirectorioActual(directorioSeleccionado);
                } else if (referencia instanceof Archivo) {
                    archivoSeleccionado = (Archivo) referencia;
                    directorioSeleccionado = getDirectorioSeleccionado();
                } else {
                    directorioSeleccionado = sistema.getRaiz();
                    archivoSeleccionado = null;
                    sistema.setDirectorioActual(directorioSeleccionado);
                }

                mostrarInformacionNodo();
            }
        };
    }

    /**
     * Selecciona en el árbol el nodo que corresponde al directorio indicado.
     */
    private void seleccionarDirectorio(Directorio directorio) {
        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) arbolDirectorios.getModel().getRoot();
        TreePath ruta = buscarPath(raiz, directorio);
        if (ruta != null) {
            arbolDirectorios.setSelectionPath(ruta);
            arbolDirectorios.scrollPathToVisible(ruta);
        }
    }

    private TreePath buscarPath(DefaultMutableTreeNode nodo, Object objetivo) {
        Object referencia = extraerReferencia(nodo);
        if (referencia == objetivo) {
            return new TreePath(nodo.getPath());
        }

        for (int i = 0; i < nodo.getChildCount(); i++) {
            DefaultMutableTreeNode hijo = (DefaultMutableTreeNode) nodo.getChildAt(i);
            TreePath resultado = buscarPath(hijo, objetivo);
            if (resultado != null) {
                return resultado;
            }
        }
        return null;
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
