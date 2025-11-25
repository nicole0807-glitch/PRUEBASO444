package modelo;

/**
 * Clase que representa un bloque individual en el disco
 */
public class Bloque {
    private int numeroBloque;
    private boolean ocupado;
    private String propietario;      // Archivo que ocupa este bloque
    private int bloqueProximo;       // Para la lista enlazada (-1 si es el último)
    private String colorAsignado;    // Color para visualización

    public Bloque(int numeroBloque) {
        this.numeroBloque = numeroBloque;
        this.ocupado = false;
        this.propietario = null;
        this.bloqueProximo = -1;
        this.colorAsignado = "#FFFFFF";  // Blanco por defecto (libre)
    }

    // Getters
    public int getNumeroBloque() {
        return numeroBloque;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public String getPropietario() {
        return propietario;
    }

    public int getBloqueProximo() {
        return bloqueProximo;
    }

    public String getColorAsignado() {
        return colorAsignado;
    }

    // Setters
    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public void setBloqueProximo(int bloqueProximo) {
        this.bloqueProximo = bloqueProximo;
    }

    public void setColorAsignado(String colorAsignado) {
        this.colorAsignado = colorAsignado;
    }

    public void liberar() {
        this.ocupado = false;
        this.propietario = null;
        this.bloqueProximo = -1;
        this.colorAsignado = "#FFFFFF";
    }

    @Override
    public String toString() {
        return "Bloque{" +
                "numero=" + numeroBloque +
                ", ocupado=" + ocupado +
                ", propietario='" + propietario + '\'' +
                ", proximo=" + bloqueProximo +
                '}';
    }
}
