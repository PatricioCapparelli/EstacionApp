package com.example.estacionapp.modelo;

import org.osmdroid.util.GeoPoint;
import java.util.Map;
import java.util.HashMap;

public class Nodo {
    private String nombre;
    private GeoPoint ubicacion;
    private Map<Nodo, Integer> vecinos;
    private boolean libre = false;

    public Nodo(String nombre, double lat, double lon) {
        this.nombre = nombre;
        this.ubicacion = new GeoPoint(lat, lon);
        this.vecinos = new HashMap<>();
    }

    public String getNombre() {
        return nombre;
    }

    public GeoPoint getUbicacion() {
        return ubicacion;
    }

    public Map<Nodo, Integer> getVecinos() {
        return vecinos;
    }

    public void agregarVecino(Nodo nodo, int distancia) {
        vecinos.put(nodo, distancia);
    }

    public boolean estaLibre() {
        return libre;
    }

    public void setLibre(boolean libre) {
        this.libre = libre;
    }
}
