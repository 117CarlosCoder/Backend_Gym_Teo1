package com.example.backendgymteo1.dto.asistencia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Registro de asistencia con hora de entrada, hora de salida y si el socio sigue dentro del gimnasio")
public class AsistenciaDetalleDto {

    @Schema(description = "ID de la asistencia", example = "15")
    private Integer idAsistencia;

    @Schema(description = "ID del socio", example = "8")
    private Integer idSocio;

    @Schema(description = "Nombres del socio", example = "Lucía")
    private String nombresSocio;

    @Schema(description = "Apellidos del socio", example = "Hernández")
    private String apellidosSocio;

    @Schema(description = "Fecha de la visita", example = "2026-10-21")
    private LocalDate fecha;

    @Schema(description = "Hora de entrada", example = "07:45:10")
    private LocalTime horaEntrada;

    @Schema(description = "Hora de salida; null si el socio sigue dentro", example = "09:10:00", nullable = true)
    private LocalTime horaSalida;

    @Schema(description = "true si el socio todavía no ha registrado su salida", example = "false")
    private boolean dentro;

    @Schema(description = "Minutos de permanencia; null si sigue dentro", example = "85", nullable = true)
    private Long minutosPermanencia;

    @Schema(description = "ID de la sucursal", example = "1")
    private Integer sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Sucursal Central")
    private String sucursalNombre;

    @Schema(description = "Usuario que registró la entrada", example = "María Castro")
    private String registradoPor;
}
