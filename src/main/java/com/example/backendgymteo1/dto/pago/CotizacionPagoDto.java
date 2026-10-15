package com.example.backendgymteo1.dto.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Cotización del pago de una membresía: precio base, descuento aplicado, recargo por pago tardío y total a pagar")
public class CotizacionPagoDto {

    @Schema(description = "ID de la membresía cotizada", example = "10")
    private Integer idMembresia;

    @Schema(description = "Precio base del plan", example = "250.00")
    private BigDecimal montoBase;

    @Schema(description = "ID de la promoción aplicada (null si no hay ninguna vigente)", example = "1")
    private Integer idPromocion;

    @Schema(description = "Nombre de la promoción aplicada", example = "Promo Fin de Año")
    private String nombrePromocion;

    @Schema(description = "Descuento aplicado", example = "25.00")
    private BigDecimal descuento;

    @Schema(description = "Indica si la membresía está vencida al momento del pago", example = "true")
    private boolean pagoTardio;

    @Schema(description = "Días de atraso respecto a la fecha de vencimiento", example = "5")
    private long diasAtraso;

    @Schema(description = "Porcentaje de recargo configurado por pago tardío", example = "10.00")
    private BigDecimal porcentajeRecargo;

    @Schema(description = "Recargo por pago tardío (porcentaje sobre el precio base)", example = "25.00")
    private BigDecimal recargo;

    @Schema(description = "Total a pagar = base - descuento + recargo", example = "250.00")
    private BigDecimal total;

    @Schema(description = "Fecha usada para la cotización", example = "2026-10-15")
    private LocalDate fechaCalculo;
}
