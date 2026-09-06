ALTER TABLE asistencia
    ADD COLUMN IF NOT EXISTS id_usuario_registro INT NULL AFTER id_recepcionista;

UPDATE asistencia SET id_usuario_registro = id_recepcionista WHERE id_usuario_registro IS NULL;

ALTER TABLE asistencia
    MODIFY COLUMN id_recepcionista INT NULL;

ALTER TABLE asistencia
    ADD CONSTRAINT fk_asistencia_usuario_registro FOREIGN KEY (id_usuario_registro) REFERENCES usuario (id_usuario);
