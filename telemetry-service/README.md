# Microservicio: Telemetría GPS

Este microservicio se encarga de simular y almacenar datos de telemetría de los camiones, y generar alertas si se detectan anomalías. Consume eventos desde RabbitMQ para saber cuándo iniciar y detener la simulación de una ruta.

## Tecnologías
- Spring Boot 3
- Spring Data JPA
- Base de datos MySQL Independiente (`mysql_telemetry`)
- RabbitMQ (AMQP) para mensajería asíncrona

## Ejecución (Docker)
Para iniciar este servicio junto con sus dependencias:
```bash
docker-compose up -d --build telemetry-service
```
El servicio estará disponible internamente en el puerto `8083`.

## Testing (Pruebas Unitarias y Cobertura)
Para ejecutar los tests de integración y generar el reporte de cobertura de JaCoCo:
```bash
./mvnw clean test jacoco:report
```
El reporte HTML se generará en `target/site/jacoco/index.html`.
