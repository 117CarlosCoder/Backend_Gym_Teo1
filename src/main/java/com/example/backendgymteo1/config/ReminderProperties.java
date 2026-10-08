package com.example.backendgymteo1.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "app.reminders")
@Validated
public class ReminderProperties {

    private boolean enabled = false;

    @Min(value = 1, message = "days debe ser un entero positivo mayor a 0")
    private int days = 7;

    @NotBlank(message = "La expresión cron de recordatorios no puede estar vacía")
    private String cron = "0 0 9 * * *";

    @NotBlank(message = "La zona horaria de recordatorios no puede estar vacía")
    private String zone = "America/Guatemala";

    @Min(value = 1, message = "max-attempts debe ser al menos 1")
    private int maxAttempts = 3;

    @Min(value = 1, message = "retry-minutes debe ser al menos 1")
    private int retryMinutes = 60;

    private long dispatcherDelayMs = 60000;

    @AssertTrue(message = "La expresión cron configurada no es válida")
    public boolean isCronValido() {
        if (cron == null || cron.isBlank()) {
            return false;
        }
        return CronExpression.isValidExpression(cron);
    }

    @AssertTrue(message = "La zona horaria configurada no es válida")
    public boolean isZoneValida() {
        if (zone == null || zone.isBlank()) {
            return false;
        }
        try {
            ZoneId.of(zone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostConstruct
    public void validarConfiguracion() {
        if (!isCronValido()) {
            throw new IllegalArgumentException("Expresión cron inválida: " + cron);
        }
        if (!isZoneValida()) {
            throw new IllegalArgumentException("Zona horaria inválida: " + zone);
        }
    }
}
