package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto;
import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto.Agrupacion;
import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto.AsistenciaSucursal;
import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto.PuntoSerie;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricasAsistenciaService {

    static final long MAX_DIAS_RANGO = 366;
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AsistenciaRepository asistenciaRepository;

    public MetricasAsistenciaResponseDto obtenerMetricas(LocalDate desde, LocalDate hasta,
                                                         Agrupacion agrupacion, Integer idSucursal) {
        if (desde == null || hasta == null) {
            throw new RuntimeException("Las fechas desde y hasta son obligatorias");
        }
        if (hasta.isBefore(desde)) {
            throw new RuntimeException("La fecha final no puede ser anterior a la fecha inicial");
        }
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (dias > MAX_DIAS_RANGO) {
            throw new RuntimeException("El rango máximo permitido es de " + MAX_DIAS_RANGO + " días");
        }
        Agrupacion agrup = agrupacion != null ? agrupacion : Agrupacion.DIA;

        Map<LocalDate, Long> porDia = new LinkedHashMap<>();
        for (Object[] fila : asistenciaRepository.contarPorDia(desde, hasta, idSucursal)) {
            porDia.put(aLocalDate(fila[0]), ((Number) fila[1]).longValue());
        }

        Map<String, Long> serie = new LinkedHashMap<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            serie.merge(periodo(dia, agrup), porDia.getOrDefault(dia, 0L), Long::sum);
        }

        long total = porDia.values().stream().mapToLong(Long::longValue).sum();

        List<AsistenciaSucursal> porSucursal = asistenciaRepository.contarPorSucursal(desde, hasta, idSucursal)
                .stream()
                .map(fila -> AsistenciaSucursal.builder()
                        .idSucursal(fila[0] != null ? ((Number) fila[0]).intValue() : null)
                        .nombreSucursal(fila[1] != null ? (String) fila[1] : "Sin sucursal")
                        .total(((Number) fila[2]).longValue())
                        .build())
                .toList();

        return MetricasAsistenciaResponseDto.builder()
                .desde(desde)
                .hasta(hasta)
                .agrupacion(agrup)
                .idSucursal(idSucursal)
                .totalAsistencias(total)
                .sociosUnicos(asistenciaRepository.contarSociosUnicos(desde, hasta, idSucursal))
                .promedioDiario(BigDecimal.valueOf(total).divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP))
                .serie(serie.entrySet().stream()
                        .map(e -> PuntoSerie.builder().periodo(e.getKey()).total(e.getValue()).build())
                        .toList())
                .porSucursal(porSucursal)
                .build();
    }

    static String periodo(LocalDate dia, Agrupacion agrupacion) {
        return switch (agrupacion) {
            case DIA -> dia.toString();
            case SEMANA -> String.format("%d-W%02d", dia.get(IsoFields.WEEK_BASED_YEAR), dia.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MES -> dia.format(MES);
        };
    }

    private static LocalDate aLocalDate(Object valor) {
        if (valor instanceof LocalDate ld) {
            return ld;
        }
        return ((Date) valor).toLocalDate();
    }
}
