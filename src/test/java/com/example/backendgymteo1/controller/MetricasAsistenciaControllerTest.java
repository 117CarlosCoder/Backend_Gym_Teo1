package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto;
import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto.Agrupacion;
import com.example.backendgymteo1.dto.asistencia.MetricasAsistenciaResponseDto.PuntoSerie;
import com.example.backendgymteo1.exception.GlobalExceptionHandler;
import com.example.backendgymteo1.service.MetricasAsistenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MetricasAsistenciaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MetricasAsistenciaService service;

    @InjectMocks
    private MetricasAsistenciaController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /asistencias/metricas retorna 200 con la serie y los totales")
    void ok() throws Exception {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 10, 7);
        when(service.obtenerMetricas(desde, hasta, Agrupacion.SEMANA, 1)).thenReturn(
                MetricasAsistenciaResponseDto.builder()
                        .desde(desde).hasta(hasta).agrupacion(Agrupacion.SEMANA).idSucursal(1)
                        .totalAsistencias(12).sociosUnicos(5).promedioDiario(new BigDecimal("1.71"))
                        .serie(List.of(PuntoSerie.builder().periodo("2026-W40").total(12).build()))
                        .porSucursal(List.of())
                        .build());

        mockMvc.perform(get("/asistencias/metricas")
                        .param("desde", "2026-10-01").param("hasta", "2026-10-07")
                        .param("agrupacion", "SEMANA").param("idSucursal", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAsistencias").value(12))
                .andExpect(jsonPath("$.sociosUnicos").value(5))
                .andExpect(jsonPath("$.serie[0].periodo").value("2026-W40"));
    }

    @Test
    @DisplayName("Sin agrupación se usa DIA y sin idSucursal se consultan todas")
    void defaults() throws Exception {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 10, 7);
        when(service.obtenerMetricas(desde, hasta, Agrupacion.DIA, null)).thenReturn(
                MetricasAsistenciaResponseDto.builder().agrupacion(Agrupacion.DIA).totalAsistencias(0).build());

        mockMvc.perform(get("/asistencias/metricas").param("desde", "2026-10-01").param("hasta", "2026-10-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agrupacion").value("DIA"));
    }

    @Test
    @DisplayName("Faltan parámetros obligatorios: 400")
    void faltanParametros() throws Exception {
        mockMvc.perform(get("/asistencias/metricas").param("desde", "2026-10-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rango inválido reportado por el servicio: 400 con mensaje")
    void rangoInvalido() throws Exception {
        LocalDate desde = LocalDate.of(2026, 10, 10);
        LocalDate hasta = LocalDate.of(2026, 10, 1);
        when(service.obtenerMetricas(desde, hasta, Agrupacion.DIA, null))
                .thenThrow(new RuntimeException("La fecha final no puede ser anterior a la fecha inicial"));

        mockMvc.perform(get("/asistencias/metricas").param("desde", "2026-10-10").param("hasta", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha final no puede ser anterior a la fecha inicial"));
    }

    @Test
    @DisplayName("Agrupación desconocida: 400")
    void agrupacionInvalida() throws Exception {
        mockMvc.perform(get("/asistencias/metricas")
                        .param("desde", "2026-10-01").param("hasta", "2026-10-07").param("agrupacion", "ANIO"))
                .andExpect(status().isBadRequest());
    }
}
