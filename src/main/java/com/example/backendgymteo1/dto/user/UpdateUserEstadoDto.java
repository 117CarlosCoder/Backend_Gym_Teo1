package com.example.backendgymteo1.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para modificar el estado de activación de un usuario")
public class UpdateUserEstadoDto {

    @Schema(description = "Nuevo estado de la cuenta (true = activo, false = inactivo)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
