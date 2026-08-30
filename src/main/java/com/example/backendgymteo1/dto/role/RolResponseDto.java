package com.example.backendgymteo1.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Información del rol de usuario")
public class RolResponseDto {

    @Schema(description = "Identificador único del rol", example = "1")
    private Integer id;

    @Schema(description = "Nombre clave del rol", example = "ADMIN")
    private String nombre;

    @Schema(description = "Descripción de los privilegios del rol", example = "Administrador total del sistema del gimnasio")
    private String descripcion;
}
