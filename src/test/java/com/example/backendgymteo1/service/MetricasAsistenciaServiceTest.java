package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto;
import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto.Agrupacion;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class MetricasAsistenciaServiceTest {

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @InjectMocks
    private MetricasAsistenciaService service;

    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 10, 10);

    private void stubDatos() {
        when(asistenciaRepository.contarPorDia(DESDE, HASTA, null)).thenReturn(List.of(
                new Object[]{LocalDate.of(2026, 10, 1), 3L},
                new Object[]{LocalDate.of(2026, 10, 5), 2L},
                new Object[]{LocalDate.of(2026, 10, 6), 5L}));
        when(asistenciaRepository.contarPorSucursal(DESDE, HASTA, null)).thenReturn(List.of(
                new Object[]{1, "Central", 7L},
                new Object[]{null, null, 3L}));
        when(asistenciaRepository.contarSociosUnicos(DESDE, HASTA, null)).thenReturn(4L);
    }

    @Test
    @DisplayName("Agrupación por día: incluye días sin asistencias con 0 y calcula total y promedio")
    void porDia() {
        stubDatos();

        MetricasAsistenciaResponseDto r = service.obtenerMetricas(DESDE, HASTA, Agrupacion.DIA, null);

        assertEquals(10, r.getSerie().size());
        assertEquals("2026-10-01", r.getSerie().get(0).getPeriodo());
        assertEquals(3, r.getSerie().get(0).getTotal());
        assertEquals(0, r.getSerie().get(1).getTotal());
        assertEquals(10, r.getTotalAsistencias());
        assertEquals(4, r.getSociosUnicos());
        assertEquals(new BigDecimal("1.00"), r.getPromedioDiario());
    }

    @Test
    @DisplayName("Agrupación por semana ISO: el 1 de oct (jueves) y el 5-6 de oct (lun-mar) caen en semanas distintas")
    void porSemana() {
        stubDatos();

        MetricasAsistenciaResponseDto r = service.obtenerMetricas(DESDE, HASTA, Agrupacion.SEMANA, null);

        assertEquals(2, r.getSerie().size());
        assertEquals("2026-W40", r.getSerie().get(0).getPeriodo());
        assertEquals(3, r.getSerie().get(0).getTotal());
        assertEquals("2026-W41", r.getSerie().get(1).getPeriodo());
        assertEquals(7, r.getSerie().get(1).getTotal());
    }

    @Test
    @DisplayName("Agrupación por mes: un solo período con el total del rango")
    void porMes() {
        stubDatos();

        MetricasAsistenciaResponseDto r = service.obtenerMetricas(DESDE, HASTA, Agrupacion.MES, null);

        assertEquals(1, r.getSerie().size());
        assertEquals("2026-10", r.getSerie().get(0).getPeriodo());
        assertEquals(10, r.getSerie().get(0).getTotal());
    }

    @Test
    @DisplayName("Asistencias históricas sin sucursal se reportan como 'Sin sucursal'")
    void sinSucursal() {
        stubDatos();

        MetricasAsistenciaResponseDto r = service.obtenerMetricas(DESDE, HASTA, Agrupacion.DIA, null);

        assertEquals(2, r.getPorSucursal().size());
        assertEquals("Central", r.getPorSucursal().get(0).getNombreSucursal());
        assertEquals(1, r.getPorSucursal().get(0).getIdSucursal());
        assertNull(r.getPorSucursal().get(1).getIdSucursal());
        assertEquals("Sin sucursal", r.getPorSucursal().get(1).getNombreSucursal());
    }

    @Test
    @DisplayName("Sin asistencias en el rango: totales en cero y promedio 0.00")
    void sinDatos() {
        when(asistenciaRepository.contarPorDia(DESDE, HASTA, 2)).thenReturn(List.of());
        when(asistenciaRepository.contarPorSucursal(DESDE, HASTA, 2)).thenReturn(List.of());
        when(asistenciaRepository.contarSociosUnicos(DESDE, HASTA, 2)).thenReturn(0L);

        MetricasAsistenciaResponseDto r = service.obtenerMetricas(DESDE, HASTA, Agrupacion.DIA, 2);

        assertEquals(0, r.getTotalAsistencias());
        assertEquals(new BigDecimal("0.00"), r.getPromedioDiario());
        assertEquals(2, r.getIdSucursal());
    }

    @Test
    @DisplayName("Sin agrupación indicada se usa DIA")
    void agrupacionPorDefecto() {
        stubDatos();

        assertEquals(Agrupacion.DIA, service.obtenerMetricas(DESDE, HASTA, null, null).getAgrupacion());
    }

    @Test
    @DisplayName("Rango invertido es rechazado sin consultar la base")
    void rangoInvertido() {
        assertThrows(RuntimeException.class, () -> service.obtenerMetricas(HASTA, DESDE, Agrupacion.DIA, null));
        verify(asistenciaRepository, never()).contarPorDia(any(), any(), any());
    }

    @Test
    @DisplayName("Rango mayor a 366 días es rechazado")
    void rangoDemasiadoGrande() {
        assertThrows(RuntimeException.class,
                () -> service.obtenerMetricas(DESDE, DESDE.plusDays(366), Agrupacion.DIA, null));
    }

    @Test
    @DisplayName("Rango de exactamente 366 días es aceptado")
    void rangoLimite() {
        LocalDate fin = DESDE.plusDays(365);
        when(asistenciaRepository.contarPorDia(DESDE, fin, null)).thenReturn(List.of());
        when(asistenciaRepository.contarPorSucursal(DESDE, fin, null)).thenReturn(List.of());
        when(asistenciaRepository.contarSociosUnicos(DESDE, fin, null)).thenReturn(0L);

        assertEquals(366, service.obtenerMetricas(DESDE, fin, Agrupacion.DIA, null).getSerie().size());
    }

    @Test
    @DisplayName("Fechas obligatorias")
    void fechasNulas() {
        assertThrows(RuntimeException.class, () -> service.obtenerMetricas(null, HASTA, Agrupacion.DIA, null));
        assertThrows(RuntimeException.class, () -> service.obtenerMetricas(DESDE, null, Agrupacion.DIA, null));
    }
}
