package modelo;

import java.util.LinkedList;

/**
 * Clase central del sistema de archivos que integra todos los componentes
 */
public class SistemaArchivos {
    private Disco disco;
    private Directorio raiz;
    private Directorio directorioActual;
    private ColaIO colaIO;
    private Planificador planificador;
    private Buffer buffer;
    private LinkedList<Proceso> procesos;
    private int contadorProcesos;
    private int contadorSolicitudes;
    private boolean modoAdministrador;
    private String usuarioActual;

    // Estadísticas
    private int totalOperacionesExitosas;
    private int totalOperacionesFallidas;
    private long tiempoPromedioOperacion;

    public SistemaArchivos(int totalBloques, boolean incluirBuffer) {
        this.disco = new Disco(totalBloques);
        this.raiz = new Directorio("root", "admin", null);
        this.directorioActual = raiz;
        this.colaIO = new ColaIO();
        this.planificador = new Planificador(Planificador.PoliticaplanificacionDisco.FIFO, totalBloques);
        this.buffer = incluirBuffer ? new Buffer(totalBloques / 4) : null;
        this.procesos = new LinkedList<>();
        this.contadorProcesos = 0;
        this.contadorSolicitudes = 0;
        this.modoAdministrador = true;
        this.usuarioActual = "admin";
        this.totalOperacionesExitosas = 0;
        this.totalOperacionesFallidas = 0;
        this.tiempoPromedioOperacion = 0;

        // Crear directorios del sistema
        inicializarDirectoriosSistema();
    }

    /**
     * Inicializa la estructura de directorios del sistema
     */
    private void inicializarDirectoriosSistema() {
        Directorio dirHome = new Directorio("home", "admin", raiz);
        raiz.agregarSubdirectorio(dirHome);

        Directorio dirUsuarios = new Directorio("usuarios", "admin", raiz);
        raiz.agregarSubdirectorio(dirUsuarios);

        Directorio dirSistema = new Directorio("sistema", "admin", raiz);
        raiz.agregarSubdirectorio(dirSistema);
    }

    // ====== OPERACIONES DE DIRECTORIOS ======

    /**
     * Navega a un directorio
     */
    public boolean navegarDirectorio(String nombreDirectorio) {
        if (nombreDirectorio.equals("..")) {
            if (directorioActual.getPadreDirectorio() != null) {
                directorioActual = directorioActual.getPadreDirectorio();
                return true;
            }
            return false;
        }

        Directorio subdirectorio = directorioActual.buscarSubdirectorio(nombreDirectorio);
        if (subdirectorio != null) {
            directorioActual = subdirectorio;
            return true;
        }
        return false;
    }

    /**
     * Crea un nuevo directorio (NO requiere bloques en disco)
     */
    public boolean crearDirectorio(String nombreDirectorio) {
        if (!modoAdministrador) {
            totalOperacionesFallidas++;
            return false;
        }

        if (directorioActual.buscarSubdirectorio(nombreDirectorio) != null) {
            totalOperacionesFallidas++;
            return false;
        }

        Directorio nuevoDirectorio = new Directorio(nombreDirectorio, usuarioActual, directorioActual);
        directorioActual.agregarSubdirectorio(nuevoDirectorio);
        totalOperacionesExitosas++;
        return true;
    }

    /**
     * Elimina un directorio y su contenido
     */
    public boolean eliminarDirectorio(String nombreDirectorio) {
        if (!modoAdministrador) {
            totalOperacionesFallidas++;
            return false;
        }

        Directorio directorio = directorioActual.buscarSubdirectorio(nombreDirectorio);
        if (directorio == null) {
            totalOperacionesFallidas++;
            return false;
        }

        // Liberar todos los bloques de los archivos en este directorio
        LinkedList<Archivo> archivos = directorio.obtenerTodosLosArchivos();
        for (Archivo archivo : archivos) {
            disco.liberarBloques(archivo);
        }

        directorioActual.removerSubdirectorio(directorio);
        totalOperacionesExitosas++;
        return true;
    }

    // ====== OPERACIONES DE ARCHIVOS ======

    /**
     * Crea un archivo en el directorio actual
     */
    public boolean crearArchivo(String nombreArchivo, int tamañoBloques, boolean esPublico) {
        // Verificar permisos
        if (!modoAdministrador) {
            totalOperacionesFallidas++;
            return false;
        }

        // Verificar si ya existe
        if (directorioActual.buscarArchivo(nombreArchivo) != null) {
            totalOperacionesFallidas++;
            return false;
        }

        // Verificar espacio disponible
        if (disco.getBloquesLibres() < tamañoBloques) {
            totalOperacionesFallidas++;
            return false;
        }

        // Crear archivo
        Archivo nuevoArchivo = new Archivo(nombreArchivo, tamañoBloques, usuarioActual, esPublico);

        // Asignar bloques
        if (disco.asignarBloques(nuevoArchivo)) {
            directorioActual.agregarArchivo(nuevoArchivo);

            // Crear solicitud de E/S
            crearSolicitudIO(SolicitudIO.TipoOperacion.CREAR, nuevoArchivo, 
                           disco.obtenerPrimerBloqueLibre());

            totalOperacionesExitosas++;
            return true;
        }

        totalOperacionesFallidas++;
        return false;
    }

    /**
     * Lee un archivo
     */
    public boolean leerArchivo(String nombreArchivo) {
        Archivo archivo = directorioActual.buscarArchivo(nombreArchivo);
        if (archivo == null) {
            totalOperacionesFallidas++;
            return false;
        }

        // Verificar permisos de lectura
        if (!modoAdministrador && !archivo.isEsPublico() && 
            !archivo.getPropietario().equals(usuarioActual)) {
            totalOperacionesFallidas++;
            return false;
        }

        // Crear solicitud de E/S
        crearSolicitudIO(SolicitudIO.TipoOperacion.LEER, archivo, archivo.getPrimerBloque());

        totalOperacionesExitosas++;
        return true;
    }

    /**
     * Actualiza un archivo (renombra)
     */
    public boolean actualizarArchivo(String nombreActual, String nombreNuevo) {
        if (!modoAdministrador) {
            totalOperacionesFallidas++;
            return false;
        }

        Archivo archivo = directorioActual.buscarArchivo(nombreActual);
        if (archivo == null) {
            totalOperacionesFallidas++;
            return false;
        }

        archivo.setNombre(nombreNuevo);

        // Crear solicitud de E/S
        crearSolicitudIO(SolicitudIO.TipoOperacion.ACTUALIZAR, archivo, 
                       archivo.getPrimerBloque());

        totalOperacionesExitosas++;
        return true;
    }

    /**
     * Elimina un archivo
     */
    public boolean eliminarArchivo(String nombreArchivo) {
        if (!modoAdministrador) {
            totalOperacionesFallidas++;
            return false;
        }

        Archivo archivo = directorioActual.buscarArchivo(nombreArchivo);
        if (archivo == null) {
            totalOperacionesFallidas++;
            return false;
        }

        // Liberar bloques
        disco.liberarBloques(archivo);

        // Remover de directorios
        directorioActual.removerArchivo(archivo);

        // Crear solicitud de E/S
        crearSolicitudIO(SolicitudIO.TipoOperacion.ELIMINAR, archivo, -1);

        totalOperacionesExitosas++;
        return true;
    }

    // ====== OPERACIONES DE PROCESOS E I/O ======

    /**
     * Crea una solicitud de E/S
     */
    private void crearSolicitudIO(SolicitudIO.TipoOperacion tipo, Archivo archivo, 
                                 int cilindroAcceso) {
        int idProceso = contadorProcesos++;
        Proceso proceso = new Proceso(idProceso, usuarioActual, tipo.name());
        procesos.add(proceso);

        SolicitudIO solicitud = new SolicitudIO(contadorSolicitudes++, idProceso, tipo, 
                                               archivo, cilindroAcceso);
        solicitud.setTiempoInicio(System.currentTimeMillis());
        colaIO.agregarSolicitud(solicitud);

        proceso.setEstado(Proceso.EstadoProceso.LISTO);
    }

    /**
     * Procesa la siguiente solicitud en la cola
     */
    public void procesarSiguienteSolicitud() {
        if (colaIO.estaVacia()) {
            return;
        }

        planificador.planificar(colaIO);
        SolicitudIO solicitud = colaIO.extraerPrimera();

        if (solicitud != null) {
            solicitud.setTiempoInicio(System.currentTimeMillis());

            // Procesar según tipo de operación
            switch (solicitud.getTipo()) {
                case CREAR:
                    // Ya está creado, solo simulamos E/S
                    break;
                case LEER:
                    // Consultar buffer si existe
                    if (buffer != null) {
                        buffer.buscarBloque(solicitud.getCilindroAcceso());
                    }
                    break;
                case ACTUALIZAR:
                    // Simular actualización
                    break;
                case ELIMINAR:
                    // Ya está eliminado, solo simulamos E/S
                    break;
            }

            solicitud.setTiempoFinalizacion(System.currentTimeMillis());
            solicitud.setCompletada(true);

            // Actualizar proceso
            int idProceso = solicitud.getIdProceso();
            for (Proceso p : procesos) {
                if (p.getIdProceso() == idProceso) {
                    p.setEstado(Proceso.EstadoProceso.TERMINADO);
                    p.setTiempoFinalizacion(System.currentTimeMillis());
                }
            }
        }
    }

    // ====== GETTERS ======

    public Disco getDisco() {
        return disco;
    }

    public Directorio getRaiz() {
        return raiz;
    }

    public Directorio getDirectorioActual() {
        return directorioActual;
    }

    public ColaIO getColaIO() {
        return colaIO;
    }

    public Planificador getPlanificador() {
        return planificador;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public LinkedList<Proceso> getProcesos() {
        return procesos;
    }

    public boolean isModoAdministrador() {
        return modoAdministrador;
    }

    public String getUsuarioActual() {
        return usuarioActual;
    }

    public int getTotalOperacionesExitosas() {
        return totalOperacionesExitosas;
    }

    public int getTotalOperacionesFallidas() {
        return totalOperacionesFallidas;
    }

    // ====== SETTERS ======

    public void setModoAdministrador(boolean admin) {
        this.modoAdministrador = admin;
    }

    public void setUsuarioActual(String usuario) {
        this.usuarioActual = usuario;
    }

    public void setPoliticaPlanificacion(Planificador.PoliticaplanificacionDisco politica) {
        planificador.setPoliticaActual(politica);
    }

    @Override
    public String toString() {
        return "SistemaArchivos{" +
                "usuario=" + usuarioActual +
                ", modo=" + (modoAdministrador ? "ADMIN" : "USER") +
                ", bloquesLibres=" + disco.getBloquesLibres() +
                ", colaE/S=" + colaIO.getTamaño() +
                '}';
    }
}