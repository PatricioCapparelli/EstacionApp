package com.example.estacionapp.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Geocodificador {

    public static Integer buscarCodCalle(String nombreCalle) {
        try {
            String url = "https://apitransporte.buenosaires.gob.ar/geocodificacion/v1/callejero?nombre=" +
                    URLEncoder.encode(nombreCalle, "UTF-8");

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            in.close();
            JSONArray calles = new JSONArray(result.toString());

            if (calles.length() > 0) {
                return calles.getJSONObject(0).getInt("cod_calle");
            }

        } catch (Exception e) {
            Log.e("Geocodificador", "Error obteniendo cod_calle", e);
        }

        return null;
    }

    public static GeoPoint geocodificar(int codCalle, int altura) {
        try {
            String url = "https://apitransporte.buenosaires.gob.ar/geocodificacion?" +
                    "cod_calle=" + codCalle +
                    "&altura=" + altura +
                    "&metodo=puertas";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            in.close();
            JSONArray respuesta = new JSONArray(result.toString());

            if (respuesta.length() > 0) {
                JSONObject punto = respuesta.getJSONObject(0);
                double x = punto.getDouble("x");
                double y = punto.getDouble("y");

                // La API del GCBA devuelve x = long, y = lat en WGS84
                return new GeoPoint(y, x);
            }

        } catch (Exception e) {
            Log.e("Geocodificador", "Error geocodificando", e);
        }

        return null;
    }
}
