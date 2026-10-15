-- Desglose del monto de la factura: monto_base (precio del plan), descuento (promocion vigente)
-- y recargo (pago tardio). monto = monto_base - descuento + recargo.
-- Las facturas historicas quedan con monto_base NULL, descuento 0 y recargo 0.
ALTER TABLE factura
    ADD COLUMN monto_base DECIMAL(10,2) NULL AFTER id_promocion,
    ADD COLUMN descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER monto_base,
    ADD COLUMN recargo DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER descuento;

-- Facturas existentes: el monto base es el monto ya registrado
UPDATE factura SET monto_base = monto WHERE monto_base IS NULL;
