# Route Service — Logística Andina

Motor logístico principal del sistema. Gestiona todas las entidades de negocio: **rutas, camiones, clientes, facturas y cargamentos**. Protegido con autenticación JWT.

## Puerto: `8081`
## Base de Datos: `routes_db` (MySQL)

## Tecnologías

| Tecnología | Propósito |
|---|---|
| Spring Boot 4.0.6 | Framework base |
| Spring Data JPA | Persistencia ORM con Hibernate |
| Spring Security | Autenticación y autorización JWT |
| jjwt 0.11.5 | Generación y validación de tokens JWT |
| MySQL Connector | Driver de base de datos |
| Lombok | Reducción de boilerplate |

## Estructura del Código

```
src/main/java/com/routes/route_service/
├── RouteServiceApplication.java     # Punto de entrada
├── controller/
│   ├── AuthController.java          # POST /api/auth/login
│   ├── RouteController.java         # CRUD /api/rutas
│   ├── TruckController.java         # CRUD /api/camiones
│   ├── ClientController.java        # CRUD /api/clientes
│   ├── InvoiceController.java       # CRUD /api/facturas
│   └── CargoController.java         # CRUD /api/cargamentos
├── dto/
│   ├── AuthRequest.java             # { username, password }
│   └── AuthResponse.java            # { token }
├── model/
│   ├── Route.java                   # Entidad JPA: rutas operativas
│   ├── Truck.java                   # Entidad JPA: flota de camiones
│   ├── Client.java                  # Entidad JPA: empresas clientes
│   ├── Invoice.java                 # Entidad JPA: facturas
│   └── Cargo.java                   # Entidad JPA: cargamentos
├── repository/
│   ├── RouteRepository.java         # JpaRepository<Route>
│   ├── TruckRepository.java         # JpaRepository<Truck>
│   ├── ClientRepository.java        # JpaRepository<Client>
│   ├── InvoiceRepository.java       # JpaRepository<Invoice>
│   └── CargoRepository.java         # JpaRepository<Cargo>
└── security/
    ├── SecurityConfig.java          # Configuración de filtros de seguridad
    ├── JwtFilter.java               # Filtro que valida el token en cada petición
    └── JwtUtil.java                 # Utilidad para generar/validar JWT
```

## Endpoints Principales

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Autenticación, retorna JWT |
| GET/POST/PUT/DELETE | `/api/rutas` | CRUD de rutas operativas |
| GET/POST/PUT/DELETE | `/api/camiones` | CRUD de flota de camiones |
| GET/POST/PUT/DELETE | `/api/clientes` | CRUD de empresas clientes |
| GET/POST/PUT/DELETE | `/api/facturas` | CRUD de facturación |
| GET/POST/PUT/DELETE | `/api/cargamentos` | CRUD de cargamentos |

## Seguridad

Todas las peticiones (excepto `/api/auth/login`) requieren un header:
```
Authorization: Bearer <token_jwt>
```
El token se obtiene mediante autenticación desde el BFF.

## Ejecución

**Con Docker (recomendado):**
```bash
docker-compose up -d --build
```

**Local (requiere Java 17 + Maven + MySQL):**
```bash
cd route_service
./mvnw spring-boot:run
```
Requiere configurar:
- `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/routes_db`
- `SPRING_DATASOURCE_USERNAME=root`
- `SPRING_DATASOURCE_PASSWORD=rootPassword`
