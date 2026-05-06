# BFF Service (Backend For Frontend) — Logística Andina

API Gateway que actúa como único punto de contacto entre el frontend React y los microservicios internos. Implementa el patrón **BFF (Backend For Frontend)** y **Proxy con inyección JWT**.

## Puerto: `8082`

## Tecnologías

| Tecnología | Propósito |
|---|---|
| Spring Boot 4.0.6 | Framework base |
| Spring WebMVC | API REST |
| Lombok | Reducción de boilerplate |
| RestTemplate | Cliente HTTP para comunicación inter-servicio |

## Estructura del Código

```
src/main/java/com/bff/bff_service/
├── BffServiceApplication.java       # Punto de entrada Spring Boot
├── config/
│   └── RestTemplateConfig.java      # Bean RestTemplate + configuración CORS
├── controller/
│   └── DashboardController.java     # Controlador principal (dashboard + proxy)
├── dto/
│   ├── DashboardResponse.java       # Wrapper: { routes, users }
│   ├── RouteSummaryDTO.java         # Ruta + camión + progreso GPS calculado
│   ├── TruckSummaryDTO.java         # Datos del vehículo asignado
│   ├── UserSummaryDTO.java          # Datos de usuario
│   └── RoleDTO.java                 # Rol del usuario
└── service/
    └── AuthService.java             # Autenticación M2M (obtención de tokens JWT)
```

## Funcionalidad Principal

### 1. Endpoint Dashboard (`GET /api/dashboard`)
Agrega datos de los 3 microservicios en una sola respuesta:
- Obtiene rutas del `route-service` (puerto 8081)
- Obtiene usuarios del `user-service` (puerto 8080)
- Calcula progreso GPS con datos del `telemetry-service` (puerto 8083)
- Retorna un `DashboardResponse` unificado con DTOs enriquecidos

### 2. Proxy Transparente (`/api/dashboard/proxy/*`)
Reenvía peticiones CRUD del frontend al microservicio correspondiente:
- `/proxy/routes` → `route-service:8081/api/rutas`
- `/proxy/camiones` → `route-service:8081/api/camiones`
- `/proxy/clients` → `route-service:8081/api/clientes`
- `/proxy/invoices` → `route-service:8081/api/facturas`
- `/proxy/cargo` → `route-service:8081/api/cargamentos`
- `/proxy/users` → `user-service:8080/api/usuarios`
- `/proxy/telemetry` → `telemetry-service:8083/api/telemetry`
- `/proxy/alerts` → `telemetry-service:8083/api/alertas`

Cada petición proxy inyecta automáticamente un token JWT M2M.

### 3. Autenticación M2M (`AuthService`)
Antes de hacer una petición a un microservicio, el BFF:
1. Envía credenciales a `POST /api/auth/login` del servicio destino
2. Recibe un token JWT
3. Lo cachea y lo inyecta como header `Authorization: Bearer <token>`

## Ejecución

**Con Docker (recomendado):**
```bash
docker-compose up -d --build
```

**Local (requiere Java 17 + Maven):**
```bash
cd bff-service
./mvnw spring-boot:run
```
Requiere configurar las variables de entorno: `USER_SERVICE_URL`, `ROUTE_SERVICE_URL`, `TELEMETRY_SERVICE_URL`.

## Deuda Técnica Conocida

- **God Controller:** `DashboardController.java` concentra todas las rutas proxy. En producción, se dividiría en controladores separados o se reemplazaría por Spring Cloud Gateway.
