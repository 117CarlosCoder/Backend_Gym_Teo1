-- =============================================================================
-- V6__add_sucursal_to_asistencia.sql: Relacionar asistencia con sucursal
-- =============================================================================

ALTER TABLE asistencia
    ADD COLUMN IF NOT EXISTS id_sucursal INT NULL AFTER id_socio;

-- Asignar sucursal 1 por defecto a los registros existentes si los hay
UPDATE asistencia SET id_sucursal = 1 WHERE id_sucursal IS NULL;

ALTER TABLE asistencia
    ADD CONSTRAINT fk_asistencia_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal (id_sucursal);
