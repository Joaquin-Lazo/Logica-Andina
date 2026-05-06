# Logística Andina — Plataforma de Gestión Logística
*Sistema de Microservicios para Trazabilidad y Telemetría*

Logística Andina es un MVP diseñado para modernizar la gestión de flotas, reemplazando un monolito heredado con una arquitectura orientada a microservicios en la nube. Este sistema proporciona gestión de rutas, control de cargas, facturación y telemetría GPS en tiempo real.

---

## Arquitectura del Sistema

El proyecto está construido bajo una arquitectura de microservicios con un patrón **BFF (Backend for Frontend)** y seguridad transversal basada en **JWT (JSON Web Tokens)**.

### Componentes Principales:

1. **`user-service` (Puerto 8080):**
   - **Responsabilidad:** Gestión de usuarios, autenticación y emisión de tokens JWT.
   - **Base de Datos:** `auth_db` (MySQL).

2. **`route-service` (Puerto 8081):**
   - **Responsabilidad:** Motor logístico principal. Maneja CRUDs de rutas, camiones, cargamentos, clientes y facturas.
   - **Base de Datos:** `routes_db` (MySQL).

3. **`telemetry-service` (Puerto 8083):**
   - **Responsabilidad:** Recibe y procesa coordenadas GPS, simula el desplazamiento de camiones (`@Scheduled`) y genera alertas de desvío.
   - **Base de Datos:** `telemetry_db` (MySQL).

4. **`bff-service` (Puerto 8082):**
   - **Responsabilidad:** Actúa como *API Gateway* y unificador de datos (Backend for Frontend).
   - Es el **único** punto de contacto para el frontend. Evita que el navegador tenga que hacer peticiones cruzadas a 3 puertos distintos.

5. **`frontend-service` (Puerto 8088):**
   - **Responsabilidad:** Interfaz de usuario construida en React. 
   - Provee dashboards y formularios de gestión para los roles de Administrador, Despachador y Conductor.

---

## Despliegue Local (Docker)

El proyecto está completamente contenerizado para evitar el problema de "funciona en mi máquina". Todos los microservicios y la base de datos se orquestan mediante Docker Compose.

**Requisitos previos:** 
- Docker y Docker Desktop instalados.

**Instrucciones de ejecución:**
1. Abre una terminal en la raíz del proyecto.
2. Ejecuta el siguiente comando para compilar y levantar toda la arquitectura:
   ```bash
   docker-compose up -d --build
   ```
3. Espera a que los contenedores reporten el estado `Healthy` y `Started` (puede tardar de 1 a 2 minutos la primera vez mientras compila Java e inicializa MySQL).
4. Accede a la interfaz en tu navegador:
   **[http://localhost:8088](http://localhost:8088)**

**Para detener el sistema:**
```bash
docker-compose down -v
```
*(El flag `-v` destruye los volúmenes, reseteando la base de datos para la próxima prueba).*
