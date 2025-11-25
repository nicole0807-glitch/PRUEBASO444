package modelo;

import java.util.LinkedList;

/**
 * Clase que implementa la cola de solicitudes de E/S del disco
 */
public class ColaIO {
    private LinkedList<SolicitudIO> cola;
    private int contadorSolicitudes;
    private long tiempoPromedio;
    private int totalSolicitudesCompletadas;

    public ColaIO() {
        this.cola = new LinkedList<>();
        this.contadorSolicitudes = 0;
        this.tiempoPromedio = 0;
        this.totalSolicitudesCompletadas = 0;
    }

    /**
     * Agrega una solicitud a la cola
     */
    public void agregarSolicitud(SolicitudIO solicitud) {
        cola.add(solicitud);
    }

    /**
     * Obtiene la primera solicitud de la cola sin removerla
     */
    public SolicitudIO obtenerPrimera() {
        if (!cola.isEmpty()) {
            return cola.getFirst();
        }
        return null;
    }

    /**
     * Extrae la primera solicitud de la cola
     */
    public SolicitudIO extraerPrimera() {
        if (!cola.isEmpty()) {
            return cola.removeFirst();
        }
        return null;
    }

    /**
     * Obtiene una solicitud por su ID
     */
    public SolicitudIO obtenerPorId(int idSolicitud) {
        for (SolicitudIO solicitud : cola) {
            if (solicitud.getIdSolicitud() == idSolicitud) {
                return solicitud;
            }
        }
        return null;
    }

    /**
     * Remueve una solicitud específica
     */
    public boolean removerSolicitud(SolicitudIO solicitud) {
        boolean resultado = cola.remove(solicitud);
        if (resultado) {
            actualizarEstadisticas(solicitud);
        }
        return resultado;
    }

    /**
     * Obtiene el tamaño actual de la cola
     */
    public int getTamaño() {
        return cola.size();
    }

    /**
     * Verifica si la cola está vacía
     */
    public boolean estaVacia() {
        return cola.isEmpty();
    }

    /**
     * Obtiene todas las solicitudes de la cola
     */
    public LinkedList<SolicitudIO> obtenerTodas() {
        return cola;
    }

    /**
     * Limpia la cola
     */
    public void limpiar() {
        cola.clear();
    }

    /**
     * Actualiza las estadísticas cuando se completa una solicitud
     */
    private void actualizarEstadisticas(SolicitudIO solicitud) {
        totalSolicitudesCompletadas++;
        // Calcular tiempo promedio simple
        long tiempoServicio = solicitud.getTiempoServicio();
        tiempoPromedio = ((tiempoPromedio * (totalSolicitudesCompletadas - 1)) + tiempoServicio) 
                        / totalSolicitudesCompletadas;
    }

    // Getters
    public long getTiempoPromedio() {
        return tiempoPromedio;
    }

    public int getTotalSolicitudesCompletadas() {
        return totalSolicitudesCompletadas;
    }

    @Override
    public String toString() {
        return "ColaIO{" +
                "tamaño=" + cola.size() +
                ", solicitudesCompletadas=" + totalSolicitudesCompletadas +
                ", tiempoPromedio=" + tiempoPromedio + "ms" +
                '}';
    }
}
