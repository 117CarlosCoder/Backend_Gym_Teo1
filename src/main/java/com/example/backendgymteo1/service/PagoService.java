package com.example.backendgymteo1.service;

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

    private static final BigDecimal MONTO_MINIMO = new BigDecimal("0.01");

    public CotizacionPagoDto calcularCotizacion(Membresia membresia, LocalDate hoy) {
        BigDecimal base = (membresia.getPlan() != null && membresia.getPlan().getPrecio() != null)
                ? membresia.getPlan().getPrecio().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        Promocion mejorPromocion = promocionRepository.findVigentes(hoy).stream()
                .max(Comparator.comparing(p -> p.calcularDescuento(base)))
                .orElse(null);

        BigDecimal descuento = BigDecimal.ZERO.setScale(2);
        if (mejorPromocion != null) {
            // El total nunca puede quedar por debajo de MONTO_MINIMO (restricción chk_monto_factura)
            descuento = mejorPromocion.calcularDescuento(base)
                    .min(base.subtract(MONTO_MINIMO).max(BigDecimal.ZERO));
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
                .idPromocion(descuento.signum() > 0 ? mejorPromocion.getId() : null)
                .nombrePromocion(descuento.signum() > 0 ? mejorPromocion.getNombre() : null)
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
        Membresia membresia = membresiaRepository.findByIdWithDetails(idMembresia)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + idMembresia));
        return calcularCotizacion(membresia, LocalDate.now());
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

        CotizacionPagoDto cotizacion = calcularCotizacion(membresia, LocalDate.now());

        if (request.getMonto() == null || request.getMonto().compareTo(cotizacion.getTotal()) < 0) {
            throw new RuntimeException(String.format("Disculpe pero su deuda es de Q. %.2f", cotizacion.getTotal()));
        }


        Recepcionista recepcionista = recepcionistaRepository.findById(usuarioActual.getId())
                .orElseGet(() -> recepcionistaRepository.save(Recepcionista.builder()
                        .usuario(usuarioActual)
                        .fechaContratacion(LocalDate.now())
                        .build()));

        EstadoFactura estadoPagada = estadoFacturaRepository.findById(2)
                .orElseGet(() -> estadoFacturaRepository.findByNombreIgnoreCase("PAGADA")
                        .orElseGet(() -> estadoFacturaRepository.save(EstadoFactura.builder()
                                .nombre("PAGADA")
                                .descripcion("Factura pagada completamente")
                                .build())));

        Factura factura = Factura.builder()
                .membresia(membresia)
                .recepcionista(recepcionista)
                .estadoFactura(estadoPagada)
                .idPromocion(cotizacion.getIdPromocion())
                .montoBase(cotizacion.getMontoBase())
                .descuento(cotizacion.getDescuento())
                .recargo(cotizacion.getRecargo())
                .monto(cotizacion.getTotal())
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now())
                .observacion(request.getObservacion() != null ? request.getObservacion() : "Pago de membresía")
                .build();
        factura = facturaRepository.save(factura);

        ComprobantePago comprobante = ComprobantePago.builder()
                .factura(factura)
                .metodoPago(metodoPago)
                .fechaPago(LocalDateTime.now())
                .montoPagado(request.getMonto())
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

        EstadoMembresia estadoActiva = estadoMembresiaRepository.findById(1)
                .orElseGet(() -> estadoMembresiaRepository.findByNombreIgnoreCase("ACTIVA")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'ACTIVA' no encontrado en el catálogo")));
        membresia.setEstadoMembresia(estadoActiva);
        membresiaRepository.save(membresia);

        auditoriaService.registrar(
                usuarioActual,
                "pago",
                "INSERT",
                comprobante.getId(),
                String.format("Pago registrado ID %d (Monto: %s, Ref: %s). Nueva fecha vencimiento membresía ID %d: %s",
                        comprobante.getId(), request.getMonto(), request.getReferencia(), membresia.getId(), nuevaFechaVencimiento)
        );

        avisoPagoService.registrar(factura, cotizacion);
        return pagoMapper.toPagoDto(comprobante);
    }

    public Page<PagoResponseDto> obtenerHistorialPagosPorSocio(Integer socioId, Pageable pageable) {
        Pageable sanitizedPageable = sanitizePageable(pageable, "fechaPago");
        return comprobantePagoRepository.findBySocioIdPaged(socioId, sanitizedPageable)
                .map(pagoMapper::toPagoDto);
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
                .map(pagoMapper::toPagoDto);
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
        return pagoMapper.toPagoDtoList(comprobantes);
    }

    public PagoResponseDto obtenerPagoPorId(Integer id) {
        ComprobantePago comprobante = comprobantePagoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago/Comprobante no encontrado con ID: " + id));
        return pagoMapper.toPagoDto(comprobante);
    }

    public List<MetodoPagoResponseDto> listarMetodosPago() {
        return metodoPagoRepository.findAll().stream()
                .map(pagoMapper::toMetodoPagoDto)
                .toList();
    }
}
