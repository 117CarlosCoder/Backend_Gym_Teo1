package com.example.backendgymteo1.dto.membresia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Estado actual de la membresía")
public class EstadoMembresiaResponseDto {

    @Schema(description = "Identificador del estado", example = "1")
    private Integer id;

    @Schema(description = "Nombre del estado (ACTIVA, VENCIDA, CONGELADA, CANCELADA)", example = "ACTIVA")
    private String nombre;

    @Schema(description = "Descripción del estado")
    private String descripcion;
}
