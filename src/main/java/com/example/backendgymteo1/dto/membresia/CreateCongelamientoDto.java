package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para congelar temporalmente una membresía")
public class CreateCongelamientoDto {

    @Schema(description = "Primer día congelado (hoy si se omite)", example = "2026-11-01")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha final del congelamiento es obligatoria")
    @Schema(description = "Último día congelado", example = "2026-11-15")
    private LocalDate fechaFin;

    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    @Schema(description = "Motivo del congelamiento", example = "Viaje de trabajo")
    private String motivo;
}
