package modelo;

/**
 * Clase que representa un proceso del sistema
 */
public class Proceso {
    public enum EstadoProceso {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, TERMINADO
    }

    private int idProceso;
    private String nombreUsuario;
    private EstadoProceso estado;
    private SolicitudIO solicitudActual;
    private long tiempoCreacion;
    private long tiempoFinalizacion;
    private String tipoOperacion;  // CREATE, READ, UPDATE, DELETE

    public Proceso(int idProceso, String nombreUsuario, String tipoOperacion) {
        this.idProceso = idProceso;
        this.nombreUsuario = nombreUsuario;
        this.estado = EstadoProceso.NUEVO;
        this.solicitudActual = null;
        this.tiempoCreacion = System.currentTimeMillis();
        this.tiempoFinalizacion = -1;
        this.tipoOperacion = tipoOperacion;
    }

    // Getters
    public int getIdProceso() {
        return idProceso;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public SolicitudIO getSolicitudActual() {
        return solicitudActual;
    }

    public long getTiempoCreacion() {
        return tiempoCreacion;
    }

    public long getTiempoFinalizacion() {
        return tiempoFinalizacion;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    // Setters
    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public void setSolicitudActual(SolicitudIO solicitud) {
        this.solicitudActual = solicitud;
    }

    public void setTiempoFinalizacion(long tiempoFinalizacion) {
        this.tiempoFinalizacion = tiempoFinalizacion;
    }

    /**
     * Calcula el tiempo que el proceso ha estado en el sistema
     */
    public long getTiempoTranscurrido() {
        long fin = tiempoFinalizacion > 0 ? tiempoFinalizacion : System.currentTimeMillis();
        return fin - tiempoCreacion;
    }

    /**
     * Verifica si el proceso está activo
     */
    public boolean estaActivo() {
        return estado != EstadoProceso.TERMINADO;
    }

    @Override
    public String toString() {
        return "Proceso{" +
                "id=" + idProceso +
                ", usuario='" + nombreUsuario + '\'' +
                ", estado=" + estado +
                ", tipoOp='" + tipoOperacion + '\'' +
                '}';
    }
}
