package com.example.backendgymteo1.dto.pago;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para la creación y registro de un pago de membresía")
public class CreatePagoDto {

    @NotNull(message = "El ID de la membresía es obligatorio")
    @Schema(description = "ID de la membresía a pagar", example = "1")
    private Integer idMembresia;

    @NotNull(message = "El ID del método de pago es obligatorio")
    @Schema(description = "ID del método de pago (1: Efectivo, 2: Tarjeta, 3: Transferencia)", example = "1")
    private Integer idMetodoPago;

    @NotNull(message = "El monto pagado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Schema(description = "Monto pagado", example = "250.00")
    private BigDecimal monto;

    @JsonAlias({"promocionId", "id_promocion"})
    @Schema(description = "ID de la promoción a aplicar (opcional; si se omite se aplica la mejor promoción vigente)", example = "1")
    private Integer idPromocion;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    @JsonAlias({"montoDescuento", "valorDescuento", "descuentoAplicado"})
    @Schema(description = "Monto de descuento directo a aplicar en quetzales (opcional)", example = "25.00")
    private BigDecimal descuento;

    @DecimalMin(value = "0.00", message = "El porcentaje de descuento no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje de descuento no puede ser mayor a 100")
    @JsonAlias({"descuentoPorcentaje"})
    @Schema(description = "Porcentaje de descuento directo a aplicar (opcional, 0 a 100)", example = "10.00")
    private BigDecimal porcentajeDescuento;

    @Schema(description = "Referencia de la transacción (voucher, no. transferencia, etc.)", example = "TRANS-987654")
    private String referencia;

    @Schema(description = "Observaciones adicionales sobre el pago", example = "Pago realizado en caja sucursal central")
    private String observacion;
}
