package com.example.backendgymteo1.dto.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de respuesta para los métodos de pago disponibles")
public class MetodoPagoResponseDto {

    @Schema(description = "ID del método de pago", example = "1")
    private Integer id;

    @Schema(description = "Nombre del método de pago", example = "EFECTIVO")
    private String nombre;

    @Schema(description = "Descripción del método de pago", example = "Pago en efectivo en recepción")
    private String descripcion;
}
