package com.example.estacionapp.modelo;

public class LugarLibre {
    private Nodo ubicacion;

    public LugarLibre(Nodo ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Nodo getUbicacion() {
        return ubicacion;
    }

    public String toString() {
        return "Lugar libre en: " + ubicacion.getNombre();
    }
}