SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS routes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
    correo_contacto VARCHAR(150) NOT NULL,
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8)
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
('AB-CD-12', 'Volvo FH16', 30.0, 'En Ruta'),
('EF-GH-34', 'Scania R500', 28.0, 'En Ruta'),
('IJ-KL-56', 'Mercedes-Benz Arocs', 25.0, 'En Ruta'),
('MN-OP-78', 'Volvo FMX', 32.0, 'En Ruta'),
('QR-ST-90', 'Scania G410', 22.0, 'En Ruta'),
('UV-WX-12', 'Mack Pinnacle', 24.0, 'En Ruta'),
('YZ-AB-34', 'Freightliner Cascadia', 26.5, 'En Ruta'),
('KL-MN-56', 'Kenworth T680', 26.0, 'Disponible'),
('OP-QR-78', 'Mercedes-Benz Actros', 30.0, 'Disponible'),
('ST-UV-90', 'Mack Anthem', 22.5, 'Disponible');

INSERT INTO clientes (rut_empresa, razon_social, direccion_facturacion, correo_contacto, latitud, longitud) VALUES
('11111111-K', 'Minera Norte SpA', 'Av. Industrial 400, Antofagasta', 'pagos@mineranorte.cl', -23.65000000, -70.40000000),
('22222222-2', 'Retail del Sur S.A.', 'Ruta 5 Sur Km 1000, Puerto Montt', 'logistica@retaildelsur.cl', -41.46930000, -72.94230000),
('33333333-3', 'Supermercados El Valle', 'Av. Central 123, Santiago', 'facturacion@elvalle.cl', -33.45690000, -70.64820000),
('44444444-4', 'Exportadora Andina', 'Ruta 68 Km 15, Casablanca', 'logistica@exportandina.cl', -33.31500000, -71.40500000),
('55555555-5', 'Agricola del Maule', 'Camino Real S/N, Talca', 'despachos@agrimaule.cl', -35.42640000, -71.65540000);

INSERT INTO rutas (id_conductor_ref, id_despachador_ref, id_camion, origen_direccion, destino_direccion, lat_destino, lng_destino, distancia_estimada_km, estado) VALUES
(7, 2, 1, 'Centro Logístico Renca, Santiago', 'Faena Minera Antofagasta', -23.65000000, -70.40000000, 1335.2, 'Pendiente'),
(8, 3, 2, 'Puerto San Antonio', 'Centro de Distribución Quilicura', -33.36440000, -70.73000000, 115.8, 'Pendiente'),
(9, 4, 3, 'Bodega Central, Santiago', 'Puerto Coronel, Concepción', -36.82010000, -73.04440000, 500.2, 'Pendiente'),
(10, 5, 4, 'Puerto Valparaíso', 'Bodegas La Serena', -29.90270000, -71.25190000, 430.0, 'Pendiente'),
(11, 6, 5, 'Planta Industrial Temuco', 'Puerto Montt, Zona Sur', -41.46930000, -72.94230000, 355.0, 'Pendiente'),
(12, 2, 6, 'Zofri Iquique', 'Arica Puerto', -18.47830000, -70.31260000, 310.0, 'Pendiente'),
(13, 3, 7, 'Rancagua Agro', 'Talca Centro de Acopio', -35.42640000, -71.65540000, 170.5, 'Pendiente'),
(14, 4, 8, 'Bodega Maipú, Santiago', 'Puerto Valparaíso', -33.04560000, -71.62140000, 120.0, 'Completada'),
(15, 5, 9, 'Minera Antofagasta', 'Puerto Iquique', -20.21330000, -70.15030000, 415.0, 'Completada'),
(16, 6, 10, 'Coronel, Concepción', 'Temuco Industrial', -38.73970000, -72.59010000, 260.0, 'Completada'),
(7, 2, 1, 'Talca Acopio', 'Centro Logístico Renca, Santiago', -33.40000000, -70.70000000, 255.0, 'Completada'),
(14, 3, 8, 'Puerto Valparaíso', 'San Antonio Bodegas', -33.58330000, -71.61670000, 105.0, 'Pendiente'),
(15, 4, 9, 'Zofri Iquique', 'Minera Antofagasta', -23.65000000, -70.40000000, 415.0, 'Pendiente'),
(16, 5, 10, 'Temuco Industrial', 'Valdivia Centro', -39.81420000, -73.24590000, 165.0, 'Pendiente'),
(13, 6, 7, 'Talca Centro de Acopio', 'Concepción Industrial', -36.82010000, -73.04440000, 250.0, 'Pendiente');

INSERT INTO cargamentos (id_ruta, id_cliente, descripcion_productos, tipo_carga, peso_toneladas, volumen_m3, estado_entrega) VALUES
(1, 1, 'Insumos Químicos Mineros', 'Peligrosa', 24.0, 45.0, 'Pendiente'),
(2, 3, 'Abarrotes y Enlatados', 'General', 15.5, 60.0, 'Intacto'),
(3, 5, 'Maquinaria Agrícola', 'Pesada', 20.0, 50.0, 'Pendiente'),
(4, 4, 'Vino Embotellado', 'Frágil', 18.0, 45.0, 'Pendiente'),
(5, 2, 'Electrodomésticos', 'General', 12.0, 65.0, 'Pendiente'),
(6, 1, 'Repuestos Mineros', 'General', 22.0, 30.0, 'Pendiente'),
(7, 5, 'Fertilizantes', 'Peligrosa', 25.0, 40.0, 'Pendiente'),
(8, 4, 'Fruta de Exportación', 'Refrigerada', 14.0, 55.0, 'Entregado'),
(9, 1, 'Cobre Refinado', 'Pesada', 30.0, 20.0, 'Entregado'),
(10, 2, 'Madera Procesada', 'General', 26.0, 60.0, 'Entregado'),
(11, 3, 'Carga Consolidada Retail', 'General', 10.0, 45.0, 'Entregado'),
(12, 4, 'Materiales de Embalaje', 'General', 8.0, 70.0, 'Pendiente'),
(13, 1, 'Insumos Químicos Secundarios', 'Peligrosa', 20.0, 35.0, 'Pendiente'),
(14, 2, 'Lácteos y Quesos', 'Refrigerada', 19.5, 50.0, 'Pendiente'),
(15, 5, 'Semillas Agrícolas', 'General', 22.0, 40.0, 'Pendiente');

INSERT INTO facturas (id_ruta, id_cliente, monto_neto, impuestos, total_pagar, estado_pago) VALUES
(1, 2, 1500000.00, 285000.00, 1785000.00, 'Pendiente'),
(2, 1, 3200000.00, 608000.00, 3808000.00, 'Pendiente'),
(3, 3, 450000.00, 85500.00, 535500.00, 'Pagada'),
(4, 4, 2100000.00, 399000.00, 2499000.00, 'Pendiente'),
(5, 2, 850000.00, 161500.00, 1011500.00, 'Pendiente'),
(6, 1, 1750000.00, 332500.00, 2082500.00, 'Pendiente'),
(7, 5, 980000.00, 186200.00, 1166200.00, 'Pendiente'),
(8, 4, 1200000.00, 228000.00, 1428000.00, 'Pagada'),
(9, 1, 4500000.00, 855000.00, 5355000.00, 'Pagada'),
(10, 2, 620000.00, 117800.00, 737800.00, 'Pagada'),
(11, 3, 380000.00, 72200.00, 452200.00, 'Pagada'),
(12, 4, 290000.00, 55100.00, 345100.00, 'Pendiente'),
(13, 1, 2800000.00, 532000.00, 3332000.00, 'Pendiente'),
(14, 2, 750000.00, 142500.00, 892500.00, 'Pendiente'),
(15, 5, 420000.00, 79800.00, 499800.00, 'Pendiente');
