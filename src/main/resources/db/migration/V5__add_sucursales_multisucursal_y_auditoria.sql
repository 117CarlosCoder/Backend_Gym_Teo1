-- =============================================================================
-- V5__add_sucursales_multisucursal_y_auditoria.sql:
-- Creación de sucursales, soporte multisucursal para membresías, nuevos campos en usuario/socio,
-- campos de cancelación detallada en membresía y datos iniciales.
-- =============================================================================

-- 1. Tabla de Sucursales
CREATE TABLE IF NOT EXISTS sucursal (
    id_sucursal INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Insertar sucursales de prueba iniciales
INSERT INTO sucursal (id_sucursal, nombre, direccion, telefono, activo) VALUES
(1, 'Sucursal Central (Zona 1)', '12 Calle 5-45 Zona 1, Ciudad de Guatemala', '22340001', 1),
(2, 'Sucursal Norte (Zona 10)', 'Avenida Las Américas 8-20 Zona 10', '22340002', 1),
(3, 'Sucursal Sur (Carretera)', 'Km 14.5 Carretera a El Salvador', '22340003', 1)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), direccion = VALUES(direccion), telefono = VALUES(telefono);

-- 3. Modificar tabla usuario para agregar direccion y fecha_nacimiento
ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS direccion VARCHAR(255) NULL AFTER telefono,
    ADD COLUMN IF NOT EXISTS fecha_nacimiento DATE NULL AFTER direccion;

-- 4. Modificar tabla socio para agregar sucursal de registro/base
ALTER TABLE socio
    ADD COLUMN IF NOT EXISTS id_sucursal INT NULL AFTER fecha_registro;

-- Asignar sucursal 1 por defecto a los socios existentes y aplicar clave foránea
UPDATE socio SET id_sucursal = 1 WHERE id_sucursal IS NULL;

ALTER TABLE socio
    MODIFY COLUMN id_sucursal INT NOT NULL,
    ADD CONSTRAINT fk_socio_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal (id_sucursal);

-- 5. Tabla intermedia para membresías multisucursal
CREATE TABLE IF NOT EXISTS membresia_sucursal (
    id_membresia INT NOT NULL,
    id_sucursal INT NOT NULL,
    PRIMARY KEY (id_membresia, id_sucursal),
    CONSTRAINT fk_membresiasucursal_membresia FOREIGN KEY (id_membresia) REFERENCES membresia (id_membresia) ON DELETE CASCADE,
    CONSTRAINT fk_membresiasucursal_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal (id_sucursal) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Asociar las membresías existentes a la sucursal 1 por defecto
INSERT IGNORE INTO membresia_sucursal (id_membresia, id_sucursal)
SELECT id_membresia, 1 FROM membresia;

-- 6. Agregar campos de cancelación detallada en membresia
ALTER TABLE membresia
    ADD COLUMN IF NOT EXISTS fecha_cancelacion DATETIME NULL AFTER fecha_vencimiento,
    ADD COLUMN IF NOT EXISTS id_usuario_cancelo INT NULL AFTER fecha_cancelacion,
    ADD COLUMN IF NOT EXISTS motivo_cancelacion VARCHAR(100) NULL AFTER id_usuario_cancelo,
    ADD COLUMN IF NOT EXISTS comentarios_cancelacion TEXT NULL AFTER motivo_cancelacion,
    ADD COLUMN IF NOT EXISTS reembolso TINYINT(1) NOT NULL DEFAULT 0 AFTER comentarios_cancelacion,
    ADD COLUMN IF NOT EXISTS monto_reembolso DECIMAL(10,2) NULL AFTER reembolso;

ALTER TABLE membresia
    ADD CONSTRAINT fk_membresia_usuario_cancelo FOREIGN KEY (id_usuario_cancelo) REFERENCES usuario (id_usuario);
