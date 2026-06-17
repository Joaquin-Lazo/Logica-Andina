# Client Website - Logística Andina

Aplicación frontend (SPA) desarrollada en HTML5, CSS3, JavaScript (Vanilla) y Bootstrap 5, que consume la API REST a través del BFF (Backend-For-Frontend).

## Características
- **Dashboard en Tiempo Real**: Consumo periódico de los endpoints del BFF para visualizar el estado de la flota, el progreso simulado de las rutas y alertas.
- **Mapa Interactivo**: Integración con Leaflet.js para mostrar las ubicaciones y progreso de los camiones sobre mapas de CartoDB (Dark Theme).
- **Diseño Responsivo**: Adaptado para web y móviles mediante Bootstrap.

## Ejecución
Para mantener la simplicidad y ligereza, esta aplicación se sirve de forma estática. 

### Opción A (Recomendada): Docker
La arquitectura general monta automáticamente el contenido de esta carpeta en un contenedor **Nginx** (puerto 80).
1. En la raíz del proyecto, ejecuta: `docker-compose up`
2. Abre tu navegador en: `http://localhost:80`

### Opción B: Ejecución Local
Puedes levantar un servidor estático usando Python o Node.js:
- **Python**: `python -m http.server 80`
- **Node.js**: `npx serve -p 80`
