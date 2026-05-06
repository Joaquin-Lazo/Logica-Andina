# Frontend Service — Logística Andina

Interfaz de usuario construida en **React 18** que provee un dashboard de gestión logística con visualización en tiempo real.

## Tecnologías

| Tecnología | Versión | Propósito |
|---|---|---|
| React | 18.2 | Biblioteca de interfaz de usuario |
| React Router DOM | 7.14 | Navegación SPA (Single Page Application) |
| React Scripts | 5.0.1 | Toolchain de build (Create React App) |

## Estructura del Código

```
src/
├── App.jsx                    # Shell principal: rutas, navegación, selector de roles
├── App.css                    # Estilos globales (sistema de diseño completo)
├── index.js                   # Punto de entrada React
├── index.css                  # Reset CSS base
├── context/
│   └── RoleContext.jsx        # Proveedor de estado global para rol y sesión
├── components/
│   ├── Dashboard.jsx          # Panel principal con tablas en vivo (polling 5s)
│   ├── ManageRoutes.jsx       # CRUD de rutas operativas
│   ├── ManageTrucks.jsx       # CRUD de flota de camiones
│   ├── ManageUsers.jsx        # CRUD de usuarios (solo Admin)
│   ├── ManageClients.jsx      # CRUD de empresas clientes
│   ├── ManageInvoices.jsx     # Vista de facturas (solo lectura)
│   ├── ManageCargo.jsx        # Vista de cargamentos (solo lectura)
│   ├── Telemetry.jsx          # Logs GPS en vivo + alertas (polling 10s, paginación)
│   └── ErrorBoundary.jsx      # Aislamiento de errores por pestaña
└── utils/
    └── validators.js          # Validación de RUT, email, coordenadas, patentes
```

## Ejecución Local (sin Docker)

```bash
cd frontend-service
npm install
npm start
```

La aplicación se abre en `http://localhost:3000`. Requiere que el BFF esté corriendo en el puerto 8082.

## Ejecución con Docker (recomendado)

Desde la raíz del proyecto:

```bash
docker-compose up -d --build
```

La aplicación se sirve en `http://localhost:8088`.

## Roles de Usuario

| Rol | Pestañas Visibles |
|---|---|
| Administrador | Todas (8 pestañas) |
| Despachador | Todas excepto Usuarios |
| Conductor | Solo Panel General (con filtro por conductor) |

## Patrones Implementados

- **Error Boundary:** Cada ruta está aislada. Si una pestaña falla, las demás siguen funcionando.
- **Polling con cleanup:** Los intervalos se limpian al desmontar componentes (`clearInterval` en `useEffect`).
- **Estado centralizado:** `RoleContext` evita prop drilling del rol activo.
- **Validación client-side:** Formatos chilenos (RUT, patentes) validados antes de enviar al servidor.
