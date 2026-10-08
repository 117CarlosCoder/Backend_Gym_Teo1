package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.notificacion.NotificacionResponseDto;
import com.example.backendgymteo1.entity.Notificacion;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {

    public NotificacionResponseDto toDto(Notificacion notificacion) {
        if (notificacion == null) {
            return null;
        }

        return NotificacionResponseDto.builder()
                .id(notificacion.getId())
                .tipo(notificacion.getTipo() != null ? notificacion.getTipo().name() : null)
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .fechaCreacion(notificacion.getFechaCreacion())
                .leida(notificacion.isLeida())
                .build();
    }
}
