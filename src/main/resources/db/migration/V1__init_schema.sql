-- =============================================================================
-- V1__init_schema.sql: Creación inicial del esquema de base de datos para el Gimnasio
-- Motor: MariaDB / MySQL (InnoDB, UTF-8 MB4)
-- =============================================================================

-- 1. Roles del sistema
CREATE TABLE IF NOT EXISTS rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Usuarios del sistema
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_rol INT NOT NULL,
    dpi VARCHAR(20) UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME,
    eliminado_en DATETIME,
    doble_autenticacion TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Entrenadores
CREATE TABLE IF NOT EXISTS entrenador (
    id_entrenador INT PRIMARY KEY,
    fecha_contratacion DATE NOT NULL,
    CONSTRAINT fk_entrenador_usuario FOREIGN KEY (id_entrenador) REFERENCES usuario (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Recepcionistas
CREATE TABLE IF NOT EXISTS recepcionista (
    id_recepcionista INT PRIMARY KEY,
    fecha_contratacion DATE NOT NULL,
    CONSTRAINT fk_recepcionista_usuario FOREIGN KEY (id_recepcionista) REFERENCES usuario (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Socios
CREATE TABLE IF NOT EXISTS socio (
    id_socio INT PRIMARY KEY,
    fecha_registro DATE NOT NULL,
    CONSTRAINT fk_socio_usuario FOREIGN KEY (id_socio) REFERENCES usuario (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Especializaciones
CREATE TABLE IF NOT EXISTS especializacion (
    id_especializacion INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Especializaciones de Entrenadores
CREATE TABLE IF NOT EXISTS entrenador_especializacion (
    id_entrenador INT NOT NULL,
    id_especializacion INT NOT NULL,
    PRIMARY KEY (id_entrenador, id_especializacion),
    CONSTRAINT fk_entrenadorespecializacion_entrenador FOREIGN KEY (id_entrenador) REFERENCES entrenador (id_entrenador) ON DELETE CASCADE,
    CONSTRAINT fk_entrenadorespecializacion_especializacion FOREIGN KEY (id_especializacion) REFERENCES especializacion (id_especializacion) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Especializaciones de Socios
CREATE TABLE IF NOT EXISTS socio_especializacion (
    id_socio INT NOT NULL,
    id_especializacion INT NOT NULL,
    PRIMARY KEY (id_socio, id_especializacion),
    CONSTRAINT fk_socioespecializacion_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio) ON DELETE CASCADE,
    CONSTRAINT fk_socioespecializacion_especializacion FOREIGN KEY (id_especializacion) REFERENCES especializacion (id_especializacion) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Beneficios
CREATE TABLE IF NOT EXISTS beneficio (
    id_beneficio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Planes de Membresía
CREATE TABLE IF NOT EXISTS plan_membresia (
    id_plan INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    duracion INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Relación Beneficios - Planes
CREATE TABLE IF NOT EXISTS beneficio_plan (
    id_plan INT NOT NULL,
    id_beneficio INT NOT NULL,
    PRIMARY KEY (id_plan, id_beneficio),
    CONSTRAINT fk_beneficioplan_plan FOREIGN KEY (id_plan) REFERENCES plan_membresia (id_plan) ON DELETE CASCADE,
    CONSTRAINT fk_beneficioplan_beneficio FOREIGN KEY (id_beneficio) REFERENCES beneficio (id_beneficio) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Historial de Precios de Planes
CREATE TABLE IF NOT EXISTS historial_precio_plan (
    id_historial_precio INT AUTO_INCREMENT PRIMARY KEY,
    id_plan INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    observacion TEXT,
    CONSTRAINT fk_historialprecio_plan FOREIGN KEY (id_plan) REFERENCES plan_membresia (id_plan),
    CONSTRAINT chk_fechas_historial_precio CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Estados de Membresía
CREATE TABLE IF NOT EXISTS estado_membresia (
    id_estado_membresia INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Membresías
CREATE TABLE IF NOT EXISTS membresia (
    id_membresia INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL,
    id_plan INT NOT NULL,
    id_estado_membresia INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    CONSTRAINT fk_membresia_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio),
    CONSTRAINT fk_membresia_plan FOREIGN KEY (id_plan) REFERENCES plan_membresia (id_plan),
    CONSTRAINT fk_membresia_estado FOREIGN KEY (id_estado_membresia) REFERENCES estado_membresia (id_estado_membresia),
    CONSTRAINT chk_fechas_membresia CHECK (fecha_vencimiento >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Historial de Estados de Membresía
CREATE TABLE IF NOT EXISTS historial_membresia (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_membresia INT NOT NULL,
    id_estado_anterior INT NOT NULL,
    id_estado_nuevo INT NOT NULL,
    fecha_cambio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    CONSTRAINT fk_historial_membresia FOREIGN KEY (id_membresia) REFERENCES membresia (id_membresia),
    CONSTRAINT fk_historial_estado_anterior FOREIGN KEY (id_estado_anterior) REFERENCES estado_membresia (id_estado_membresia),
    CONSTRAINT fk_historial_estado_nuevo FOREIGN KEY (id_estado_nuevo) REFERENCES estado_membresia (id_estado_membresia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Congelamientos de Membresía
CREATE TABLE IF NOT EXISTS congelamiento_membresia (
    id_congelamiento INT AUTO_INCREMENT PRIMARY KEY,
    id_membresia INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    motivo TEXT,
    CONSTRAINT fk_congelamiento_membresia FOREIGN KEY (id_membresia) REFERENCES membresia (id_membresia),
    CONSTRAINT chk_fechas_congelamiento CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Promociones
CREATE TABLE IF NOT EXISTS promocion (
    id_promocion INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    tipo_descuento ENUM('PORCENTAJE', 'MONTO_FIJO') NOT NULL,
    valor_descuento DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT chk_fechas_promocion CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Estados de Factura
CREATE TABLE IF NOT EXISTS estado_factura (
    id_estado_factura INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Facturas
CREATE TABLE IF NOT EXISTS factura (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_membresia INT NOT NULL,
    id_recepcionista INT NOT NULL,
    id_estado_factura INT NOT NULL,
    id_promocion INT,
    monto DECIMAL(10,2) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    observacion TEXT,
    CONSTRAINT fk_factura_membresia FOREIGN KEY (id_membresia) REFERENCES membresia (id_membresia),
    CONSTRAINT fk_factura_recepcionista FOREIGN KEY (id_recepcionista) REFERENCES recepcionista (id_recepcionista),
    CONSTRAINT fk_factura_estado FOREIGN KEY (id_estado_factura) REFERENCES estado_factura (id_estado_factura),
    CONSTRAINT fk_factura_promocion FOREIGN KEY (id_promocion) REFERENCES promocion (id_promocion),
    CONSTRAINT chk_fechas_factura CHECK (fecha_vencimiento >= fecha_emision),
    CONSTRAINT chk_monto_factura CHECK (monto > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Métodos de Pago
CREATE TABLE IF NOT EXISTS metodo_pago (
    id_metodo_pago INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. Comprobantes de Pago
CREATE TABLE IF NOT EXISTS comprobante_pago (
    id_comprobante INT AUTO_INCREMENT PRIMARY KEY,
    id_factura INT NOT NULL,
    id_metodo_pago INT NOT NULL,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_pagado DECIMAL(10,2) NOT NULL,
    referencia VARCHAR(150),
    CONSTRAINT fk_comprobante_factura FOREIGN KEY (id_factura) REFERENCES factura (id_factura),
    CONSTRAINT fk_comprobante_metodo_pago FOREIGN KEY (id_metodo_pago) REFERENCES metodo_pago (id_metodo_pago),
    CONSTRAINT chk_monto_pagado CHECK (monto_pagado > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. Tipos de Clase
CREATE TABLE IF NOT EXISTS tipo_clase (
    id_tipo_clase INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. Estados de Clase
CREATE TABLE IF NOT EXISTS estado_clase (
    id_estado_clase INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. Clases
CREATE TABLE IF NOT EXISTS clase (
    id_clase INT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_clase INT NOT NULL,
    id_entrenador INT NOT NULL,
    id_estado_clase INT NOT NULL,
    horario DATETIME NOT NULL,
    duracion INT NOT NULL,
    cupo_maximo INT NOT NULL,
    CONSTRAINT fk_clase_tipo FOREIGN KEY (id_tipo_clase) REFERENCES tipo_clase (id_tipo_clase),
    CONSTRAINT fk_clase_entrenador FOREIGN KEY (id_entrenador) REFERENCES entrenador (id_entrenador),
    CONSTRAINT fk_clase_estado FOREIGN KEY (id_estado_clase) REFERENCES estado_clase (id_estado_clase),
    CONSTRAINT chk_cupo_maximo CHECK (cupo_maximo > 0),
    CONSTRAINT chk_duracion_clase CHECK (duracion > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. Estados de Inscripción
CREATE TABLE IF NOT EXISTS estado_inscripcion (
    id_estado_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 26. Inscripciones a Clases
CREATE TABLE IF NOT EXISTS inscripcion_clase (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_clase INT NOT NULL,
    id_socio INT NOT NULL,
    id_estado_inscripcion INT NOT NULL,
    fecha_inscripcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_inscripcion UNIQUE (id_clase, id_socio),
    CONSTRAINT fk_inscripcion_clase FOREIGN KEY (id_clase) REFERENCES clase (id_clase),
    CONSTRAINT fk_inscripcion_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio),
    CONSTRAINT fk_inscripcion_estado FOREIGN KEY (id_estado_inscripcion) REFERENCES estado_inscripcion (id_estado_inscripcion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 27. Calificaciones de Clase
CREATE TABLE IF NOT EXISTS calificacion_clase (
    id_calificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_inscripcion INT NOT NULL UNIQUE,
    calificacion TINYINT NOT NULL,
    comentario TEXT,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calificacion_inscripcion FOREIGN KEY (id_inscripcion) REFERENCES inscripcion_clase (id_inscripcion),
    CONSTRAINT chk_calificacion CHECK (calificacion BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 28. Lista de Espera
CREATE TABLE IF NOT EXISTS lista_espera (
    id_espera INT AUTO_INCREMENT PRIMARY KEY,
    id_clase INT NOT NULL,
    id_socio INT NOT NULL,
    posicion INT NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notificado TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uq_lista_espera UNIQUE (id_clase, id_socio),
    CONSTRAINT fk_listaespera_clase FOREIGN KEY (id_clase) REFERENCES clase (id_clase),
    CONSTRAINT fk_listaespera_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio),
    CONSTRAINT chk_posicion CHECK (posicion > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 29. Asistencias
CREATE TABLE IF NOT EXISTS asistencia (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_recepcionista INT NOT NULL,
    id_socio INT NOT NULL,
    fecha DATE NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida TIME,
    CONSTRAINT fk_asistencia_recepcionista FOREIGN KEY (id_recepcionista) REFERENCES recepcionista (id_recepcionista),
    CONSTRAINT fk_asistencia_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio),
    CONSTRAINT chk_horas_asistencia CHECK (hora_salida IS NULL OR hora_salida >= hora_entrada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 30. Asignación de Entrenador
CREATE TABLE IF NOT EXISTS asignacion_entrenador (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL,
    id_entrenador INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    motivo TEXT,
    CONSTRAINT fk_asignacion_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio),
    CONSTRAINT fk_asignacion_entrenador FOREIGN KEY (id_entrenador) REFERENCES entrenador (id_entrenador),
    CONSTRAINT chk_fechas_asignacion CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 31. Rutinas
CREATE TABLE IF NOT EXISTS rutina (
    id_rutina INT AUTO_INCREMENT PRIMARY KEY,
    id_asignacion INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vigente TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_rutina_asignacion FOREIGN KEY (id_asignacion) REFERENCES asignacion_entrenador (id_asignacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 32. Ejercicios
CREATE TABLE IF NOT EXISTS ejercicio (
    id_ejercicio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    grupo_muscular VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 33. Rutina - Ejercicios
CREATE TABLE IF NOT EXISTS rutina_ejercicio (
    id_rutina INT NOT NULL,
    id_ejercicio INT NOT NULL,
    dia_entrenamiento VARCHAR(20) NOT NULL,
    series TINYINT NOT NULL,
    repeticiones TINYINT NOT NULL,
    peso DECIMAL(6,2),
    descanso SMALLINT,
    PRIMARY KEY (id_rutina, id_ejercicio),
    CONSTRAINT fk_rutinaejercicio_rutina FOREIGN KEY (id_rutina) REFERENCES rutina (id_rutina) ON DELETE CASCADE,
    CONSTRAINT fk_rutinaejercicio_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES ejercicio (id_ejercicio),
    CONSTRAINT chk_series CHECK (series > 0),
    CONSTRAINT chk_repeticiones CHECK (repeticiones > 0),
    CONSTRAINT chk_peso CHECK (peso IS NULL OR peso >= 0),
    CONSTRAINT chk_descanso CHECK (descanso IS NULL OR descanso >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 34. Mediciones Físicas
CREATE TABLE IF NOT EXISTS medicion_fisica (
    id_medicion INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL,
    fecha DATE NOT NULL,
    peso DECIMAL(5,2),
    estatura DECIMAL(4,2),
    porcentaje_grasa DECIMAL(5,2),
    masa_muscular DECIMAL(5,2),
    cintura DECIMAL(5,2),
    brazo DECIMAL(5,2),
    pierna DECIMAL(5,2),
    observaciones TEXT,
    CONSTRAINT fk_medicion_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 35. Bitácora de Auditoría
CREATE TABLE IF NOT EXISTS bitacora (
    id_bitacora INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre_tabla VARCHAR(100) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    id_registro INT NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion TEXT,
    CONSTRAINT fk_bitacora_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 36. Códigos de Verificación (2FA / Activación)
CREATE TABLE IF NOT EXISTS codigo_verificacion (
    id_codigo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    codigo VARCHAR(100) NOT NULL,
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME NOT NULL,
    utilizado TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_codigo_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT chk_fecha_expiracion_codigo CHECK (fecha_expiracion > fecha_generacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 37. Recuperación de Contraseña
CREATE TABLE IF NOT EXISTS recuperacion_password (
    id_recuperacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME NOT NULL,
    utilizado TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_recuperacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT chk_fecha_expiracion_recuperacion CHECK (fecha_expiracion > fecha_generacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
