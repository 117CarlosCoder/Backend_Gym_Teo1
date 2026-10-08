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
@Schema(description = "DTO de respuesta para los detalles de una factura")
public class FacturaResponseDto {

    @Schema(description = "ID de la factura", example = "10")
    private Integer id;

    @Schema(description = "ID de la membresía asociada", example = "1")
    private Integer idMembresia;

    @Schema(description = "Nombre completo del socio", example = "Juan Pérez")
    private String nombreSocio;

    @Schema(description = "Nombre del plan contratado", example = "Plan Mensual")
    private String nombrePlan;

    @Schema(description = "Estado de la factura", example = "PAGADA")
    private String estadoFactura;

    @Schema(description = "Monto de la factura", example = "250.00")
    private BigDecimal monto;

    @Schema(description = "Fecha de emisión de la factura", example = "2026-10-08")
    private LocalDate fechaEmision;

    @Schema(description = "Fecha de vencimiento de la factura", example = "2026-10-08")
    private LocalDate fechaVencimiento;

    @Schema(description = "Observaciones", example = "Factura generada por pago de membresía")
    private String observacion;
}
