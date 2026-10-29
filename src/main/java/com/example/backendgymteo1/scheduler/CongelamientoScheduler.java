package com.example.backendgymteo1.scheduler;

import com.example.backendgymteo1.service.CongelamientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Cada madrugada (y al iniciar) congela o reactiva las membresías según sus congelamientos. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CongelamientoScheduler {

    private final CongelamientoService congelamientoService;

    @Scheduled(cron = "0 5 0 * * *", zone = "${app.reminders.zone:America/Guatemala}")
    public void sincronizarDiario() {
        sincronizar();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sincronizarAlIniciar() {
        sincronizar();
    }

    private void sincronizar() {
        try {
            congelamientoService.sincronizarEstados(LocalDate.now());
        } catch (Exception e) {
            log.error("Error al sincronizar los estados de membresías congeladas: {}", e.getMessage(), e);
        }
    }
}
