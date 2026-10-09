package com.example.backendgymteo1.scheduler;

import com.example.backendgymteo1.config.ReminderProperties;
import com.example.backendgymteo1.service.RecordatorioVencimientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioScheduler {

    private final RecordatorioVencimientoService recordatorioVencimientoService;
    private final ReminderProperties reminderProperties;

    @Scheduled(cron = "${app.reminders.cron:0 0 9 * * *}", zone = "${app.reminders.zone:America/Guatemala}")
    public void ejecutarDeteccionDiaria() {
        if (!reminderProperties.isEnabled()) {
            log.info("Ejecución programada de recordatorios desactivada (app.reminders.enabled=false)");
            return;
        }

        log.info("Disparando tarea programada de detección diaria de vencimientos de membresía");
        try {
            recordatorioVencimientoService.generarYProcesarRecordatorios();
        } catch (Exception e) {
            log.error("Error durante la ejecución diaria del scheduler de recordatorios: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "${app.reminders.dispatcher-delay-ms:60000}", initialDelay = 30000)
    public void ejecutarDespachoYReintentos() {
        if (!reminderProperties.isEnabled()) {
            return;
        }

        try {
            recordatorioVencimientoService.procesarEnviosPendientes();
        } catch (Exception e) {
            log.error("Error durante el despacho periódico de recordatorios de correo: {}", e.getMessage(), e);
        }
    }
}
