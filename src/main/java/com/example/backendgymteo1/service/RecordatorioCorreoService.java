package com.example.backendgymteo1.service;

import com.example.backendgymteo1.config.ReminderProperties;
import com.example.backendgymteo1.entity.EstadoRecordatorioCorreo;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.Notificacion;
import com.example.backendgymteo1.entity.RecordatorioCorreo;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.RecordatorioCorreoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordatorioCorreoService {

    private final RecordatorioCorreoRepository recordatorioCorreoRepository;
    private final ReminderProperties reminderProperties;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRED)
    public RecordatorioCorreo registrarRecordatorioPendienteEnTransaccion(
            Notificacion notificacion,
            Membresia membresia,
            String idempotencyKey,
            String nombreSocioSnapshot,
            String nombrePlanSnapshot,
            int diasRestantesSnapshot) {

        User socioUser = membresia.getSocio().getUsuario();
        String destinatario = socioUser.getCorreo();
        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        RecordatorioCorreo recordatorio = RecordatorioCorreo.builder()
                .notificacion(notificacion)
                .membresia(membresia)
                .usuario(socioUser)
                .destinatario(destinatario)
                .nombreSocioSnapshot(nombreSocioSnapshot)
                .nombrePlanSnapshot(nombrePlanSnapshot)
                .diasRestantesSnapshot(diasRestantesSnapshot)
                .estado(EstadoRecordatorioCorreo.PENDIENTE)
                .intentos(0)
                .maxIntentos(reminderProperties.getMaxAttempts())
                .proximaEjecucion(ahora)
                .idempotencyKey(idempotencyKey)
                .fechaCreacion(ahora)
                .build();

        return recordatorioCorreoRepository.save(recordatorio);
    }

    @Transactional(readOnly = true)
    public List<Integer> obtenerCandidatosParaEnvio(int batchSize, LocalDateTime staleThreshold) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        return recordatorioCorreoRepository.findCandidateIdsParaEnvio(
                ahora,
                staleThreshold,
                PageRequest.of(0, batchSize)
        );
    }

    @Transactional
    public boolean reclamarCandidatoParaEnvio(Integer candidateId, LocalDateTime staleThreshold, String claimToken) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        int filasActualizadas = recordatorioCorreoRepository.claimRecordatorio(
                candidateId,
                ahora,
                staleThreshold,
                claimToken
        );
        return filasActualizadas > 0;
    }

    @Transactional(readOnly = true)
    public Optional<RecordatorioCorreo> obtenerConDetalles(Integer id) {
        return recordatorioCorreoRepository.findByIdWithDetails(id);
    }

    @Transactional
    public void marcarExito(Integer id, String claimToken, String resendId) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        int filas = recordatorioCorreoRepository.marcarExitoConFence(id, claimToken, resendId, ahora);
        if (filas > 0) {
            log.info("Recordatorio correo {} marcado como ENVIADO (Proveedor ID: {})", id, resendId);
        } else {
            log.warn("Fencing evitó update de éxito para recordatorio correo {} con token {}", id, claimToken);
        }
    }

    @Transactional
    public void marcarFalloConReintento(Integer id, String claimToken, LocalDateTime proximaEjecucion, String error) {
        int filas = recordatorioCorreoRepository.marcarFalloConFence(id, claimToken, proximaEjecucion, error);
        if (filas > 0) {
            log.warn("Recordatorio correo {} falló. Próximo reintento: {}", id, proximaEjecucion);
        } else {
            log.warn("Fencing evitó update de fallo para recordatorio correo {} con token {}", id, claimToken);
        }
    }

    @Transactional
    public void marcarFalloDefinitivo(Integer id, String claimToken, String error) {
        int filas = recordatorioCorreoRepository.marcarFalloDefinitivoConFence(id, claimToken, error);
        if (filas > 0) {
            log.error("Recordatorio correo {} alcanzó el límite máximo de intentos. Error final: {}", id, error);
        } else {
            log.warn("Fencing evitó update de fallo definitivo para recordatorio correo {} con token {}", id, claimToken);
        }
    }

    @Transactional
    public void marcarObsoleto(Integer id, String claimToken, String motivo) {
        int filas = recordatorioCorreoRepository.marcarObsoletoConFence(id, claimToken, motivo);
        if (filas > 0) {
            log.info("Recordatorio correo {} marcado como obsoleto/cancelado sin envío: {}", id, motivo);
        } else {
            log.warn("Fencing evitó update de obsoleto para recordatorio correo {} con token {}", id, claimToken);
        }
    }

    @Transactional
    public int reconciliarReclamosAgotadosStale(LocalDateTime staleThreshold) {
        int actualizados = recordatorioCorreoRepository.terminarReclamosAgotadosStale(staleThreshold);
        if (actualizados > 0) {
            log.warn("Se reconciliaron {} recordatorios abandonados en estado ENVIANDO con intentos agotados a FALLIDO", actualizados);
        }
        return actualizados;
    }
}
