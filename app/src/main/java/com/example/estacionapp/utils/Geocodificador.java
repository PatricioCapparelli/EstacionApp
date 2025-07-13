package com.example.estacionapp.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.estacionapp.BuildConfig;

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
                            BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, nombreCalle.replace(" ", "%20"));

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
                    JSONObject calle = json.getJSONArray("calles").getJSONObject(0);
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

    public static void obtenerCoordenadas(final Context context, final String nombreCalle, final int altura, final CodCalleCallback callback) {
        buscarCodCalle(nombreCalle, new CodCalleCallback() {
            @Override
            public void onCodCalleObtenido(int codCalle) {
                new AsyncTask<Void, Void, double[]>() {
                    private Exception error;

                    @Override
                    protected double[] doInBackground(Void... voids) {
                        try {
                            String urlStr = String.format(Locale.US,
                                    "https://apitransporte.buenosaires.gob.ar/geocoder/altura?client_id=%s&client_secret=%s&cod_calle=%d&altura=%d&formato=json&metodo=interpolacion",
                                    BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, codCalle, altura);

                            Log.d(TAG, "URL obtenerCoordenadas: " + urlStr);

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
                            JSONObject coordenadas = json.getJSONArray("coordenadas").getJSONObject(0);
                            double lat = coordenadas.getDouble("y");
                            double lon = coordenadas.getDouble("x");

                            return new double[]{lat, lon};

                        } catch (Exception e) {
                            error = e;
                            Log.e(TAG, "Error en obtenerCoordenadas", e);
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(double[] result) {
                        if (error != null || result == null) {
                            callback.onError(error != null ? error : new Exception("Error obteniendo coordenadas"));
                        } else {
                            callback.onResultado(result[0], result[1]);
                        }
                    }
                }.execute();
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }

            @Override
            public void onResultado(double lat, double lon) {
                // Ignorado en este contexto
            }
        });
    }
}
