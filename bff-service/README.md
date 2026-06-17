# Microservicio: Backend-For-Frontend (BFF)

Este microservicio actúa como un API Gateway y un agregador de datos (Backend-For-Frontend). Es el único microservicio expuesto directamente al cliente Frontend en React, encapsulando la lógica de enrutamiento y agregación de estado.

## Tecnologías
- Spring Boot 3
- RestTemplate para solicitudes inter-servicios
- AuthFilter (Validación de tokens JWT compartidos)

## Funciones Principales
1. **Composición de API (`/api/dashboard`)**: Llama simultáneamente a los microservicios de Usuarios, Rutas y Telemetría y unifica todo en un solo JSON para el dashboard de React.
2. **Simulación de Progreso**: Calcula la posición estimada (ETA, km recorridos) de los camiones en tiempo real en función del tiempo de salida y la distancia.
3. **Proxy Inverso (`/proxy/*`)**: Redirige operaciones CRUD y creación de rutas a los microservicios correspondientes, inyectando el token JWT necesario.

## Ejecución (Docker)
Para iniciar este servicio junto con sus dependencias:
```bash
docker-compose up -d --build bff-service
```
El servicio estará disponible en el puerto `8082`.
