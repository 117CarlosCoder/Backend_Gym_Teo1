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
@Schema(description = "Reglas del gimnasio para pagar una membresía en cuotas o abonos")
public class ReglasAbonoDto {

    @NotNull(message = "El porcentaje mínimo del primer abono es obligatorio")
    @Min(value = 1, message = "El primer abono debe ser al menos el 1% del total")
    @Max(value = 100, message = "El porcentaje no puede superar 100")
    @Schema(description = "Porcentaje mínimo del total que se paga en el primer abono (100 = no se permiten abonos)", example = "50")
    private Integer porcentajeMinimoInicial;

    @NotNull(message = "El plazo para liquidar el saldo es obligatorio")
    @Min(value = 1, message = "El plazo debe ser de al menos 1 día")
    @Max(value = 90, message = "El plazo no puede superar 90 días")
    @Schema(description = "Días de plazo para liquidar el saldo pendiente", example = "15")
    private Integer plazoDias;

    @Schema(description = "Última modificación de las reglas (solo respuesta)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaActualizacion;
}
