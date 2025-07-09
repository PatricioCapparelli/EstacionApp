package com.example.estacionapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.estacionapp.modelo.*;
import com.example.estacionapp.logica.AlgoritmoRuta;
import com.example.estacionapp.simulacion.Simulador;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Simulador simulador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        simulador = new Simulador();                 // reutilizás todas las clases Java

        EditText editUbicacion = findViewById(R.id.editUbicacion);
        Button   btnBuscar     = findViewById(R.id.btnBuscar);
        TextView txtResultado  = findViewById(R.id.txtResultado);

        btnBuscar.setOnClickListener(v -> {
            String ubic = editUbicacion.getText().toString().trim().toUpperCase();
            Nodo inicio = simulador.getGrafo().obtenerNodo(ubic);

            if (inicio == null) {
                txtResultado.setText("Ubicación inválida (usa A-E)");
                return;
            }

            // Elegir el lugar libre más cercano
            LugarLibre destino = simulador.getLugaresLibres().stream()
                    .min((l1, l2) -> {
                        int d1 = AlgoritmoRuta.encontrarRutaMasCorta(simulador.getGrafo(), inicio, l1.getUbicacion()).size();
                        int d2 = AlgoritmoRuta.encontrarRutaMasCorta(simulador.getGrafo(), inicio, l2.getUbicacion()).size();
                        return Integer.compare(d1, d2);
                    }).orElse(null);

            if (destino == null) {
                txtResultado.setText("No hay lugares libres");
                return;
            }

            List<Nodo> ruta = AlgoritmoRuta.encontrarRutaMasCorta(simulador.getGrafo(), inicio, destino.getUbicacion());

            StringBuilder sb = new StringBuilder("Ruta: ");
            for (int i = 0; i < ruta.size(); i++) {
                sb.append(ruta.get(i).getNombre());
                if (i < ruta.size() - 1) sb.append(" → ");
            }
            sb.append("\n¡Estacioná en ").append(destino.getUbicacion().getNombre()).append("!");

            txtResultado.setText(sb.toString());
        });
    }
}
