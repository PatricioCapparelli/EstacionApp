package com.example.estacionapp.logica;

import com.example.estacionapp.modelo.Grafo;
import com.example.estacionapp.modelo.Nodo;

import java.util.*;

public class AlgoritmoRuta {

    public static List<Nodo> encontrarRutaMasCorta(Grafo grafo, Nodo inicio, Nodo destino) {

        // === 1. Inicializar estructuras ===
        Map<Nodo, Integer> distancias = new HashMap<>();
        Map<Nodo, Nodo> previos     = new HashMap<>();

        for (Nodo n : grafo.obtenerNodos()) {        // <── usamos TODOS los nodos
            distancias.put(n, Integer.MAX_VALUE);
        }
        distancias.put(inicio, 0);

        PriorityQueue<Nodo> cola = new PriorityQueue<>(Comparator.comparingInt(distancias::get));
        Set<Nodo> visitados = new HashSet<>();
        cola.add(inicio);

        // === 2. Dijkstra estándar ===
        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            if (!visitados.add(actual)) continue;

            for (Map.Entry<Nodo, Integer> e : actual.getVecinos().entrySet()) {
                Nodo vecino  = e.getKey();
                int  peso    = e.getValue();

                int nuevaDist = distancias.get(actual) + peso;
                if (nuevaDist < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDist);
                    previos.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        // === 3. Reconstruir ruta ===
        List<Nodo> ruta = new ArrayList<>();
        for (Nodo at = destino; at != null; at = previos.get(at)) {
            ruta.add(at);
        }
        Collections.reverse(ruta);
        return ruta;
    }
}