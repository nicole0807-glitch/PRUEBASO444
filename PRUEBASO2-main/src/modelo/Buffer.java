package modelo;

import java.util.LinkedList;

/**
 * Clase que implementa un buffer (memoria caché) para operaciones de E/S
 */
public class Buffer {
    public enum PoliticaReemplazo {
        FIFO, LRU, LFU
    }

    private LinkedList<BloqueBlog> bufferMemoria;
    private int capacidadMaxima;
    private PoliticaReemplazo politicaActual;

    public Buffer(int capacidadMaxima) {
        this.bufferMemoria = new LinkedList<>();
        this.capacidadMaxima = capacidadMaxima;
        this.politicaActual = PoliticaReemplazo.LRU;
    }

    /**
     * Busca un bloque en el buffer
     */
    public BloqueBlog buscarBloque(int numeroBloque) {
        for (BloqueBlog bloque : bufferMemoria) {
            if (bloque.getNumeroBloque() == numeroBloque) {
                bloque.incrementarAccesos();  // Para LFU
                bloque.actualizarTiempoAcceso();  // Para LRU
                return bloque;
            }
        }
        return null;
    }

    /**
     * Agrega un bloque al buffer
     */
    public void agregarBloque(int numeroBloque, byte[] datos) {
        // Verificar si el bloque ya existe
        BloqueBlog existente = buscarBloque(numeroBloque);
        if (existente != null) {
            return;
        }

        // Si el buffer está lleno, aplicar política de reemplazo
        if (bufferMemoria.size() >= capacidadMaxima) {
            reemplazarBloque();
        }

        BloqueBlog nuevoBloque = new BloqueBlog(numeroBloque, datos);
        bufferMemoria.add(nuevoBloque);
    }

    /**
     * Reemplaza un bloque según la política establecida
     */
    private void reemplazarBloque() {
        if (bufferMemoria.isEmpty()) {
            return;
        }

        switch (politicaActual) {
            case FIFO:
                bufferMemoria.removeFirst();
                break;
            case LRU:
                reemplazarLRU();
                break;
            case LFU:
                reemplazarLFU();
                break;
        }
    }

    /**
     * Reemplaza el bloque menos recientemente usado (LRU)
     */
    private void reemplazarLRU() {
        BloqueBlog bloqueAntiguo = bufferMemoria.getFirst();
        for (BloqueBlog bloque : bufferMemoria) {
            if (bloque.getTiempoUltimoAcceso() < bloqueAntiguo.getTiempoUltimoAcceso()) {
                bloqueAntiguo = bloque;
            }
        }
        bufferMemoria.remove(bloqueAntiguo);
    }

    /**
     * Reemplaza el bloque menos frecuentemente usado (LFU)
     */
    private void reemplazarLFU() {
        BloqueBlog bloqueMenosUsado = bufferMemoria.getFirst();
        for (BloqueBlog bloque : bufferMemoria) {
            if (bloque.getContadorAccesos() < bloqueMenosUsado.getContadorAccesos()) {
                bloqueMenosUsado = bloque;
            }
        }
        bufferMemoria.remove(bloqueMenosUsado);
    }

    /**
     * Elimina un bloque específico del buffer
     */
    public boolean removerBloque(int numeroBloque) {
        for (BloqueBlog bloque : bufferMemoria) {
            if (bloque.getNumeroBloque() == numeroBloque) {
                bufferMemoria.remove(bloque);
                return true;
            }
        }
        return false;
    }

    /**
     * Limpia todo el buffer
     */
    public void limpiar() {
        bufferMemoria.clear();
    }

    // Getters
    public LinkedList<BloqueBlog> obtenerBloques() {
        return bufferMemoria;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public int getEspacioDisponible() {
        return capacidadMaxima - bufferMemoria.size();
    }

    public int getEspacioOcupado() {
        return bufferMemoria.size();
    }

    public PoliticaReemplazo getPoliticaActual() {
        return politicaActual;
    }

    public void setPoliticaActual(PoliticaReemplazo politica) {
        this.politicaActual = politica;
    }

    public double getPercentajeOcupacion() {
        return (double) bufferMemoria.size() / capacidadMaxima * 100;
    }

    @Override
    public String toString() {
        return "Buffer{" +
                "ocupado=" + bufferMemoria.size() +
                ", capacidad=" + capacidadMaxima +
                ", politica=" + politicaActual +
                '}';
    }

    /**
     * Clase interna que representa un bloque en el buffer
     */
    public static class BloqueBlog {
        private int numeroBloque;
        private byte[] datos;
        private long tiempoCreacion;
        private long tiempoUltimoAcceso;
        private int contadorAccesos;

        public BloqueBlog(int numeroBloque, byte[] datos) {
            this.numeroBloque = numeroBloque;
            this.datos = datos;
            this.tiempoCreacion = System.currentTimeMillis();
            this.tiempoUltimoAcceso = System.currentTimeMillis();
            this.contadorAccesos = 1;
        }

        public int getNumeroBloque() {
            return numeroBloque;
        }

        public byte[] getDatos() {
            return datos;
        }

        public long getTiempoUltimoAcceso() {
            return tiempoUltimoAcceso;
        }

        public int getContadorAccesos() {
            return contadorAccesos;
        }

        public void actualizarTiempoAcceso() {
            this.tiempoUltimoAcceso = System.currentTimeMillis();
        }

        public void incrementarAccesos() {
            this.contadorAccesos++;
        }
    }
}
