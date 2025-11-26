/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import gui.VentanaPrincipal;

/**
 *
 * @author Daniel
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Variables básicas
        int numBloques = 256;
        
        // Instanciar el simulador
        VentanaPrincipal gui = new VentanaPrincipal(numBloques);
        gui.setVisible(true);
    }
    
}
