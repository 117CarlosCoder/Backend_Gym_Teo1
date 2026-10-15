package com.example.backendgymteo1.dto.configuracion;

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
@Schema(description = "DTO para modificar el porcentaje de recargo por pago tardío")
public class UpdateRecargoTardioDto {

    @NotNull(message = "El porcentaje de recargo es obligatorio")
    @DecimalMin(value = "0", message = "El porcentaje no puede ser negativo")
    @DecimalMax(value = "100", message = "El porcentaje no puede ser mayor a 100")
    @Schema(description = "Porcentaje (0-100) sobre el precio del plan", example = "15.00")
    private BigDecimal porcentaje;
}
