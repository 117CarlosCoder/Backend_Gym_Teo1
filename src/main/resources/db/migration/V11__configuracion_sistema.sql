-- Configuracion del sistema editable por el ADMIN (clave/valor).
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id_configuracion INT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO configuracion_sistema (clave, valor, descripcion) VALUES
('recargo_tardio_porcentaje', '10',
 'Porcentaje (0-100) de recargo sobre el precio del plan cuando la membresia ya vencio al momento del pago');
