package com.example.backendgymteo1.dto.pago;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Referencia de la transacción (voucher, no. transferencia, etc.)", example = "TRANS-987654")
    private String referencia;

    @Schema(description = "Observaciones adicionales sobre el pago", example = "Pago realizado en caja sucursal central")
    private String observacion;
}
