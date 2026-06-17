# Logística Andina - Arquitectura de Microservicios

Proyecto final de Arquitectura de Microservicios para Logística Andina. Este sistema permite gestionar rutas, camiones, usuarios y telemetría de forma distribuida utilizando Spring Boot, RabbitMQ, MySQL y MongoDB.

## Arquitectura

El sistema está compuesto por 4 servicios backend y un cliente web estático:

1. **BFF Service (Backend-For-Frontend)**: Actúa como API Gateway ligero y orquestador. Puerto 8082.
2. **User Service**: Gestión de usuarios, roles, autenticación JWT y contactos. Puerto 8080. Base de datos MySQL (`users_db`).
3. **Route Service**: Gestión de rutas, clientes, camiones, cargamentos y facturación. Puerto 8081. Base de datos MySQL (`routes_db`).
4. **Telemetry Service**: Ingesta de datos GPS simulados, alertas en tiempo real vía RabbitMQ. Puerto 8083. Base de datos MongoDB (`telemetry_db`).
5. **Client Website**: Interfaz de usuario interactiva (HTML/JS/Bootstrap) que consume el BFF para mostrar el dashboard, mapa en vivo (Leaflet.js) y formularios.

## Tecnologías Utilizadas
- **Java 17** con **Spring Boot 3.x**
- **Spring Data JPA** y **Spring Security** (JWT)
- **RabbitMQ** para mensajería asíncrona
- **MySQL** (Relacional) y **MongoDB** (NoSQL)
- **Docker & Docker Compose** para orquestación de contenedores
- **JUnit 5 & Mockito** (Pruebas unitarias > 60% cobertura)

## Instrucciones de Instalación y Ejecución

Todo el sistema está contenerizado y orquestado mediante Docker Compose.

1. Clonar este repositorio.
2. Asegurarse de tener Docker y Docker Compose instalados.
3. En la raíz del proyecto, ejecutar:

```bash
docker-compose up --build
```

Esto levantará 10 contenedores:
- Nginx (Frontend en el puerto 80)
- BFF Service
- User Service
- Route Service
- Telemetry Service
- RabbitMQ (Panel de control en el puerto 15672)
- 3 Bases de datos MySQL
- MongoDB

## Accesos Rápidos
- **Frontend / Dashboard**: `http://localhost:80`
- **Swagger UI (Users)**: `http://localhost:8080/swagger-ui.html`
- **Swagger UI (Routes)**: `http://localhost:8081/swagger-ui.html`
- **Swagger UI (Telemetry)**: `http://localhost:8083/swagger-ui.html`
- **RabbitMQ Admin**: `http://localhost:15672` (user/password)
