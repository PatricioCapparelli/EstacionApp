package com.example.estacionapp.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class Geocodificador {

    private static final String TAG = "Geocodificador";

    public static void buscarCodCalle(final String nombreCalle, final CodCalleCallback callback) {
        new AsyncTask<Void, Void, Integer>() {
            private Exception error;

            @Override
            protected Integer doInBackground(Void... voids) {
                try {
                    String urlStr = String.format(Locale.US,
                            "https://apitransporte.buenosaires.gob.ar/transito/v1/calles?client_id=%s&client_secret=%s&nombre=%s&formato=json",
                            "tu_client_id", "tu_client_secret", nombreCalle.replace(" ", "%20"));

                    Log.d(TAG, "URL buscarCodCalle: " + urlStr);

                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    conn.setRequestMethod("GET");

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        error = new Exception("Error de conexión. Código: " + responseCode);
                        return null;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject json = new JSONObject(response.toString());
                    JSONArray calles = json.getJSONArray("calles");

                    if (calles.length() == 0) return null;

                    JSONObject calle = calles.getJSONObject(0);
                    return calle.getInt("cod_calle");

                } catch (Exception e) {
                    error = e;
                    Log.e(TAG, "Error en buscarCodCalle", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Integer codCalle) {
                if (error != null) {
                    callback.onError(error);
                } else {
                    callback.onCodCalleObtenido(codCalle);
                }
            }
        }.execute();
    }
}
