package gui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(sistema.getRaiz().getNombre());
        construirSubarbol(raiz, sistema.getRaiz());
        return raiz;
    }

    private void construirSubarbol(DefaultMutableTreeNode nodoParente, Directorio directorio) {
        // Agregar subdirectorios
        for (Directorio subdir : directorio.getSubdirectorios()) {
            DefaultMutableTreeNode nodoSubdir = new DefaultMutableTreeNode(subdir.getNombre());
            nodoParente.add(nodoSubdir);
            construirSubarbol(nodoSubdir, subdir);
        }

        // Agregar archivos
        for (Archivo archivo : directorio.getArchivos()) {
            String etiqueta = archivo.getNombre() + " (" + 
                            archivo.getCantidadBloquesAsignados() + " bloques)";
            DefaultMutableTreeNode nodoArchivo = new DefaultMutableTreeNode(etiqueta);
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
}
