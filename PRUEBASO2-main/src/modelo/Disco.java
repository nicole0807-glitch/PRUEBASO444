package modelo;

import java.util.LinkedList;

/**
 * Clase que simula el disco duro con asignación encadenada de bloques
 */
public class Disco {
    private Bloque[] bloques;
    private int totalBloques;
    private int bloquesOcupados;
    private int bloquesLibres;

    public Disco(int totalBloques) {
        this.totalBloques = totalBloques;
        this.bloques = new Bloque[totalBloques];
        this.bloquesOcupados = 0;
        this.bloquesLibres = totalBloques;

        // Inicializar todos los bloques
        for (int i = 0; i < totalBloques; i++) {
            bloques[i] = new Bloque(i);
        }
    }

    /**
     * Asigna bloques continuos o en forma encadenada para un archivo
     */
    public boolean asignarBloques(Archivo archivo) {
        int cantidadBloques = archivo.getTamañoBloques();

        // Verificar si hay suficientes bloques libres
        if (bloquesLibres < cantidadBloques) {
            return false;
        }

        int contadorAsignados = 0;
        int ultimoBloqueAsignado = -1;

        // Buscar bloques libres y asignarlos
        for (int i = 0; i < totalBloques && contadorAsignados < cantidadBloques; i++) {
            if (!bloques[i].isOcupado()) {
                bloques[i].setOcupado(true);
                bloques[i].setPropietario(archivo.getNombre());
                bloques[i].setColorAsignado(archivo.getColorAsignado());

                archivo.agregarBloque(i);

                // Encadenar los bloques
                if (ultimoBloqueAsignado != -1) {
                    bloques[ultimoBloqueAsignado].setBloqueProximo(i);
                } else {
                    // Primer bloque
                    archivo.setPrimerBloque(i);
                }

                ultimoBloqueAsignado = i;
                contadorAsignados++;
            }
        }

        if (contadorAsignados == cantidadBloques) {
            bloquesOcupados += cantidadBloques;
            bloquesLibres -= cantidadBloques;
            return true;
        }

        return false;
    }

    /**
     * Libera los bloques asignados a un archivo
     */
    public void liberarBloques(Archivo archivo) {
        LinkedList<Integer> bloquesDelArchivo = archivo.getBloques();

        for (Integer numBloque : bloquesDelArchivo) {
            if (numBloque >= 0 && numBloque < totalBloques) {
                bloques[numBloque].liberar();
            }
        }

        int cantidadLiberada = bloquesDelArchivo.size();
        bloquesOcupados -= cantidadLiberada;
        bloquesLibres += cantidadLiberada;

        // Limpiar la lista de bloques del archivo
        bloquesDelArchivo.clear();
        archivo.setPrimerBloque(-1);
    }

    /**
     * Obtiene el primer bloque libre
     */
    public int obtenerPrimerBloqueLibre() {
        for (int i = 0; i < totalBloques; i++) {
            if (!bloques[i].isOcupado()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Obtiene la cantidad de bloques libres consecutivos a partir de una posición
     */
    public int obtenerBloquesLibresConsecutivos(int inicio) {
        int contador = 0;
        for (int i = inicio; i < totalBloques; i++) {
            if (!bloques[i].isOcupado()) {
                contador++;
            } else {
                break;
            }
        }
        return contador;
    }

    // Getters
    public Bloque[] getBloques() {
        return bloques;
    }

    public int getTotalBloques() {
        return totalBloques;
    }

    public int getBloquesOcupados() {
        recalcularEstado();
        return bloquesOcupados;
    }

    public int getBloquesLibres() {
        recalcularEstado();
        return bloquesLibres;
    }

    public Bloque getBloque(int numero) {
        if (numero >= 0 && numero < totalBloques) {
            return bloques[numero];
        }
        return null;
    }

    /**
     * Verifica si un bloque específico está ocupado
     */
    public boolean estaBloqueOcupado(int numero) {
        return bloques[numero].isOcupado();
    }

    /**
     * Calcula el porcentaje de ocupación del disco
     */
    public double getPercentajeOcupacion() {
        recalcularEstado();
        return (double) bloquesOcupados / totalBloques * 100;
    }

    private void recalcularEstado() {
        int ocupados = 0;
        for (Bloque bloque : bloques) {
            if (bloque.isOcupado()) {
                ocupados++;
            }
        }

        bloquesOcupados = ocupados;
        bloquesLibres = totalBloques - ocupados;
    }

    @Override
    public String toString() {
        return "Disco{" +
                "totalBloques=" + totalBloques +
                ", bloquesOcupados=" + bloquesOcupados +
                ", bloquesLibres=" + bloquesLibres +
                ", porcentajeOcupacion=" + String.format("%.2f%%", getPercentajeOcupacion()) +
                '}';
    }
}
