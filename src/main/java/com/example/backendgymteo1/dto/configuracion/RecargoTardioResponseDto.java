package com.example.backendgymteo1.dto.configuracion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Configuración vigente del recargo por pago tardío")
public class RecargoTardioResponseDto {

    @Schema(description = "Porcentaje de recargo sobre el precio del plan", example = "10.00")
    private BigDecimal porcentaje;

    @Schema(description = "Última modificación (null si nunca se ha modificado o se usa el valor por defecto)", example = "2026-10-15T09:50:00")
    private LocalDateTime fechaActualizacion;
}
