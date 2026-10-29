package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.configuracion.RecargoTardioResponseDto;
import com.example.backendgymteo1.dto.configuracion.ReglasAbonoDto;
import com.example.backendgymteo1.dto.configuracion.ReglasCongelamientoDto;
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
    static final int CONGELAMIENTO_DIAS_MINIMO_POR_DEFECTO = 7;
    static final int CONGELAMIENTO_DIAS_MAXIMO_POR_DEFECTO = 30;
    static final int CONGELAMIENTO_MAX_POR_ANIO_POR_DEFECTO = 2;
    static final int ABONO_MINIMO_PORCENTAJE_POR_DEFECTO = 50;
    static final int ABONO_PLAZO_DIAS_POR_DEFECTO = 15;

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

    // --- Reglas de congelamiento ---

    public ReglasCongelamientoDto consultarReglasCongelamiento() {
        return ReglasCongelamientoDto.builder()
                .diasMinimo(obtenerEntero(ConfiguracionSistema.CONGELAMIENTO_DIAS_MINIMO, CONGELAMIENTO_DIAS_MINIMO_POR_DEFECTO, 1, 365))
                .diasMaximo(obtenerEntero(ConfiguracionSistema.CONGELAMIENTO_DIAS_MAXIMO, CONGELAMIENTO_DIAS_MAXIMO_POR_DEFECTO, 1, 365))
                .maximoPorAnio(obtenerEntero(ConfiguracionSistema.CONGELAMIENTO_MAX_POR_ANIO, CONGELAMIENTO_MAX_POR_ANIO_POR_DEFECTO, 1, 12))
                .fechaActualizacion(ultimaActualizacion(
                        ConfiguracionSistema.CONGELAMIENTO_DIAS_MINIMO,
                        ConfiguracionSistema.CONGELAMIENTO_DIAS_MAXIMO,
                        ConfiguracionSistema.CONGELAMIENTO_MAX_POR_ANIO))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ReglasCongelamientoDto actualizarReglasCongelamiento(ReglasCongelamientoDto reglas, User usuarioActual) {
        if (reglas == null || reglas.getDiasMinimo() == null || reglas.getDiasMaximo() == null || reglas.getMaximoPorAnio() == null) {
            throw new RuntimeException("Todas las reglas de congelamiento son obligatorias");
        }
        if (reglas.getDiasMinimo() > reglas.getDiasMaximo()) {
            throw new RuntimeException("Los días mínimos no pueden ser mayores que los días máximos");
        }
        guardarValor(ConfiguracionSistema.CONGELAMIENTO_DIAS_MINIMO, reglas.getDiasMinimo(),
                "Dias minimos que puede durar un congelamiento de membresia");
        guardarValor(ConfiguracionSistema.CONGELAMIENTO_DIAS_MAXIMO, reglas.getDiasMaximo(),
                "Dias maximos que puede durar un congelamiento de membresia");
        ConfiguracionSistema ultima = guardarValor(ConfiguracionSistema.CONGELAMIENTO_MAX_POR_ANIO, reglas.getMaximoPorAnio(),
                "Cantidad maxima de congelamientos por membresia en un mismo anio");

        auditoriaService.registrar(usuarioActual, "configuracion_sistema", "UPDATE", ultima.getId(),
                String.format("Reglas de congelamiento: %d a %d días, máximo %d por año",
                        reglas.getDiasMinimo(), reglas.getDiasMaximo(), reglas.getMaximoPorAnio()));
        return consultarReglasCongelamiento();
    }

    // --- Reglas de pagos en abonos ---

    public ReglasAbonoDto consultarReglasAbono() {
        return ReglasAbonoDto.builder()
                .porcentajeMinimoInicial(obtenerEntero(ConfiguracionSistema.ABONO_MINIMO_PORCENTAJE, ABONO_MINIMO_PORCENTAJE_POR_DEFECTO, 1, 100))
                .plazoDias(obtenerEntero(ConfiguracionSistema.ABONO_PLAZO_DIAS, ABONO_PLAZO_DIAS_POR_DEFECTO, 1, 90))
                .fechaActualizacion(ultimaActualizacion(
                        ConfiguracionSistema.ABONO_MINIMO_PORCENTAJE,
                        ConfiguracionSistema.ABONO_PLAZO_DIAS))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ReglasAbonoDto actualizarReglasAbono(ReglasAbonoDto reglas, User usuarioActual) {
        if (reglas == null || reglas.getPorcentajeMinimoInicial() == null || reglas.getPlazoDias() == null) {
            throw new RuntimeException("Todas las reglas de abonos son obligatorias");
        }
        guardarValor(ConfiguracionSistema.ABONO_MINIMO_PORCENTAJE, reglas.getPorcentajeMinimoInicial(),
                "Porcentaje minimo (1-100) del total que se debe pagar en el primer abono");
        ConfiguracionSistema ultima = guardarValor(ConfiguracionSistema.ABONO_PLAZO_DIAS, reglas.getPlazoDias(),
                "Dias de plazo para liquidar el saldo pendiente de una factura pagada en abonos");

        auditoriaService.registrar(usuarioActual, "configuracion_sistema", "UPDATE", ultima.getId(),
                String.format("Reglas de abonos: primer abono mínimo %d%%, plazo %d días",
                        reglas.getPorcentajeMinimoInicial(), reglas.getPlazoDias()));
        return consultarReglasAbono();
    }

    /** Lee un parámetro entero; si falta o es inválido se usa el valor por defecto. */
    int obtenerEntero(String clave, int porDefecto, int minimo, int maximo) {
        return configuracionRepository.findByClave(clave)
                .map(c -> {
                    try {
                        int valor = Integer.parseInt(c.getValor().trim());
                        if (valor < minimo || valor > maximo) {
                            log.warn("[CONFIG] Valor fuera de rango para {}: {}. Se usa el valor por defecto.", clave, valor);
                            return porDefecto;
                        }
                        return valor;
                    } catch (RuntimeException e) {
                        log.warn("[CONFIG] Valor inválido para {}: '{}'. Se usa el valor por defecto.", clave, c.getValor());
                        return porDefecto;
                    }
                })
                .orElse(porDefecto);
    }

    private ConfiguracionSistema guardarValor(String clave, int valor, String descripcion) {
        ConfiguracionSistema config = configuracionRepository.findByClave(clave)
                .orElseGet(() -> ConfiguracionSistema.builder().clave(clave).descripcion(descripcion).build());
        config.setValor(String.valueOf(valor));
        config.setFechaActualizacion(LocalDateTime.now());
        return configuracionRepository.save(config);
    }

    private LocalDateTime ultimaActualizacion(String... claves) {
        LocalDateTime ultima = null;
        for (String clave : claves) {
            LocalDateTime fecha = configuracionRepository.findByClave(clave)
                    .map(ConfiguracionSistema::getFechaActualizacion)
                    .orElse(null);
            if (fecha != null && (ultima == null || fecha.isAfter(ultima))) {
                ultima = fecha;
            }
        }
        return ultima;
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
