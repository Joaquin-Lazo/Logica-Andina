# Logística Andina — Plataforma de Gestión Logística

**Sistema de Microservicios para Trazabilidad y Telemetría**

Logística Andina es un MVP diseñado para modernizar la gestión de flotas, reemplazando un monolito heredado con una arquitectura distribuida orientada a microservicios en la nube. Este sistema proporciona gestión de rutas, control de cargas, facturación, y monitoreo de telemetría GPS en tiempo real.

## Arquitectura del Sistema

El proyecto está construido bajo una arquitectura de microservicios con un patrón **BFF (Backend for Frontend)** y comunicación asíncrona mediante **RabbitMQ** para soportar alta escalabilidad. Todo el sistema está securizado transversalmente mediante **JWT (JSON Web Tokens)**.

### Componentes y Servicios:

1. **`user-service` (Puerto 8080)**:
   - **Responsabilidad:** Gestión de usuarios, autenticación, emisión de tokens JWT, y recepción de formularios de contacto público.
   - **Base de Datos:** `mysql_users` (auth_db).

2. **`route-service` (Puerto 8081)**:
   - **Responsabilidad:** Motor logístico principal. Maneja CRUDs de rutas, camiones, cargamentos, clientes y facturas.
   - **Mensajería:** Publica eventos de inicio y fin de rutas hacia RabbitMQ.
   - **Base de Datos:** `mysql_routes` (routes_db).

3. **`telemetry-service` (Puerto 8083)**:
   - **Responsabilidad:** Monitorea flotas en tiempo real. Simula el desplazamiento GPS y consume coordenadas desde la cola de RabbitMQ, aislando la base de datos de picos de tráfico. Genera alertas de seguridad automáticas por exceso de velocidad.
   - **Base de Datos:** `mysql_telemetry` (telemetry_db).

4. **`bff-service` (Puerto 8082)**:
   - **Responsabilidad:** Actúa como API Gateway (Backend for Frontend). Es el único punto de contacto para las aplicaciones frontend, enrutando de manera segura las peticiones hacia el microservicio correspondiente.

5. **`rabbitmq` (Puertos 5672 / 15672)**:
   - **Responsabilidad:** Broker de mensajería (Event-Driven Architecture) utilizado para manejar el alto volumen de datos del tracking GPS sin saturar las bases de datos.

6. **`frontend-service` (Puerto 8088)**:
   - **Responsabilidad:** Interfaz de usuario administrativa (Panel React/Vite). Provee dashboards, telemetría en vivo y formularios de gestión para los roles de Administrador, Despachador y Conductor.

7. **`client-website` (Puerto 80)**:
   - **Responsabilidad:** Sitio web público y corporativo para los clientes finales. Contiene información comercial y el formulario de "Contáctanos" que se integra directamente con el `user-service`.

---

## Despliegue Local (Docker Compose)

El proyecto está completamente contenerizado en un "Monorepo" local para facilitar su evaluación y ejecución. Todos los microservicios, bases de datos, y brokers de mensajería se orquestan mediante **Docker Compose**.

### Requisitos previos:
- **Docker** y **Docker Desktop** instalados y ejecutándose.

### Instrucciones de ejecución:

1. Abre una terminal en la raíz de este proyecto.
2. Ejecuta el siguiente comando para compilar las imágenes y levantar toda la arquitectura:
   ```bash
   docker-compose up -d --build
   ```
3. Espera a que los contenedores descarguen sus dependencias y cambien a estado *Healthy* y *Started* (puede tardar entre 1 y 3 minutos la primera vez mientras compila Java e inicializa MySQL).
4. Accede a las interfaces en tu navegador:
   - **Sitio Web Público:** [http://localhost](http://localhost)
   - **Panel Administrativo:** [http://localhost:8088](http://localhost:8088) (*user: admin@transandina.cl / pass: password123*)
   - **Panel RabbitMQ:** [http://localhost:15672](http://localhost:15672) *(user: rabbit / pass: rabbit123)*

### Para detener el sistema:
Si deseas apagar los contenedores, ejecuta:
```bash
docker-compose down
```
*(Si deseas reiniciar la base de datos desde cero, borrando todos los registros almacenados, puedes añadir el flag `-v` para destruir los volúmenes de datos: `docker-compose down -v`).*

---

## Pruebas Unitarias y Cobertura (JaCoCo)

Cada microservicio del backend cuenta con su propia batería de pruebas unitarias implementadas con **JUnit 5** y **Mockito**. Para medir la cobertura de estas pruebas se utiliza **JaCoCo**.

### Cómo ejecutar las pruebas:
Para ejecutar las pruebas y generar los reportes de cobertura, debes ejecutar Maven dentro de la carpeta de cada microservicio:

1. Abre una terminal y navega hacia la carpeta del microservicio que deseas probar (por ejemplo, `route_service`):
   ```bash
   cd route_service
   ```
2. Ejecuta el comando de Maven para limpiar, probar y generar el reporte:
   ```bash
   ./mvnw clean test jacoco:report
   ```
   *(Si estás en Windows PowerShell, usa `.\mvnw.cmd clean test jacoco:report`)*

### Cómo revisar los reportes de cobertura:
1. Una vez finalizado el comando anterior con éxito (`BUILD SUCCESS`), navega a la carpeta generada:
   ```bash
   ruta_del_microservicio/target/site/jacoco/
   ```
2. Abre el archivo **`index.html`** en cualquier navegador web.
3. Se desplegará un reporte visual interactivo mostrando el porcentaje de líneas de código cubiertas, ramas (branches) y métodos, permitiendo navegar clase por clase para verificar qué lógica de negocio está completamente validada por los tests.
