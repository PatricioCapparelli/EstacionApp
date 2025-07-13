package com.example.estacionapp.bot;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.estacionapp.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EstacionamientoBot {
    private static final String TAG = "EstacionamientoBot";
    private Context context;
    private MapView mapView;

    public EstacionamientoBot(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
    }

    public void buscarEstacionamientosCercanos(double lat, double lon) {
        new ConsultaEstacionamientosTask().execute(lat, lon);
    }

    private class ConsultaEstacionamientosTask extends AsyncTask<Double, Void, String> {

        @Override
        protected String doInBackground(Double... params) {
            double lat = params[0];
            double lon = params[1];
            String clientId = BuildConfig.CLIENT_ID;
            String clientSecret = BuildConfig.CLIENT_SECRET;

            String apiUrl = String.format(
                    "https://apitransporte.buenosaires.gob.ar/transito/v1/estacionamientos" +
                            "?client_id=%s&client_secret=%s&x=%.6f&y=%.6f&srid=4326&radio=100&orden=distancia&limite=10&formato=json&fullInfo=true",
                    clientId, clientSecret, lon, lat
            );

            Log.d(TAG, "Consultando URL: " + apiUrl);

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Código de respuesta HTTP: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                } else {
                    Log.e(TAG, "Error HTTP: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Excepción al consultar API", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Log.e(TAG, "No se recibió respuesta");
                return;
            }

            try {
                JSONObject json = new JSONObject(result);

                if (!json.has("instancias")) {
                    Log.e(TAG, "No se encontraron instancias en el JSON");
                    return;
                }

                JSONArray instancias = json.getJSONArray("instancias");
                Log.d(TAG, "Cantidad de instancias: " + instancias.length());

                for (int i = 0; i < instancias.length(); i++) {
                    JSONObject instancia = instancias.getJSONObject(i);
                    String nombre = instancia.optString("nombre", "sin nombre");
                    Log.d(TAG, "Instancia " + (i+1) + ": " + nombre);

                    JSONObject contenido = instancia.optJSONObject("contenido");
                    if (contenido != null && contenido.has("contenido")) {
                        JSONArray contenidoArray = contenido.getJSONArray("contenido");

                        for (int j = 0; j < contenidoArray.length(); j++) {
                            JSONObject campo = contenidoArray.getJSONObject(j);
                            String clave = campo.optString("nombreId");
                            String valor = campo.optString("valor");
                            Log.d(TAG, "   " + clave + ": " + valor);
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error parseando JSON", e);
            }
        }
    }
}
