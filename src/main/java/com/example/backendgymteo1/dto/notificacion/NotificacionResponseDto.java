package com.example.backendgymteo1.dto.notificacion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de notificación del usuario")
public class NotificacionResponseDto {

    @Schema(description = "Identificador único de la notificación", example = "1")
    private Integer id;

    @Schema(description = "Tipo de notificación", example = "VENCIMIENTO_MEMBRESIA")
    private String tipo;

    @Schema(description = "Título de la notificación", example = "Recordatorio de Vencimiento de Membresía")
    private String titulo;

    @Schema(description = "Mensaje o contenido detallado", example = "Tu membresía vencerá el 2026-10-15 (5 días restantes). Por favor acércate a recepción.")
    private String mensaje;

    @Schema(description = "Fecha y hora de creación de la notificación", example = "2026-10-07T08:00:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Indica si la notificación ya fue leída por el usuario", example = "false")
    private Boolean leida;
}
