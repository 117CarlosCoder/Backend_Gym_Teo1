-- =============================================================================
-- V9__ciclo_vida_usuarios_socios.sql
-- Desacoplamiento del ciclo de vida entre cuentas de usuario y perfiles de socio.
-- 1. Permite correo NULL en usuario para soft delete liberando UNIQUE sin colisiones.
-- 2. Agrega activo y eliminado_en en socio para gestión independiente de baja lógica.
-- 3. Normaliza cuentas legacy previamente dadas de baja: activo = 0 y columnas únicas en NULL.
-- =============================================================================

-- 1. Modificar columna correo en usuario a NULL (la restricción UNIQUE permanece)
ALTER TABLE usuario MODIFY COLUMN correo VARCHAR(150) NULL;

-- 2. Agregar columnas de ciclo de vida en la tabla socio
ALTER TABLE socio
    ADD COLUMN IF NOT EXISTS activo TINYINT(1) NOT NULL DEFAULT 1 AFTER id_sucursal,
    ADD COLUMN IF NOT EXISTS eliminado_en DATETIME NULL AFTER activo;

-- 3. Normalizar cuentas de usuario legacy con baja previa (eliminado_en IS NOT NULL)
-- Se garantiza activo = 0 y liberación de identificadores únicos (correo, dpi, username)
UPDATE usuario
SET activo = 0,
    correo = NULL,
    dpi = NULL,
    username = NULL
WHERE eliminado_en IS NOT NULL;
