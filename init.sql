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

-- Seed Data for auth_db
INSERT INTO roles (nombre_rol, descripcion) VALUES 
('ROLE_ADMINISTRADOR', 'Acceso total al sistema'),
('ROLE_DESPACHADOR', 'Asigna rutas y monitorea alertas'),
('ROLE_CONDUCTOR', 'Recibe rutas y emite coordenadas GPS');

INSERT INTO usuarios (id_rol, rut, nombres, apellidos, correo, password_hash) VALUES
(1, '11111111-1', 'Admin', 'Sistema', 'admin@transandina.cl', 'dummyhash123'),
(2, '22222222-2', 'Carlos', 'Despachador', 'carlos@transandina.cl', 'dummyhash456'),
(3, '33333333-3', 'Juan', 'Chofer', 'juan@transandina.cl', 'dummyhash789');


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
INSERT INTO camiones (patente, marca_modelo, capacidad_max_toneladas) VALUES
('AB-CD-12', 'Volvo FH16', 25.5),
('WX-YZ-99', 'Scania R500', 28.0);


-- =========================================================================
-- MICROSERVICIO DE TELEMETRÍA Y GPS (telemetry_db)
-- =========================================================================
CREATE DATABASE IF NOT EXISTS telemetry_db;
USE telemetry_db;

CREATE TABLE IF NOT EXISTS logs_telemetria (
    id_log BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_ruta_ref INT NOT NULL,         -- Logical Reference to routes_db.rutas
    latitud DECIMAL(10, 8) NOT NULL,
    longitud DECIMAL(11, 8) NOT NULL,
    velocidad_kmh FLOAT NOT NULL,
    nivel_combustible_pct FLOAT NOT NULL,
    temperatura_motor_c FLOAT NOT NULL,
    timestamp_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Creacion de index para lectura mas rapida.
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