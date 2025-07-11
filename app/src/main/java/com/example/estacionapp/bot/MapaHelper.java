package com.example.estacionapp.bot;

import android.content.Context;

import androidx.core.content.ContextCompat;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;

public class MapaHelper {

    public static void marcarZona(Context context, MapView mapView, double lon, double lat, String info) {
        Marker marcador = new Marker(mapView);
        marcador.setPosition(new GeoPoint(lat, lon)); // latitud, longitud
        marcador.setTitle("Zona de estacionamiento");
        marcador.setSubDescription(info);
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcador.setIcon(ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.osm_ic_follow_me_on)); // Icono default osmdroid
        mapView.getOverlays().add(marcador);
        mapView.invalidate(); // refrescar el mapa
    }
}
