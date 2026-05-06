# User Service — Logística Andina

Microservicio de **gestión de usuarios y autenticación**. Administra los perfiles de Administradores, Despachadores y Conductores, y emite tokens JWT para la autenticación M2M (Machine-to-Machine).

## Puerto: `8080`
## Base de Datos: `auth_db` (MySQL)

## Tecnologías

| Tecnología | Propósito |
|---|---|
| Spring Boot 4.0.4 | Framework base |
| Spring Data JPA | Persistencia ORM con Hibernate |
| Spring Security | Autenticación y autorización JWT |
| jjwt 0.11.5 | Generación y validación de tokens JWT |
| MySQL Connector | Driver de base de datos |
| Lombok | Reducción de boilerplate |

## Estructura del Código

```
src/main/java/com/Users/user_service/
├── UserServiceApplication.java      # Punto de entrada
├── controller/
│   ├── AuthController.java          # POST /api/auth/login (emisión de JWT)
│   └── UserController.java          # CRUD /api/usuarios
├── dto/
│   ├── AuthRequest.java             # { username, password }
│   └── AuthResponse.java            # { token }
├── model/
│   ├── User.java                    # Entidad JPA: usuario del sistema
│   └── Role.java                    # Entidad JPA: rol (Admin, Despachador, Conductor)
├── repository/
│   ├── UserRepository.java          # JpaRepository<User>
│   └── RoleRepository.java          # JpaRepository<Role>
└── security/
    ├── SecurityConfig.java          # Configuración de filtros de seguridad
    ├── JwtFilter.java               # Filtro que valida el token en cada petición
    └── JwtUtil.java                 # Utilidad para generar/validar JWT
```

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Autenticación, retorna token JWT válido por 10 horas |
| GET | `/api/usuarios` | Lista todos los usuarios del sistema |
| POST | `/api/usuarios` | Crea un nuevo usuario |
| PUT | `/api/usuarios/{id}` | Actualiza un usuario existente |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario |

## Modelo de Datos

**Tabla `usuarios`:** `id`, `rut`, `nombres`, `apellidos`, `correo`, `telefono`, `password_hash`, `estado_activo`, `id_rol`

**Tabla `roles`:** `id_rol`, `nombre_rol` (ROLE_ADMINISTRADOR, ROLE_DESPACHADOR, ROLE_CONDUCTOR)

## Seguridad

- El endpoint `/api/auth/login` es público (permite autenticación inicial)
- Todos los demás endpoints requieren un JWT válido en el header `Authorization`
- Los tokens tienen una validez de 10 horas

## Ejecución

**Con Docker (recomendado), correr en la carpeta raiz:**
```bash
docker-compose up -d --build
```

**Local (requiere Java 17 + Maven + MySQL):**
```bash
cd user-service
./mvnw spring-boot:run
```
Requiere configurar:
- `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/auth_db`
- `SPRING_DATASOURCE_USERNAME=root`
- `SPRING_DATASOURCE_PASSWORD=rootPassword`
