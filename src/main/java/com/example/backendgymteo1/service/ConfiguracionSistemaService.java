package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.configuracion.RecargoTardioResponseDto;
import com.example.backendgymteo1.entity.ConfiguracionSistema;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.ConfiguracionSistemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionSistemaService {

    static final BigDecimal RECARGO_TARDIO_POR_DEFECTO = new BigDecimal("10.00");

    private final ConfiguracionSistemaRepository configuracionRepository;
    private final AuditoriaService auditoriaService;

    public BigDecimal obtenerRecargoTardioPorcentaje() {
        return configuracionRepository.findByClave(ConfiguracionSistema.RECARGO_TARDIO_PORCENTAJE)
                .map(c -> parsear(c.getValor()))
                .orElse(RECARGO_TARDIO_POR_DEFECTO);
    }

    public RecargoTardioResponseDto consultarRecargoTardio() {
        return configuracionRepository.findByClave(ConfiguracionSistema.RECARGO_TARDIO_PORCENTAJE)
                .map(c -> RecargoTardioResponseDto.builder()
                        .porcentaje(parsear(c.getValor()))
                        .fechaActualizacion(c.getFechaActualizacion())
                        .build())
                .orElseGet(() -> RecargoTardioResponseDto.builder()
                        .porcentaje(RECARGO_TARDIO_POR_DEFECTO)
                        .build());
    }

    @Transactional(rollbackFor = Exception.class)
    public RecargoTardioResponseDto actualizarRecargoTardio(BigDecimal porcentaje, User usuarioActual) {
        if (porcentaje == null || porcentaje.signum() < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("El porcentaje de recargo debe estar entre 0 y 100");
        }
        BigDecimal nuevo = porcentaje.setScale(2, RoundingMode.HALF_UP);

        ConfiguracionSistema config = configuracionRepository
                .findByClave(ConfiguracionSistema.RECARGO_TARDIO_PORCENTAJE)
                .orElseGet(() -> ConfiguracionSistema.builder()
                        .clave(ConfiguracionSistema.RECARGO_TARDIO_PORCENTAJE)
                        .descripcion("Porcentaje (0-100) de recargo sobre el precio del plan cuando la membresia ya vencio al momento del pago")
                        .valor(RECARGO_TARDIO_POR_DEFECTO.toPlainString())
                        .build());

        BigDecimal anterior = parsear(config.getValor());
        config.setValor(nuevo.toPlainString());
        config.setFechaActualizacion(LocalDateTime.now());
        config = configuracionRepository.save(config);

        auditoriaService.registrar(usuarioActual, "configuracion_sistema", "UPDATE", config.getId(),
                String.format("Recargo por pago tardío modificado: %s%% -> %s%%", anterior.toPlainString(), nuevo.toPlainString()));

        return RecargoTardioResponseDto.builder()
                .porcentaje(nuevo)
                .fechaActualizacion(config.getFechaActualizacion())
                .build();
    }

    private BigDecimal parsear(String valor) {
        try {
            BigDecimal v = new BigDecimal(valor.trim());
            if (v.signum() < 0 || v.compareTo(new BigDecimal("100")) > 0) {
                log.warn("[CONFIG] Valor fuera de rango para recargo_tardio_porcentaje: {}. Se usa el valor por defecto.", valor);
                return RECARGO_TARDIO_POR_DEFECTO;
            }
            return v.setScale(2, RoundingMode.HALF_UP);
        } catch (RuntimeException e) {
            log.warn("[CONFIG] Valor inválido para recargo_tardio_porcentaje: '{}'. Se usa el valor por defecto.", valor);
            return RECARGO_TARDIO_POR_DEFECTO;
        }
    }
}
