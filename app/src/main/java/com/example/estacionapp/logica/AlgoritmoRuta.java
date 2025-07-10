package com.example.estacionapp.logica;

import com.example.estacionapp.modelo.Nodo;
import com.example.estacionapp.modelo.Grafo;
import java.util.*;

public class AlgoritmoRuta {

    public static List<Nodo> encontrarRutaMasCorta(Grafo grafo, Nodo inicio, Nodo destino) {
        Map<Nodo, Integer> distancias = new HashMap<>();
        Map<Nodo, Nodo> padres = new HashMap<>();
        Set<Nodo> visitados = new HashSet<>();
        PriorityQueue<Nodo> cola = new PriorityQueue<>(Comparator.comparingInt(distancias::get));

        // Inicializar distancias para todos los nodos
        for (Nodo nodo : grafo.obtenerNodos()) {
            distancias.put(nodo, Integer.MAX_VALUE);
        }
        distancias.put(inicio, 0);
        cola.add(inicio);

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            if (visitados.contains(actual)) continue;
            visitados.add(actual);

            if (actual.equals(destino)) break;

            int distanciaActual = distancias.get(actual);

            for (Map.Entry<Nodo, Integer> vecinoEntry : actual.getVecinos().entrySet()) {
                Nodo vecino = vecinoEntry.getKey();
                int peso = vecinoEntry.getValue();
                int nuevaDistancia = distanciaActual + peso;

                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    padres.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        // Reconstruir ruta
        List<Nodo> ruta = new ArrayList<>();
        Nodo actual = destino;
        while (actual != null) {
            ruta.add(actual);
            actual = padres.get(actual);
        }
        Collections.reverse(ruta);

        if (!ruta.isEmpty() && ruta.get(0).equals(inicio)) {
            return ruta;
        } else {
            return Collections.emptyList();
        }
    }
}
