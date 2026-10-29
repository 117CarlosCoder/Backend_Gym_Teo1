package com.example.backendgymteo1.dto.configuracion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Reglas del gimnasio para congelar membresías")
public class ReglasCongelamientoDto {

    @NotNull(message = "Los días mínimos son obligatorios")
    @Min(value = 1, message = "Los días mínimos deben ser al menos 1")
    @Max(value = 365, message = "Los días mínimos no pueden superar 365")
    @Schema(description = "Días mínimos que puede durar un congelamiento", example = "7")
    private Integer diasMinimo;

    @NotNull(message = "Los días máximos son obligatorios")
    @Min(value = 1, message = "Los días máximos deben ser al menos 1")
    @Max(value = 365, message = "Los días máximos no pueden superar 365")
    @Schema(description = "Días máximos que puede durar un congelamiento", example = "30")
    private Integer diasMaximo;

    @NotNull(message = "La cantidad máxima por año es obligatoria")
    @Min(value = 1, message = "Debe permitirse al menos 1 congelamiento por año")
    @Max(value = 12, message = "No se permiten más de 12 congelamientos por año")
    @Schema(description = "Congelamientos permitidos por membresía en un mismo año", example = "2")
    private Integer maximoPorAnio;

    @Schema(description = "Última modificación de las reglas (solo respuesta)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaActualizacion;
}
