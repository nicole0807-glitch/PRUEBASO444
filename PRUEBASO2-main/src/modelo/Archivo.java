package modelo;

import java.util.LinkedList;

/**
 * Clase que representa un archivo en el sistema
 */
public class Archivo {
    private String nombre;
    private int tamañoBloques;
    private int primerBloque;           // Inicio de la lista enlazada
    private String propietario;
    private boolean esPublico;
    private String colorAsignado;
    private LinkedList<Integer> bloques;  // Lista de bloques asignados
    private long fechaCreacion;
    private long ultimaModificacion;

    public Archivo(String nombre, int tamañoBloques, String propietario, boolean esPublico) {
        this.nombre = nombre;
        this.tamañoBloques = tamañoBloques;
        this.propietario = propietario;
        this.esPublico = esPublico;
        this.primerBloque = -1;
        this.bloques = new LinkedList<>();
        this.fechaCreacion = System.currentTimeMillis();
        this.ultimaModificacion = System.currentTimeMillis();
        this.colorAsignado = generarColorAleatorio();
    }

    private String generarColorAleatorio() {
        int r = (int) (Math.random() * 200) + 50;
        int g = (int) (Math.random() * 200) + 50;
        int b = (int) (Math.random() * 200) + 50;
        return String.format("#%02X%02X%02X", r, g, b);
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public int getTamañoBloques() {
        return tamañoBloques;
    }

    public int getPrimerBloque() {
        return primerBloque;
    }

    public String getPropietario() {
        return propietario;
    }

    public boolean isEsPublico() {
        return esPublico;
    }

    public String getColorAsignado() {
        return colorAsignado;
    }

    public LinkedList<Integer> getBloques() {
        return bloques;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public long getUltimaModificacion() {
        return ultimaModificacion;
    }

    // Setters
    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.ultimaModificacion = System.currentTimeMillis();
    }

    public void setPrimerBloque(int primerBloque) {
        this.primerBloque = primerBloque;
    }

    public void setEsPublico(boolean esPublico) {
        this.esPublico = esPublico;
    }

    public void setColorAsignado(String colorAsignado) {
        this.colorAsignado = colorAsignado;
    }

    // Métodos de gestión de bloques
    public void agregarBloque(int numeroBloque) {
        bloques.add(numeroBloque);
        if (primerBloque == -1) {
            primerBloque = numeroBloque;
        }
        this.ultimaModificacion = System.currentTimeMillis();
    }

    public void removerBloque(int numeroBloque) {
        bloques.remove(Integer.valueOf(numeroBloque));
        if (bloques.isEmpty()) {
            primerBloque = -1;
        } else if (primerBloque == numeroBloque) {
            primerBloque = bloques.getFirst();
        }
        this.ultimaModificacion = System.currentTimeMillis();
    }

    public int getCantidadBloquesAsignados() {
        return bloques.size();
    }

    public boolean contieneBloqueAsignado(int numeroBloque) {
        return bloques.contains(numeroBloque);
    }

    @Override
    public String toString() {
        return "Archivo{" +
                "nombre='" + nombre + '\'' +
                ", tamañoBloques=" + tamañoBloques +
                ", primerBloque=" + primerBloque +
                ", propietario='" + propietario + '\'' +
                ", esPublico=" + esPublico +
                '}';
    }
}