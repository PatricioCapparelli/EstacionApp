# EstacionApp

Aplicación Android para encontrar el lugar de estacionamiento libre más cercano en un estacionamiento simulado.

---

## Descripción

Este proyecto Android permite al usuario ingresar una ubicación dentro de un estacionamiento (nodos A-E) 
y encuentra la ruta más corta hacia el lugar libre más cercano utilizando un algoritmo de rutas.

Está desarrollado en Java y Kotlin para Android con arquitectura basada en:

- `MainActivity.java`: Interfaz principal y controlador de la app.
- `Simulador`: Lógica y simulación del grafo y lugares libres.
- `AlgoritmoRuta`: Algoritmo para encontrar la ruta más corta en el grafo.
- Recursos XML para la interfaz.

---

## Cómo correr

1. Clonar el repositorio:
```bash
git clone https://github.com/PatricioCapparelli/EstacionApp
