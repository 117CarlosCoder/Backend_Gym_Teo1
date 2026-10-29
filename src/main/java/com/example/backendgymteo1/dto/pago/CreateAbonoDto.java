package com.example.backendgymteo1.dto.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Abono al saldo pendiente de una factura pagada en cuotas")
public class CreateAbonoDto {

    @NotNull(message = "El ID de la factura es obligatorio")
    @Schema(description = "Factura con saldo pendiente", example = "15")
    private Integer idFactura;

    @NotNull(message = "El ID del método de pago es obligatorio")
    @Schema(description = "ID del método de pago", example = "1")
    private Integer idMetodoPago;

    @NotNull(message = "El monto del abono es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Schema(description = "Monto abonado (no puede superar el saldo)", example = "100.00")
    private BigDecimal monto;

    @Size(max = 150, message = "La referencia no puede superar 150 caracteres")
    @Schema(description = "Referencia de la transacción", example = "TRANS-123")
    private String referencia;
}
