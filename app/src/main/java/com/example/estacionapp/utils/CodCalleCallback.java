package com.example.estacionapp.utils;

public interface CodCalleCallback {
    void onCodCalleObtenido(int codCalle);
    void onResultado(double lat, double lon);
    void onError(Exception e);
}
