-- =============================================================================
-- V3__seed_users.sql: Inserción de usuarios iniciales y de prueba
-- Incluye roles: ADMIN, RECEPCIONISTA, ENTRENADOR y SOCIOS (CLIENTES)
-- =============================================================================

-- 1. Inserción de Usuarios base
INSERT INTO usuario (id_usuario, id_rol, dpi, nombres, apellidos, correo, telefono, username, password, activo, fecha_creacion, doble_autenticacion) VALUES
-- Administrador principal
(1, 1, '1000000000001', 'Carlos Raúl', 'López (Admin)', 'clp64413@gmail.com', '55550001', 'clopez.admin', '$2a$10$FAqfvfwaohst5xwZEKAHo.nkZcAIMP5gxxukdACWHR/4/f/vPs3yC', 1, NOW(), 0),

-- Recepcionista 1
(2, 2, '1000000000002', 'Carlos Raúl', 'López (Recepción)', 'calin10@outlook.es', '55550002', 'calin10', '$2a$10$X5NfUrdWsXJh3tlATp5mle0okBL7VOifftQ3AKn3RsoFNFXSWChxi', 1, NOW(), 0),

-- Entrenador 1
(3, 3, '1000000000003', 'Carlos Raúl', 'López (Entrenador)', '1.carlosraullopez@gmail.com', '55550003', 'carlos.coach', '$2a$10$XT1WtokklwvXcSQdpcNTU.lLuCemPmtHmOlish53MJfbBckw58Gsa', 1, NOW(), 0),

-- Socios / Clientes de prueba reales
(4, 4, '1000000000004', 'Carlos Raúl', 'López (Socio CUNOC)', 'carloslopez202031871@cunoc.edu.gt', '55550004', 'carlos202031871', '$2a$10$ZSZrXLEPzZQ3dkwbejL9hO5RhEIPR1CGJu7HyeVhV4jCjzPH.Jcpe', 1, NOW(), 0),
(5, 4, '1000000000005', 'Carlos Raúl', 'López (Socio INTECAP)', '2018-072874@intecap.edu.gt', '55550005', 'carlos2018072874', '$2a$10$eIZ1.UG8j3JyAnTyORdt2.YvdUMDVAC8ait/r4DIhJqZmKSFxrDYy', 1, NOW(), 0),

-- Entrenador 2 (Pruebas)
(6, 3, '1000000000006', 'Alejandro', 'García Morales', 'alejandro.garcia@gymdemo.com', '55550006', 'agarcia.coach', '$2a$10$FAqfvfwaohst5xwZEKAHo.nkZcAIMP5gxxukdACWHR/4/f/vPs3yC', 1, NOW(), 0),

-- Recepcionista 2 (Pruebas)
(7, 2, '1000000000007', 'María Fernanda', 'Castro Silva', 'maria.castro@gymdemo.com', '55550007', 'mcastro.recep', '$2a$10$FAqfvfwaohst5xwZEKAHo.nkZcAIMP5gxxukdACWHR/4/f/vPs3yC', 1, NOW(), 0),

-- Socios adicionales (Pruebas)
(8, 4, '1000000000008', 'Lucía Elena', 'Hernández Ruiz', 'lucia.hernandez@gymdemo.com', '55550008', 'lhernandez', '$2a$10$FAqfvfwaohst5xwZEKAHo.nkZcAIMP5gxxukdACWHR/4/f/vPs3yC', 1, NOW(), 0),
(9, 4, '1000000000009', 'Diego Andrés', 'Pineda Estrada', 'diego.pineda@gymdemo.com', '55550009', 'dpineda', '$2a$10$FAqfvfwaohst5xwZEKAHo.nkZcAIMP5gxxukdACWHR/4/f/vPs3yC', 1, NOW(), 0),
(10, 4, '1000000000010', 'Valeria Sofía', 'Méndez Alvarado', 'valeria.mendez@gymdemo.com', '55550010', 'vmendez', '$2a$10$FAqfvfwaohst5xwZEKAHo.nkZcAIMP5gxxukdACWHR/4/f/vPs3yC', 1, NOW(), 0)

ON DUPLICATE KEY UPDATE 
    dpi = VALUES(dpi),
    nombres = VALUES(nombres),
    apellidos = VALUES(apellidos),
    correo = VALUES(correo),
    telefono = VALUES(telefono),
    username = VALUES(username),
    password = VALUES(password),
    activo = VALUES(activo);

-- 2. Registro de Recepcionistas en su tabla correspondiente
INSERT INTO recepcionista (id_recepcionista, fecha_contratacion) VALUES
(2, '2024-01-15'),
(7, '2024-02-01')
ON DUPLICATE KEY UPDATE fecha_contratacion = VALUES(fecha_contratacion);

-- 3. Registro de Entrenadores en su tabla correspondiente
INSERT INTO entrenador (id_entrenador, fecha_contratacion) VALUES
(3, '2024-01-10'),
(6, '2024-02-15')
ON DUPLICATE KEY UPDATE fecha_contratacion = VALUES(fecha_contratacion);

-- 4. Registro de Socios en su tabla correspondiente
INSERT INTO socio (id_socio, fecha_registro) VALUES
(4, '2024-03-01'),
(5, '2024-03-15'),
(8, '2024-04-01'),
(9, '2024-04-10'),
(10, '2024-05-01')
ON DUPLICATE KEY UPDATE fecha_registro = VALUES(fecha_registro);
