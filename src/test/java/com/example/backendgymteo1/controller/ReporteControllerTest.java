package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.reporte.DashboardResumenDto;
import com.example.backendgymteo1.dto.reporte.MetricaAsistenciaDto;
import com.example.backendgymteo1.dto.reporte.ReporteIngresosDto;
import com.example.backendgymteo1.dto.reporte.ReporteMiembrosEstadoDto;
import com.example.backendgymteo1.dto.reporte.ReporteRenovacionesDto;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReporteService reporteService;

    @InjectMocks
    private ReporteController reporteController;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1)
                .correo("admin@gym.com")
                .nombres("Admin")
                .apellidos("General")
                .rol(Rol.admin())
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(reporteController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentUser.class)
                                && parameter.getParameterType().equals(User.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        return currentUser;
                    }
                })
                .build();
    }

    @Test
    void testGetReporteIngresos() throws Exception {
        ReporteIngresosDto dto = ReporteIngresosDto.builder()
                .anio(2026)
                .totalPeriodo(new BigDecimal("15000.00"))
                .cantidadPagosPeriodo(50L)
                .ingresosPorMes(Collections.emptyList())
                .ingresosPorMetodoPago(Collections.emptyList())
                .build();

        when(reporteService.getReporteIngresos(eq(2026))).thenReturn(dto);

        mockMvc.perform(get("/reportes/ingresos")
                        .param("anio", "2026")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anio").value(2026))
                .andExpect(jsonPath("$.totalPeriodo").value(15000.00))
                .andExpect(jsonPath("$.cantidadPagosPeriodo").value(50));
    }

    @Test
    void testGetReporteMiembrosEstado() throws Exception {
        ReporteMiembrosEstadoDto dto = ReporteMiembrosEstadoDto.builder()
                .totalSocios(200L)
                .activos(160L)
                .morosos(40L)
                .porcentajeActivos(80.0)
                .porcentajeMorosos(20.0)
                .build();

        when(reporteService.getReporteMiembrosEstado(any(), any())).thenReturn(dto);

        mockMvc.perform(get("/reportes/miembros-estado")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSocios").value(200))
                .andExpect(jsonPath("$.activos").value(160))
                .andExpect(jsonPath("$.morosos").value(40))
                .andExpect(jsonPath("$.porcentajeActivos").value(80.0));
    }

    @Test
    void testGetReporteMiembrosEstadoConFiltros() throws Exception {
        ReporteMiembrosEstadoDto dto = ReporteMiembrosEstadoDto.builder()
                .totalSocios(200L)
                .activos(160L)
                .morosos(40L)
                .porcentajeActivos(80.0)
                .porcentajeMorosos(20.0)
                .build();

        when(reporteService.getReporteMiembrosEstado(eq("MOROSO"), eq(1))).thenReturn(dto);

        mockMvc.perform(get("/reportes/miembros-estado")
                        .param("estado", "MOROSO")
                        .param("sucursalId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSocios").value(200));
    }

    @Test
    void testGetReporteRenovaciones() throws Exception {
        ReporteRenovacionesDto dto = ReporteRenovacionesDto.builder()
                .anio(2026)
                .totalMembresiasPeriodo(100L)
                .nuevasMembresias(40L)
                .totalRenovaciones(60L)
                .tasaRenovacionGlobal(60.0)
                .build();

        when(reporteService.getReporteRenovaciones(eq(2026))).thenReturn(dto);

        mockMvc.perform(get("/reportes/renovaciones")
                        .param("anio", "2026")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembresiasPeriodo").value(100))
                .andExpect(jsonPath("$.totalRenovaciones").value(60))
                .andExpect(jsonPath("$.tasaRenovacionGlobal").value(60.0));
    }

    @Test
    void testGetDashboardResumen() throws Exception {
        DashboardResumenDto dto = DashboardResumenDto.builder()
                .ingresosMesActual(new BigDecimal("20000.00"))
                .ingresosMesAnterior(new BigDecimal("18000.00"))
                .crecimientoIngresosPorcentaje(11.11)
                .totalSociosActivos(150L)
                .totalSociosMorosos(30L)
                .membresiasPorVencer7Dias(5L)
                .asistenciasHoy(45L)
                .asistenciasMesActual(800L)
                .build();

        when(reporteService.getDashboardResumen()).thenReturn(dto);

        mockMvc.perform(get("/reportes/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresosMesActual").value(20000.00))
                .andExpect(jsonPath("$.totalSociosActivos").value(150))
                .andExpect(jsonPath("$.asistenciasHoy").value(45));
    }

    @Test
    void testGetDashboardResumen_recepcionistaNoVeIngresos() throws Exception {
        currentUser.setRol(Rol.recepcionista());
        DashboardResumenDto dto = DashboardResumenDto.builder()
                .ingresosMesActual(new BigDecimal("20000.00"))
                .ingresosMesAnterior(new BigDecimal("18000.00"))
                .crecimientoIngresosPorcentaje(11.11)
                .totalSociosActivos(150L)
                .asistenciasHoy(45L)
                .build();

        when(reporteService.getDashboardResumen()).thenReturn(dto);

        mockMvc.perform(get("/reportes/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresosMesActual").doesNotExist())
                .andExpect(jsonPath("$.ingresosMesAnterior").doesNotExist())
                .andExpect(jsonPath("$.crecimientoIngresosPorcentaje").doesNotExist())
                .andExpect(jsonPath("$.totalSociosActivos").value(150))
                .andExpect(jsonPath("$.asistenciasHoy").value(45));
    }

    @Test
    void testGetMetricasAsistencia() throws Exception {
        MetricaAsistenciaDto dto = MetricaAsistenciaDto.builder()
                .rol("ADMIN")
                .asistenciasHoy(50L)
                .asistenciasSemanaActual(250L)
                .asistenciasMesActual(1000L)
                .promedioDiarioMes(33.33)
                .build();

        when(reporteService.getMetricasAsistencia(any(User.class), eq(10), eq(2026), eq(1))).thenReturn(dto);

        mockMvc.perform(get("/reportes/asistencias")
                        .param("mes", "10")
                        .param("anio", "2026")
                        .param("sucursalId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.asistenciasHoy").value(50))
                .andExpect(jsonPath("$.asistenciasMesActual").value(1000));
    }
}
