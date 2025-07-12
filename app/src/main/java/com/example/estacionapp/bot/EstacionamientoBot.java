package com.example.estacionapp.bot;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.estacionapp.utils.CodCalleCallback;
import com.example.estacionapp.utils.Geocodificador;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class EstacionamientoBot {

    private static final String TAG = "EstacionamientoBot";

    private static final String CLIENT_ID = "tu_client_id";
    private static final String CLIENT_SECRET = "tu_client_secret";

    public static void sugerirEstacionamiento(final Context context, final Location ubicacion, final MapView mapView) {
        new ConsultaEstacionamientosTask(context, ubicacion, mapView).execute();
    }

    private static class ConsultaEstacionamientosTask extends AsyncTask<Void, Void, JSONArray> {
        private final Context context;
        private final Location ubicacion;
        private final MapView mapView;

        private Exception error;

        public ConsultaEstacionamientosTask(Context context, Location ubicacion, MapView mapView) {
            this.context = context;
            this.ubicacion = ubicacion;
            this.mapView = mapView;
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                double x = ubicacion.getLongitude();
                double y = ubicacion.getLatitude();

                String urlStr = String.format(Locale.US,
                        "https://apitransporte.buenosaires.gob.ar/transito/v1/estacionamientos?client_id=%s&client_secret=%s&x=%f&y=%f&srid=4326&radio=100&orden=distancia&limite=10&formato=json&fullInfo=true",
                        CLIENT_ID, CLIENT_SECRET, x, y);

                Log.d(TAG, "URL generada: " + urlStr);

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Código de respuesta HTTP: " + responseCode);

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
                return json.getJSONArray("instancias");

            } catch (Exception e) {
                error = e;
                Log.e(TAG, "Error en la consulta API", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray instancias) {
            if (instancias == null || instancias.length() == 0) {
                Log.d(TAG, "No se recibieron instancias de la API");
                return;
            }

            // Limpiar solo marcadores para no borrar overlays como MapEventsOverlay
            for (int i = mapView.getOverlays().size() - 1; i >= 0; i--) {
                if (mapView.getOverlays().get(i) instanceof Marker) {
                    mapView.getOverlays().remove(i);
                }
            }

            for (int i = 0; i < instancias.length(); i++) {
                try {
                    JSONObject instancia = instancias.getJSONObject(i);
                    String nombreCalle = instancia.getString("calle");
                    String altura = instancia.getString("altura");
                    String tipo = instancia.getString("tipo");

                    if (altura == null || altura.isEmpty()) {
                        Log.d(TAG, "Datos insuficientes para geocodificar: " + nombreCalle + ", " + altura);
                        continue;
                    }

                    String[] partesAltura = altura.split("-");
                    String alturaInicio = partesAltura.length > 0 ? partesAltura[0].trim() : "";
                    String alturaFin = partesAltura.length > 1 ? partesAltura[1].trim() : alturaInicio;

                    String direccion = nombreCalle + " " + alturaInicio;

                    Log.d(TAG, "Procesando: " + tipo + " en " + direccion);

                    Geocodificador.buscarCodCalle(nombreCalle, new CodCalleCallback() {
                        @Override
                        public void onCodCalleObtenido(Integer codCalle) {
                            if (codCalle == null) {
                                Log.d(TAG, "No se encontró cod_calle para " + nombreCalle);
                                return;
                            }

                            GeoPoint punto = new GeoPoint(ubicacion.getLatitude(), ubicacion.getLongitude());
                            Marker marker = new Marker(mapView);
                            marker.setPosition(punto);
                            marker.setTitle(tipo + ": " + direccion);
                            mapView.getOverlays().add(marker);
                            mapView.invalidate();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error obteniendo cod_calle", e);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error procesando instancia", e);
                }
            }
        }
    }
}
