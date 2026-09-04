package com.example.backendgymteo1.dto.socio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resumen de la última asistencia registrada del socio")
public class UltimaAsistenciaDto {

    @Schema(description = "Identificador del registro de asistencia", example = "42")
    private Integer idAsistencia;

    @Schema(description = "Fecha de la asistencia", example = "2026-09-03")
    private LocalDate fecha;

    @Schema(description = "Hora de ingreso al gimnasio", example = "07:30:00")
    private LocalTime horaEntrada;

    @Schema(description = "Hora de salida del gimnasio", example = "09:00:00")
    private LocalTime horaSalida;

    @Schema(description = "Nombre del recepcionista que registró la entrada", example = "María Castro")
    private String recepcionista;
}
