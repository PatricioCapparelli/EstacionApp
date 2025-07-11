# EstacionApp - App para encontrar estacionamientos en Ciudad de Buenos Aires

EstacionApp es una aplicación Android que permite al usuario conocer en tiempo real las zonas donde está permitido o prohibido estacionar en la Ciudad de Buenos Aires, mostrando la información directamente sobre un mapa interactivo.

---

## Características principales

- Detecta la ubicación real del usuario usando FusedLocationProviderClient.
- Consulta la API oficial de estacionamientos del Gobierno de la Ciudad de Buenos Aires (GCBA) para obtener tramos de estacionamiento cercanos.
- Procesa y geocodifica los datos recibidos (calle y altura) para convertirlos en coordenadas geográficas reales.
- Muestra en un mapa (usando OSMDroid) los puntos con íconos que indican si el estacionamiento está permitido o prohibido.
- Actualiza la información y la ubicación en tiempo real mientras el usuario se mueve.

---

## Tecnologías utilizadas

- **Lenguaje:** Java
- **Mapas:** OSMDroid para mapas offline y mapas de código abierto
- **Ubicación:** FusedLocationProviderClient de Google Play Services
- **API oficial:** API de Estacionamientos y Geocodificación del GCBA
- **Permisos:** Ubicación en tiempo real (GPS)
- **Interacción con API:** HttpURLConnection y AsyncTask para consultas HTTP
- **Manejo JSON:** org.json para parsing de respuestas

---

## Flujo general de la aplicación

1. Al abrir la app, se solicita el permiso para acceder a la ubicación.
2. Se obtiene la ubicación actual del usuario (latitud y longitud).
3. Se realiza una consulta a la API oficial de estacionamientos del GCBA, enviando las coordenadas actuales y otros parámetros (radio, orden, límite).
4. La API devuelve una lista de tramos de estacionamiento cercanos con información detallada.
5. Para cada tramo recibido:
    - Se obtiene el código de calle (`cod_calle`) mediante una consulta automática basada en el nombre de la calle.
    - Se utiliza el `cod_calle` junto con la altura para geocodificar y obtener coordenadas exactas (x, y).
    - Se crea un marcador en el mapa en esa posición, con un ícono que indica si está permitido o prohibido estacionar.
6. El mapa se actualiza mostrando la ubicación del usuario y los marcadores de estacionamiento cercanos.

---

## Instrucciones para ejecutar

1. Clonar o descargar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Asegurarse de tener configurados los permisos de ubicación en el `AndroidManifest.xml`.
4. Colocar los íconos `permitido.png` y `prohibido.png` en la carpeta `res/drawable`.
5. Insertar el `client_id` y `client_secret` de la API oficial del GCBA en la clase `EstacionamientoBot`.
6. Ejecutar la app en un dispositivo o emulador con acceso a GPS y conexión a Internet.
7. Permitir el acceso a la ubicación cuando la app lo solicite.

---

## Estructura principal del código

- `MainActivity.java`: Maneja la interfaz principal, permisos de ubicación, y actualiza la posición del usuario en el mapa.
- `EstacionamientoBot.java`: Se encarga de consultar la API de estacionamientos y agregar los marcadores en el mapa según la respuesta.
- `Geocodificador.java`: Provee métodos para obtener el código de calle y geocodificar alturas para obtener coordenadas reales.
- Recursos gráficos: íconos que diferencian zonas permitidas y prohibidas.

---

## Consideraciones

- La app depende de la cobertura y actualización de la API oficial del GCBA, por lo que algunas zonas pueden no tener datos disponibles.
- Se recomienda probar con radios mayores si no se obtienen resultados inmediatos.
- El sistema prioriza siempre la ubicación más precisa y reciente para mejorar la experiencia.

---

## Posibles mejoras futuras

- Añadir filtrado por horarios de estacionamiento permitidos.
- Mostrar alertas o notificaciones cuando se ingrese a zonas prohibidas.
- Integrar tráfico en tiempo real para evitar zonas congestionadas.
- Soporte para otras ciudades o APIs.

---

## Contacto

Desarrollado por Patricio Capparelli

---

¡Gracias por probar EstacionApp! 🚗🅿️
