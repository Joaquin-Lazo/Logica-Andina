# Microservicio: Gestión de Rutas y Logística

Este microservicio es el núcleo central del dominio. Administra Rutas, Camiones, Facturas y Cargamentos. Implementa el patrón Publisher para emitir eventos mediante RabbitMQ cuando el estado de una ruta cambia.

## Tecnologías
- Spring Boot 3
- Spring Data JPA
- Base de datos MySQL Independiente (`mysql_routes`)
- RabbitMQ (AMQP) para mensajería asíncrona

## Ejecución (Docker)
Para iniciar este servicio junto con sus dependencias:
```bash
docker-compose up -d --build route-service
```
El servicio estará disponible internamente en el puerto `8081`.

## Testing (Pruebas Unitarias y Cobertura)
Para ejecutar los tests de integración y generar el reporte de cobertura de JaCoCo:
```bash
./mvnw clean test jacoco:report
```
El reporte HTML se generará en `target/site/jacoco/index.html`.
