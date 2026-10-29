package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.pago.EstadoCuentaDto;
import com.example.backendgymteo1.entity.ComprobantePago;
import com.example.backendgymteo1.entity.Factura;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.ComprobantePagoRepository;
import com.example.backendgymteo1.repository.FacturaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ajuste del PO: pagos en cuotas o abonos.
 * El saldo de una factura es su monto menos la suma de sus comprobantes; mientras haya saldo
 * la factura queda PENDIENTE con una fecha límite (factura.fecha_vencimiento).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstadoCuentaService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FacturaRepository facturaRepository;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final SocioRepository socioRepository;

    public BigDecimal saldoDe(Factura factura) {
        BigDecimal pagado = cero(comprobantePagoRepository.sumPagadoPorFactura(factura.getId()));
        return factura.getMonto().subtract(pagado).max(BigDecimal.ZERO);
    }

    /** No se puede cobrar un nuevo periodo mientras la membresía tenga una factura con saldo. */
    public void validarSinSaldoPendiente(Integer idMembresia) {
        for (Factura factura : facturaRepository.findPendientesByMembresiaId(idMembresia)) {
            BigDecimal saldo = saldoDe(factura);
            if (saldo.signum() > 0) {
                throw new RuntimeException(String.format(
                        "La membresía tiene un saldo pendiente de Q. %.2f en la factura #%d. Registre un abono para liquidarlo antes de cobrar un nuevo periodo.",
                        saldo, factura.getId()));
            }
        }
    }

    /** El socio no puede ingresar si tiene saldo cuyo plazo ya venció. */
    public void validarSinSaldoVencido(Integer socioId, LocalDate hoy) {
        for (Factura factura : facturaRepository.findPendientesBySocioId(socioId)) {
            if (factura.getFechaVencimiento() != null && factura.getFechaVencimiento().isBefore(hoy)) {
                BigDecimal saldo = saldoDe(factura);
                if (saldo.signum() > 0) {
                    throw new RuntimeException(String.format(
                            "El socio tiene un saldo vencido de Q. %.2f (factura #%d, plazo hasta el %s). Registre el abono para habilitar su ingreso.",
                            saldo, factura.getId(), factura.getFechaVencimiento().format(FORMATO_FECHA)));
                }
            }
        }
    }

    public EstadoCuentaDto estadoCuenta(Integer socioId) {
        Socio socio = socioRepository.findById(socioId)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con ID: " + socioId));
        LocalDate hoy = LocalDate.now();

        List<Factura> facturas = facturaRepository.findEstadoCuentaBySocioId(socioId);
        Map<Integer, List<ComprobantePago>> pagosPorFactura = facturas.isEmpty()
                ? Map.of()
                : comprobantePagoRepository.findByFacturaIds(facturas.stream().map(Factura::getId).toList()).stream()
                        .collect(Collectors.groupingBy(c -> c.getFactura().getId()));

        BigDecimal totalFacturado = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        BigDecimal saldoPendiente = BigDecimal.ZERO;
        BigDecimal saldoVencido = BigDecimal.ZERO;
        LocalDate proximaFechaLimite = null;

        List<EstadoCuentaDto.FacturaCuentaDto> detalle = new java.util.ArrayList<>();
        for (Factura factura : facturas) {
            List<ComprobantePago> pagos = pagosPorFactura.getOrDefault(factura.getId(), List.of());
            BigDecimal pagado = pagos.stream().map(ComprobantePago::getMontoPagado).reduce(BigDecimal.ZERO, BigDecimal::add);
            String nombreEstado = factura.getEstadoFactura() != null ? factura.getEstadoFactura().getNombre().toUpperCase() : "PAGADA";
            boolean anulada = "ANULADA".equals(nombreEstado);
            BigDecimal saldo = anulada ? BigDecimal.ZERO : factura.getMonto().subtract(pagado).max(BigDecimal.ZERO);

            String estado;
            if (anulada) {
                estado = "ANULADA";
            } else if (saldo.signum() == 0) {
                estado = "PAGADA";
            } else if (factura.getFechaVencimiento() != null && factura.getFechaVencimiento().isBefore(hoy)) {
                estado = "VENCIDA";
            } else {
                estado = "PENDIENTE";
            }

            if (!anulada) {
                totalFacturado = totalFacturado.add(factura.getMonto());
                totalPagado = totalPagado.add(pagado);
                saldoPendiente = saldoPendiente.add(saldo);
                if ("VENCIDA".equals(estado)) {
                    saldoVencido = saldoVencido.add(saldo);
                }
                if (saldo.signum() > 0 && factura.getFechaVencimiento() != null
                        && (proximaFechaLimite == null || factura.getFechaVencimiento().isBefore(proximaFechaLimite))) {
                    proximaFechaLimite = factura.getFechaVencimiento();
                }
            }

            detalle.add(EstadoCuentaDto.FacturaCuentaDto.builder()
                    .idFactura(factura.getId())
                    .idMembresia(factura.getMembresia().getId())
                    .nombrePlan(factura.getMembresia().getPlan() != null ? factura.getMembresia().getPlan().getNombre() : null)
                    .fechaEmision(factura.getFechaEmision())
                    .fechaLimite(saldo.signum() > 0 ? factura.getFechaVencimiento() : null)
                    .montoBase(factura.getMontoBase() != null ? factura.getMontoBase() : factura.getMonto())
                    .descuento(factura.getDescuento())
                    .recargo(factura.getRecargo())
                    .total(factura.getMonto())
                    .pagado(pagado)
                    .saldo(saldo)
                    .estado(estado)
                    .abonos(pagos.stream().map(c -> EstadoCuentaDto.AbonoDto.builder()
                            .idComprobante(c.getId())
                            .fechaPago(c.getFechaPago())
                            .monto(c.getMontoPagado())
                            .metodoPago(c.getMetodoPago() != null ? c.getMetodoPago().getNombre() : null)
                            .referencia(c.getReferencia())
                            .build()).toList())
                    .build());
        }

        String nombreSocio = socio.getUsuario() != null
                ? socio.getUsuario().getNombres() + " " + socio.getUsuario().getApellidos()
                : null;

        return EstadoCuentaDto.builder()
                .idSocio(socioId)
                .nombreSocio(nombreSocio)
                .totalFacturado(totalFacturado)
                .totalPagado(totalPagado)
                .saldoPendiente(saldoPendiente)
                .saldoVencido(saldoVencido)
                .proximaFechaLimite(proximaFechaLimite)
                .facturas(detalle)
                .build();
    }

    static BigDecimal cero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
