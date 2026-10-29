-- Ajustes finales del PO (Sprint 6)
-- 1) Congelamiento de membresia: el tiempo congelado se suma al vencimiento.
-- 2) Pagos en cuotas / abonos: una factura PENDIENTE puede recibir varios comprobantes
--    hasta cubrir su monto; el saldo = factura.monto - SUM(comprobante_pago.monto_pagado).

ALTER TABLE congelamiento_membresia
    ADD COLUMN dias INT NOT NULL DEFAULT 0 AFTER fecha_fin,
    ADD COLUMN anulado TINYINT(1) NOT NULL DEFAULT 0 AFTER motivo,
    ADD COLUMN fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER anulado,
    ADD COLUMN registrado_por VARCHAR(150) NULL AFTER fecha_registro;

CREATE INDEX idx_congelamiento_membresia_fechas
    ON congelamiento_membresia (id_membresia, fecha_inicio, fecha_fin);

-- Reglas definidas por el gimnasio (editables por el ADMIN)
INSERT INTO configuracion_sistema (clave, valor, descripcion) VALUES
('congelamiento_dias_minimo', '7',
 'Dias minimos que puede durar un congelamiento de membresia'),
('congelamiento_dias_maximo', '30',
 'Dias maximos que puede durar un congelamiento de membresia'),
('congelamiento_max_por_anio', '2',
 'Cantidad maxima de congelamientos por membresia en un mismo anio'),
('abono_minimo_porcentaje', '50',
 'Porcentaje minimo (1-100) del total que se debe pagar en el primer abono'),
('abono_plazo_dias', '15',
 'Dias de plazo para liquidar el saldo pendiente de una factura pagada en abonos')
ON DUPLICATE KEY UPDATE clave = clave;
