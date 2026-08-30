package com.example.backendgymteo1.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta estándar de la API")
public class ApiResponseDto {

    @Schema(description = "Mensaje descriptivo del resultado de la operación", example = "Operación realizada con éxito.")
    private String message;
}
