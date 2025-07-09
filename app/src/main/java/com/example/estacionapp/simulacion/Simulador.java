package com.example.estacionapp.simulacion;

import com.example.estacionapp.modelo.*;

import java.util.ArrayList;
import java.util.List;

public class Simulador {
    private Grafo grafo;
    private List<LugarLibre> lugaresLibres;

    public Simulador() {
        grafo = new Grafo();
        lugaresLibres = new ArrayList<>();
        inicializarMapa();
    }

    private void inicializarMapa() {
        // Crear nodos (calles/esquinas)
        grafo.agregarNodo("A");
        grafo.agregarNodo("B");
        grafo.agregarNodo("C");
        grafo.agregarNodo("D");
        grafo.agregarNodo("E");

        // Crear calles (aristas)
        grafo.agregarArista("A", "B", 2);
        grafo.agregarArista("A", "C", 4);
        grafo.agregarArista("B", "D", 3);
        grafo.agregarArista("C", "D", 1);
        grafo.agregarArista("D", "E", 5);

        // Lugar libre en D
        lugaresLibres.add(new LugarLibre(grafo.obtenerNodo("D")));
        lugaresLibres.add(new LugarLibre(grafo.obtenerNodo("E")));
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public List<LugarLibre> getLugaresLibres() {
        return lugaresLibres;
    }
}
