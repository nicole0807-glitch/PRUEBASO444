package modelo;

import java.util.LinkedList;

/**
 * Clase que representa un directorio en el sistema de archivos
 */
public class Directorio {
    private String nombre;
    private Directorio padreDirectorio;
    private String propietario;
    private boolean esPublico;
    private LinkedList<Directorio> subdirectorios;  // Sin librerías
    private LinkedList<Archivo> archivos;           // Sin librerías
    private long fechaCreacion;

    public Directorio(String nombre, String propietario, Directorio padre) {
        this(nombre, propietario, padre, true);
    }

    public Directorio(String nombre, String propietario, Directorio padre, boolean esPublico) {
        this.nombre = nombre;
        this.propietario = propietario;
        this.padreDirectorio = padre;
        this.esPublico = esPublico;
        this.subdirectorios = new LinkedList<>();
        this.archivos = new LinkedList<>();
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public Directorio getPadreDirectorio() {
        return padreDirectorio;
    }

    public String getPropietario() {
        return propietario;
    }

    public boolean isEsPublico() {
        return esPublico;
    }

    public LinkedList<Directorio> getSubdirectorios() {
        return subdirectorios;
    }

    public LinkedList<Archivo> getArchivos() {
        return archivos;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    // Setters
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEsPublico(boolean esPublico) {
        this.esPublico = esPublico;
    }

    // Métodos de gestión de subdirectorios
    public void agregarSubdirectorio(Directorio directorio) {
        if (!subdirectorios.contains(directorio)) {
            subdirectorios.add(directorio);
        }
    }

    public void removerSubdirectorio(Directorio directorio) {
        subdirectorios.remove(directorio);
    }

    public Directorio buscarSubdirectorio(String nombre) {
        for (Directorio d : subdirectorios) {
            if (d.getNombre().equals(nombre)) {
                return d;
            }
        }
        return null;
    }

    // Métodos de gestión de archivos
    public void agregarArchivo(Archivo archivo) {
        if (!archivos.contains(archivo)) {
            archivos.add(archivo);
        }
    }

    public void removerArchivo(Archivo archivo) {
        archivos.remove(archivo);
    }

    public Archivo buscarArchivo(String nombre) {
        for (Archivo a : archivos) {
            if (a.getNombre().equals(nombre)) {
                return a;
            }
        }
        return null;
    }

    public boolean contieneSoloEstaDirectorio(String nombre) {
        return buscarArchivo(nombre) != null;
    }

    public LinkedList<Archivo> obtenerTodosLosArchivos() {
        LinkedList<Archivo> todos = new LinkedList<>();
        // Agregar archivos de este directorio
        for (Archivo a : archivos) {
            todos.add(a);
        }
        // Agregar archivos recursivamente de subdirectorios
        for (Directorio d : subdirectorios) {
            todos.addAll(d.obtenerTodosLosArchivos());
        }
        return todos;
    }

    @Override
    public String toString() {
        return "Directorio{" +
                "nombre='" + nombre + '\'' +
                ", propietario='" + propietario + '\'' +
                ", esPublico=" + esPublico +
                ", subdirectorios=" + subdirectorios.size() +
                ", archivos=" + archivos.size() +
                '}';
    }
}
