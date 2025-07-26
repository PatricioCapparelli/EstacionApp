package com.example.estacionapp.bot;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.estacionapp.BuildConfig;
import com.example.estacionapp.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
                Log.e(TAG, "No se recibió respuesta de la API de estacionamientos");
                mostrarToast("Error al consultar estacionamientos");
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                JSONArray instancias = json.optJSONArray("instancias");

                if (instancias == null || instancias.length() == 0) {
                    Log.d(TAG, "No se encontraron estacionamientos cercanos");
                    mostrarToast("No hay estacionamientos cercanos");
                    return;
                }

                Log.d(TAG, "Cantidad de estacionamientos encontrados: " + instancias.length());
                enviarJSONaServidorMCP(json.toString());

            } catch (Exception e) {
                Log.e(TAG, "Error parseando JSON", e);
                mostrarToast("Error procesando datos");
            }
        }
    }

    private void enviarJSONaServidorMCP(String jsonOriginal) {
        new Thread(() -> {
            try {
                // URL para emulador (10.0.2.2) o dispositivo físico (IP de tu PC)
                URL url = new URL("http://10.0.2.2:3000/mcp-tool/analizar-estacionamiento");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Estructura correcta del JSON
                JSONObject requestBody = new JSONObject();
                JSONObject input = new JSONObject();
                input.put("data", new JSONObject(jsonOriginal));
                requestBody.put("input", input);

                Log.d(TAG, "Enviando JSON al servidor MCP: " + requestBody.toString());

                // Enviar datos
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] inputBytes = requestBody.toString().getBytes("utf-8");
                    os.write(inputBytes, 0, inputBytes.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Código de respuesta MCP Server: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String respuestaIA = jsonResponse.getJSONArray("content")
                            .getJSONObject(0)
                            .getString("text");

                    mostrarToast("Análisis de estacionamiento:\n" + respuestaIA);
                } else {
                    mostrarToast("Error del servidor: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al enviar JSON a MCP Server", e);
                mostrarToast("Error de conexión con el servidor");
            }
        }).start();
    }

    private void mostrarToast(final String mensaje) {
        if (context != null) {
            ((MainActivity) context).runOnUiThread(() -> {
                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
            });
        }
    }
}