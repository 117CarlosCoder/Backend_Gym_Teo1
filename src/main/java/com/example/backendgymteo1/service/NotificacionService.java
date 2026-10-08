package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.notificacion.NotificacionResponseDto;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.Notificacion;
import com.example.backendgymteo1.entity.TipoNotificacion;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.NotificacionMapper;
import com.example.backendgymteo1.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final RecordatorioCorreoService recordatorioCorreoService;
    private final NotificacionMapper notificacionMapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<NotificacionResponseDto> findMyNotificaciones(User currentUser, Boolean leida) {
        if (currentUser == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }

        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndLeidaOptional(
                currentUser.getId(),
                leida
        );

        return notificaciones.stream()
                .map(notificacionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countNoLeidas(Integer usuarioId) {
        if (usuarioId == null) {
            return 0;
        }
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificacionResponseDto marcarComoLeida(Integer notificacionId, User currentUser) {
        if (currentUser == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }

        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        int filas = notificacionRepository.marcarComoLeidaSiNoLeida(notificacionId, currentUser.getId(), ahora);

        Notificacion notificacion = notificacionRepository.findByIdAndUsuarioId(notificacionId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notificación con ID " + notificacionId + " no encontrada"));

        if (filas > 0) {
            log.info("Notificación {} marcada como leída por el usuario {}", notificacionId, currentUser.getId());
        }

        return notificacionMapper.toDto(notificacion);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<Notificacion> crearNotificacionYRecordatorioEnTransaccion(
            Membresia membresia,
            LocalDate fechaVencimiento,
            int umbralDias,
            int diasRestantes) {

        if (notificacionRepository.existsByMembresiaIdAndFechaVencimientoAndUmbralDias(
                membresia.getId(), fechaVencimiento, umbralDias)) {
            return Optional.empty();
        }

        User socioUser = membresia.getSocio().getUsuario();
        String planNombre = membresia.getPlan() != null ? membresia.getPlan().getNombre() : "Plan contratado";
        String nombres = socioUser.getNombres() != null ? socioUser.getNombres().trim() : "";
        String apellidos = socioUser.getApellidos() != null ? socioUser.getApellidos().trim() : "";
        String nombreSocio = (nombres + " " + apellidos).trim();
        if (nombreSocio.length() > 255) {
            nombreSocio = nombreSocio.substring(0, 255);
        }

        String titulo = "Recordatorio de Vencimiento de Membresía";
        String mensaje = String.format(
                "Tu membresía del plan '%s' vencerá el %s (%d día(s) restante(s)). Te invitamos a renovarla en recepción.",
                planNombre, fechaVencimiento, diasRestantes
        );

        Notificacion notificacion = Notificacion.builder()
                .usuario(socioUser)
                .membresia(membresia)
                .tipo(TipoNotificacion.VENCIMIENTO_MEMBRESIA)
                .titulo(titulo)
                .mensaje(mensaje)
                .leida(false)
                .fechaCreacion(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS))
                .fechaVencimiento(fechaVencimiento)
                .umbralDias(umbralDias)
                .build();

        notificacion = notificacionRepository.save(notificacion);

        String idempotencyKey = String.format("remind-m-%d-v-%s-d-%d",
                membresia.getId(), fechaVencimiento, umbralDias);

        recordatorioCorreoService.registrarRecordatorioPendienteEnTransaccion(
                notificacion,
                membresia,
                idempotencyKey,
                nombreSocio,
                planNombre,
                diasRestantes
        );

        log.info("Notificación interna {} y recordatorio de correo creados atómicamente para membresía {} (Vence: {}, Umbral: {}d)",
                notificacion.getId(), membresia.getId(), fechaVencimiento, umbralDias);

        return Optional.of(notificacion);
    }
}
