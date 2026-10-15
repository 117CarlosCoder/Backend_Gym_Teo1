package com.example.backendgymteo1.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromocionTest {

    private Promocion promo(Promocion.TipoDescuento tipo, String valor) {
        return Promocion.builder()
                .nombre("P").tipoDescuento(tipo).valorDescuento(new BigDecimal(valor))
                .fechaInicio(LocalDate.of(2026, 12, 1)).fechaFin(LocalDate.of(2026, 12, 31)).activo(true).build();
    }

    @Test
    @DisplayName("Descuento porcentual se calcula sobre el precio base con 2 decimales")
    void porcentaje() {
        assertEquals(new BigDecimal("32.50"),
                promo(Promocion.TipoDescuento.PORCENTAJE, "12.5").calcularDescuento(new BigDecimal("260.00")));
    }

    @Test
    @DisplayName("Descuento de monto fijo se aplica tal cual")
    void montoFijo() {
        assertEquals(new BigDecimal("30.00"),
                promo(Promocion.TipoDescuento.MONTO_FIJO, "30").calcularDescuento(new BigDecimal("250.00")));
    }

    @Test
    @DisplayName("El descuento nunca supera el precio base")
    void topeEnPrecioBase() {
        assertEquals(new BigDecimal("100.00"),
                promo(Promocion.TipoDescuento.MONTO_FIJO, "500").calcularDescuento(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Vigencia: inclusiva en ambos extremos, falsa fuera del rango o si está inactiva")
    void vigencia() {
        Promocion p = promo(Promocion.TipoDescuento.PORCENTAJE, "10");
        assertTrue(p.estaVigente(LocalDate.of(2026, 12, 1)));
        assertTrue(p.estaVigente(LocalDate.of(2026, 12, 31)));
        assertFalse(p.estaVigente(LocalDate.of(2026, 11, 30)));
        assertFalse(p.estaVigente(LocalDate.of(2027, 1, 1)));
        p.setActivo(false);
        assertFalse(p.estaVigente(LocalDate.of(2026, 12, 15)));
    }
}
