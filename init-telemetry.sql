SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS telemetry_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
