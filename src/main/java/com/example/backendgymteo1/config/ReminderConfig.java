package com.example.backendgymteo1.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(ReminderProperties.class)
public class ReminderConfig {

    @Bean
    public Clock clock(ReminderProperties reminderProperties) {
        String zoneId = reminderProperties.getZone();
        try {
            return Clock.system(ZoneId.of(zoneId));
        } catch (Exception e) {
            log.warn("Zona horaria '{}' inválida o no encontrada. Usando America/Guatemala por defecto.", zoneId);
            return Clock.system(ZoneId.of("America/Guatemala"));
        }
    }
}
