package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.membresia.CancelarMembresiaRequestDto;
import com.example.backendgymteo1.dto.membresia.CreateMembresiaDto;
import com.example.backendgymteo1.dto.membresia.MembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.UpdateMembresiaDto;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.MembresiaMapper;
import com.example.backendgymteo1.repository.EstadoMembresiaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.PlanMembresiaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembresiaService {

    private final MembresiaRepository membresiaRepository;
    private final SocioRepository socioRepository;
    private final PlanMembresiaRepository planMembresiaRepository;
    private final EstadoMembresiaRepository estadoMembresiaRepository;
    private final SucursalRepository sucursalRepository;
    private final MembresiaMapper membresiaMapper;
    private final AuditoriaService auditoriaService;
    private final EmailService emailService;

    @Transactional(rollbackFor = Exception.class)
    public MembresiaResponseDto create(CreateMembresiaDto request) {
        return create(request, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public MembresiaResponseDto create(CreateMembresiaDto request, User usuarioActual) {
        Socio socio = socioRepository.findByIdAndActiveWithUser(request.getIdSocio())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Socio activo no encontrado con ID: " + request.getIdSocio()));

        PlanMembresia plan = planMembresiaRepository.findById(request.getIdPlan())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan de membresía no encontrado con ID: " + request.getIdPlan()));

        EstadoMembresia estado;
        if (request.getIdEstadoMembresia() != null) {
            estado = estadoMembresiaRepository.findById(request.getIdEstadoMembresia())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Estado de membresía no encontrado con ID: " + request.getIdEstadoMembresia()));
        } else {
            estado = estadoMembresiaRepository.findById(1)
                    .orElseGet(() -> estadoMembresiaRepository.findByNombreIgnoreCase("ACTIVA")
                            .orElseThrow(() -> new ResourceNotFoundException("Estado 'ACTIVA' no encontrado en el catálogo")));
        }

        LocalDate fechaInicio = request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now();

        if (estado.getId() == 1 || "ACTIVA".equalsIgnoreCase(estado.getNombre())) {
            boolean yaTieneActiva = membresiaRepository.existsActiveBySocio(socio.getId(), estado.getId(), fechaInicio);
            if (yaTieneActiva) {
                throw new RuntimeException("El socio ya cuenta con una membresía activa vigente.");
            }
        }

        LocalDate fechaVencimiento = fechaInicio.plusDays(plan.getDuracion());

        Set<Sucursal> sucursales = new HashSet<>();
        if (request.getSucursalIds() != null && !request.getSucursalIds().isEmpty()) {
            for (Integer idSucursal : request.getSucursalIds()) {
                Sucursal suc = sucursalRepository.findById(idSucursal)
                        .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + idSucursal));
                sucursales.add(suc);
            }
        } else if (socio.getSucursal() != null) {
            sucursales.add(socio.getSucursal());
        }

        Membresia membresia = Membresia.builder()
                .socio(socio)
                .plan(plan)
                .estadoMembresia(estado)
                .fechaInicio(fechaInicio)
                .fechaVencimiento(fechaVencimiento)
                .sucursales(sucursales)
                .build();

        membresia = membresiaRepository.save(membresia);

        auditoriaService.registrar(
                usuarioActual,
                "membresia",
                "INSERT",
                membresia.getId(),
                String.format("Asignación de membresía ID %d para socio ID %d (Plan: %s, Sucursales: %d)",
                        membresia.getId(), socio.getId(), plan.getNombre(), sucursales.size())
        );

        return membresiaMapper.toDto(membresia);
    }

    public Page<MembresiaResponseDto> findAllPaged(
            Integer socioId,
            Integer planId,
            Integer sucursalId,
            Integer estadoId,
            String estadoNombre,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Pageable pageable) {
        return membresiaRepository.findWithFiltersPaged(
                socioId, planId, sucursalId, estadoId, estadoNombre, fechaDesde, fechaHasta, pageable)
                .map(membresiaMapper::toDto);
    }

    public List<MembresiaResponseDto> findAll(
            Integer socioId,
            Integer planId,
            Integer estadoId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {
        List<Membresia> membresias = membresiaRepository.findWithFilters(
                socioId, planId, null, estadoId, null, fechaDesde, fechaHasta);
        return membresiaMapper.toDtoList(membresias);
    }

    public MembresiaResponseDto findById(Integer id) {
        Membresia membresia = membresiaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + id));
        return membresiaMapper.toDto(membresia);
    }

    public List<MembresiaResponseDto> findBySocioId(Integer socioId) {
        if (!socioRepository.existsById(socioId)) {
            throw new ResourceNotFoundException("Socio no encontrado con ID: " + socioId);
        }
        List<Membresia> membresias = membresiaRepository.findBySocioIdWithDetails(socioId);
        return membresiaMapper.toDtoList(membresias);
    }

    @Transactional(rollbackFor = Exception.class)
    public MembresiaResponseDto update(Integer id, UpdateMembresiaDto request) {
        return update(id, request, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public MembresiaResponseDto update(Integer id, UpdateMembresiaDto request, User usuarioActual) {
        Membresia membresia = membresiaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + id));

        List<String> cambios = new ArrayList<>();

        if (request.getTipoMembresiaId() != null && !request.getTipoMembresiaId().equals(membresia.getPlan().getId())) {
            PlanMembresia nuevoPlan = planMembresiaRepository.findById(request.getTipoMembresiaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con ID: " + request.getTipoMembresiaId()));
            cambios.add(String.format("plan: '%s' -> '%s'", membresia.getPlan().getNombre(), nuevoPlan.getNombre()));
            membresia.setPlan(nuevoPlan);
        }

        if (request.getFechaVencimiento() != null && !request.getFechaVencimiento().equals(membresia.getFechaVencimiento())) {
            if (request.getFechaVencimiento().isBefore(membresia.getFechaInicio())) {
                throw new RuntimeException("La fecha de vencimiento no puede ser anterior a la fecha de inicio (" + membresia.getFechaInicio() + ")");
            }
            cambios.add(String.format("fechaVencimiento: '%s' -> '%s'", membresia.getFechaVencimiento(), request.getFechaVencimiento()));
            membresia.setFechaVencimiento(request.getFechaVencimiento());
        }

        if (request.getSucursalIds() != null) {
            Set<Sucursal> nuevasSucursales = new HashSet<>();
            for (Integer sucId : request.getSucursalIds()) {
                Sucursal suc = sucursalRepository.findById(sucId)
                        .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + sucId));
                nuevasSucursales.add(suc);
            }
            cambios.add(String.format("sucursales: %d asignadas", nuevasSucursales.size()));
            membresia.setSucursales(nuevasSucursales);
        }

        membresia = membresiaRepository.save(membresia);

        String descripcion = cambios.isEmpty() ? "Actualización sin cambios" : String.join(", ", cambios);
        auditoriaService.registrar(usuarioActual, "membresia", "UPDATE", membresia.getId(), descripcion);

        return membresiaMapper.toDto(membresia);
    }

    @Transactional(rollbackFor = Exception.class)
    public MembresiaResponseDto cancelar(Integer id, String motivo) {
        CancelarMembresiaRequestDto req = CancelarMembresiaRequestDto.builder()
                .motivo(motivo != null ? motivo : "Cancelación manual")
                .reembolso(false)
                .build();
        return cancelar(id, req, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public MembresiaResponseDto cancelar(Integer id, CancelarMembresiaRequestDto request, User usuarioActual) {
        Membresia membresia = membresiaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + id));

        boolean reembolso = Boolean.TRUE.equals(request.getReembolso());
        if (reembolso && (request.getMontoReembolso() == null || request.getMontoReembolso().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("El monto a reembolsar debe ser mayor a cero si se aprueba reembolso.");
        }

        EstadoMembresia estadoCancelada = estadoMembresiaRepository.findById(4)
                .orElseGet(() -> estadoMembresiaRepository.findByNombreIgnoreCase("CANCELADA")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'CANCELADA' no encontrado en el catálogo")));

        membresia.setEstadoMembresia(estadoCancelada);
        membresia.setFechaCancelacion(LocalDateTime.now());
        membresia.setUsuarioCancelo(usuarioActual);
        membresia.setMotivoCancelacion(request.getMotivo());
        membresia.setComentariosCancelacion(request.getComentarios());
        membresia.setReembolso(reembolso);
        membresia.setMontoReembolso(reembolso ? request.getMontoReembolso() : BigDecimal.ZERO);

        membresia = membresiaRepository.save(membresia);

        auditoriaService.registrar(
                usuarioActual,
                "membresia",
                "CANCEL",
                membresia.getId(),
                String.format("Cancelación de membresía ID %d. Motivo: %s. Reembolso: %s (Monto: %s)",
                        membresia.getId(),
                        request.getMotivo(),
                        reembolso ? "SÍ" : "NO",
                        reembolso ? request.getMontoReembolso() : "0.00")
        );

        if (membresia.getSocio() != null && membresia.getSocio().getUsuario() != null) {
            User socioUser = membresia.getSocio().getUsuario();
            String nombreSocio = socioUser.getNombres() + " " + socioUser.getApellidos();
            String nombrePlan = membresia.getPlan() != null ? membresia.getPlan().getNombre() : "Plan de Membresía";
            emailService.enviarNotificacionCancelacionMembresia(
                    socioUser.getCorreo(),
                    nombreSocio,
                    nombrePlan,
                    request.getMotivo(),
                    reembolso,
                    request.getMontoReembolso()
            );
        }

        return membresiaMapper.toDto(membresia);
    }

    public MembresiaResponseDto findActiveBySocioId(Integer socioId) {
        if (!socioRepository.existsById(socioId)) {
            throw new ResourceNotFoundException("Socio no encontrado con ID: " + socioId);
        }
        Membresia membresia = membresiaRepository.findActiveBySocioId(socioId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("El socio no cuenta con una membresía activa vigente"));
        return membresiaMapper.toDto(membresia);
    }

    public void validarSocioConMembresiaActiva(Integer socioId) {
        if (!socioRepository.existsById(socioId)) {
            throw new ResourceNotFoundException("Socio no encontrado con ID: " + socioId);
        }
        boolean tieneActiva = membresiaRepository.findActiveBySocioId(socioId, LocalDate.now()).isPresent();
        if (!tieneActiva) {
            throw new RuntimeException("El socio ID " + socioId + " no cuenta con una membresía activa y vigente para realizar esta acción.");
        }
    }
}
