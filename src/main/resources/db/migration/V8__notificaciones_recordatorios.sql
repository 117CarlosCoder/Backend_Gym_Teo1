-- =============================================================================
-- V8__notificaciones_recordatorios.sql: Sistema de notificaciones en bandeja y
-- seguimiento persistente de recordatorios de vencimiento por correo electrónico
-- Motor: MariaDB / MySQL (InnoDB, UTF-8 MB4)
-- =============================================================================

-- 1. Tabla de Notificaciones en Bandeja de Entrada (Cliente)
CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_membresia INT NULL,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leida TINYINT(1) NOT NULL DEFAULT 0,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura DATETIME NULL,
    fecha_vencimiento DATE NULL,
    umbral_dias INT NULL,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_notificacion_membresia FOREIGN KEY (id_membresia) REFERENCES membresia (id_membresia) ON DELETE SET NULL,
    CONSTRAINT uq_notif_membresia_vencimiento_umbral UNIQUE (id_membresia, fecha_vencimiento, umbral_dias)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notificacion_usuario_fecha ON notificacion (id_usuario, fecha_creacion DESC);
CREATE INDEX idx_notificacion_usuario_leida ON notificacion (id_usuario, leida);

-- 2. Tabla de Seguimiento Persistente de Recordatorios por Correo
CREATE TABLE IF NOT EXISTS recordatorio_correo (
    id_recordatorio_correo INT AUTO_INCREMENT PRIMARY KEY,
    id_notificacion INT NOT NULL,
    id_membresia INT NOT NULL,
    id_usuario INT NOT NULL,
    destinatario VARCHAR(150) NOT NULL,
    nombre_socio_snapshot VARCHAR(255) NOT NULL,
    nombre_plan_snapshot VARCHAR(100) NOT NULL,
    dias_restantes_snapshot INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    intentos INT NOT NULL DEFAULT 0,
    max_intentos INT NOT NULL DEFAULT 3,
    proxima_ejecucion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_intento DATETIME NULL,
    fecha_envio DATETIME NULL,
    locked_at DATETIME NULL,
    claim_token VARCHAR(36) NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    proveedor_mensaje_id VARCHAR(100) NULL,
    error_mensaje TEXT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rec_correo_notificacion FOREIGN KEY (id_notificacion) REFERENCES notificacion (id_notificacion) ON DELETE CASCADE,
    CONSTRAINT fk_rec_correo_membresia FOREIGN KEY (id_membresia) REFERENCES membresia (id_membresia) ON DELETE CASCADE,
    CONSTRAINT fk_rec_correo_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT uq_rec_correo_idempotency UNIQUE (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_rec_correo_estado_proxima ON recordatorio_correo (estado, proxima_ejecucion);
CREATE INDEX idx_rec_correo_claim ON recordatorio_correo (estado, locked_at, claim_token);
