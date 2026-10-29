package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.configuracion.ReglasAbonoDto;
import com.example.backendgymteo1.dto.pago.CreateAbonoDto;
import com.example.backendgymteo1.dto.pago.CreatePagoDto;
import com.example.backendgymteo1.dto.pago.MetodoPagoResponseDto;
import com.example.backendgymteo1.dto.pago.PagoResponseDto;
import com.example.backendgymteo1.entity.ComprobantePago;
import com.example.backendgymteo1.entity.EstadoFactura;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Factura;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.MetodoPago;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Promocion;
import com.example.backendgymteo1.entity.Recepcionista;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.dto.pago.CotizacionPagoDto;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.PromocionRepository;
import com.example.backendgymteo1.mapper.PagoMapper;
import com.example.backendgymteo1.repository.ComprobantePagoRepository;
import com.example.backendgymteo1.repository.EstadoFacturaRepository;
import com.example.backendgymteo1.repository.EstadoMembresiaRepository;
import com.example.backendgymteo1.repository.FacturaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.MetodoPagoRepository;
import com.example.backendgymteo1.repository.RecepcionistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoService {

    private final ComprobantePagoRepository comprobantePagoRepository;
    private final FacturaRepository facturaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoFacturaRepository estadoFacturaRepository;
    private final MembresiaRepository membresiaRepository;
    private final EstadoMembresiaRepository estadoMembresiaRepository;
    private final RecepcionistaRepository recepcionistaRepository;
    private final PagoMapper pagoMapper;
    private final AuditoriaService auditoriaService;
    private final PromocionRepository promocionRepository;
    private final ConfiguracionSistemaService configuracionSistemaService;
    private final AvisoPagoService avisoPagoService;
    private final EstadoCuentaService estadoCuentaService;
    private final CongelamientoService congelamientoService;

    private static final BigDecimal MONTO_MINIMO = new BigDecimal("0.01");

    public CotizacionPagoDto calcularCotizacion(Membresia membresia, LocalDate hoy) {
        return calcularCotizacion(membresia, hoy, null, null, null);
    }

    public CotizacionPagoDto calcularCotizacion(Membresia membresia, LocalDate hoy,
                                                Integer idPromocionSolicitada,
                                                BigDecimal descuentoSolicitado,
                                                BigDecimal porcentajeDescuentoSolicitado) {
        BigDecimal base = (membresia.getPlan() != null && membresia.getPlan().getPrecio() != null)
                ? membresia.getPlan().getPrecio().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        BigDecimal maxDescuento = base.subtract(MONTO_MINIMO).max(BigDecimal.ZERO);
        BigDecimal descuento = BigDecimal.ZERO.setScale(2);
        Integer idPromocionAplicada = null;
        String nombrePromocionAplicada = null;

        if (idPromocionSolicitada != null && idPromocionSolicitada > 0) {
            Promocion promocion = promocionRepository.findById(idPromocionSolicitada)
                    .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + idPromocionSolicitada));
            if (!promocion.estaVigente(hoy)) {
                throw new RuntimeException("La promoción seleccionada no está vigente");
            }
            descuento = promocion.calcularDescuento(base).min(maxDescuento);
            if (descuento.signum() > 0) {
                idPromocionAplicada = promocion.getId();
                nombrePromocionAplicada = promocion.getNombre();
            }
        } else if (descuentoSolicitado != null && descuentoSolicitado.compareTo(BigDecimal.ZERO) > 0) {
            if (descuentoSolicitado.compareTo(base) > 0) {
                throw new RuntimeException("El descuento no puede ser mayor al precio base del plan");
            }
            descuento = descuentoSolicitado.setScale(2, RoundingMode.HALF_UP).min(maxDescuento);
            BigDecimal descuentoCalculado = descuento;
            Promocion promocionCoincidente = promocionRepository.findVigentes(hoy).stream()
                    .filter(p -> p.calcularDescuento(base).min(maxDescuento).compareTo(descuentoCalculado) == 0)
                    .findFirst()
                    .orElse(null);
            if (promocionCoincidente != null) {
                idPromocionAplicada = promocionCoincidente.getId();
                nombrePromocionAplicada = promocionCoincidente.getNombre();
            }
        } else if (porcentajeDescuentoSolicitado != null && porcentajeDescuentoSolicitado.compareTo(BigDecimal.ZERO) > 0) {
            if (porcentajeDescuentoSolicitado.compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("El porcentaje de descuento no puede superar el 100%");
            }
            descuento = base.multiply(porcentajeDescuentoSolicitado)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                    .min(maxDescuento);
        } else {
            Promocion mejorPromocion = promocionRepository.findVigentes(hoy).stream()
                    .max(Comparator.comparing(p -> p.calcularDescuento(base)))
                    .orElse(null);
            if (mejorPromocion != null) {
                // El total nunca puede quedar por debajo de MONTO_MINIMO (restricción chk_monto_factura)
                descuento = mejorPromocion.calcularDescuento(base).min(maxDescuento);
                if (descuento.signum() > 0) {
                    idPromocionAplicada = mejorPromocion.getId();
                    nombrePromocionAplicada = mejorPromocion.getNombre();
                }
            }
        }

        LocalDate vencimiento = membresia.getFechaVencimiento();
        boolean tardio = vencimiento != null && vencimiento.isBefore(hoy);
        long diasAtraso = tardio ? ChronoUnit.DAYS.between(vencimiento, hoy) : 0;
        // Porcentaje configurable por el ADMIN (tabla configuracion_sistema)
        BigDecimal porcentajeRecargo = configuracionSistemaService.obtenerRecargoTardioPorcentaje();
        BigDecimal recargo = tardio
                ? base.multiply(porcentajeRecargo).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        BigDecimal total = base.subtract(descuento).add(recargo);

        return CotizacionPagoDto.builder()
                .idMembresia(membresia.getId())
                .montoBase(base)
                .idPromocion(idPromocionAplicada)
                .nombrePromocion(nombrePromocionAplicada)
                .descuento(descuento)
                .pagoTardio(tardio)
                .diasAtraso(diasAtraso)
                .porcentajeRecargo(porcentajeRecargo)
                .recargo(recargo)
                .total(total)
                .fechaCalculo(hoy)
                .build();
    }

    public CotizacionPagoDto cotizarPago(Integer idMembresia) {
        return cotizarPago(idMembresia, null, null);
    }

    public CotizacionPagoDto cotizarPago(Integer idMembresia, Integer idPromocion, BigDecimal descuento) {
        Membresia membresia = membresiaRepository.findByIdWithDetails(idMembresia)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + idMembresia));
        return calcularCotizacion(membresia, LocalDate.now(), idPromocion, descuento, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDto registrarPago(CreatePagoDto request, User usuarioActual) {
        if (request == null) {
            throw new RuntimeException("Los datos del pago son obligatorios");
        }
        if (usuarioActual == null) {
            throw new RuntimeException("Se requiere un usuario autenticado para registrar el pago");
        }

        Membresia membresia = membresiaRepository.findByIdWithDetails(request.getIdMembresia())
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + request.getIdMembresia()));

        MetodoPago metodoPago = metodoPagoRepository.findById(request.getIdMetodoPago())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado con ID: " + request.getIdMetodoPago()));

        // Pagos en abonos: primero se liquida el saldo pendiente
        estadoCuentaService.validarSinSaldoPendiente(membresia.getId());

        CotizacionPagoDto cotizacion = calcularCotizacion(
                membresia,
                LocalDate.now(),
                request.getIdPromocion(),
                request.getDescuento(),
                request.getPorcentajeDescuento()
        );

        if (request.getMonto() != null
                && cotizacion.getDescuento().signum() > 0
                && cotizacion.getRecargo().signum() > 0) {
            BigDecimal totalSinRecargo = cotizacion.getMontoBase()
                    .subtract(cotizacion.getDescuento())
                    .max(MONTO_MINIMO);
            if (request.getMonto().compareTo(totalSinRecargo) == 0) {
                cotizacion.setRecargo(BigDecimal.ZERO.setScale(2));
                cotizacion.setTotal(totalSinRecargo);
            }
        }

        if (request.getMonto() == null) {
            throw new RuntimeException(String.format("Disculpe pero su deuda es de Q. %.2f", cotizacion.getTotal()));
        }

        // Pago en cuotas: si el monto no cubre el total, es un primer abono y la factura queda con saldo
        boolean esAbono = request.getMonto().compareTo(cotizacion.getTotal()) < 0;
        ReglasAbonoDto reglasAbono = null;
        if (esAbono) {
            reglasAbono = configuracionSistemaService.consultarReglasAbono();
            if (reglasAbono.getPorcentajeMinimoInicial() >= 100) {
                throw new RuntimeException(String.format("Disculpe pero su deuda es de Q. %.2f", cotizacion.getTotal()));
            }
            BigDecimal minimo = cotizacion.getTotal()
                    .multiply(BigDecimal.valueOf(reglasAbono.getPorcentajeMinimoInicial()))
                    .divide(new BigDecimal("100"), 2, RoundingMode.CEILING);
            if (request.getMonto().compareTo(minimo) < 0) {
                throw new RuntimeException(String.format(
                        "El primer abono debe ser de al menos Q. %.2f (%d%% del total de Q. %.2f)",
                        minimo, reglasAbono.getPorcentajeMinimoInicial(), cotizacion.getTotal()));
            }
        }

        BigDecimal montoSinDescuento = cotizacion.getMontoBase().add(cotizacion.getRecargo());
        BigDecimal montoPagado = (cotizacion.getDescuento().signum() > 0
                && request.getMonto().compareTo(montoSinDescuento) == 0)
                ? cotizacion.getTotal()
                : request.getMonto();

        Recepcionista recepcionista = recepcionistaRepository.findById(usuarioActual.getId())
                .orElseGet(() -> recepcionistaRepository.save(Recepcionista.builder()
                        .usuario(usuarioActual)
                        .fechaContratacion(LocalDate.now())
                        .build()));

        EstadoFactura estadoFactura = esAbono ? estadoFactura(ESTADO_FACTURA_PENDIENTE, "PENDIENTE") : estadoPagada();
        LocalDate fechaLimite = esAbono ? LocalDate.now().plusDays(reglasAbono.getPlazoDias()) : LocalDate.now();

        Factura factura = Factura.builder()
                .membresia(membresia)
                .recepcionista(recepcionista)
                .estadoFactura(estadoFactura)
                .idPromocion(cotizacion.getIdPromocion())
                .montoBase(cotizacion.getMontoBase())
                .descuento(cotizacion.getDescuento())
                .recargo(cotizacion.getRecargo())
                .monto(cotizacion.getTotal())
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(fechaLimite)
                .observacion(request.getObservacion() != null ? request.getObservacion() : "Pago de membresía")
                .build();
        factura = facturaRepository.save(factura);

        ComprobantePago comprobante = ComprobantePago.builder()
                .factura(factura)
                .metodoPago(metodoPago)
                .fechaPago(LocalDateTime.now())
                .montoPagado(montoPagado)
                .referencia(request.getReferencia())
                .build();
        comprobante = comprobantePagoRepository.save(comprobante);

        PlanMembresia plan = membresia.getPlan();
        int duracionDias = (plan != null && plan.getDuracion() != null) ? plan.getDuracion() : 30;

        LocalDate hoy = LocalDate.now();
        LocalDate fechaBase = (membresia.getFechaVencimiento() != null && !membresia.getFechaVencimiento().isBefore(hoy))
                ? membresia.getFechaVencimiento()
                : hoy;

        LocalDate nuevaFechaVencimiento = fechaBase.plusDays(duracionDias);
        membresia.setFechaVencimiento(nuevaFechaVencimiento);

        // Si tiene un congelamiento en curso sigue CONGELADA; al terminar el congelamiento vuelve a ACTIVA
        boolean congelada = congelamientoService.estaCongeladaEn(membresia.getId(), hoy);
        EstadoMembresia nuevoEstado = estadoMembresiaRepository.findById(congelada ? 3 : 1)
                .orElseGet(() -> estadoMembresiaRepository.findByNombreIgnoreCase(congelada ? "CONGELADA" : "ACTIVA")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado de membresía no encontrado en el catálogo")));
        membresia.setEstadoMembresia(nuevoEstado);
        membresiaRepository.save(membresia);

        auditoriaService.registrar(
                usuarioActual,
                "pago",
                "INSERT",
                comprobante.getId(),
                String.format("Pago registrado ID %d (Monto: %s, Ref: %s%s). Nueva fecha vencimiento membresía ID %d: %s",
                        comprobante.getId(), montoPagado, request.getReferencia(),
                        esAbono ? String.format(", abono inicial; saldo Q. %s hasta %s", cotizacion.getTotal().subtract(montoPagado), fechaLimite) : "",
                        membresia.getId(), nuevaFechaVencimiento)
        );

        avisoPagoService.registrar(factura, cotizacion);
        return enriquecer(pagoMapper.toPagoDto(comprobante));
    }

    /**
     * Abono al saldo de una factura pagada en cuotas. Cuando el saldo llega a cero la factura queda PAGADA.
     */
    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDto registrarAbono(CreateAbonoDto request, User usuarioActual) {
        if (request == null || request.getIdFactura() == null || request.getMonto() == null) {
            throw new RuntimeException("Los datos del abono son obligatorios");
        }
        if (usuarioActual == null) {
            throw new RuntimeException("Se requiere un usuario autenticado para registrar el abono");
        }
        Factura factura = facturaRepository.findByIdWithDetails(request.getIdFactura())
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + request.getIdFactura()));
        MetodoPago metodoPago = metodoPagoRepository.findById(request.getIdMetodoPago())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado con ID: " + request.getIdMetodoPago()));

        String estadoActual = factura.getEstadoFactura() != null ? factura.getEstadoFactura().getNombre() : "";
        BigDecimal saldo = estadoCuentaService.saldoDe(factura);
        if (!"PENDIENTE".equalsIgnoreCase(estadoActual) || saldo.signum() <= 0) {
            throw new RuntimeException(String.format("La factura #%d no tiene saldo pendiente", factura.getId()));
        }
        BigDecimal monto = request.getMonto().setScale(2, RoundingMode.HALF_UP);
        if (monto.compareTo(saldo) > 0) {
            throw new RuntimeException(String.format(
                    "El abono (Q. %.2f) supera el saldo pendiente de la factura (Q. %.2f)", monto, saldo));
        }

        ComprobantePago comprobante = comprobantePagoRepository.save(ComprobantePago.builder()
                .factura(factura)
                .metodoPago(metodoPago)
                .fechaPago(LocalDateTime.now())
                .montoPagado(monto)
                .referencia(request.getReferencia())
                .build());

        BigDecimal saldoRestante = saldo.subtract(monto);
        if (saldoRestante.signum() == 0) {
            factura.setEstadoFactura(estadoPagada());
            facturaRepository.save(factura);
        }

        auditoriaService.registrar(usuarioActual, "pago", "INSERT", comprobante.getId(),
                String.format("Abono ID %d a factura #%d por Q. %s. Saldo restante: Q. %s",
                        comprobante.getId(), factura.getId(), monto, saldoRestante));

        return enriquecer(pagoMapper.toPagoDto(comprobante));
    }

    private static final int ESTADO_FACTURA_PENDIENTE = 1;

    private EstadoFactura estadoPagada() {
        return estadoFactura(2, "PAGADA");
    }

    private EstadoFactura estadoFactura(int id, String nombre) {
        return estadoFacturaRepository.findById(id)
                .orElseGet(() -> estadoFacturaRepository.findByNombreIgnoreCase(nombre)
                        .orElseGet(() -> estadoFacturaRepository.save(EstadoFactura.builder()
                                .nombre(nombre)
                                .descripcion("PAGADA".equals(nombre) ? "Factura pagada completamente" : "Factura con saldo pendiente")
                                .build())));
    }

    public Page<PagoResponseDto> obtenerHistorialPagosPorSocio(Integer socioId, Pageable pageable) {
        Pageable sanitizedPageable = sanitizePageable(pageable, "fechaPago");
        return comprobantePagoRepository.findBySocioIdPaged(socioId, sanitizedPageable)
                .map(pagoMapper::toPagoDto)
                .map(this::enriquecer);
    }

    public Page<PagoResponseDto> buscarHistorialPagos(Integer socioId, LocalDate fechaDesde, LocalDate fechaHasta,
                                                      Integer idMetodoPago, Pageable pageable) {
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new RuntimeException("La fecha final no puede ser anterior a la fecha inicial");
        }
        LocalDateTime desde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hastaExclusivo = fechaHasta != null ? fechaHasta.plusDays(1).atStartOfDay() : null;
        Pageable sanitizedPageable = sanitizePageable(pageable, "fechaPago");
        return comprobantePagoRepository
                .buscarHistorial(socioId, desde, hastaExclusivo, idMetodoPago, sanitizedPageable)
                .map(pagoMapper::toPagoDto)
                .map(this::enriquecer);
    }

    private Pageable sanitizePageable(Pageable pageable, String defaultSortProperty) {
        if (pageable == null) {
            return org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, defaultSortProperty));
        }
        org.springframework.data.domain.Sort sort = pageable.getSort();
        boolean hasInvalidSort = sort.stream().anyMatch(order ->
                "string".equalsIgnoreCase(order.getProperty())
        );
        if (hasInvalidSort || sort.isUnsorted()) {
            return org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, defaultSortProperty));
        }
        return pageable;
    }


    public List<PagoResponseDto> obtenerHistorialPagosPorSocio(Integer socioId) {
        List<ComprobantePago> comprobantes = comprobantePagoRepository.findBySocioIdWithDetails(socioId);
        return pagoMapper.toPagoDtoList(comprobantes).stream()
                .map(this::enriquecer)
                .toList();
    }

    public PagoResponseDto obtenerPagoPorId(Integer id) {
        ComprobantePago comprobante = comprobantePagoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago/Comprobante no encontrado con ID: " + id));
        return enriquecer(pagoMapper.toPagoDto(comprobante));
    }

    /** Nombre de la promoción y, para pagos en abonos, cuánto se llevaba pagado y el saldo que quedó. */
    private PagoResponseDto enriquecer(PagoResponseDto dto) {
        conNombrePromocion(dto);
        if (dto != null && dto.getIdFactura() != null && dto.getIdComprobante() != null && dto.getTotalFactura() != null) {
            BigDecimal abonado = EstadoCuentaService.cero(
                    comprobantePagoRepository.sumPagadoHasta(dto.getIdFactura(), dto.getIdComprobante()));
            dto.setTotalAbonado(abonado);
            dto.setSaldoPendiente(dto.getTotalFactura().subtract(abonado).max(BigDecimal.ZERO));
            dto.setNumeroAbono((int) Math.max(1, comprobantePagoRepository.contarHasta(dto.getIdFactura(), dto.getIdComprobante())));
        }
        return dto;
    }

    /** Agrega el nombre de la promoción aplicada para mostrarlo en el historial y en el comprobante. */
    private PagoResponseDto conNombrePromocion(PagoResponseDto dto) {
        if (dto != null && dto.getIdPromocion() != null && dto.getNombrePromocion() == null) {
            promocionRepository.findById(dto.getIdPromocion())
                    .ifPresent(promocion -> dto.setNombrePromocion(promocion.getNombre()));
        }
        return dto;
    }

    public List<MetodoPagoResponseDto> listarMetodosPago() {
        return metodoPagoRepository.findAll().stream()
                .map(pagoMapper::toMetodoPagoDto)
                .toList();
    }
}
