SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
(1, '11111111-1', 'Admin', 'Sistema', 'admin@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(2, '22222222-2', 'Carlos', 'Valdes', 'cvaldes@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(2, '33333333-3', 'Andrea', 'Mendoza', 'amendoza@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(2, '44444444-4', 'Felipe', 'Rojas', 'frojas@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(2, '55555555-5', 'Laura', 'Gomez', 'lgomez@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(2, '66666666-6', 'Valentina', 'Soto', 'vsoto@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '77777777-7', 'Juan', 'Chofer', 'jchofer@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '88888888-8', 'Luis', 'Perez', 'lperez@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '99999999-9', 'Miguel', 'Tapia', 'mtapia@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '10101010-0', 'Roberto', 'Salinas', 'rsalinas@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '11223344-1', 'Sebastian', 'Valenzuela', 'svalenzuela@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '12233445-2', 'Natalia', 'Rios', 'nrios@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '13344556-3', 'Esteban', 'Paredes', 'eparedes@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '14455667-4', 'Diego', 'Muñoz', 'dmunoz@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '15566778-5', 'Hector', 'Silva', 'hsilva@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2'),
(3, '16677889-6', 'Camila', 'Morales', 'cmorales@transandina.cl', '$2b$12$oubyYWlGL2oa3EDxKMUU6e9y5tlw9LMymh2GU0WX15rqA6P3DyxO2');
