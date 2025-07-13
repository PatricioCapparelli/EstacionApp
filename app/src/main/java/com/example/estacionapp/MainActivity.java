package com.example.estacionapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.estacionapp.bot.EstacionamientoBot;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import android.view.MotionEvent;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final long LOCATION_UPDATE_INTERVAL = 10000;
    private static final long FASTEST_LOCATION_INTERVAL = 5000;
    private static final float LOCATION_ACCURACY_THRESHOLD = 50;

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker userLocationMarker;
    private Location lastKnownLocation;

    // Bandera para bloquear la actualización automática desde el GPS
    private boolean modoManual = false;

    // Instancia global de EstacionamientoBot para reusar
    private EstacionamientoBot estacionamientoBot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_main);

        initializeMap();
        initializeLocationClient();
        requestLocationPermissions();

        // Crear instancia de EstacionamientoBot una vez
        estacionamientoBot = new EstacionamientoBot(this, mapView);
    }

    private void initializeMap() {
        mapView = findViewById(R.id.map);
        if (mapView != null) {
            mapView.setMultiTouchControls(true);
            mapView.getOverlays().add(new TapOverlay(mapView));
        } else {
            Log.e("MainActivity", "Error al inicializar el mapa");
            Toast.makeText(this, "Error al inicializar el mapa", Toast.LENGTH_LONG).show();
        }
    }

    private class TapOverlay extends Overlay {
        private MapView mapView;

        public TapOverlay(MapView mapView) {
            this.mapView = mapView;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
            GeoPoint p = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
            Log.d("MainActivity", "Tap detectado en lat: " + p.getLatitude() + ", lon: " + p.getLongitude());

            // Limpiar marcadores anteriores
            for (int i = mapView.getOverlays().size() - 1; i >= 0; i--) {
                if (mapView.getOverlays().get(i) instanceof Marker) {
                    mapView.getOverlays().remove(i);
                }
            }

            // Mostrar marcador en zona tocada
            Marker marker = new Marker(mapView);
            marker.setPosition(p);
            marker.setTitle("Zona seleccionada");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
            mapView.invalidate();

            // Crear ubicación para API
            Location location = new Location("tap");
            location.setLatitude(p.getLatitude());
            location.setLongitude(p.getLongitude());

            // Modo manual activo (desactiva GPS automático)
            modoManual = true;

            // Consultar API con esta ubicación usando la instancia de EstacionamientoBot
            if (estacionamientoBot != null) {
                estacionamientoBot.buscarEstacionamientosCercanos(location.getLatitude(), location.getLongitude());
            }

            return true;
        }
    }

    private void initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            checkLocationSettingsAndStartUpdates();
        }
    }

    private void checkLocationSettingsAndStartUpdates() {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(this, locationSettingsResponse -> startLocationUpdates())
                .addOnFailureListener(this, e -> {
                    Log.w("MainActivity", "Configuración de ubicación no satisfecha", e);
                    Toast.makeText(MainActivity.this, "Active los servicios de ubicación para una mejor experiencia", Toast.LENGTH_LONG).show();
                });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                createLocationRequest(),
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (locationResult == null) return;

            Location location = locationResult.getLastLocation();
            if (location != null && isBetterLocation(location, lastKnownLocation)) {
                lastKnownLocation = location;
                Log.d("MainActivity", "Nueva ubicación: " + location.getLatitude() + ", " + location.getLongitude());
                updateUserLocationOnMap(location);

                if (!modoManual && estacionamientoBot != null) {
                    // Solo consultar API si no estamos en modo manual
                    estacionamientoBot.buscarEstacionamientosCercanos(location.getLatitude(), location.getLongitude());
                }
            }
        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            if (!locationAvailability.isLocationAvailable()) {
                Log.w("MainActivity", "Ubicación no disponible");
            }
        }
    };

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) return true;

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > LOCATION_UPDATE_INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -LOCATION_UPDATE_INTERVAL;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) return true;
        if (isSignificantlyOlder) return false;

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > LOCATION_ACCURACY_THRESHOLD;

        boolean isFromSameProvider = location.getProvider().equals(currentBestLocation.getProvider());

        if (isMoreAccurate) return true;
        if (isNewer && !isLessAccurate) return true;
        return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    private void updateUserLocationOnMap(Location location) {
        runOnUiThread(() -> {
            if (mapView == null) return;

            boolean shouldCenterMap = (userLocationMarker == null);

            if (userLocationMarker == null) {
                userLocationMarker = new Marker(mapView);
                userLocationMarker.setTitle("Tu ubicación");
                userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(userLocationMarker);
            }

            GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            userLocationMarker.setPosition(newLocation);

            if (shouldCenterMap) {
                mapView.getController().setZoom(18.0);
                mapView.getController().setCenter(newLocation);
            }

            mapView.invalidate();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                checkLocationSettingsAndStartUpdates();
            } else {
                Toast.makeText(this, "Los permisos de ubicación son necesarios para la funcionalidad completa", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        if (hasLocationPermissions()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
