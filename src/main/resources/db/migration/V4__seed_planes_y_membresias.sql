-- =============================================================================
-- V4__seed_planes_y_membresias.sql: Catálogo de planes y membresías iniciales
-- Garantiza que los socios base cuenten con membresías activas vigentes
-- para cumplir con las validaciones de seguridad de membresía activa.
-- =============================================================================

-- 1. Catálogo de Planes de Membresía
INSERT INTO plan_membresia (id_plan, nombre, duracion, precio, descripcion) VALUES
(1, 'Plan Mensual', 30, 250.00, 'Acceso ilimitado por 30 días a todas las instalaciones'),
(2, 'Plan Trimestral', 90, 650.00, 'Acceso ilimitado por 90 días con descuento especial'),
(3, 'Plan Semestral', 180, 1200.00, 'Acceso ilimitado por 180 días con evaluación física'),
(4, 'Plan Anual', 365, 2200.00, 'Acceso ilimitado por 365 días con todos los beneficios premium')
ON DUPLICATE KEY UPDATE 
    nombre = VALUES(nombre), 
    duracion = VALUES(duracion), 
    precio = VALUES(precio), 
    descripcion = VALUES(descripcion);

-- 2. Membresías iniciales para Socios base (cumplir validación de membresía activa)
INSERT INTO membresia (id_socio, id_plan, id_estado_membresia, fecha_inicio, fecha_vencimiento)
SELECT 4, 1, 1, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
WHERE NOT EXISTS (SELECT 1 FROM membresia WHERE id_socio = 4 AND id_estado_membresia = 1 AND fecha_vencimiento >= CURRENT_DATE);

INSERT INTO membresia (id_socio, id_plan, id_estado_membresia, fecha_inicio, fecha_vencimiento)
SELECT 5, 2, 1, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY)
WHERE NOT EXISTS (SELECT 1 FROM membresia WHERE id_socio = 5 AND id_estado_membresia = 1 AND fecha_vencimiento >= CURRENT_DATE);

INSERT INTO membresia (id_socio, id_plan, id_estado_membresia, fecha_inicio, fecha_vencimiento)
SELECT 8, 4, 1, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 365 DAY)
WHERE NOT EXISTS (SELECT 1 FROM membresia WHERE id_socio = 8 AND id_estado_membresia = 1 AND fecha_vencimiento >= CURRENT_DATE);

INSERT INTO membresia (id_socio, id_plan, id_estado_membresia, fecha_inicio, fecha_vencimiento)
SELECT 9, 1, 1, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
WHERE NOT EXISTS (SELECT 1 FROM membresia WHERE id_socio = 9 AND id_estado_membresia = 1 AND fecha_vencimiento >= CURRENT_DATE);

INSERT INTO membresia (id_socio, id_plan, id_estado_membresia, fecha_inicio, fecha_vencimiento)
SELECT 10, 3, 1, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY)
WHERE NOT EXISTS (SELECT 1 FROM membresia WHERE id_socio = 10 AND id_estado_membresia = 1 AND fecha_vencimiento >= CURRENT_DATE);

-- 3. Usuario de prueba con Soft Delete (inactivo) para pruebas de seguridad
INSERT INTO usuario (id_usuario, id_rol, dpi, nombres, apellidos, correo, telefono, username, password, activo, fecha_creacion, eliminado_en, doble_autenticacion)
VALUES (11, 4, '1000000000011', 'Usuario', 'Desactivado (Prueba)', 'inactivo.prueba@gymdemo.com', '55550011', 'inactivo.prueba', '$2a$10$FAqfvfwaohst5xwZEKAHo.nkZcAIMP5gxxukdACWHR/4/f/vPs3yC', 0, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE 
    activo = 0, 
    eliminado_en = COALESCE(eliminado_en, NOW());

-- 4. Registro en tabla socio para el usuario desactivado
INSERT INTO socio (id_socio, fecha_registro)
VALUES (11, '2024-01-01')
ON DUPLICATE KEY UPDATE fecha_registro = VALUES(fecha_registro);
