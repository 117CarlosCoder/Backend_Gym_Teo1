package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.membresia.CreateMembresiaDto;
import com.example.backendgymteo1.dto.membresia.MembresiaResponseDto;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.MembresiaMapper;
import com.example.backendgymteo1.repository.EstadoMembresiaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.PlanMembresiaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembresiaService {

    private final MembresiaRepository membresiaRepository;
    private final SocioRepository socioRepository;
    private final PlanMembresiaRepository planMembresiaRepository;
    private final EstadoMembresiaRepository estadoMembresiaRepository;
    private final MembresiaMapper membresiaMapper;

    @Transactional(rollbackFor = Exception.class)
    public MembresiaResponseDto create(CreateMembresiaDto request) {
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

        Membresia membresia = Membresia.builder()
                .socio(socio)
                .plan(plan)
                .estadoMembresia(estado)
                .fechaInicio(fechaInicio)
                .fechaVencimiento(fechaVencimiento)
                .build();

        membresia = membresiaRepository.save(membresia);
        log.info("Membresía ID {} creada exitosamente para el socio ID {} (Plan: {}, Vigencia: {} al {})",
                membresia.getId(), socio.getId(), plan.getNombre(), fechaInicio, fechaVencimiento);

        return membresiaMapper.toDto(membresia);
    }

    public List<MembresiaResponseDto> findAll(
            Integer socioId,
            Integer planId,
            Integer estadoId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {
        List<Membresia> membresias = membresiaRepository.findWithFilters(
                socioId, planId, estadoId, fechaDesde, fechaHasta);
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
    public MembresiaResponseDto cancelar(Integer id, String motivo) {
        Membresia membresia = membresiaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membresía no encontrada con ID: " + id));

        EstadoMembresia estadoCancelada = estadoMembresiaRepository.findById(4)
                .orElseGet(() -> estadoMembresiaRepository.findByNombreIgnoreCase("CANCELADA")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'CANCELADA' no encontrado en el catálogo")));

        membresia.setEstadoMembresia(estadoCancelada);
        membresia = membresiaRepository.save(membresia);
        log.info("Membresía ID {} cancelada. Motivo: {}", id, motivo != null ? motivo : "Sin motivo especificado");

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
