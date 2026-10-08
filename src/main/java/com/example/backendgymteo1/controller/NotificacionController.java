package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.notificacion.NotificacionResponseDto;
import com.example.backendgymteo1.dto.notificacion.NotificacionesCountResponseDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Endpoints para la bandeja de notificaciones persistente del cliente autenticado")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(
            summary = "Consultar bandeja de notificaciones del usuario autenticado (/me)",
            description = "Retorna la lista de notificaciones con filtro opcional por estado leída, ordenadas cronológicamente de forma descendente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionResponseDto>> findMyNotificaciones(
            @CurrentUser User currentUser,
            @RequestParam(required = false) Boolean leida) {

        return ResponseEntity.ok(notificacionService.findMyNotificaciones(currentUser, leida));
    }

    @Operation(
            summary = "Contar notificaciones no leídas del usuario autenticado (/me/no-leidas/count)",
            description = "Retorna la cantidad total de notificaciones pendientes de lectura en formato {count: N}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificacionesCountResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @GetMapping("/me/no-leidas/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacionesCountResponseDto> countNoLeidas(@CurrentUser User currentUser) {
        long count = notificacionService.countNoLeidas(currentUser.getId());
        return ResponseEntity.ok(new NotificacionesCountResponseDto(count));
    }

    @Operation(
            summary = "Marcar notificación como leída (/me/{id}/leida)",
            description = "Marca una notificación como leída. Operación idempotente. Retorna 404 si la notificación no existe o pertenece a otro usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación marcada como leída",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificacionResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notificación inexistente o ajena", content = @Content)
    })
    @PatchMapping("/me/{id}/leida")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacionResponseDto> marcarComoLeida(
            @PathVariable("id") Integer id,
            @CurrentUser User currentUser) {

        return ResponseEntity.ok(notificacionService.marcarComoLeida(id, currentUser));
    }
}
