-- =========================================================================
-- MICROSERVICIO DE USUARIOS Y ROLES (auth_db)
-- =========================================================================
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE IF NOT EXISTS roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_rol INT NOT NULL,
    rut VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado_activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_usuarios_roles FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE RESTRICT
);

-- Data para auth_db
INSERT INTO roles (nombre_rol, descripcion) VALUES 
('ROLE_ADMINISTRADOR', 'Acceso total al sistema'),
('ROLE_DESPACHADOR', 'Asigna rutas y monitorea alertas'),
('ROLE_CONDUCTOR', 'Recibe rutas y emite coordenadas GPS');

INSERT INTO usuarios (id_rol, rut, nombres, apellidos, correo, password_hash) VALUES
(1, '11111111-1', 'Admin', 'Sistema', 'admin@transandina.cl', 'dummyhash123'),
(2, '22222222-2', 'Carlos', 'Despachador', 'carlos@transandina.cl', 'dummyhash456'),
(3, '33333333-3', 'Juan', 'Chofer', 'juan@transandina.cl', 'dummyhash789'),
(2, '44444444-4', 'Andrea', 'Mendoza', 'amendoza@transandina.cl', 'dummyhash101'),
(3, '55555555-5', 'Luis', 'Perez', 'lperez@transandina.cl', 'dummyhash202'),
(3, '66666666-6', 'Miguel', 'Tapia', 'mtapia@transandina.cl', 'dummyhash303'),
(3, '77777777-7', 'Roberto', 'Salinas', 'rsalinas@transandina.cl', 'dummyhash404');


-- =========================================================================
-- MICROSERVICIO DE RUTAS Y LOGÍSTICA (routes_db)
-- =========================================================================
CREATE DATABASE IF NOT EXISTS routes_db;
USE routes_db;

CREATE TABLE IF NOT EXISTS camiones (
    id_camion INT AUTO_INCREMENT PRIMARY KEY,
    patente VARCHAR(15) NOT NULL UNIQUE,
    marca_modelo VARCHAR(100) NOT NULL,
    capacidad_max_toneladas FLOAT NOT NULL,
    estado_operativo VARCHAR(50) DEFAULT 'Disponible'
);

CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    rut_empresa VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    direccion_facturacion VARCHAR(255) NOT NULL,
    correo_contacto VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS rutas (
    id_ruta INT AUTO_INCREMENT PRIMARY KEY,
    id_conductor_ref INT NOT NULL,   
    id_despachador_ref INT NOT NULL, 
    id_camion INT NOT NULL,
    origen_direccion VARCHAR(255) NOT NULL,
    destino_direccion VARCHAR(255) NOT NULL,
    lat_destino DECIMAL(10, 8) NOT NULL,
    lng_destino DECIMAL(11, 8) NOT NULL,
    distancia_estimada_km FLOAT NOT NULL,
    estado VARCHAR(50) DEFAULT 'Pendiente',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_salida_real TIMESTAMP NULL,
    eta_calculado TIMESTAMP NULL,
    CONSTRAINT fk_rutas_camiones FOREIGN KEY (id_camion) REFERENCES camiones(id_camion) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS cargamentos (
    id_cargamento INT AUTO_INCREMENT PRIMARY KEY,
    id_ruta INT NOT NULL,
    id_cliente INT NOT NULL,
    descripcion_productos TEXT NOT NULL,
    tipo_carga VARCHAR(50) NOT NULL,
    peso_toneladas FLOAT NOT NULL,
    volumen_m3 FLOAT NOT NULL,
    estado_entrega VARCHAR(50) DEFAULT 'Intacto',
    CONSTRAINT fk_cargamentos_rutas FOREIGN KEY (id_ruta) REFERENCES rutas(id_ruta) ON DELETE CASCADE,
    CONSTRAINT fk_cargamentos_clientes FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_ruta INT NOT NULL,
    id_cliente INT NOT NULL,
    monto_neto DECIMAL(12, 2) NOT NULL,
    impuestos DECIMAL(12, 2) NOT NULL,
    total_pagar DECIMAL(12, 2) NOT NULL,
    estado_pago VARCHAR(50) DEFAULT 'Pendiente',
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_facturas_rutas FOREIGN KEY (id_ruta) REFERENCES rutas(id_ruta) ON DELETE RESTRICT,
    CONSTRAINT fk_facturas_clientes FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE RESTRICT
);

-- Data para rutas
INSERT INTO camiones (patente, marca_modelo, capacidad_max_toneladas, estado_operativo) VALUES
('FG-HJ-34', 'Mercedes-Benz Actros', 30.0, 'Disponible'),
('KL-MN-56', 'Mack Anthem', 22.5, 'En Mantenimiento'),
('OP-QR-78', 'Kenworth T680', 26.0, 'Disponible');

INSERT INTO clientes (rut_empresa, razon_social, direccion_facturacion, correo_contacto) VALUES
('77777777-K', 'Minera Norte SpA', 'Av. Industrial 400, Antofagasta', 'pagos@mineranorte.cl'),
('88888888-8', 'Retail del Sur S.A.', 'Ruta 5 Sur Km 1000, Puerto Montt', 'logistica@retaildelsur.cl'),
('99999999-9', 'Supermercados El Valle', 'Av. Central 123, Santiago', 'facturacion@elvalle.cl');

INSERT INTO rutas (id_conductor_ref, id_despachador_ref, id_camion, origen_direccion, destino_direccion, lat_destino, lng_destino, distancia_estimada_km, estado) VALUES
(3, 2, 1, 'Centro Logístico Santiago', 'Bodega Central Puerto Montt', -41.46930000, -72.94230000, 1030.5, 'En Transito'),
(5, 4, 3, 'Centro Logístico Santiago', 'Faena Minera Antofagasta', -23.65000000, -70.40000000, 1335.2, 'Pendiente'),
(6, 2, 5, 'Puerto San Antonio', 'Centro de Distribución Quilicura', -33.36440000, -70.73000000, 115.8, 'Completada');

INSERT INTO cargamentos (id_ruta, id_cliente, descripcion_productos, tipo_carga, peso_toneladas, volumen_m3, estado_entrega) VALUES
(1, 2, 'Electrodomésticos y Línea Blanca', 'General', 15.5, 60.0, 'Intacto'),
(2, 1, 'Insumos Químicos para Extracción', 'Peligrosa', 24.0, 45.5, 'Pendiente'),
(3, 3, 'Abarrotes y Alimentos Secos', 'General', 20.0, 50.0, 'Entregado');

INSERT INTO facturas (id_ruta, id_cliente, monto_neto, impuestos, total_pagar, estado_pago) VALUES
(1, 2, 1500000.00, 285000.00, 1785000.00, 'Pendiente'),
(2, 1, 3200000.00, 608000.00, 3808000.00, 'Pendiente'),
(3, 3, 450000.00, 85500.00, 535500.00, 'Pagada');


-- =========================================================================
-- MICROSERVICIO DE TELEMETRÍA Y GPS (telemetry_db)
-- =========================================================================
CREATE DATABASE IF NOT EXISTS telemetry_db;
USE telemetry_db;

CREATE TABLE IF NOT EXISTS logs_telemetria (
    id_log BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_ruta_ref INT NOT NULL,         
    latitud DECIMAL(10, 8) NOT NULL,
    longitud DECIMAL(11, 8) NOT NULL,
    velocidad_kmh FLOAT NOT NULL,
    timestamp_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_telemetria_ruta_tiempo ON logs_telemetria(id_ruta_ref, timestamp_evento);

CREATE TABLE IF NOT EXISTS alertas (
    id_alerta BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_log_gps BIGINT NOT NULL,
    tipo_alerta VARCHAR(100) NOT NULL,
    nivel_severidad VARCHAR(50) NOT NULL,
    resuelta BOOLEAN DEFAULT FALSE,
    fecha_resolucion TIMESTAMP NULL,
    comentarios_despachador TEXT,
    CONSTRAINT fk_alertas_logs FOREIGN KEY (id_log_gps) REFERENCES logs_telemetria(id_log) ON DELETE CASCADE
);

INSERT INTO logs_telemetria (id_ruta_ref, latitud, longitud, velocidad_kmh) VALUES
(1, -33.45690000, -70.64820000, 0.0),
(1, -33.58910000, -70.68930000, 75.5),
(1, -34.12340000, -70.75610000, 82.3),
(3, -33.58000000, -71.60000000, 60.5),
(3, -33.59000000, -71.55000000, 0.0);

INSERT INTO alertas (id_log_gps, tipo_alerta, nivel_severidad, resuelta, comentarios_despachador) VALUES
(3, 'Exceso de Velocidad (>80km/h)', 'Media', TRUE, 'Conductor advertido por radio. Redujo la velocidad.'),
(5, 'Desvío de Ruta Crítico', 'Crítica', FALSE, 'Detención obligatoria. El camión abandonó el corredor seguro en la ruta a San Antonio y se detuvo inesperadamente.');