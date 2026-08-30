-- =============================================================================
-- V2__seed_catalogs.sql: Carga de catálogos y datos iniciales requeridos
-- =============================================================================

-- 1. Roles del Sistema
INSERT INTO rol (id_rol, nombre, descripcion) VALUES
(1, 'ADMIN', 'Administrador total del sistema del gimnasio'),
(2, 'RECEPCIONISTA', 'Personal de recepción, control de asistencias, pagos y socios'),
(3, 'ENTRENADOR', 'Instructor / Entrenador físico para asignaciones y clases'),
(4, 'CLIENTE', 'Socio / Miembro del gimnasio')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);

-- 2. Estados de Membresía
INSERT INTO estado_membresia (id_estado_membresia, nombre, descripcion) VALUES
(1, 'ACTIVA', 'Membresía vigente y con acceso habilitado'),
(2, 'VENCIDA', 'Membresía caducada pendiente de renovación'),
(3, 'CONGELADA', 'Membresía temporalmente suspendida por solicitud'),
(4, 'CANCELADA', 'Membresía dada de baja definitivamente')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);

-- 3. Estados de Factura
INSERT INTO estado_factura (id_estado_factura, nombre, descripcion) VALUES
(1, 'PENDIENTE', 'Factura emitida pendiente de pago'),
(2, 'PAGADA', 'Factura cancelada y con comprobante'),
(3, 'ANULADA', 'Factura cancelada o invalidada')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);

-- 4. Estados de Clase
INSERT INTO estado_clase (id_estado_clase, nombre, descripcion) VALUES
(1, 'PROGRAMADA', 'Clase agendada y abierta para inscripciones'),
(2, 'EN_CURSO', 'Clase actualmente en desarrollo'),
(3, 'COMPLETADA', 'Clase finalizada con registro de asistencia'),
(4, 'CANCELADA', 'Clase suspendida por el gimnasio o entrenador')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);

-- 5. Estados de Inscripción
INSERT INTO estado_inscripcion (id_estado_inscripcion, nombre, descripcion) VALUES
(1, 'CONFIRMADA', 'Inscripción confirmada con cupo reservado'),
(2, 'EN_ESPERA', 'Socio en lista de espera por cupo lleno'),
(3, 'CANCELADA', 'Inscripción dada de baja por el socio o admin'),
(4, 'ASISTIO', 'Socio asistió y completó la clase')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);

-- 6. Métodos de Pago
INSERT INTO metodo_pago (id_metodo_pago, nombre, descripcion) VALUES
(1, 'EFECTIVO', 'Pago en efectivo en ventanilla'),
(2, 'TARJETA_CREDITO', 'Pago con tarjeta de crédito'),
(3, 'TARJETA_DEBITO', 'Pago con tarjeta de débito'),
(4, 'TRANSFERENCIA', 'Transferencia bancaria o depósito')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);

-- 7. Tipos de Clase iniciales
INSERT INTO tipo_clase (id_tipo_clase, nombre, descripcion) VALUES
(1, 'Spinning', 'Entrenamiento cardiovascular intensivo en bicicleta estática'),
(2, 'CrossFit', 'Entrenamiento funcional de alta intensidad'),
(3, 'Yoga', 'Disciplina física y mental de flexibilidad y postura'),
(4, 'Pilates', 'Sistema de entrenamiento físico y control corporal'),
(5, 'Zumba', 'Clase aeróbica basada en ritmos y bailes latinos')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);
