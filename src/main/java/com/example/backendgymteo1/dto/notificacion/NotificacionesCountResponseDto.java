package com.example.backendgymteo1.dto.notificacion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Conteo de notificaciones no leídas")
public class NotificacionesCountResponseDto {

    @Schema(description = "Cantidad total de notificaciones no leídas", example = "3")
    private long count;
}
