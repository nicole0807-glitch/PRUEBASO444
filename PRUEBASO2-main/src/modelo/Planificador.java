package modelo;

import java.util.LinkedList;

/**
 * Clase que implementa el planificador de disco con diferentes políticas
 */
public class Planificador {
    public enum PoliticaplanificacionDisco {
        FIFO, SSTF, SCAN, CSCAN
    }

    private PoliticaplanificacionDisco politicaActual;
    private int posicionCabezal;  // Para SCAN y CSCAN
    private boolean direccionAdelante;  // Para SCAN y CSCAN
    private int totalBloques;

    public Planificador(PoliticaplanificacionDisco politica, int totalBloques) {
        this.politicaActual = politica;
        this.posicionCabezal = 0;
        this.direccionAdelante = true;
        this.totalBloques = totalBloques;
    }

    /**
     * Ordena la cola de solicitudes según la política establecida
     */
    public void planificar(ColaIO cola) {
        if (cola.estaVacia()) {
            return;
        }

        switch (politicaActual) {
            case FIFO:
                // No requiere reorden
                break;
            case SSTF:
                planificarSSTF(cola);
                break;
            case SCAN:
                planificarSCAN(cola);
                break;
            case CSCAN:
                planificarCSCAN(cola);
                break;
        }
    }

    /**
     * FIFO (First In First Out): No hace nada, se atiende en orden de llegada
     */
    private void planificarFIFO(ColaIO cola) {
        // La cola ya está en orden FIFO
    }

    /**
     * SSTF (Shortest Seek Time First): 
     * Atiende la solicitud con el cilindro más cercano
     */
    private void planificarSSTF(ColaIO cola) {
        LinkedList<SolicitudIO> solicitudes = cola.obtenerTodas();
        if (solicitudes.isEmpty()) return;

        // Ordenamiento simple (burbuja adaptado) - O(n²) pero suficiente para el proyecto
        for (int i = 0; i < solicitudes.size(); i++) {
            for (int j = 0; j < solicitudes.size() - 1 - i; j++) {
                int distancia1 = Math.abs(solicitudes.get(j).getCilindroAcceso() - posicionCabezal);
                int distancia2 = Math.abs(solicitudes.get(j + 1).getCilindroAcceso() - posicionCabezal);

                if (distancia1 > distancia2) {
                    // Intercambiar
                    SolicitudIO temp = solicitudes.get(j);
                    solicitudes.set(j, solicitudes.get(j + 1));
                    solicitudes.set(j + 1, temp);
                }
            }
        }

        // Actualizar posición del cabezal
        if (!solicitudes.isEmpty()) {
            posicionCabezal = solicitudes.getFirst().getCilindroAcceso();
        }
    }

    /**
     * SCAN (Barrido del cabezal):
     * El cabezal se mueve en una dirección hasta el final, luego regresa
     */
    private void planificarSCAN(ColaIO cola) {
        LinkedList<SolicitudIO> solicitudes = cola.obtenerTodas();
        if (solicitudes.isEmpty()) return;

        // Separar solicitudes adelante y atrás de la posición actual
        LinkedList<SolicitudIO> adelante = new LinkedList<>();
        LinkedList<SolicitudIO> atras = new LinkedList<>();

        for (SolicitudIO s : solicitudes) {
            if (s.getCilindroAcceso() >= posicionCabezal) {
                adelante.add(s);
            } else {
                atras.add(s);
            }
        }

        // Ordenar ambas listas
        ordenarPorCilindro(adelante);
        ordenarPorCilindro(atras);

        // Limpiar la cola original y reconstruir según la dirección
        solicitudes.clear();

        if (direccionAdelante) {
            solicitudes.addAll(adelante);
            solicitudes.addAll(atras);
            if (!atras.isEmpty()) {
                direccionAdelante = false;
            }
        } else {
            // Invertir atras para ir hacia atrás
            LinkedList<SolicitudIO> atrasInvertida = new LinkedList<>();
            for (int i = atras.size() - 1; i >= 0; i--) {
                atrasInvertida.add(atras.get(i));
            }
            solicitudes.addAll(atrasInvertida);
            solicitudes.addAll(adelante);
            if (!adelante.isEmpty()) {
                direccionAdelante = true;
            }
        }

        // Actualizar posición
        if (!solicitudes.isEmpty()) {
            posicionCabezal = solicitudes.getFirst().getCilindroAcceso();
        }
    }

    /**
     * CSCAN (Circular SCAN): 
     * Similar a SCAN pero solo va en una dirección y regresa al inicio
     */
    private void planificarCSCAN(ColaIO cola) {
        LinkedList<SolicitudIO> solicitudes = cola.obtenerTodas();
        if (solicitudes.isEmpty()) return;

        LinkedList<SolicitudIO> adelante = new LinkedList<>();
        LinkedList<SolicitudIO> atras = new LinkedList<>();

        for (SolicitudIO s : solicitudes) {
            if (s.getCilindroAcceso() >= posicionCabezal) {
                adelante.add(s);
            } else {
                atras.add(s);
            }
        }

        ordenarPorCilindro(adelante);
        ordenarPorCilindro(atras);

        solicitudes.clear();
        solicitudes.addAll(adelante);
        solicitudes.addAll(atras);

        if (!solicitudes.isEmpty()) {
            posicionCabezal = solicitudes.getFirst().getCilindroAcceso();
        }
    }

    /**
     * Ordena una lista de solicitudes por número de cilindro
     */
    private void ordenarPorCilindro(LinkedList<SolicitudIO> lista) {
        for (int i = 0; i < lista.size(); i++) {
            for (int j = 0; j < lista.size() - 1 - i; j++) {
                if (lista.get(j).getCilindroAcceso() > lista.get(j + 1).getCilindroAcceso()) {
                    SolicitudIO temp = lista.get(j);
                    lista.set(j, lista.get(j + 1));
                    lista.set(j + 1, temp);
                }
            }
        }
    }

    // Getters y Setters
    public PoliticaplanificacionDisco getPoliticaActual() {
        return politicaActual;
    }

    public void setPoliticaActual(PoliticaplanificacionDisco politica) {
        this.politicaActual = politica;
        this.posicionCabezal = 0;
        this.direccionAdelante = true;
    }

    public int getPosicionCabezal() {
        return posicionCabezal;
    }

    public void setPosicionCabezal(int posicion) {
        this.posicionCabezal = posicion;
    }

    @Override
    public String toString() {
        return "Planificador{" +
                "politica=" + politicaActual +
                ", posicionCabezal=" + posicionCabezal +
                '}';
    }
}
