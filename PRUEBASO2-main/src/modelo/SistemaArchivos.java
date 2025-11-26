package modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Clase central del sistema de archivos que integra todos los componentes
 */
public class SistemaArchivos {
    public enum Modo { ADMIN, USUARIO }

    private Disco disco;
    private Directorio raiz;
    private Directorio directorioActual;
    private ColaIO colaIO;
    private Planificador planificador;
    private Buffer buffer;
    private LinkedList<Proceso> procesos;
    private int contadorProcesos;
    private int contadorSolicitudes;
    private Modo modoActual;
    private String usuarioActual;

    // Estadísticas
    private int totalOperacionesExitosas;
    private int totalOperacionesFallidas;
    private long tiempoPromedioOperacion;

    public SistemaArchivos(int totalBloques, boolean incluirBuffer) {
        this.disco = new Disco(totalBloques);
        this.raiz = new Directorio("root", "admin", null, true);
        this.directorioActual = raiz;
        this.colaIO = new ColaIO();
        this.planificador = new Planificador(Planificador.PoliticaplanificacionDisco.FIFO, totalBloques);
        this.buffer = incluirBuffer ? new Buffer(totalBloques / 4) : null;
        this.procesos = new LinkedList<>();
        this.contadorProcesos = 0;
        this.contadorSolicitudes = 0;
        this.modoActual = Modo.ADMIN;
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
        Directorio dirHome = new Directorio("home", "admin", raiz, true);
        raiz.agregarSubdirectorio(dirHome);

        Directorio dirUsuarios = new Directorio("usuarios", "admin", raiz, true);
        raiz.agregarSubdirectorio(dirUsuarios);

        Directorio dirSistema = new Directorio("sistema", "admin", raiz, true);
        raiz.agregarSubdirectorio(dirSistema);
    }

    private boolean esAdmin() {
        return modoActual == Modo.ADMIN;
    }

    /**
     * Indica si el archivo puede ser visualizado por el usuario/modo actual.
     */
    public boolean puedeVerArchivo(Archivo archivo) {
        return archivo != null && (esAdmin()
            || archivo.isEsPublico()
            || usuarioActual.equals(archivo.getPropietario()));
    }

    /**
     * Indica si el directorio puede mostrarse al usuario/modo actual.
     */
    public boolean puedeVerDirectorio(Directorio directorio) {
        if (directorio == null) {
            return false;
        }
        if (directorio == raiz) {
            return true;
        }
        return esAdmin() || directorio.isEsPublico()
            || usuarioActual.equals(directorio.getPropietario());
    }

    /**
     * Verifica si se puede operar sobre un archivo (leer/escribir/eliminar).
     */
    public boolean puedeOperarArchivo(Archivo archivo) {
        return puedeVerArchivo(archivo);
    }

    /**
     * Verifica si el usuario puede operar dentro de un directorio.
     */
    public boolean puedeOperarEnDirectorio(Directorio directorio) {
        if (directorio == null) {
            return false;
        }
        return esAdmin() || usuarioActual.equals(directorio.getPropietario())
            || directorio.isEsPublico();
    }

    /**
     * Devuelve los archivos visibles según permisos del modo actual.
     */
    public LinkedList<Archivo> obtenerArchivosVisibles() {
        LinkedList<Archivo> visibles = new LinkedList<>();
        for (Archivo archivo : raiz.obtenerTodosLosArchivos()) {
            if (puedeVerArchivo(archivo)) {
                visibles.add(archivo);
            }
        }
        return visibles;
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
        if (!puedeOperarEnDirectorio(directorioActual)) {
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
        if (!esAdmin()) {
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
        if (!puedeOperarEnDirectorio(directorioActual)) {
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
        if (!puedeOperarArchivo(archivo)) {
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
        Archivo archivo = directorioActual.buscarArchivo(nombreActual);
        if (archivo == null) {
            totalOperacionesFallidas++;
            return false;
        }

        if (!puedeOperarArchivo(archivo)) {
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
        String rutaActual = obtenerRuta(directorioActual);
        if (!"/".equals(rutaActual)) {
            rutaActual += "/";
        }
        return eliminarArchivoPorRuta(rutaActual + nombreArchivo);
    }

    /**
     * Elimina un archivo a partir de su ruta absoluta.
     */
    public boolean eliminarArchivoPorRuta(String rutaCompleta) {
        if (rutaCompleta == null || rutaCompleta.isEmpty()) {
            totalOperacionesFallidas++;
            return false;
        }

        String rutaNormalizada = rutaCompleta.startsWith("/")
            ? rutaCompleta
            : "/" + rutaCompleta;
        int ultimaBarra = rutaNormalizada.lastIndexOf('/') >= 0 ? rutaNormalizada.lastIndexOf('/') : 0;
        String rutaDirectorio = rutaNormalizada.substring(0, ultimaBarra > 0 ? ultimaBarra : 1);
        String nombreArchivo = rutaNormalizada.substring(ultimaBarra + 1);

        Directorio directorioDestino = obtenerDirectorioPorRuta(rutaDirectorio);
        if (directorioDestino == null) {
            totalOperacionesFallidas++;
            return false;
        }

        Archivo archivo = directorioDestino.buscarArchivo(nombreArchivo);
        if (archivo == null) {
            totalOperacionesFallidas++;
            return false;
        }

        if (!puedeOperarArchivo(archivo)) {
            totalOperacionesFallidas++;
            return false;
        }

        // Liberar bloques
        disco.liberarBloques(archivo);

        // Remover de directorios
        directorioDestino.removerArchivo(archivo);

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
        if (!esAdmin() && archivo != null && !puedeOperarArchivo(archivo)) {
            totalOperacionesFallidas++;
            return;
        }
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

    /**
     * Guarda una representación simple del estado actual del sistema en un archivo de texto.
     */
    public void guardarEstado(File archivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write("# Estado del sistema de archivos");
            writer.newLine();

            escribirEstadoDisco(writer);
            writer.newLine();
            writer.write("[DIRECTORIOS]");
            writer.newLine();
            guardarDirectorios(writer, raiz);
            writer.newLine();
            writer.write("[ARCHIVOS]");
            writer.newLine();
            guardarArchivos(writer, raiz);
        }
    }

    /**
     * Carga el estado del sistema desde un archivo previamente guardado.
     */
    public void cargarEstado(File archivo) throws IOException {
        List<String> directoriosLeidos = new ArrayList<>();
        List<String> archivosLeidos = new ArrayList<>();
        List<String> bloquesLeidos = new ArrayList<>();
        Map<Integer, Integer> mapaProximos = new HashMap<>();

        Planificador.PoliticaplanificacionDisco politicaLeida = null;
        int totalBloquesArchivo = -1;
        String seccion = "";
        boolean leyendoBloques = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                if (linea.startsWith("[")) {
                    seccion = linea;
                    leyendoBloques = false;
                    continue;
                }

                switch (seccion) {
                    case "[DISCO]":
                        if ("bloques=".equalsIgnoreCase(linea)) {
                            leyendoBloques = true;
                            continue;
                        }

                        if (leyendoBloques) {
                            bloquesLeidos.add(linea);
                        } else if (linea.startsWith("totalBloques=")) {
                            totalBloquesArchivo = Integer.parseInt(linea.split("=")[1]);
                        } else if (linea.startsWith("politica=")) {
                            String valor = linea.split("=")[1];
                            try {
                                politicaLeida = Planificador.PoliticaplanificacionDisco.valueOf(valor);
                            } catch (IllegalArgumentException ignored) {
                            }
                        } else if (linea.startsWith("bloquesLibres=") || linea.startsWith("bloquesOcupados=")
                                || linea.startsWith("porcentajeOcupacion=")) {
                            // Datos derivados, no necesarios para reconstruir
                        }
                        break;
                    case "[DIRECTORIOS]":
                        directoriosLeidos.add(linea);
                        break;
                    case "[ARCHIVOS]":
                        archivosLeidos.add(linea);
                        break;
                    default:
                        break;
                }
            }
        }

        if (totalBloquesArchivo <= 0) {
            throw new IOException("Formato de archivo inválido: total de bloques no encontrado");
        }

        // Reconstruir disco y estructura base
        this.disco = new Disco(totalBloquesArchivo);
        Planificador.PoliticaplanificacionDisco politicaActual = politicaLeida != null
            ? politicaLeida
            : planificador != null
                ? planificador.getPoliticaActual()
                : Planificador.PoliticaplanificacionDisco.FIFO;
        this.planificador = new Planificador(politicaActual, totalBloquesArchivo);
        this.buffer = buffer != null ? new Buffer(totalBloquesArchivo / 4) : null;
        this.raiz = new Directorio("root", "admin", null, true);
        this.directorioActual = raiz;
        this.colaIO = new ColaIO();
        this.procesos = new LinkedList<>();
        this.contadorProcesos = 0;
        this.contadorSolicitudes = 0;
        this.totalOperacionesExitosas = 0;
        this.totalOperacionesFallidas = 0;

        // Construir mapa de proximos para los bloques
        for (String bloqueLinea : bloquesLeidos) {
            String[] partes = bloqueLinea.split(";");
            if (partes.length >= 4) {
                try {
                    int indice = Integer.parseInt(partes[0]);
                    int siguiente = Integer.parseInt(partes[3]);
                    mapaProximos.put(indice, siguiente);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Reconstruir directorios
        for (String directorioLinea : directoriosLeidos) {
            if (directorioLinea == null || directorioLinea.isEmpty()) {
                continue;
            }
            String[] partesDir = directorioLinea.split(";");
            String rutaDirectorio = partesDir.length > 0 ? partesDir[0] : "/";
            if ("/".equals(rutaDirectorio)) {
                continue;
            }
            String propietarioDir = partesDir.length > 1 ? partesDir[1] : "admin";
            boolean publicoDir = partesDir.length > 2 ? Boolean.parseBoolean(partesDir[2]) : true;
            obtenerOCrearDirectorio(rutaDirectorio, propietarioDir, publicoDir);
        }

        // Reconstruir archivos
        for (String lineaArchivo : archivosLeidos) {
            String[] partes = lineaArchivo.split(";");
            if (partes.length < 2) {
                continue;
            }

            String rutaArchivo = partes[0];
            int tamaño = Integer.parseInt(partes[1]);
            String propietarioArchivo = partes.length >= 4 ? partes[3] : "admin";
            boolean publicoArchivo = partes.length >= 5 ? Boolean.parseBoolean(partes[4]) : true;
            LinkedList<Integer> bloquesArchivo = new LinkedList<>();
            if (partes.length >= 3 && !partes[2].isEmpty()) {
                String[] bloquesStr = partes[2].split(",");
                for (String b : bloquesStr) {
                    try {
                        bloquesArchivo.add(Integer.parseInt(b));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            int ultimaBarra = rutaArchivo.lastIndexOf('/') >= 0 ? rutaArchivo.lastIndexOf('/') : 0;
            String rutaDirectorio = rutaArchivo.substring(0, ultimaBarra > 0 ? ultimaBarra : 1);
            String nombreArchivo = rutaArchivo.substring(ultimaBarra + 1);

            Directorio directorioDestino = obtenerOCrearDirectorio(rutaDirectorio, propietarioArchivo, true);
            Archivo archivoCargado = new Archivo(nombreArchivo, tamaño, propietarioArchivo, publicoArchivo);
            directorioDestino.agregarArchivo(archivoCargado);

            for (int i = 0; i < bloquesArchivo.size(); i++) {
                int bloqueNum = bloquesArchivo.get(i);
                archivoCargado.agregarBloque(bloqueNum);
                Bloque bloque = disco.getBloque(bloqueNum);
                if (bloque != null) {
                    bloque.setOcupado(true);
                    bloque.setPropietario(archivoCargado.getNombre());
                    bloque.setColorAsignado(archivoCargado.getColorAsignado());
                    int siguiente = mapaProximos.getOrDefault(bloqueNum, -1);
                    bloque.setBloqueProximo(siguiente);
                }
            }
        }

        // Recalcular estado del disco
        disco.getBloquesLibres();
    }

    /**
     * Registra las métricas del disco y el estado de cada bloque.
     */
    private void escribirEstadoDisco(BufferedWriter writer) throws IOException {
        writer.write("[DISCO]");
        writer.newLine();
        writer.write("totalBloques=" + disco.getTotalBloques());
        writer.newLine();
        writer.write("politica=" + planificador.getPoliticaActual());
        writer.newLine();
        writer.write("bloquesOcupados=" + disco.getBloquesOcupados());
        writer.newLine();
        writer.write("bloquesLibres=" + disco.getBloquesLibres());
        writer.newLine();
        writer.write("porcentajeOcupacion=" + String.format(Locale.US, "%.2f", disco.getPercentajeOcupacion()));
        writer.newLine();
        writer.write("bloques=");
        writer.newLine();
        for (int i = 0; i < disco.getTotalBloques(); i++) {
            Bloque bloque = disco.getBloque(i);
            String estado = bloque.isOcupado() ? "OCUPADO" : "LIBRE";
            String propietario = bloque.isOcupado() ? bloque.getPropietario() : "-";
            writer.write(i + ";" + estado + ";" + propietario + ";" + bloque.getBloqueProximo());
            writer.newLine();
        }
    }

    /**
     * Escribe la jerarquía de directorios empezando por la raíz.
     */
    private void guardarDirectorios(BufferedWriter writer, Directorio directorio) throws IOException {
        writer.write(obtenerRuta(directorio) + ";" + directorio.getPropietario()
            + ";" + directorio.isEsPublico());
        writer.newLine();
        for (Directorio sub : directorio.getSubdirectorios()) {
            guardarDirectorios(writer, sub);
        }
    }

    /**
     * Registra los archivos con su ruta completa, tamaño y bloques asignados.
     */
    private void guardarArchivos(BufferedWriter writer, Directorio directorio) throws IOException {
        for (Archivo archivo : directorio.getArchivos()) {
            String rutaDirectorio = obtenerRuta(directorio);
            String rutaArchivo = "/".equals(rutaDirectorio)
                ? rutaDirectorio + archivo.getNombre()
                : rutaDirectorio + "/" + archivo.getNombre();

            StringBuilder bloquesAsignados = new StringBuilder();
            LinkedList<Integer> bloques = archivo.getBloques();
            for (int i = 0; i < bloques.size(); i++) {
                bloquesAsignados.append(bloques.get(i));
                if (i < bloques.size() - 1) {
                    bloquesAsignados.append(",");
                }
            }

            writer.write(rutaArchivo + ";" + archivo.getTamañoBloques() + ";" + bloquesAsignados);
            writer.write(";" + archivo.getPropietario() + ";" + archivo.isEsPublico());
            writer.newLine();
        }

        for (Directorio sub : directorio.getSubdirectorios()) {
            guardarArchivos(writer, sub);
        }
    }

    /**
     * Construye la ruta absoluta de un directorio dentro de la jerarquía.
     */
    private String obtenerRuta(Directorio directorio) {
        if (directorio.getPadreDirectorio() == null) {
            return "/";
        }

        LinkedList<String> partes = new LinkedList<>();
        Directorio actual = directorio;
        while (actual != null && actual.getPadreDirectorio() != null) {
            partes.addFirst(actual.getNombre());
            actual = actual.getPadreDirectorio();
        }

        StringBuilder ruta = new StringBuilder("/");
        for (int i = 0; i < partes.size(); i++) {
            ruta.append(partes.get(i));
            if (i < partes.size() - 1) {
                ruta.append("/");
            }
        }
        return ruta.toString();
    }

    private Directorio obtenerDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || "/".equals(ruta)) {
            return raiz;
        }

        String[] partes = ruta.split("/");
        Directorio actual = raiz;
        for (String nombre : partes) {
            if (nombre.isEmpty()) {
                continue;
            }
            Directorio siguiente = actual.buscarSubdirectorio(nombre);
            if (siguiente == null) {
                return null;
            }
            actual = siguiente;
        }
        return actual;
    }

    /**
     * Busca o crea los directorios necesarios para alcanzar la ruta indicada.
     */
    private Directorio obtenerOCrearDirectorio(String ruta) {
        return obtenerOCrearDirectorio(ruta, "admin", true);
    }

    private Directorio obtenerOCrearDirectorio(String ruta, String propietario, boolean publico) {
        if (ruta == null || ruta.isEmpty() || "/".equals(ruta)) {
            raiz.setEsPublico(true);
            return raiz;
        }

        String[] partes = ruta.split("/");
        Directorio actual = raiz;
        for (String nombre : partes) {
            if (nombre.isEmpty()) {
                continue;
            }
            Directorio existente = actual.buscarSubdirectorio(nombre);
            if (existente == null) {
                existente = new Directorio(nombre, propietario, actual, publico);
                actual.agregarSubdirectorio(existente);
            }
            actual = existente;
        }
        actual.setEsPublico(publico);
        return actual;
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
        return esAdmin();
    }

    public Modo getModoActual() {
        return modoActual;
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
        this.modoActual = admin ? Modo.ADMIN : Modo.USUARIO;
    }

    public void setModoActual(Modo modo) {
        this.modoActual = modo;
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
                ", modo=" + (esAdmin() ? "ADMIN" : "USER") +
                ", bloquesLibres=" + disco.getBloquesLibres() +
                ", colaE/S=" + colaIO.getTamaño() +
                '}';
    }
}