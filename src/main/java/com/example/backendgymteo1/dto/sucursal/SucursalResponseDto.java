package com.example.backendgymteo1.dto.sucursal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de respuesta de una sucursal del gimnasio")
public class SucursalResponseDto {

    @Schema(description = "Identificador único de la sucursal", example = "1")
    private Integer id;

    @Schema(description = "Nombre de la sucursal", example = "Sucursal Central (Zona 1)")
    private String nombre;

    @Schema(description = "Dirección de la sucursal", example = "12 Calle 5-45 Zona 1, Ciudad de Guatemala")
    private String direccion;

    @Schema(description = "Teléfono de contacto de la sucursal", example = "22340001")
    private String telefono;

    @Schema(description = "Estado de actividad de la sucursal", example = "true")
    private boolean activo;
}
