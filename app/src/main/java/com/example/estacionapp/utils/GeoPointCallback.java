package com.example.estacionapp.utils;

import org.osmdroid.util.GeoPoint;

public interface GeoPointCallback {
    void onSuccess(GeoPoint punto);
    void onError(Exception e);
}
