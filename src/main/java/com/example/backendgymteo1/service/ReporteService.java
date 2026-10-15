package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.reporte.AsistenciaResumenDto;
import com.example.backendgymteo1.dto.reporte.DashboardResumenDto;
import com.example.backendgymteo1.dto.reporte.DiaSemanaAsistenciaDto;
import com.example.backendgymteo1.dto.reporte.DistribucionPlanDto;
import com.example.backendgymteo1.dto.reporte.DistribucionSucursalDto;
import com.example.backendgymteo1.dto.reporte.HoraPicoDto;
import com.example.backendgymteo1.dto.reporte.IngresoMensualDto;
import com.example.backendgymteo1.dto.reporte.IngresoMetodoPagoDto;
import com.example.backendgymteo1.dto.reporte.MembresiaPorVencerDto;
import com.example.backendgymteo1.dto.reporte.MetricaAsistenciaDto;
import com.example.backendgymteo1.dto.reporte.MiembroEstadoDetalleDto;
import com.example.backendgymteo1.dto.reporte.PagoDetalleReporteDto;
import com.example.backendgymteo1.dto.reporte.RenovacionDetalleDto;
import com.example.backendgymteo1.dto.reporte.RenovacionMesDto;
import com.example.backendgymteo1.dto.reporte.ReporteIngresosDto;
import com.example.backendgymteo1.dto.reporte.ReporteMiembrosEstadoDto;
import com.example.backendgymteo1.dto.reporte.ReporteRenovacionesDto;
import com.example.backendgymteo1.dto.reporte.SucursalAsistenciaDto;
import com.example.backendgymteo1.dto.reporte.TopSocioAsistenciaDto;
import com.example.backendgymteo1.dto.reporte.VisitasMesDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.ComprobantePago;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import com.example.backendgymteo1.repository.ComprobantePagoRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private static final String[] DIAS_SEMANA = {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    private final ComprobantePagoRepository comprobantePagoRepository;
    private final SocioRepository socioRepository;
    private final MembresiaRepository membresiaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final SucursalRepository sucursalRepository;

    public ReporteIngresosDto getReporteIngresos(Integer anio) {
        List<Object[]> rawMensuales = comprobantePagoRepository.findIngresosMensuales(anio);
        List<IngresoMensualDto> ingresosPorMes = new ArrayList<>();
        BigDecimal totalPeriodo = BigDecimal.ZERO;
        long totalCantidadPagos = 0L;
        IngresoMensualDto maxMes = null;

        for (Object[] row : rawMensuales) {
            Integer rowAnio = toInteger(row[0]);
            Integer rowMes = toInteger(row[1]);
            BigDecimal rowTotal = toBigDecimal(row[2]);
            Long rowCantidad = toLong(row[3]);

            String nombreMes = (rowMes != null && rowMes >= 1 && rowMes <= 12) ? MESES[rowMes - 1] : "";

            IngresoMensualDto itemMes = IngresoMensualDto.builder()
                    .anio(rowAnio)
                    .mes(rowMes)
                    .nombreMes(nombreMes)
                    .total(rowTotal)
                    .cantidadPagos(rowCantidad)
                    .build();

            ingresosPorMes.add(itemMes);

            if (maxMes == null || (rowTotal != null && rowTotal.compareTo(maxMes.getTotal()) > 0)) {
                maxMes = itemMes;
            }

            totalPeriodo = totalPeriodo.add(rowTotal);
            totalCantidadPagos += rowCantidad;
        }

        BigDecimal ingresoPromedio = ingresosPorMes.isEmpty()
                ? BigDecimal.ZERO
                : totalPeriodo.divide(BigDecimal.valueOf(ingresosPorMes.size()), 2, RoundingMode.HALF_UP);

        String mesMayor = maxMes != null ? maxMes.getNombreMes() + " " + maxMes.getAnio() : "N/A";
        BigDecimal montoMayor = maxMes != null ? maxMes.getTotal() : BigDecimal.ZERO;

        List<Object[]> rawMetodos = comprobantePagoRepository.findIngresosPorMetodoPago(anio);
        List<IngresoMetodoPagoDto> ingresosPorMetodo = new ArrayList<>();

        for (Object[] row : rawMetodos) {
            String metodo = row[0] != null ? row[0].toString() : "OTROS";
            BigDecimal monto = toBigDecimal(row[1]);
            Long cantidad = toLong(row[2]);
            Double porcentaje = calcularPorcentaje(monto, totalPeriodo);

            ingresosPorMetodo.add(IngresoMetodoPagoDto.builder()
                    .metodoPago(metodo)
                    .total(monto)
                    .cantidad(cantidad)
                    .porcentaje(porcentaje)
                    .build());
        }

        List<ComprobantePago> rawPagosDetallados = comprobantePagoRepository.findPagosDetallados(anio, PageRequest.of(0, 20));
        List<PagoDetalleReporteDto> ultimosPagos = new ArrayList<>();

        if (rawPagosDetallados != null) {
            for (ComprobantePago c : rawPagosDetallados) {
                if (c == null) {
                    continue;
                }

                String nombreSocio = "";
                String dpiSocio = "";
                String correoSocio = "";
                Integer socioId = null;
                String planNombre = "";
                String numFactura = "";

                if (c.getFactura() != null) {
                    numFactura = "FAC-" + c.getFactura().getId();
                    if (c.getFactura().getMembresia() != null) {
                        Membresia m = c.getFactura().getMembresia();
                        if (m.getPlan() != null) {
                            planNombre = m.getPlan().getNombre();
                        }
                        if (m.getSocio() != null) {
                            socioId = m.getSocio().getId();
                            if (m.getSocio().getUsuario() != null) {
                                User u = m.getSocio().getUsuario();
                                nombreSocio = (u.getNombres() + " " + u.getApellidos()).trim();
                                dpiSocio = u.getDpi();
                                correoSocio = u.getCorreo();
                            }
                        }
                    }
                }

                String metodo = c.getMetodoPago() != null ? c.getMetodoPago().getNombre() : "OTROS";

                ultimosPagos.add(PagoDetalleReporteDto.builder()
                        .comprobanteId(c.getId())
                        .socioId(socioId)
                        .nombreSocio(nombreSocio)
                        .dpiSocio(dpiSocio)
                        .correoSocio(correoSocio)
                        .planNombre(planNombre)
                        .metodoPago(metodo)
                        .montoPagado(c.getMontoPagado())
                        .fechaPago(c.getFechaPago())
                        .numeroFactura(numFactura)
                        .referencia(c.getReferencia())
                        .build());
            }
        }

        return ReporteIngresosDto.builder()
                .anio(anio)
                .totalPeriodo(totalPeriodo)
                .cantidadPagosPeriodo(totalCantidadPagos)
                .ingresoPromedioMensual(ingresoPromedio)
                .mesMayorIngreso(mesMayor)
                .montoMayorIngreso(montoMayor)
                .ingresosPorMes(ingresosPorMes)
                .ingresosPorMetodoPago(ingresosPorMetodo)
                .ultimosPagos(ultimosPagos)
                .build();
    }

    public ReporteMiembrosEstadoDto getReporteMiembrosEstado() {
        return getReporteMiembrosEstado(null, null);
    }

    public ReporteMiembrosEstadoDto getReporteMiembrosEstado(String estadoFiltro, Integer sucursalId) {
        LocalDate hoy = LocalDate.now();

        long totalSocios = socioRepository.countTotalSociosActivos();
        long activos = socioRepository.countSociosConMembresiaActiva(hoy);
        long morosos = socioRepository.countSociosMorosos(hoy);

        Double porcentajeActivos = totalSocios > 0
                ? redondear((activos * 100.0) / totalSocios)
                : 0.0;
        Double porcentajeMorosos = totalSocios > 0
                ? redondear((morosos * 100.0) / totalSocios)
                : 0.0;

        List<Object[]> rawPlanes = membresiaRepository.countActiveMembershipsByPlan(hoy);
        long totalPlanes = 0L;
        for (Object[] row : rawPlanes) {
            totalPlanes += toLong(row[2]);
        }

        List<DistribucionPlanDto> distribucionPlan = new ArrayList<>();
        for (Object[] row : rawPlanes) {
            Integer planId = toInteger(row[0]);
            String nombrePlan = row[1] != null ? row[1].toString() : "";
            Long cantidad = toLong(row[2]);
            Double porcentaje = totalPlanes > 0
                    ? redondear((cantidad * 100.0) / totalPlanes)
                    : 0.0;

            distribucionPlan.add(DistribucionPlanDto.builder()
                    .planId(planId)
                    .nombrePlan(nombrePlan)
                    .cantidad(cantidad)
                    .porcentaje(porcentaje)
                    .build());
        }

        List<Object[]> rawSucursales = socioRepository.countSociosPorSucursalYEstado(hoy);
        List<DistribucionSucursalDto> distribucionSucursal = new ArrayList<>();

        for (Object[] row : rawSucursales) {
            Integer sucId = toInteger(row[0]);
            String sucNombre = row[1] != null ? row[1].toString() : "Sin Asignar";
            Long sucActivos = toLong(row[2]);
            Long sucMorosos = toLong(row[3]);
            Long sucTotal = toLong(row[4]);
            Double solvencia = sucTotal > 0
                    ? redondear((sucActivos * 100.0) / sucTotal)
                    : 0.0;

            distribucionSucursal.add(DistribucionSucursalDto.builder()
                    .sucursalId(sucId)
                    .nombreSucursal(sucNombre)
                    .activos(sucActivos)
                    .morosos(sucMorosos)
                    .total(sucTotal)
                    .porcentajeSolvencia(solvencia)
                    .build());
        }

        List<Membresia> ultimasMembresias = membresiaRepository.findUltimaMembresiaPorSocio();
        Map<Integer, Membresia> membresiaPorSocio = new HashMap<>();
        if (ultimasMembresias != null) {
            for (Membresia m : ultimasMembresias) {
                if (m.getSocio() != null && m.getSocio().getId() != null) {
                    membresiaPorSocio.put(m.getSocio().getId(), m);
                }
            }
        }

        List<Object[]> rawUltimasAsistencias = asistenciaRepository.findUltimaFechaAsistenciaPorSocio();
        Map<Integer, LocalDate> ultimaAsistenciaPorSocio = new HashMap<>();
        if (rawUltimasAsistencias != null) {
            for (Object[] row : rawUltimasAsistencias) {
                Integer sId = toInteger(row[0]);
                if (sId != null && row[1] instanceof LocalDate fecha) {
                    ultimaAsistenciaPorSocio.put(sId, fecha);
                }
            }
        }

        List<Socio> todosLosSocios = socioRepository.findAllActiveWithUser();
        List<MiembroEstadoDetalleDto> miembrosDetalle = new ArrayList<>();

        if (todosLosSocios != null) {
            for (Socio s : todosLosSocios) {
                if (s == null) {
                    continue;
                }

                Integer sSucursalId = s.getSucursal() != null ? s.getSucursal().getId() : null;
                if (sucursalId != null && !sucursalId.equals(sSucursalId)) {
                    continue;
                }

                Membresia latest = membresiaPorSocio.get(s.getId());
                boolean isActivo = latest != null
                        && latest.getEstadoMembresia() != null
                        && latest.getEstadoMembresia().getId() != null
                        && latest.getEstadoMembresia().getId() == 1
                        && latest.getFechaVencimiento() != null
                        && !latest.getFechaVencimiento().isBefore(hoy);

                String estadoSocio = isActivo ? "ACTIVO" : "MOROSO";

                if (estadoFiltro != null && !estadoFiltro.isBlank()) {
                    if (!estadoSocio.equalsIgnoreCase(estadoFiltro.trim())) {
                        continue;
                    }
                }

                User u = s.getUsuario();
                String nombres = u != null && u.getNombres() != null ? u.getNombres() : "";
                String apellidos = u != null && u.getApellidos() != null ? u.getApellidos() : "";
                String nombreCompleto = (nombres + " " + apellidos).trim();
                String dpi = u != null ? u.getDpi() : null;
                String correo = u != null ? u.getCorreo() : null;
                String telefono = u != null ? u.getTelefono() : null;
                String sucursalNombre = s.getSucursal() != null ? s.getSucursal().getNombre() : "Sin Asignar";

                String planNombre = "Sin membresía";
                Integer membresiaId = null;
                LocalDate fechaInicio = null;
                LocalDate fechaVencimiento = null;
                Long diasRestantes = null;
                Long diasMora = null;

                if (latest != null) {
                    membresiaId = latest.getId();
                    planNombre = latest.getPlan() != null ? latest.getPlan().getNombre() : "Plan no especificado";
                    fechaInicio = latest.getFechaInicio();
                    fechaVencimiento = latest.getFechaVencimiento();

                    if (fechaVencimiento != null) {
                        if (isActivo) {
                            diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVencimiento);
                            diasMora = 0L;
                        } else {
                            diasRestantes = 0L;
                            diasMora = Math.max(0, ChronoUnit.DAYS.between(fechaVencimiento, hoy));
                        }
                    }
                }

                LocalDate ultimaFecha = ultimaAsistenciaPorSocio.get(s.getId());

                miembrosDetalle.add(MiembroEstadoDetalleDto.builder()
                        .socioId(s.getId())
                        .dpi(dpi)
                        .nombres(nombres)
                        .apellidos(apellidos)
                        .nombreCompleto(nombreCompleto)
                        .correo(correo)
                        .telefono(telefono)
                        .sucursalId(sSucursalId)
                        .sucursalNombre(sucursalNombre)
                        .fechaRegistro(s.getFechaRegistro())
                        .estado(estadoSocio)
                        .membresiaId(membresiaId)
                        .planNombre(planNombre)
                        .fechaInicioMembresia(fechaInicio)
                        .fechaVencimientoMembresia(fechaVencimiento)
                        .diasRestantes(diasRestantes)
                        .diasMora(diasMora)
                        .ultimaAsistencia(ultimaFecha)
                        .build());
            }
        }

        return ReporteMiembrosEstadoDto.builder()
                .totalSocios(totalSocios)
                .activos(activos)
                .morosos(morosos)
                .porcentajeActivos(porcentajeActivos)
                .porcentajeMorosos(porcentajeMorosos)
                .distribucionPorPlan(distribucionPlan)
                .distribucionPorSucursal(distribucionSucursal)
                .miembros(miembrosDetalle)
                .build();
    }

    public ReporteRenovacionesDto getReporteRenovaciones(Integer anio) {
        LocalDate hoy = LocalDate.now();

        List<Object[]> rawTotales = membresiaRepository.countTotalesRenovaciones(anio);
        long totalPeriodo = 0L;
        long nuevas = 0L;
        long renovaciones = 0L;

        if (rawTotales != null && !rawTotales.isEmpty() && rawTotales.get(0) != null) {
            Object[] totalesRow = rawTotales.get(0);
            totalPeriodo = toLong(totalesRow[0]);
            nuevas = toLong(totalesRow[1]);
            renovaciones = toLong(totalesRow[2]);
        }

        Double tasaGlobal = totalPeriodo > 0
                ? redondear((renovaciones * 100.0) / totalPeriodo)
                : 0.0;

        List<Membresia> rawProximas = membresiaRepository.findProximasAVencer(hoy, hoy.plusDays(7));
        long proximasVencerCount = rawProximas != null ? rawProximas.size() : 0L;

        List<MembresiaPorVencerDto> proximasDetalle = new ArrayList<>();
        if (rawProximas != null) {
            for (Membresia m : rawProximas) {
                if (m == null) {
                    continue;
                }

                Integer socioId = null;
                String nombreSocio = "";
                String dpi = "";
                String correo = "";
                String telefono = "";
                String sucursalNombre = "Central";

                if (m.getSocio() != null) {
                    socioId = m.getSocio().getId();
                    if (m.getSocio().getUsuario() != null) {
                        User u = m.getSocio().getUsuario();
                        nombreSocio = (u.getNombres() + " " + u.getApellidos()).trim();
                        dpi = u.getDpi();
                        correo = u.getCorreo();
                        telefono = u.getTelefono();
                    }
                    if (m.getSocio().getSucursal() != null) {
                        sucursalNombre = m.getSocio().getSucursal().getNombre();
                    }
                }

                String planNombre = m.getPlan() != null ? m.getPlan().getNombre() : "";
                Long diasRestantes = m.getFechaVencimiento() != null
                        ? Math.max(0, ChronoUnit.DAYS.between(hoy, m.getFechaVencimiento()))
                        : 0L;

                proximasDetalle.add(MembresiaPorVencerDto.builder()
                        .membresiaId(m.getId())
                        .socioId(socioId)
                        .nombreSocio(nombreSocio)
                        .dpi(dpi)
                        .correo(correo)
                        .telefono(telefono)
                        .planNombre(planNombre)
                        .sucursalNombre(sucursalNombre)
                        .fechaVencimiento(m.getFechaVencimiento())
                        .diasRestantes(diasRestantes)
                        .build());
            }
        }

        List<Object[]> rawMeses = membresiaRepository.findRenovacionesPorMes(anio);
        List<RenovacionMesDto> renovacionesPorMes = new ArrayList<>();

        for (Object[] row : rawMeses) {
            Integer rowAnio = toInteger(row[0]);
            Integer rowMes = toInteger(row[1]);
            Long rowNuevas = toLong(row[2]);
            Long rowRenovaciones = toLong(row[3]);
            Long rowTotal = toLong(row[4]);

            String nombreMes = (rowMes != null && rowMes >= 1 && rowMes <= 12) ? MESES[rowMes - 1] : "";
            Double tasaMes = rowTotal > 0
                    ? redondear((rowRenovaciones * 100.0) / rowTotal)
                    : 0.0;

            renovacionesPorMes.add(RenovacionMesDto.builder()
                    .anio(rowAnio)
                    .mes(rowMes)
                    .nombreMes(nombreMes)
                    .nuevas(rowNuevas)
                    .renovaciones(rowRenovaciones)
                    .total(rowTotal)
                    .tasaRenovacion(tasaMes)
                    .build());
        }

        List<Object[]> rawPrimeras = membresiaRepository.findPrimeraMembresiaIdPorSocio();
        Map<Integer, Integer> primeraMembresiaMap = new HashMap<>();
        if (rawPrimeras != null) {
            for (Object[] row : rawPrimeras) {
                Integer sId = toInteger(row[0]);
                Integer mId = toInteger(row[1]);
                if (sId != null && mId != null) {
                    primeraMembresiaMap.put(sId, mId);
                }
            }
        }

        List<Membresia> rawDetalladas = membresiaRepository.findMembresiasDetalladas(anio, PageRequest.of(0, 20));
        List<RenovacionDetalleDto> ultimasRenovaciones = new ArrayList<>();

        if (rawDetalladas != null) {
            for (Membresia m : rawDetalladas) {
                if (m == null) {
                    continue;
                }

                Integer socioId = null;
                String nombreSocio = "";
                String dpi = "";
                String correo = "";

                if (m.getSocio() != null) {
                    socioId = m.getSocio().getId();
                    if (m.getSocio().getUsuario() != null) {
                        User u = m.getSocio().getUsuario();
                        nombreSocio = (u.getNombres() + " " + u.getApellidos()).trim();
                        dpi = u.getDpi();
                        correo = u.getCorreo();
                    }
                }

                Integer primerId = primeraMembresiaMap.get(socioId);
                String tipo = (primerId != null && primerId.equals(m.getId())) ? "NUEVA" : "RENOVACIÓN";
                String planNombre = m.getPlan() != null ? m.getPlan().getNombre() : "";

                ultimasRenovaciones.add(RenovacionDetalleDto.builder()
                        .membresiaId(m.getId())
                        .socioId(socioId)
                        .nombreSocio(nombreSocio)
                        .dpi(dpi)
                        .correo(correo)
                        .planNombre(planNombre)
                        .tipo(tipo)
                        .fechaInicio(m.getFechaInicio())
                        .fechaVencimiento(m.getFechaVencimiento())
                        .build());
            }
        }

        return ReporteRenovacionesDto.builder()
                .anio(anio)
                .totalMembresiasPeriodo(totalPeriodo)
                .nuevasMembresias(nuevas)
                .totalRenovaciones(renovaciones)
                .tasaRenovacionGlobal(tasaGlobal)
                .proximasAVencerSiguienteSemana(proximasVencerCount)
                .renovacionesPorMes(renovacionesPorMes)
                .proximasAVencer(proximasDetalle)
                .ultimasRenovaciones(ultimasRenovaciones)
                .build();
    }

    public DashboardResumenDto getDashboardResumen() {
        LocalDate hoy = LocalDate.now();
        YearMonth mesActualYm = YearMonth.from(hoy);
        YearMonth mesAnteriorYm = mesActualYm.minusMonths(1);

        LocalDateTime inicioMesActual = mesActualYm.atDay(1).atStartOfDay();
        LocalDateTime finMesActual = mesActualYm.atEndOfMonth().atTime(23, 59, 59);

        LocalDateTime inicioMesAnterior = mesAnteriorYm.atDay(1).atStartOfDay();
        LocalDateTime finMesAnterior = mesAnteriorYm.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal ingresosMesActual = comprobantePagoRepository.sumMontoPagadoBetween(inicioMesActual, finMesActual);
        BigDecimal ingresosMesAnterior = comprobantePagoRepository.sumMontoPagadoBetween(inicioMesAnterior, finMesAnterior);

        Double crecimiento = 0.0;
        if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diferencia = ingresosMesActual.subtract(ingresosMesAnterior);
            crecimiento = diferencia.divide(ingresosMesAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            crecimiento = redondear(crecimiento);
        } else if (ingresosMesActual.compareTo(BigDecimal.ZERO) > 0) {
            crecimiento = 100.0;
        }

        long totalActivos = socioRepository.countSociosConMembresiaActiva(hoy);
        long totalMorosos = socioRepository.countSociosMorosos(hoy);

        List<Membresia> rawProximas = membresiaRepository.findProximasAVencer(hoy, hoy.plusDays(7));
        long proximasVencer7Dias = rawProximas != null ? rawProximas.size() : 0L;

        List<MembresiaPorVencerDto> proximosVencimientos = new ArrayList<>();
        if (rawProximas != null) {
            for (Membresia m : rawProximas) {
                if (m == null) {
                    continue;
                }

                Integer socioId = null;
                String nombreSocio = "";
                String dpi = "";
                String correo = "";
                String telefono = "";
                String sucursalNombre = "Central";

                if (m.getSocio() != null) {
                    socioId = m.getSocio().getId();
                    if (m.getSocio().getUsuario() != null) {
                        User u = m.getSocio().getUsuario();
                        nombreSocio = (u.getNombres() + " " + u.getApellidos()).trim();
                        dpi = u.getDpi();
                        correo = u.getCorreo();
                        telefono = u.getTelefono();
                    }
                    if (m.getSocio().getSucursal() != null) {
                        sucursalNombre = m.getSocio().getSucursal().getNombre();
                    }
                }

                String planNombre = m.getPlan() != null ? m.getPlan().getNombre() : "";
                Long diasRestantes = m.getFechaVencimiento() != null
                        ? Math.max(0, ChronoUnit.DAYS.between(hoy, m.getFechaVencimiento()))
                        : 0L;

                proximosVencimientos.add(MembresiaPorVencerDto.builder()
                        .membresiaId(m.getId())
                        .socioId(socioId)
                        .nombreSocio(nombreSocio)
                        .dpi(dpi)
                        .correo(correo)
                        .telefono(telefono)
                        .planNombre(planNombre)
                        .sucursalNombre(sucursalNombre)
                        .fechaVencimiento(m.getFechaVencimiento())
                        .diasRestantes(diasRestantes)
                        .build());
            }
        }

        long asistenciasHoy = asistenciaRepository.countByFecha(hoy);
        long asistenciasMes = asistenciaRepository.countByFechaBetween(mesActualYm.atDay(1), hoy);

        List<Asistencia> asistenciasRecientes = asistenciaRepository.findUltimasAsistenciasConDetalles(null, hoy, PageRequest.of(0, 10));
        List<AsistenciaResumenDto> ultimasAsistencias = new ArrayList<>();
        if (asistenciasRecientes != null) {
            for (Asistencia a : asistenciasRecientes) {
                ultimasAsistencias.add(mapToAsistenciaResumen(a));
            }
        }

        return DashboardResumenDto.builder()
                .ingresosMesActual(ingresosMesActual)
                .ingresosMesAnterior(ingresosMesAnterior)
                .crecimientoIngresosPorcentaje(crecimiento)
                .totalSociosActivos(totalActivos)
                .totalSociosMorosos(totalMorosos)
                .membresiasPorVencer7Dias(proximasVencer7Dias)
                .asistenciasHoy(asistenciasHoy)
                .asistenciasMesActual(asistenciasMes)
                .proximosVencimientos(proximosVencimientos)
                .ultimasAsistencias(ultimasAsistencias)
                .build();
    }

    public MetricaAsistenciaDto getMetricasAsistencia(User currentUser, Integer mes, Integer anio, Integer sucursalId) {
        if (currentUser == null) {
            throw new RuntimeException("Usuario autenticado no válido");
        }

        String rol = currentUser.getRol() != null && currentUser.getRol().getNombre() != null
                ? currentUser.getRol().getNombre().toUpperCase()
                : "";

        if ("CLIENTE".equals(rol)) {
            return getMetricasCliente(currentUser);
        } else if ("RECEPCIONISTA".equals(rol)) {
            return getMetricasRecepcionista(sucursalId, currentUser);
        } else {
            return getMetricasAdmin(mes, anio, sucursalId);
        }
    }

    private MetricaAsistenciaDto getMetricasAdmin(Integer mes, Integer anio, Integer sucursalId) {
        LocalDate hoy = LocalDate.now();
        int consultaAnio = anio != null ? anio : hoy.getYear();
        int consultaMes = mes != null ? mes : hoy.getMonthValue();

        YearMonth ym = YearMonth.of(consultaAnio, consultaMes);
        LocalDate inicioMes = ym.atDay(1);
        LocalDate finMes = ym.atEndOfMonth();

        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        long asistenciasHoy = sucursalId != null
                ? asistenciaRepository.countBySucursalIdAndFecha(sucursalId, hoy)
                : asistenciaRepository.countByFecha(hoy);

        long asistenciasSemana = asistenciaRepository.countByFechaBetween(inicioSemana, finSemana);

        long asistenciasMes = sucursalId != null
                ? asistenciaRepository.countBySucursalIdAndFechaBetween(sucursalId, inicioMes, finMes)
                : asistenciaRepository.countByFechaBetween(inicioMes, finMes);

        int diasTranscurridos = (consultaAnio == hoy.getYear() && consultaMes == hoy.getMonthValue())
                ? hoy.getDayOfMonth()
                : ym.lengthOfMonth();

        Double promedioDiario = diasTranscurridos > 0
                ? redondear((double) asistenciasMes / diasTranscurridos)
                : 0.0;

        List<Object[]> rawHoras = sucursalId != null
                ? asistenciaRepository.findHorasPicoBySucursalAndFecha(sucursalId, hoy)
                : asistenciaRepository.findHorasPicoBetween(inicioMes, finMes);
        List<HoraPicoDto> horasPico = formatearHorasPico(rawHoras);

        List<Object[]> rawDias = asistenciaRepository.findAsistenciasDiariasBetween(inicioMes, finMes);
        List<DiaSemanaAsistenciaDto> asistenciasPorDiaSemana = agruparPorDiaSemana(rawDias, asistenciasMes);

        List<Object[]> rawSucursales = asistenciaRepository.findAsistenciasPorSucursalBetween(inicioMes, finMes);
        List<SucursalAsistenciaDto> asistenciasPorSucursal = new ArrayList<>();

        for (Object[] row : rawSucursales) {
            Integer sucId = toInteger(row[0]);
            String sucNombre = row[1] != null ? row[1].toString() : "Sin Sede";
            Long cantidad = toLong(row[2]);
            Double porcentaje = asistenciasMes > 0
                    ? redondear((cantidad * 100.0) / asistenciasMes)
                    : 0.0;

            asistenciasPorSucursal.add(SucursalAsistenciaDto.builder()
                    .sucursalId(sucId)
                    .nombreSucursal(sucNombre)
                    .cantidad(cantidad)
                    .porcentaje(porcentaje)
                    .build());
        }

        List<Object[]> rawTop = asistenciaRepository.findTopSociosAsistenciasBetween(inicioMes, finMes, PageRequest.of(0, 10));
        List<TopSocioAsistenciaDto> topSocios = new ArrayList<>();

        for (Object[] row : rawTop) {
            Integer socioId = toInteger(row[0]);
            String nombres = row[1] != null ? row[1].toString() : "";
            String apellidos = row[2] != null ? row[2].toString() : "";
            Long cantidad = toLong(row[3]);

            topSocios.add(TopSocioAsistenciaDto.builder()
                    .socioId(socioId)
                    .nombreCompleto((nombres + " " + apellidos).trim())
                    .asistencias(cantidad)
                    .build());
        }

        List<Asistencia> asistenciasRecientes = asistenciaRepository.findUltimasAsistenciasConDetalles(sucursalId, hoy, PageRequest.of(0, 15));
        List<AsistenciaResumenDto> ultimosRegistros = new ArrayList<>();
        if (asistenciasRecientes != null) {
            for (Asistencia a : asistenciasRecientes) {
                ultimosRegistros.add(mapToAsistenciaResumen(a));
            }
        }

        return MetricaAsistenciaDto.builder()
                .rol("ADMIN")
                .asistenciasHoy(asistenciasHoy)
                .asistenciasSemanaActual(asistenciasSemana)
                .asistenciasMesActual(asistenciasMes)
                .promedioDiarioMes(promedioDiario)
                .horasPico(horasPico)
                .asistenciasPorDiaSemana(asistenciasPorDiaSemana)
                .asistenciasPorSucursal(asistenciasPorSucursal)
                .topSociosFrecuentes(topSocios)
                .ultimosRegistros(ultimosRegistros)
                .build();
    }

    private MetricaAsistenciaDto getMetricasRecepcionista(Integer sucursalId, User currentUser) {
        LocalDate hoy = LocalDate.now();
        YearMonth ym = YearMonth.from(hoy);
        LocalDate inicioMes = ym.atDay(1);
        LocalDate finMes = ym.atEndOfMonth();

        Sucursal sucursal = null;
        if (sucursalId != null) {
            sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + sucursalId));
        } else {
            List<Sucursal> sucursales = sucursalRepository.findAll();
            if (!sucursales.isEmpty()) {
                sucursal = sucursales.get(0);
            }
        }

        Integer actualSucursalId = sucursal != null ? sucursal.getId() : null;
        String actualSucursalNombre = sucursal != null ? sucursal.getNombre() : "General";

        long asistenciasHoy = actualSucursalId != null
                ? asistenciaRepository.countBySucursalIdAndFecha(actualSucursalId, hoy)
                : asistenciaRepository.countByFecha(hoy);

        long asistenciasMes = actualSucursalId != null
                ? asistenciaRepository.countBySucursalIdAndFechaBetween(actualSucursalId, inicioMes, finMes)
                : asistenciaRepository.countByFechaBetween(inicioMes, finMes);

        long registradasPorMiHoy = currentUser != null
                ? asistenciaRepository.countByUsuarioRegistroOrRecepcionistaAndFecha(currentUser.getId(), hoy)
                : 0L;

        List<Object[]> rawHoras = actualSucursalId != null
                ? asistenciaRepository.findHorasPicoBySucursalAndFecha(actualSucursalId, hoy)
                : asistenciaRepository.findHorasPicoBetween(hoy, hoy);
        List<HoraPicoDto> afluenciaHoy = formatearHorasPico(rawHoras);

        List<Asistencia> asistenciasRecientes = actualSucursalId != null
                ? asistenciaRepository.findUltimasAsistenciasBySucursalAndFecha(actualSucursalId, hoy, PageRequest.of(0, 15))
                : Collections.emptyList();

        List<AsistenciaResumenDto> ultimosRegistros = new ArrayList<>();
        for (Asistencia a : asistenciasRecientes) {
            ultimosRegistros.add(mapToAsistenciaResumen(a));
        }

        return MetricaAsistenciaDto.builder()
                .rol("RECEPCIONISTA")
                .sucursalId(actualSucursalId)
                .nombreSucursal(actualSucursalNombre)
                .asistenciasHoy(asistenciasHoy)
                .asistenciasMesActual(asistenciasMes)
                .registradasPorMiHoy(registradasPorMiHoy)
                .horasPico(afluenciaHoy)
                .ultimosRegistros(ultimosRegistros)
                .build();
    }

    private MetricaAsistenciaDto getMetricasCliente(User currentUser) {
        Socio socio = socioRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de socio no encontrado para el usuario actual"));

        LocalDate hoy = LocalDate.now();
        YearMonth ym = YearMonth.from(hoy);
        LocalDate inicioMes = ym.atDay(1);
        LocalDate finMes = ym.atEndOfMonth();

        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        long totalHistorico = asistenciaRepository.countBySocioId(socio.getId());
        long visitasMes = asistenciaRepository.countBySocioIdAndFechaBetween(socio.getId(), inicioMes, finMes);
        long visitasSemana = asistenciaRepository.countBySocioIdAndFechaBetween(socio.getId(), inicioSemana, finSemana);

        List<LocalDate> fechasAsistencias = asistenciaRepository.findFechasAsistenciaBySocioId(socio.getId());
        int racha = calcularRacha(fechasAsistencias, hoy);

        List<Object[]> rawHoras = asistenciaRepository.findHorasAsistenciaBySocioId(socio.getId());
        String horarioPreferido = determinarHorarioPreferido(rawHoras);

        LocalDate haceSeisMeses = hoy.minusMonths(5).withDayOfMonth(1);
        List<Object[]> rawVisitasMes = asistenciaRepository.findVisitasMesBySocioId(socio.getId(), haceSeisMeses);
        List<VisitasMesDto> historialVisitas = new ArrayList<>();

        for (Object[] row : rawVisitasMes) {
            Integer anio = toInteger(row[0]);
            Integer mes = toInteger(row[1]);
            Long visitas = toLong(row[2]);
            String nombreMes = (mes != null && mes >= 1 && mes <= 12) ? MESES[mes - 1] : "";

            historialVisitas.add(VisitasMesDto.builder()
                    .anio(anio)
                    .mes(mes)
                    .nombreMes(nombreMes)
                    .visitas(visitas)
                    .build());
        }

        AsistenciaResumenDto ultimaAsistenciaDto = asistenciaRepository.findUltimaAsistenciaBySocioId(socio.getId())
                .map(this::mapToAsistenciaResumen)
                .orElse(null);

        String nombreCompleto = (currentUser.getNombres() + " " + currentUser.getApellidos()).trim();

        return MetricaAsistenciaDto.builder()
                .rol("CLIENTE")
                .socioId(socio.getId())
                .nombreSocio(nombreCompleto)
                .totalVisitasHistorico(totalHistorico)
                .asistenciasMesActual(visitasMes)
                .asistenciasSemanaActual(visitasSemana)
                .rachaDiasConsecutivos(racha)
                .horarioPreferido(horarioPreferido)
                .historialVisitasPorMes(historialVisitas)
                .ultimaAsistencia(ultimaAsistenciaDto)
                .build();
    }

    private AsistenciaResumenDto mapToAsistenciaResumen(Asistencia a) {
        if (a == null) {
            return null;
        }

        String nombreSocio = "";
        String dpiSocio = "";
        String correoSocio = "";
        Integer socioId = null;

        if (a.getSocio() != null) {
            socioId = a.getSocio().getId();
            if (a.getSocio().getUsuario() != null) {
                User u = a.getSocio().getUsuario();
                nombreSocio = (u.getNombres() + " " + u.getApellidos()).trim();
                dpiSocio = u.getDpi();
                correoSocio = u.getCorreo();
            }
        }

        String sucursalNombre = a.getSucursal() != null ? a.getSucursal().getNombre() : "Sede Central";

        return AsistenciaResumenDto.builder()
                .id(a.getId())
                .socioId(socioId)
                .nombreSocio(nombreSocio)
                .socioDpi(dpiSocio)
                .correoSocio(correoSocio)
                .nombreSucursal(sucursalNombre)
                .fecha(a.getFecha())
                .horaEntrada(a.getHoraEntrada())
                .horaSalida(a.getHoraSalida())
                .build();
    }

    private List<HoraPicoDto> formatearHorasPico(List<Object[]> raw) {
        Map<Integer, Long> conteoPorHora = new HashMap<>();
        for (Object[] row : raw) {
            Integer hora = toInteger(row[0]);
            Long cantidad = toLong(row[1]);
            if (hora != null) {
                conteoPorHora.put(hora, cantidad);
            }
        }

        List<HoraPicoDto> resultado = new ArrayList<>();
        for (int h = 5; h <= 22; h++) {
            Long cantidad = conteoPorHora.getOrDefault(h, 0L);
            String etiqueta = String.format("%02d:00 - %02d:00", h, (h + 1) % 24);
            resultado.add(HoraPicoDto.builder()
                    .hora(h)
                    .etiquetaHora(etiqueta)
                    .cantidad(cantidad)
                    .build());
        }
        return resultado;
    }

    private List<DiaSemanaAsistenciaDto> agruparPorDiaSemana(List<Object[]> rawDias, long totalPeriodo) {
        long[] totalesDia = new long[7];

        for (Object[] row : rawDias) {
            if (row[0] instanceof LocalDate fecha) {
                int diaIndex = fecha.getDayOfWeek().getValue() - 1;
                long cant = toLong(row[1]);
                totalesDia[diaIndex] += cant;
            }
        }

        List<DiaSemanaAsistenciaDto> resultado = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            long cantidad = totalesDia[i];
            Double porcentaje = totalPeriodo > 0
                    ? redondear((cantidad * 100.0) / totalPeriodo)
                    : 0.0;

            resultado.add(DiaSemanaAsistenciaDto.builder()
                    .numeroDia(i + 1)
                    .nombreDia(DIAS_SEMANA[i])
                    .cantidad(cantidad)
                    .porcentaje(porcentaje)
                    .build());
        }
        return resultado;
    }

    private int calcularRacha(List<LocalDate> fechas, LocalDate hoy) {
        if (fechas == null || fechas.isEmpty()) {
            return 0;
        }

        LocalDate fechaEsperada = hoy;
        if (!fechas.contains(hoy)) {
            fechaEsperada = hoy.minusDays(1);
            if (!fechas.contains(fechaEsperada)) {
                return 0;
            }
        }

        int racha = 0;
        for (LocalDate f : fechas) {
            if (f.equals(fechaEsperada)) {
                racha++;
                fechaEsperada = fechaEsperada.minusDays(1);
            } else if (f.isBefore(fechaEsperada)) {
                break;
            }
        }
        return racha;
    }

    private String determinarHorarioPreferido(List<Object[]> rawHoras) {
        if (rawHoras == null || rawHoras.isEmpty()) {
            return "Sin datos registrados";
        }

        long manana = 0L;
        long mediodia = 0L;
        long tardeNoche = 0L;

        for (Object[] row : rawHoras) {
            Integer hora = toInteger(row[0]);
            Long cant = toLong(row[1]);
            if (hora == null) {
                continue;
            }
            if (hora >= 5 && hora < 12) {
                manana += cant;
            } else if (hora >= 12 && hora < 17) {
                mediodia += cant;
            } else {
                tardeNoche += cant;
            }
        }

        if (manana >= mediodia && manana >= tardeNoche && manana > 0) {
            return "Mañana (05:00 - 12:00)";
        } else if (mediodia >= manana && mediodia >= tardeNoche && mediodia > 0) {
            return "Mediodía (12:00 - 17:00)";
        } else if (tardeNoche > 0) {
            return "Tarde / Noche (17:00 - 22:00)";
        }
        return "Flexible";
    }

    private Double calcularPorcentaje(BigDecimal monto, BigDecimal total) {
        if (monto == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return monto.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Double redondear(Double valor) {
        if (valor == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Integer toInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number num) {
            return num.intValue();
        }
        return Integer.valueOf(obj.toString());
    }

    private Long toLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number num) {
            return num.longValue();
        }
        return Long.valueOf(obj.toString());
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(obj.toString());
    }
}
