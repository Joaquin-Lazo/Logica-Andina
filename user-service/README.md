# Microservicio: Usuarios y Roles

Este microservicio se encarga de la gestión de identidades, autenticación (junto con el BFF) y el almacenamiento de peticiones de contacto recibidas desde la página web de clientes.

## Tecnologías
- Spring Boot 3
- Spring Data JPA
- Base de datos MySQL Independiente (`mysql_users`)
- Spring Security (PasswordEncoder)

## Ejecución (Docker)
Este servicio está diseñado para ser orquestado mediante `docker-compose`. 
Desde la raíz del proyecto, ejecuta:
```bash
docker-compose up -d --build user-service
```
El servicio estará disponible internamente en el puerto `8080`.

## Testing (Pruebas Unitarias)
Para ejecutar los tests y compilar el reporte JaCoCo de cobertura:
```bash
./mvnw clean test jacoco:report
```
El reporte HTML se generará en `target/site/jacoco/index.html`.
