package com.example.backendgymteo1.service;

import com.example.backendgymteo1.config.ReminderProperties;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.Notificacion;
import com.example.backendgymteo1.entity.RecordatorioCorreo;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.MembresiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordatorioVencimientoService {

    private static final int BATCH_SIZE = 50;
    private static final int STALE_LOCK_MINUTES = 5;
    private static final int IDEMPOTENCY_MAX_HOURS = 24;

    private final MembresiaRepository membresiaRepository;
    private final NotificacionService notificacionService;
    private final RecordatorioCorreoService recordatorioCorreoService;
    private final EmailService emailService;
    private final ReminderProperties reminderProperties;
    private final Clock clock;

    public void generarYProcesarRecordatorios() {
        LocalDate hoy = LocalDate.now(clock);
        generarRecordatoriosParaFecha(hoy);
        procesarEnviosPendientes();
    }

    public void generarRecordatoriosParaFecha(LocalDate hoy) {
        int reminderDays = reminderProperties.getDays();
        LocalDate maxFechaVencimiento = hoy.plusDays(reminderDays);

        log.info("Iniciando detección de membresías por vencer entre {} y {} (Ventana: {} días)",
                hoy, maxFechaVencimiento, reminderDays);

        List<Membresia> membresias = membresiaRepository.findMembresiasParaRecordatorio(hoy, maxFechaVencimiento);
        log.info("Membresías activas encontradas en ventana de vencimiento: {}", membresias.size());

        int generados = 0;

        for (Membresia m : membresias) {
            long diasRestantes = ChronoUnit.DAYS.between(hoy, m.getFechaVencimiento());

            if (diasRestantes >= 0 && diasRestantes <= reminderDays) {
                try {
                    Optional<Notificacion> notifOpt = notificacionService.crearNotificacionYRecordatorioEnTransaccion(
                            m, m.getFechaVencimiento(), reminderDays, (int) diasRestantes);

                    if (notifOpt.isPresent()) {
                        generados++;
                    }
                } catch (DataIntegrityViolationException e) {
                    if (isUniqueConstraintViolation(e)) {
                        log.debug("Aviso duplicado evitado por unicidad DB para membresía {} en fecha {}",
                                m.getId(), m.getFechaVencimiento());
                    } else {
                        log.error("Error de integridad no atribuible a duplicado al generar recordatorio para membresía {}: {}",
                                m.getId(), e.getMessage());
                        throw e;
                    }
                }
            }
        }

        log.info("Generación de recordatorios finalizada para fecha {}. Nuevos avisos creados: {}", hoy, generados);
    }

    public void procesarEnviosPendientes() {
        LocalDateTime staleThreshold = LocalDateTime.now(clock).minusMinutes(STALE_LOCK_MINUTES);
        recordatorioCorreoService.reconciliarReclamosAgotadosStale(staleThreshold);
        List<Integer> candidatos = recordatorioCorreoService.obtenerCandidatosParaEnvio(BATCH_SIZE, staleThreshold);

        if (candidatos.isEmpty()) {
            log.debug("No hay recordatorios de correo pendientes de despacho");
            return;
        }

        log.info("Procesando lote de recordatorios de correo pendientes: {} registros", candidatos.size());

        for (Integer candidateId : candidatos) {
            String claimToken = UUID.randomUUID().toString();
            boolean reclamado = recordatorioCorreoService.reclamarCandidatoParaEnvio(candidateId, staleThreshold, claimToken);
            if (!reclamado) {
                // Otra instancia tomó el registro o las condiciones de elegibilidad cambiaron
                continue;
            }

            RecordatorioCorreo recordatorio = recordatorioCorreoService.obtenerConDetalles(candidateId).orElse(null);
            if (recordatorio == null) {
                continue;
            }

            despacharRecordatorio(recordatorio, claimToken);
        }
    }

    private void despacharRecordatorio(RecordatorioCorreo recordatorio, String claimToken) {
        // Límite de idempotencia del proveedor (Resend garantiza idempotencia durante 24 horas)
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (recordatorio.getFechaCreacion() != null &&
                recordatorio.getFechaCreacion().isBefore(ahora.minusHours(IDEMPOTENCY_MAX_HOURS)) &&
                recordatorio.getIntentos() > 1) {
            recordatorioCorreoService.marcarFalloDefinitivo(
                    recordatorio.getId(),
                    claimToken,
                    "Límite de ventana de idempotencia del proveedor excedido (>24 horas desde creación inicial)"
            );
            return;
        }

        Integer membresiaId = recordatorio.getMembresia().getId();
        Optional<Membresia> membresiaOpt = membresiaRepository.findByIdWithDetails(membresiaId);

        if (membresiaOpt.isEmpty()) {
            recordatorioCorreoService.marcarObsoleto(recordatorio.getId(), claimToken, "Membresía no existe en base de datos");
            return;
        }

        Membresia actual = membresiaOpt.get();
        boolean sigueActiva = (actual.getEstadoMembresia() != null &&
                (Integer.valueOf(1).equals(actual.getEstadoMembresia().getId()) ||
                        "ACTIVA".equalsIgnoreCase(actual.getEstadoMembresia().getNombre())));

        boolean mismaFecha = actual.getFechaVencimiento() != null &&
                actual.getFechaVencimiento().isEqual(recordatorio.getNotificacion().getFechaVencimiento());

        LocalDate fechaVencimiento = actual.getFechaVencimiento();
        LocalDate hoy = LocalDate.now(clock);
        boolean noVencidaAun = fechaVencimiento != null && !fechaVencimiento.isBefore(hoy);
        boolean yaInicio = actual.getFechaInicio() != null && !actual.getFechaInicio().isAfter(hoy);

        User usuario = actual.getSocio() != null ? actual.getSocio().getUsuario() : null;
        boolean usuarioValido = usuario != null && usuario.isEstado() && usuario.getEliminadoEn() == null;

        if (!sigueActiva || !mismaFecha || !noVencidaAun || !yaInicio || !usuarioValido) {
            String motivo = String.format(
                    "Membresía no vigente para el aviso (activa=%s, mismaFecha=%s, noVencida=%s, yaInicio=%s, usuarioValido=%s)",
                    sigueActiva, mismaFecha, noVencidaAun, yaInicio, usuarioValido);
            recordatorioCorreoService.marcarObsoleto(recordatorio.getId(), claimToken, motivo);
            return;
        }

        String destinatario = recordatorio.getDestinatario();
        String nombreSocio = recordatorio.getNombreSocioSnapshot();
        String nombrePlan = recordatorio.getNombrePlanSnapshot();
        long diasRestantes = recordatorio.getDiasRestantesSnapshot();
        String idempotencyKey = recordatorio.getIdempotencyKey();

        try {
            String resendId = emailService.enviarRecordatorioVencimiento(
                    destinatario,
                    nombreSocio,
                    nombrePlan,
                    fechaVencimiento,
                    diasRestantes,
                    idempotencyKey
            );

            recordatorioCorreoService.marcarExito(recordatorio.getId(), claimToken, resendId);
        } catch (Exception e) {
            String mensajeError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            int intentosActuales = recordatorio.getIntentos();

            if (intentosActuales < recordatorio.getMaxIntentos()) {
                LocalDateTime proximaEjecucion = ahora.plusMinutes(reminderProperties.getRetryMinutes());
                recordatorioCorreoService.marcarFalloConReintento(
                        recordatorio.getId(), claimToken, proximaEjecucion, mensajeError);
            } else {
                recordatorioCorreoService.marcarFalloDefinitivo(
                        recordatorio.getId(), claimToken, mensajeError);
            }
        }
    }

    private boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("duplicate entry") ||
                        lower.contains("uq_notif_membresia_vencimiento_umbral") ||
                        lower.contains("uq_rec_correo_idempotency") ||
                        lower.contains("unique constraint")) {
                    return true;
                }
            }
            if (cause instanceof java.sql.SQLException sqlEx) {
                if (sqlEx.getErrorCode() == 1062) {
                    return true;
                }
                if ("23000".equals(sqlEx.getSQLState()) && sqlEx.getMessage() != null && sqlEx.getMessage().toLowerCase().contains("duplicate")) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
