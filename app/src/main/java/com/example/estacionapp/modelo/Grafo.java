package com.example.estacionapp.modelo;

import java.util.*;

public class Grafo {
    private Map<String, Nodo> nodos;

    public Grafo() {
        nodos = new HashMap<>();
    }

    // Crear nodo con coordenadas reales
    public Nodo agregarNodo(String nombre, double lat, double lon) {
        Nodo nodo = new Nodo(nombre, lat, lon);
        nodos.put(nombre, nodo);
        return nodo;
    }

    public void agregarArista(String origen, String destino, int distancia) {
        Nodo nodoOrigen = nodos.get(origen);
        Nodo nodoDestino = nodos.get(destino);

        if (nodoOrigen != null && nodoDestino != null) {
            nodoOrigen.agregarVecino(nodoDestino, distancia);
            nodoDestino.agregarVecino(nodoOrigen, distancia); // Bidireccional
        }
    }

    public Nodo obtenerNodo(String nombre) {
        return nodos.get(nombre);
    }

    public Collection<Nodo> obtenerNodos() {
        return nodos.values();
    }
}
