package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para procesar la cancelación formal de una membresía con opción de reembolso")
public class CancelarMembresiaRequestDto {

    @Schema(description = "Motivo de la cancelación (selección)", example = "Solicitud del cliente por cambio de domicilio", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String motivo;

    @Schema(description = "Comentarios u observaciones adicionales sobre la cancelación", example = "El cliente presentó constancia de mudanza y se acordó reembolso parcial.")
    private String comentarios;

    @Schema(description = "Indica si aplica reembolso económico", example = "true")
    private Boolean reembolso;

    @Schema(description = "Monto a reembolsar al cliente (requerido si reembolso es true)", example = "150.00")
    private BigDecimal montoReembolso;
}
