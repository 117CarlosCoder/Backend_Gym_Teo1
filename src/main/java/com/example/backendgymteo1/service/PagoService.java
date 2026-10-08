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
import com.example.backendgymteo1.entity.Recepcionista;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .monto(request.getMonto())
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

        return pagoMapper.toPagoDto(comprobante);
    }

    public Page<PagoResponseDto> obtenerHistorialPagosPorSocio(Integer socioId, Pageable pageable) {
        return comprobantePagoRepository.findBySocioIdPaged(socioId, pageable)
                .map(pagoMapper::toPagoDto);
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
