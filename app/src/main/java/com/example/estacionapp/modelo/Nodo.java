package com.example.estacionapp.modelo;

import java.util.HashMap;
import java.util.Map;

public class Nodo {
    private String nombre;
    private Map<Nodo, Integer> vecinos; // calles conectadas/distancia

    public Nodo(String nombre) {
        this.nombre = nombre;
        this.vecinos = new HashMap<>();
    }

    public String getNombre() {
        return nombre;
    }

    public Map<Nodo, Integer> getVecinos() {
        return vecinos;
    }

    public void agregarVecino(Nodo destino, int distancia) {
        vecinos.put(destino, distancia);
    }

    public String toString(){
        return nombre;
    }
}