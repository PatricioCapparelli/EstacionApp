package com.example.estacionapp.bot;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.estacionapp.BuildConfig;
import com.example.estacionapp.R;
import com.example.estacionapp.util.Geocodificador;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EstacionamientoBot {

    public static void sugerirEstacionamiento(Context context, Location ubicacion, MapView mapView) {
        double x = ubicacion.getLongitude();
        double y = ubicacion.getLatitude();
        new ConsultaEstacionamientosTask(context, mapView).execute(x, y);
    }

    private static class ConsultaEstacionamientosTask extends AsyncTask<Double, Void, JSONArray> {

        private final Context context;
        private final MapView mapView;

        public ConsultaEstacionamientosTask(Context context, MapView mapView) {
            this.context = context;
            this.mapView = mapView;
        }

        @Override
        protected JSONArray doInBackground(Double... coords) {

            try {

                String clientId = BuildConfig.CLIENT_ID;
                String clientSecret = BuildConfig.CLIENT_SECRET;

                double x = coords[0];
                double y = coords[1];

                String url = "https://apitransporte.buenosaires.gob.ar/transito/v1/estacionamientos" +
                        "?client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&x=" + x +
                        "&y=" + y +
                        "&srid=4326" +
                        "&radio=100" +
                        "&orden=distancia" +
                        "&limite=10" +
                        "&formato=json" +
                        "&fullInfo=true";

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder resultado = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    resultado.append(line);
                }
                in.close();

                JSONObject json = new JSONObject(resultado.toString());
                return json.getJSONArray("instancias");

            } catch (Exception e) {
                Log.e("EstacionamientoBot", "Error al consultar la API", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray instancias) {
            if (instancias == null || instancias.length() == 0) {
                Log.d("EstacionamientoBot", "No se recibieron instancias de la API");
                return;
            }

            Log.d("EstacionamientoBot", "Cantidad de instancias recibidas: " + instancias.length());

            for (int i = 0; i < instancias.length(); i++) {
                try {
                    JSONObject instancia = instancias.getJSONObject(i);
                    JSONObject contenidoObj = instancia.getJSONObject("contenido");
                    JSONArray contenido = contenidoObj.getJSONArray("contenido");

                    String permiso = "", calle = "", altura = "", lado = "", horario = "";

                    for (int j = 0; j < contenido.length(); j++) {
                        JSONObject campo = contenido.getJSONObject(j);
                        String nombreId = campo.getString("nombreId");
                        String valor = campo.getString("valor");

                        switch (nombreId) {
                            case "permiso": permiso = valor; break;
                            case "calle": calle = valor; break;
                            case "altura": altura = valor; break;
                            case "lado": lado = valor; break;
                            case "horario": horario = valor; break;
                        }
                    }

                    Log.d("EstacionamientoBot", "Procesando: " + permiso + " en " + calle + " " + altura);

                    Integer codCalle = Geocodificador.buscarCodCalle(calle);
                    if (codCalle != null && altura.contains("-")) {
                        int alturaEntera = Integer.parseInt(altura.split("-")[0]);
                        GeoPoint punto = Geocodificador.geocodificar(codCalle, alturaEntera);

                        if (punto != null) {
                            Marker marker = new Marker(mapView);
                            marker.setPosition(punto);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setPanToView(true);
                            marker.setTitle(calle + " " + altura);
                            marker.setSubDescription(lado + ", " + horario);

                            if (permiso.toLowerCase().contains("permitido")) {
                                marker.setIcon(context.getResources().getDrawable(R.drawable.permitido));
                            } else {
                                marker.setIcon(context.getResources().getDrawable(R.drawable.prohibido));
                            }

                            mapView.getOverlays().add(marker);
                            mapView.invalidate();
                            Log.d("EstacionamientoBot", "Marker agregado en mapa para " + calle + " " + altura);
                        } else {
                            Log.d("EstacionamientoBot", "No se pudo geocodificar " + calle + " " + altura);
                        }
                    } else {
                        Log.d("EstacionamientoBot", "Datos insuficientes para geocodificar: " + calle + ", " + altura);
                    }

                } catch (Exception e) {
                    Log.e("EstacionamientoBot", "Error procesando instancia", e);
                }
            }
        }
    }
}
