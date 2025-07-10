package com.example.estacionapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.example.estacionapp.logica.AlgoritmoRuta;
import com.example.estacionapp.modelo.Grafo;
import com.example.estacionapp.modelo.Nodo;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import android.graphics.Color;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private MapView map;
    private Grafo grafo;

    private Polyline rutaOverlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        grafo = new Grafo();
        inicializarGrafo();

        Button btnBuscar = findViewById(R.id.btnBuscar);
        EditText editUbicacion = findViewById(R.id.editUbicacion);
        TextView txtResultado = findViewById(R.id.txtResultado);

        btnBuscar.setOnClickListener(v -> {
            String ubic = editUbicacion.getText().toString().trim().toUpperCase();

            Nodo inicio = grafo.obtenerNodo(ubic);
            if (inicio == null) {
                txtResultado.setText("Ubicación inválida (usa A, B, C, ...)");
                return;
            }

            // Buscar el lugar libre más cercano
            Nodo destino = null;
            int distanciaMin = Integer.MAX_VALUE;
            List<Nodo> mejorRuta = null;

            for (Nodo nodo : grafo.obtenerNodos()) {
                if (nodo.estaLibre()) {
                    List<Nodo> ruta = AlgoritmoRuta.encontrarRutaMasCorta(grafo, inicio, nodo);
                    if (!ruta.isEmpty() && ruta.size() < distanciaMin) {
                        distanciaMin = ruta.size();
                        destino = nodo;
                        mejorRuta = ruta;
                    }
                }
            }

            if (destino == null) {
                txtResultado.setText("No hay lugares libres disponibles");
                return;
            }

            mostrarRuta(mejorRuta);

            StringBuilder sb = new StringBuilder("Ruta: ");
            for (int i = 0; i < mejorRuta.size(); i++) {
                sb.append(mejorRuta.get(i).getNombre());
                if (i < mejorRuta.size() - 1) sb.append(" → ");
            }
            sb.append("\n¡Estacioná en ").append(destino.getNombre()).append("!");
            txtResultado.setText(sb.toString());
        });

        // Pedir permiso de ubicación si no está dado
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            centrarEnUbicacionActual();
        }

        mostrarMarcadores();
    }

    private void inicializarGrafo() {
        Nodo a = grafo.agregarNodo("A", -34.6037, -58.3816);
        Nodo b = grafo.agregarNodo("B", -34.6045, -58.3825);
        Nodo c = grafo.agregarNodo("C", -34.6028, -58.3830);
        Nodo d = grafo.agregarNodo("D", -34.6020, -58.3810);
        Nodo e = grafo.agregarNodo("E", -34.6010, -58.3820);

        conectarConDistancia("A", "B");
        conectarConDistancia("B", "C");
        conectarConDistancia("C", "D");
        conectarConDistancia("D", "E");

        // Simular lugares libres
        b.setLibre(true);
        e.setLibre(true);
    }

    private void conectarConDistancia(String nombre1, String nombre2) {
        Nodo n1 = grafo.obtenerNodo(nombre1);
        Nodo n2 = grafo.obtenerNodo(nombre2);
        if (n1 != null && n2 != null) {
            int distancia = (int) n1.getUbicacion().distanceToAsDouble(n2.getUbicacion());
            grafo.agregarArista(nombre1, nombre2, distancia);
        }
    }

    private void mostrarMarcadores() {
        map.getOverlays().clear();

        for (Nodo nodo : grafo.obtenerNodos()) {
            Marker marker = new Marker(map);
            marker.setPosition(nodo.getUbicacion());
            marker.setTitle("Nodo " + nodo.getNombre());

            if (nodo.estaLibre()) {
                marker.setIcon(getResources().getDrawable(android.R.drawable.presence_online, null));
            } else {
                marker.setIcon(getResources().getDrawable(android.R.drawable.presence_busy, null));
            }

            map.getOverlays().add(marker);
        }
        map.invalidate();
    }

    private void mostrarRuta(List<Nodo> ruta) {
        if (rutaOverlay != null) {
            map.getOverlays().remove(rutaOverlay);
        }

        rutaOverlay = new Polyline();
        rutaOverlay.setColor(Color.BLUE);
        rutaOverlay.setWidth(8f);

        for (Nodo nodo : ruta) {
            rutaOverlay.addPoint(nodo.getUbicacion());
        }

        map.getOverlays().add(rutaOverlay);
        map.invalidate();

        if (!ruta.isEmpty()) {
            IMapController controller = map.getController();
            controller.setZoom(18.0);
            controller.setCenter(ruta.get(0).getUbicacion());
        }
    }

    private void centrarEnUbicacionActual() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        GeoPoint startPoint;
        if (location != null) {
            startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        } else {
            // Si no hay ubicación, centro por defecto (Buenos Aires)
            startPoint = new GeoPoint(-34.6037, -58.3816);
        }

        IMapController mapController = map.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(startPoint);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                centrarEnUbicacionActual();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                centrarEnUbicacionActual();
            }
        }
    }
}
