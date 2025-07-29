# EstacionApp - App para encontrar estacionamientos en Ciudad de Buenos Aires

EstacionApp es una aplicación Android que permite al usuario conocer en tiempo real las zonas donde está permitido o prohibido estacionar en la Ciudad de Buenos Aires, mostrando la información directamente sobre un mapa interactivo.

---

## Características principales

- Detecta la ubicación real del usuario usando FusedLocationProviderClient.
- Consulta la API oficial de estacionamientos del Gobierno de la Ciudad de Buenos Aires (GCBA) para obtener tramos de estacionamiento cercanos.
- Procesa y geocodifica los datos recibidos (calle y altura) para convertirlos en coordenadas geográficas reales.
- Muestra en un mapa (usando OSMDroid) los puntos con íconos que indican si el estacionamiento está permitido o prohibido.
- **El usuario puede tocar cualquier punto del mapa, y la app inicia automáticamente el flujo de análisis para ese lugar.**
- **Integra un servidor MCP con inteligencia artificial (IA) a través de la API de OpenRouter, que analiza los datos de la API oficial y devuelve una respuesta enriquecida.**
- **Renderiza zonas permitidas o prohibidas directamente sobre el mapa en forma de líneas o polígonos coloreados (verde/rojo), basadas en la interpretación enriquecida de la IA.**
- Actualiza la información y la ubicación en tiempo real mientras el usuario se mueve.

---

## Tecnologías utilizadas

- **Lenguaje:** Java
- **Mapas:** OSMDroid para mapas offline y de código abierto
- **Ubicación:** FusedLocationProviderClient de Google Play Services
- **API oficial:** API de Estacionamientos y Geocodificación del GCBA
- **IA:** OpenRouter API con modelos de lenguaje para análisis contextual
- **Servidor IA:** MCP Server (Model Context Protocol)
- **Permisos:** Ubicación en tiempo real (GPS)
- **Interacción con API:** HttpURLConnection y AsyncTask para consultas HTTP
- **Manejo JSON:** org.json para parsing de respuestas

---

## Flujo general de la aplicación

1. Al abrir la app, se solicita el permiso para acceder a la ubicación.
2. Se obtiene la ubicación actual del usuario (latitud y longitud).
3. Se realiza una consulta a la API oficial de estacionamientos del GCBA, enviando las coordenadas actuales o las seleccionadas por el usuario en el mapa.
4. La API devuelve una lista de tramos de estacionamiento cercanos con información detallada.
5. Para cada tramo recibido:
   - Se obtiene el código de calle (`cod_calle`) mediante una consulta automática basada en el nombre de la calle.
   - Se utiliza el `cod_calle` junto con la altura para geocodificar y obtener coordenadas exactas (x, y).
   - Se construye un JSON con las instancias de tramos obtenidos.
6. **Ese JSON es enviado a un servidor MCP que integra IA por medio de OpenRouter.**
7. **La IA interpreta y analiza la información para devolver una respuesta enriquecida que incluye zonas clasificadas como permitidas o prohibidas, cada una con sus coordenadas asociadas.**
8. Se dibujan directamente en el mapa líneas o áreas en color verde (permitido) y rojo (prohibido), representando visualmente el resultado enriquecido.
9. El mapa se actualiza mostrando tanto la ubicación del usuario como las zonas procesadas por IA en base al punto tocado.

---

## Instrucciones para ejecutar

1. Clonar o descargar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Asegurarse de tener configurados los permisos de ubicación en el `AndroidManifest.xml`.
4. Colocar los íconos `permitido.png` y `prohibido.png` en la carpeta `res/drawable`.
5. Insertar el `client_id` y `client_secret` de la API oficial del GCBA en la clase `EstacionamientoBot`.
6. Configurar la clave `OPENROUTER_API_KEY` en un archivo `.env` o en `BuildConfig`.
7. Ejecutar la app en un dispositivo o emulador con acceso a GPS y conexión a Internet.
8. Permitir el acceso a la ubicación cuando la app lo solicite.
9. **Tocar cualquier punto del mapa para iniciar el análisis de estacionamiento en esa zona.**

---

## Estructura principal del código

- `MainActivity.java`: Maneja la interfaz principal, permisos de ubicación, escucha eventos de toque en el mapa y renderiza zonas enriquecidas.
- `EstacionamientoBot.java`: Consulta la API de estacionamientos del GCBA y obtiene las instancias necesarias para el análisis.
- `Geocodificador.java`: Provee métodos para obtener el código de calle y geocodificar alturas para obtener coordenadas reales.
- `McpClient.java` (o módulo similar): Se encarga de enviar el JSON al servidor MCP y obtener la respuesta enriquecida de la IA.
- Recursos gráficos: íconos que diferencian zonas permitidas y prohibidas, además de líneas/polígonos coloreados.

---

## Consideraciones

- La app depende tanto de la actualización de la API oficial del GCBA como de la respuesta del servidor MCP.
- La IA mejora el contexto y precisión de las zonas detectadas, pero puede requerir conectividad estable para procesar los datos.
- Se recomienda probar con radios mayores si no se obtienen resultados inmediatos.
- El sistema prioriza siempre la ubicación más precisa y reciente para mejorar la experiencia.

---

## Posibles mejoras futuras

- Añadir filtrado por horarios de estacionamiento permitidos.
- Mostrar alertas o notificaciones cuando se ingrese a zonas prohibidas.
- Integrar tráfico en tiempo real para evitar zonas congestionadas.
- Soporte para otras ciudades o APIs.
- **Entrenar un modelo propio para análisis contextual sin depender de proveedores externos.**
- **Agregar una capa de historial de zonas prohibidas más frecuentes.**
- **Guardar zonas analizadas por el usuario y permitir su consulta offline.**

---

## Contacto

Desarrollado por Patricio Capparelli

---

¡Gracias por probar EstacionApp! 🚗🅿️
