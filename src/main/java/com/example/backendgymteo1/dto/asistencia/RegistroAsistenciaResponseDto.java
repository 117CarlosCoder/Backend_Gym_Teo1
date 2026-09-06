package com.example.backendgymteo1.dto.asistencia;

import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "Confirmación de registro de asistencia con los datos completos del socio")
public class RegistroAsistenciaResponseDto {

    @Schema(description = "Mensaje de confirmación del registro", example = "Asistencia registrada exitosamente")
    private String mensaje;

    @Schema(description = "Identificador de la asistencia generada", example = "15")
    private Integer idAsistencia;

    @Schema(description = "Fecha en que se registró la asistencia", example = "2026-09-05")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de ingreso registrada", example = "17:30:00")
    private LocalTime horaEntrada;

    @Schema(description = "ID del usuario que registró la asistencia (ADMIN o RECEPCIONISTA)", example = "1")
    private Integer registradoPorId;

    @Schema(description = "Nombre completo del usuario que registró la asistencia", example = "Carlos Raúl López")
    private String registradoPorNombre;

    @Schema(description = "Rol del usuario que registró la asistencia", example = "ADMIN")
    private String registradoPorRol;

    @Schema(description = "ID del recepcionista", example = "2")
    private Integer recepcionistaId;

    @Schema(description = "Nombre completo del recepcionista", example = "Carlos Raúl López")
    private String recepcionistaNombre;

    @Schema(description = "ID de la sucursal donde se registró la asistencia", example = "1")
    private Integer sucursalId;

    @Schema(description = "Nombre de la sucursal donde se registró la asistencia", example = "Sucursal Central (Zona 1)")
    private String sucursalNombre;

    @Schema(description = "Datos completos del socio al que se le registró la asistencia")
    private SocioResponseDto socio;
}
