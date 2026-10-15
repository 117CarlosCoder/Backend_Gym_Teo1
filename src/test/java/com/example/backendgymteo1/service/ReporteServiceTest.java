package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.reporte.DashboardResumenDto;
import com.example.backendgymteo1.dto.reporte.MetricaAsistenciaDto;
import com.example.backendgymteo1.dto.reporte.ReporteIngresosDto;
import com.example.backendgymteo1.dto.reporte.ReporteMiembrosEstadoDto;
import com.example.backendgymteo1.dto.reporte.ReporteRenovacionesDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.ComprobantePago;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Factura;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.MetodoPago;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import com.example.backendgymteo1.repository.ComprobantePagoRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ComprobantePagoRepository comprobantePagoRepository;

    @Mock
    private SocioRepository socioRepository;

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private ReporteService reporteService;

    private User userCliente;
    private Socio socio;
    private Sucursal sucursal;
    private PlanMembresia plan;
    private EstadoMembresia estadoActivo;

    @BeforeEach
    void setUp() {
        userCliente = User.builder()
                .id(10)
                .nombres("Carlos")
                .apellidos("Lopez")
                .correo("carlos@gym.com")
                .dpi("2987123450101")
                .telefono("+502 5555-1234")
                .rol(Rol.cliente())
                .build();

        sucursal = Sucursal.builder()
                .id(1)
                .nombre("Central")
                .activo(true)
                .build();

        socio = Socio.builder()
                .id(10)
                .usuario(userCliente)
                .sucursal(sucursal)
                .activo(true)
                .fechaRegistro(LocalDate.now().minusMonths(6))
                .build();

        plan = PlanMembresia.builder()
                .id(1)
                .nombre("Plan Mensual")
                .precio(new BigDecimal("350.00"))
                .build();

        estadoActivo = EstadoMembresia.builder()
                .id(1)
                .nombre("ACTIVA")
                .build();
    }

    @Test
    void testGetReporteIngresos() {
        List<Object[]> rawMensuales = new ArrayList<>();
        rawMensuales.add(new Object[]{2026, 10, new BigDecimal("1500.00"), 5L});
        rawMensuales.add(new Object[]{2026, 9, new BigDecimal("2000.00"), 8L});

        List<Object[]> rawMetodos = new ArrayList<>();
        rawMetodos.add(new Object[]{"TARJETA_CREDITO", new BigDecimal("2000.00"), 8L});
        rawMetodos.add(new Object[]{"EFECTIVO", new BigDecimal("1500.00"), 5L});

        Membresia membresia = Membresia.builder()
                .id(1)
                .socio(socio)
                .plan(plan)
                .build();

        Factura factura = Factura.builder()
                .id(101)
                .membresia(membresia)
                .build();

        ComprobantePago comprobante = ComprobantePago.builder()
                .id(501)
                .factura(factura)
                .metodoPago(MetodoPago.builder().id(1).nombre("EFECTIVO").build())
                .montoPagado(new BigDecimal("350.00"))
                .fechaPago(LocalDateTime.now())
                .referencia("REC-001")
                .build();

        when(comprobantePagoRepository.findIngresosMensuales(2026)).thenReturn(rawMensuales);
        when(comprobantePagoRepository.findIngresosPorMetodoPago(2026)).thenReturn(rawMetodos);
        when(comprobantePagoRepository.findPagosDetallados(eq(2026), any(Pageable.class)))
                .thenReturn(Collections.singletonList(comprobante));

        ReporteIngresosDto resultado = reporteService.getReporteIngresos(2026);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("3500.00"), resultado.getTotalPeriodo());
        assertEquals(13L, resultado.getCantidadPagosPeriodo());
        assertEquals(new BigDecimal("1750.00"), resultado.getIngresoPromedioMensual());
        assertEquals("Septiembre 2026", resultado.getMesMayorIngreso());
        assertEquals(new BigDecimal("2000.00"), resultado.getMontoMayorIngreso());
        assertEquals(2, resultado.getIngresosPorMes().size());
        assertEquals(2, resultado.getIngresosPorMetodoPago().size());
        assertEquals(1, resultado.getUltimosPagos().size());
        assertEquals("Carlos Lopez", resultado.getUltimosPagos().get(0).getNombreSocio());
        assertEquals("Plan Mensual", resultado.getUltimosPagos().get(0).getPlanNombre());
    }

    @Test
    void testGetReporteMiembrosEstado() {
        when(socioRepository.countTotalSociosActivos()).thenReturn(100L);
        when(socioRepository.countSociosConMembresiaActiva(any(LocalDate.class))).thenReturn(80L);
        when(socioRepository.countSociosMorosos(any(LocalDate.class))).thenReturn(20L);

        List<Object[]> planes = new ArrayList<>();
        planes.add(new Object[]{1, "Plan Mensual", 50L});
        planes.add(new Object[]{2, "Plan Anual", 30L});
        when(membresiaRepository.countActiveMembershipsByPlan(any(LocalDate.class))).thenReturn(planes);

        List<Object[]> sucursales = new ArrayList<>();
        sucursales.add(new Object[]{1, "Central", 80L, 20L, 100L});
        when(socioRepository.countSociosPorSucursalYEstado(any(LocalDate.class))).thenReturn(sucursales);

        Membresia memActiva = Membresia.builder()
                .id(1)
                .socio(socio)
                .plan(plan)
                .estadoMembresia(estadoActivo)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaVencimiento(LocalDate.now().plusDays(20))
                .build();

        when(membresiaRepository.findUltimaMembresiaPorSocio()).thenReturn(Collections.singletonList(memActiva));

        List<Object[]> ultimasAsistencias = new ArrayList<>();
        ultimasAsistencias.add(new Object[]{10, LocalDate.now()});
        when(asistenciaRepository.findUltimaFechaAsistenciaPorSocio()).thenReturn(ultimasAsistencias);

        when(socioRepository.findAllActiveWithUser()).thenReturn(Collections.singletonList(socio));

        ReporteMiembrosEstadoDto resultado = reporteService.getReporteMiembrosEstado();

        assertNotNull(resultado);
        assertEquals(100L, resultado.getTotalSocios());
        assertEquals(80L, resultado.getActivos());
        assertEquals(20L, resultado.getMorosos());
        assertEquals(80.0, resultado.getPorcentajeActivos());
        assertEquals(20.0, resultado.getPorcentajeMorosos());
        assertEquals(2, resultado.getDistribucionPorPlan().size());
        assertEquals(1, resultado.getDistribucionPorSucursal().size());
        assertEquals(1, resultado.getMiembros().size());

        var mDetalle = resultado.getMiembros().get(0);
        assertEquals(10, mDetalle.getSocioId());
        assertEquals("Carlos Lopez", mDetalle.getNombreCompleto());
        assertEquals("carlos@gym.com", mDetalle.getCorreo());
        assertEquals("ACTIVO", mDetalle.getEstado());
        assertEquals("Plan Mensual", mDetalle.getPlanNombre());
        assertEquals(20L, mDetalle.getDiasRestantes());
        assertEquals(0L, mDetalle.getDiasMora());
        assertEquals(LocalDate.now(), mDetalle.getUltimaAsistencia());
    }

    @Test
    void testGetReporteMiembrosEstadoConFiltroMoroso() {
        when(socioRepository.countTotalSociosActivos()).thenReturn(100L);
        when(socioRepository.countSociosConMembresiaActiva(any(LocalDate.class))).thenReturn(80L);
        when(socioRepository.countSociosMorosos(any(LocalDate.class))).thenReturn(20L);
        when(membresiaRepository.countActiveMembershipsByPlan(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(socioRepository.countSociosPorSucursalYEstado(any(LocalDate.class))).thenReturn(Collections.emptyList());

        Membresia memVencida = Membresia.builder()
                .id(2)
                .socio(socio)
                .plan(plan)
                .estadoMembresia(EstadoMembresia.builder().id(2).nombre("VENCIDA").build())
                .fechaInicio(LocalDate.now().minusMonths(2))
                .fechaVencimiento(LocalDate.now().minusDays(5))
                .build();

        when(membresiaRepository.findUltimaMembresiaPorSocio()).thenReturn(Collections.singletonList(memVencida));
        when(asistenciaRepository.findUltimaFechaAsistenciaPorSocio()).thenReturn(Collections.emptyList());
        when(socioRepository.findAllActiveWithUser()).thenReturn(Collections.singletonList(socio));

        ReporteMiembrosEstadoDto resultadoMoroso = reporteService.getReporteMiembrosEstado("MOROSO", null);
        assertNotNull(resultadoMoroso);
        assertEquals(1, resultadoMoroso.getMiembros().size());
        assertEquals("MOROSO", resultadoMoroso.getMiembros().get(0).getEstado());
        assertEquals(5L, resultadoMoroso.getMiembros().get(0).getDiasMora());

        ReporteMiembrosEstadoDto resultadoActivo = reporteService.getReporteMiembrosEstado("ACTIVO", null);
        assertNotNull(resultadoActivo);
        assertTrue(resultadoActivo.getMiembros().isEmpty());
    }

    @Test
    void testGetReporteRenovaciones() {
        List<Object[]> totales = new ArrayList<>();
        totales.add(new Object[]{100L, 30L, 70L});
        when(membresiaRepository.countTotalesRenovaciones(2026)).thenReturn(totales);

        Membresia proximasMem = Membresia.builder()
                .id(1)
                .socio(socio)
                .plan(plan)
                .fechaInicio(LocalDate.now().minusDays(25))
                .fechaVencimiento(LocalDate.now().plusDays(5))
                .build();

        when(membresiaRepository.findProximasAVencer(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(proximasMem));

        List<Object[]> meses = new ArrayList<>();
        meses.add(new Object[]{2026, 10, 10L, 20L, 30L});
        when(membresiaRepository.findRenovacionesPorMes(2026)).thenReturn(meses);

        List<Object[]> primeras = new ArrayList<>();
        primeras.add(new Object[]{10, 1});
        when(membresiaRepository.findPrimeraMembresiaIdPorSocio()).thenReturn(primeras);

        when(membresiaRepository.findMembresiasDetalladas(eq(2026), any(Pageable.class)))
                .thenReturn(Collections.singletonList(proximasMem));

        ReporteRenovacionesDto resultado = reporteService.getReporteRenovaciones(2026);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getTotalMembresiasPeriodo());
        assertEquals(30L, resultado.getNuevasMembresias());
        assertEquals(70L, resultado.getTotalRenovaciones());
        assertEquals(70.0, resultado.getTasaRenovacionGlobal());
        assertEquals(1L, resultado.getProximasAVencerSiguienteSemana());
        assertEquals(1, resultado.getRenovacionesPorMes().size());
        assertEquals(1, resultado.getProximasAVencer().size());
        assertEquals("Carlos Lopez", resultado.getProximasAVencer().get(0).getNombreSocio());
        assertEquals(5L, resultado.getProximasAVencer().get(0).getDiasRestantes());
        assertEquals(1, resultado.getUltimasRenovaciones().size());
        assertEquals("NUEVA", resultado.getUltimasRenovaciones().get(0).getTipo());
    }

    @Test
    void testGetDashboardResumen() {
        when(comprobantePagoRepository.sumMontoPagadoBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("5000.00"), new BigDecimal("4000.00"));
        when(socioRepository.countSociosConMembresiaActiva(any(LocalDate.class))).thenReturn(60L);
        when(socioRepository.countSociosMorosos(any(LocalDate.class))).thenReturn(15L);

        Membresia mem = Membresia.builder()
                .id(1)
                .socio(socio)
                .plan(plan)
                .fechaVencimiento(LocalDate.now().plusDays(3))
                .build();
        when(membresiaRepository.findProximasAVencer(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(mem));

        when(asistenciaRepository.countByFecha(any(LocalDate.class))).thenReturn(25L);
        when(asistenciaRepository.countByFechaBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(350L);

        Asistencia asistencia = Asistencia.builder()
                .id(1)
                .socio(socio)
                .sucursal(sucursal)
                .fecha(LocalDate.now())
                .horaEntrada(LocalTime.of(8, 0))
                .build();

        when(asistenciaRepository.findUltimasAsistenciasConDetalles(any(), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(asistencia));

        DashboardResumenDto resultado = reporteService.getDashboardResumen();

        assertNotNull(resultado);
        assertEquals(new BigDecimal("5000.00"), resultado.getIngresosMesActual());
        assertEquals(new BigDecimal("4000.00"), resultado.getIngresosMesAnterior());
        assertEquals(25.0, resultado.getCrecimientoIngresosPorcentaje());
        assertEquals(60L, resultado.getTotalSociosActivos());
        assertEquals(15L, resultado.getTotalSociosMorosos());
        assertEquals(25L, resultado.getAsistenciasHoy());
        assertEquals(1, resultado.getProximosVencimientos().size());
        assertEquals(1, resultado.getUltimasAsistencias().size());
        assertEquals("Carlos Lopez", resultado.getUltimasAsistencias().get(0).getNombreSocio());
    }

    @Test
    void testGetMetricasAsistenciaAdmin() {
        User userAdmin = User.builder()
                .id(1)
                .nombres("Admin")
                .rol(Rol.admin())
                .build();

        when(asistenciaRepository.countByFecha(any(LocalDate.class))).thenReturn(30L);
        when(asistenciaRepository.countByFechaBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(150L, 600L);

        List<Object[]> rawHoras = new ArrayList<>();
        rawHoras.add(new Object[]{8, 45L});
        rawHoras.add(new Object[]{18, 90L});
        when(asistenciaRepository.findHorasPicoBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(rawHoras);

        List<Object[]> rawDias = new ArrayList<>();
        rawDias.add(new Object[]{LocalDate.now(), 50L});
        when(asistenciaRepository.findAsistenciasDiariasBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(rawDias);

        List<Object[]> rawSuc = new ArrayList<>();
        rawSuc.add(new Object[]{1, "Central", 600L});
        when(asistenciaRepository.findAsistenciasPorSucursalBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(rawSuc);

        List<Object[]> rawTop = new ArrayList<>();
        rawTop.add(new Object[]{1, "Juan", "Perez", 20L});
        when(asistenciaRepository.findTopSociosAsistenciasBetween(any(LocalDate.class), any(LocalDate.class), any(Pageable.class))).thenReturn(rawTop);

        Asistencia asistencia = Asistencia.builder()
                .id(1)
                .socio(socio)
                .sucursal(sucursal)
                .fecha(LocalDate.now())
                .horaEntrada(LocalTime.of(9, 30))
                .build();
        when(asistenciaRepository.findUltimasAsistenciasConDetalles(any(), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(asistencia));

        MetricaAsistenciaDto resultado = reporteService.getMetricasAsistencia(userAdmin, 10, 2026, null);

        assertNotNull(resultado);
        assertEquals("ADMIN", resultado.getRol());
        assertEquals(30L, resultado.getAsistenciasHoy());
        assertEquals(150L, resultado.getAsistenciasSemanaActual());
        assertEquals(600L, resultado.getAsistenciasMesActual());
        assertEquals(18, resultado.getHorasPico().size());
        assertEquals(7, resultado.getAsistenciasPorDiaSemana().size());
        assertEquals(1, resultado.getAsistenciasPorSucursal().size());
        assertEquals(1, resultado.getTopSociosFrecuentes().size());
        assertEquals(1, resultado.getUltimosRegistros().size());
        assertEquals("Carlos Lopez", resultado.getUltimosRegistros().get(0).getNombreSocio());
    }

    @Test
    void testGetMetricasAsistenciaRecepcionista() {
        User userRecepcionista = User.builder()
                .id(2)
                .nombres("Maria")
                .rol(Rol.recepcionista())
                .build();

        when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursal));
        when(asistenciaRepository.countBySucursalIdAndFecha(eq(1), any(LocalDate.class))).thenReturn(40L);
        when(asistenciaRepository.countBySucursalIdAndFechaBetween(eq(1), any(LocalDate.class), any(LocalDate.class))).thenReturn(500L);
        when(asistenciaRepository.countByUsuarioRegistroOrRecepcionistaAndFecha(eq(2), any(LocalDate.class))).thenReturn(20L);

        List<Object[]> rawHoras = new ArrayList<>();
        rawHoras.add(new Object[]{7, 10L});
        when(asistenciaRepository.findHorasPicoBySucursalAndFecha(eq(1), any(LocalDate.class))).thenReturn(rawHoras);

        Asistencia asistencia = Asistencia.builder()
                .id(1)
                .socio(socio)
                .sucursal(sucursal)
                .fecha(LocalDate.now())
                .horaEntrada(LocalTime.of(8, 0))
                .build();
        when(asistenciaRepository.findUltimasAsistenciasBySucursalAndFecha(eq(1), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(asistencia));

        MetricaAsistenciaDto resultado = reporteService.getMetricasAsistencia(userRecepcionista, null, null, 1);

        assertNotNull(resultado);
        assertEquals("RECEPCIONISTA", resultado.getRol());
        assertEquals(1, resultado.getSucursalId());
        assertEquals("Central", resultado.getNombreSucursal());
        assertEquals(40L, resultado.getAsistenciasHoy());
        assertEquals(500L, resultado.getAsistenciasMesActual());
        assertEquals(20L, resultado.getRegistradasPorMiHoy());
        assertEquals(1, resultado.getUltimosRegistros().size());
    }

    @Test
    void testGetMetricasAsistenciaCliente() {
        when(socioRepository.findById(10)).thenReturn(Optional.of(socio));
        when(asistenciaRepository.countBySocioId(10)).thenReturn(48L);
        when(asistenciaRepository.countBySocioIdAndFechaBetween(eq(10), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(12L, 4L);

        List<LocalDate> fechas = List.of(LocalDate.now(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(2));
        when(asistenciaRepository.findFechasAsistenciaBySocioId(10)).thenReturn(fechas);

        List<Object[]> rawHoras = new ArrayList<>();
        rawHoras.add(new Object[]{7, 20L});
        when(asistenciaRepository.findHorasAsistenciaBySocioId(10)).thenReturn(rawHoras);

        List<Object[]> rawVisitas = new ArrayList<>();
        rawVisitas.add(new Object[]{2026, 10, 12L});
        when(asistenciaRepository.findVisitasMesBySocioId(eq(10), any(LocalDate.class))).thenReturn(rawVisitas);

        Asistencia asistencia = Asistencia.builder()
                .id(99)
                .socio(socio)
                .sucursal(sucursal)
                .fecha(LocalDate.now())
                .horaEntrada(LocalTime.of(7, 30))
                .build();
        when(asistenciaRepository.findUltimaAsistenciaBySocioId(10)).thenReturn(Optional.of(asistencia));

        MetricaAsistenciaDto resultado = reporteService.getMetricasAsistencia(userCliente, null, null, null);

        assertNotNull(resultado);
        assertEquals("CLIENTE", resultado.getRol());
        assertEquals(10, resultado.getSocioId());
        assertEquals(48L, resultado.getTotalVisitasHistorico());
        assertEquals(12L, resultado.getAsistenciasMesActual());
        assertEquals(4L, resultado.getAsistenciasSemanaActual());
        assertEquals(3, resultado.getRachaDiasConsecutivos());
        assertEquals("Mañana (05:00 - 12:00)", resultado.getHorarioPreferido());
        assertEquals(1, resultado.getHistorialVisitasPorMes().size());
        assertNotNull(resultado.getUltimaAsistencia());
    }
}
