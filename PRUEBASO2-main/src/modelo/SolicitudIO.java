package modelo;

/**
 * Clase que representa una solicitud de E/S en la cola del disco
 */
public class SolicitudIO {
    public enum TipoOperacion {
        CREAR, LEER, ACTUALIZAR, ELIMINAR
    }

    private int idSolicitud;
    private int idProceso;
    private TipoOperacion tipo;
    private Archivo archivoAfectado;
    private long tiempoLlegada;
    private long tiempoInicio;
    private long tiempoFinalizacion;
    private boolean completada;
    private int cilindroAcceso;  // Para SSTF y SCAN

    public SolicitudIO(int idSolicitud, int idProceso, TipoOperacion tipo, 
                       Archivo archivo, int cilindroAcceso) {
        this.idSolicitud = idSolicitud;
        this.idProceso = idProceso;
        this.tipo = tipo;
        this.archivoAfectado = archivo;
        this.cilindroAcceso = cilindroAcceso;
        this.tiempoLlegada = System.currentTimeMillis();
        this.tiempoInicio = -1;
        this.tiempoFinalizacion = -1;
        this.completada = false;
    }

    // Getters
    public int getIdSolicitud() {
        return idSolicitud;
    }

    public int getIdProceso() {
        return idProceso;
    }

    public TipoOperacion getTipo() {
        return tipo;
    }

    public Archivo getArchivoAfectado() {
        return archivoAfectado;
    }

    public long getTiempoLlegada() {
        return tiempoLlegada;
    }

    public long getTiempoInicio() {
        return tiempoInicio;
    }

    public long getTiempoFinalizacion() {
        return tiempoFinalizacion;
    }

    public boolean isCompletada() {
        return completada;
    }

    public int getCilindroAcceso() {
        return cilindroAcceso;
    }

    // Setters
    public void setTiempoInicio(long tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    public void setTiempoFinalizacion(long tiempoFinalizacion) {
        this.tiempoFinalizacion = tiempoFinalizacion;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public long getTiempoEspera() {
        if (tiempoInicio > 0) {
            return tiempoInicio - tiempoLlegada;
        }
        return System.currentTimeMillis() - tiempoLlegada;
    }

    public long getTiempoServicio() {
        if (tiempoFinalizacion > 0 && tiempoInicio > 0) {
            return tiempoFinalizacion - tiempoInicio;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "SolicitudIO{" +
                "id=" + idSolicitud +
                ", proceso=" + idProceso +
                ", tipo=" + tipo +
                ", archivo='" + (archivoAfectado != null ? archivoAfectado.getNombre() : "null") + '\'' +
                ", completada=" + completada +
                '}';
    }
}
