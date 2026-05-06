# Telemetry Service — Logística Andina

Microservicio de **telemetría GPS y alertas en tiempo real**. Recibe y almacena coordenadas GPS, simula el desplazamiento de camiones de forma autónoma mediante tareas programadas (`@Scheduled`), y genera alertas por exceso de velocidad o desvíos de ruta.

## Puerto: `8083`
## Base de Datos: `telemetry_db` (MySQL)

## Tecnologías

| Tecnología | Propósito |
|---|---|
| Spring Boot 4.0.6 | Framework base |
| Spring Data JPA | Persistencia ORM con Hibernate |
| Spring Security | Autenticación JWT |
| Spring Scheduling | Simulación GPS autónoma (`@Scheduled`) |
| jjwt 0.11.5 | Generación y validación de tokens JWT |
| MySQL Connector | Driver de base de datos |
| Lombok | Reducción de boilerplate |

## Estructura del Código

```
src/main/java/com/telemetry/telemetry_service/
├── TelemetryServiceApplication.java   # Punto de entrada
├── controller/
│   ├── AuthController.java            # POST /api/auth/login (autenticación M2M)
│   ├── TelemetryController.java       # GET /api/telemetry (logs GPS)
│   └── AlertController.java           # GET /api/alertas (alertas activas)
├── dto/
│   ├── AuthRequest.java               # { username, password }
│   └── AuthResponse.java              # { token }
├── model/
│   ├── TelemetryLog.java              # Entidad JPA: registro GPS (lat, lng, velocidad, timestamp)
│   └── Alert.java                     # Entidad JPA: alerta (tipo, severidad, resuelta)
├── repository/
│   ├── TelemetryLogRepository.java    # JpaRepository<TelemetryLog>
│   └── AlertRepository.java          # JpaRepository<Alert>
├── security/
│   ├── SecurityConfig.java            # Configuración de filtros de seguridad
│   ├── JwtFilter.java                 # Filtro JWT por petición
│   └── JwtUtil.java                   # Utilidad JWT
└── service/
    └── GpsSimulatorService.java       # Simulador GPS autónomo (@Scheduled)
```

## Funcionalidad Principal

### 1. Logs GPS (`GET /api/telemetry`)
Retorna el historial de coordenadas GPS registradas. Soporta filtrado por ruta:
- `GET /api/telemetry` → Todos los logs
- `GET /api/telemetry/route/{id}` → Logs de una ruta específica

### 2. Alertas (`GET /api/alertas`)
Retorna alertas generadas automáticamente (exceso de velocidad, desvíos).

### 3. Simulador GPS (`GpsSimulatorService`)
Tarea programada que se ejecuta cada **30 segundos**:
1. Consulta las rutas con estado "En Tránsito" al `route-service`
2. Genera coordenadas GPS simuladas con movimiento incremental
3. Calcula velocidad simulada
4. Si la velocidad supera el umbral, genera una alerta automática
5. Persiste los logs en `telemetry_db`

Esta funcionalidad demuestra la **comunicación autónoma inter-servicio** sin intervención del usuario ni del frontend.

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Autenticación |
| GET | `/api/telemetry` | Historial de logs GPS |
| GET | `/api/telemetry/route/{id}` | Logs filtrados por ruta |
| GET | `/api/alertas` | Alertas activas del sistema |

## Comunicación Inter-Servicio

El simulador GPS necesita saber qué rutas están "En Tránsito". Para esto:
1. Se autentica contra el `route-service` usando credenciales
2. Obtiene la lista de rutas activas
3. Genera datos de telemetría para cada una

Variables de entorno requeridas:
- `ROUTE_SERVICE_URL=http://route-service:8081`
- `ROUTE_SERVICE_USER=route`
- `ROUTE_SERVICE_PASS=route123`

## Ejecución

**Con Docker (recomendado):**
```bash
docker-compose up -d --build
```

**Local (requiere Java 17 + Maven + MySQL):**
```bash
cd telemetry-service
./mvnw spring-boot:run
```
Requiere configurar:
- `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/telemetry_db`
- `SPRING_DATASOURCE_USERNAME=root`
- `SPRING_DATASOURCE_PASSWORD=rootPassword`
- `ROUTE_SERVICE_URL`, `ROUTE_SERVICE_USER`, `ROUTE_SERVICE_PASS`
