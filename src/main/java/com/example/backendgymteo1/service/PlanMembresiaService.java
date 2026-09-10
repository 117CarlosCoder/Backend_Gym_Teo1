package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.membresia.CreatePlanMembresiaDto;
import com.example.backendgymteo1.dto.membresia.PlanMembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.UpdatePlanMembresiaDto;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.MembresiaMapper;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.PlanMembresiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanMembresiaService {

    private final PlanMembresiaRepository planMembresiaRepository;
    private final MembresiaRepository membresiaRepository;
    private final MembresiaMapper membresiaMapper;
    private final AuditoriaService auditoriaService;

    public List<PlanMembresiaResponseDto> findAll() {
        return planMembresiaRepository.findAll().stream()
                .map(membresiaMapper::toPlanDto)
                .toList();
    }

    public PlanMembresiaResponseDto findById(Integer id) {
        return planMembresiaRepository.findById(id)
                .map(membresiaMapper::toPlanDto)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de membresía no encontrado con ID: " + id));
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanMembresiaResponseDto create(CreatePlanMembresiaDto request, User usuarioActual) {
        String nombreTrimmed = request.getNombre().trim();
        if (planMembresiaRepository.existsByNombreIgnoreCase(nombreTrimmed)) {
            throw new RuntimeException("Ya existe un plan de membresía con el nombre: " + nombreTrimmed);
        }

        PlanMembresia plan = membresiaMapper.toPlanEntity(request);
        plan = planMembresiaRepository.save(plan);

        auditoriaService.registrar(
                usuarioActual,
                "plan_membresia",
                "INSERT",
                plan.getId(),
                String.format("Plan de membresía creado con ID %d, Nombre: '%s', Duración: %d días, Precio: %s",
                        plan.getId(), plan.getNombre(), plan.getDuracion(), plan.getPrecio())
        );

        return membresiaMapper.toPlanDto(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanMembresiaResponseDto update(Integer id, UpdatePlanMembresiaDto request, User usuarioActual) {
        PlanMembresia plan = planMembresiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de membresía no encontrado con ID: " + id));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            String nuevoNombre = request.getNombre().trim();
            if (!nuevoNombre.equalsIgnoreCase(plan.getNombre()) && planMembresiaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
                throw new RuntimeException("Ya existe un plan de membresía con el nombre: " + nuevoNombre);
            }
        }

        List<String> cambios = new ArrayList<>();
        if (request.getNombre() != null && !request.getNombre().isBlank() && !request.getNombre().trim().equalsIgnoreCase(plan.getNombre())) {
            cambios.add(String.format("nombre: '%s' -> '%s'", plan.getNombre(), request.getNombre().trim()));
        }
        if (request.getDuracion() != null && !request.getDuracion().equals(plan.getDuracion())) {
            cambios.add(String.format("duracion: %d -> %d", plan.getDuracion(), request.getDuracion()));
        }
        if (request.getPrecio() != null && !request.getPrecio().equals(plan.getPrecio())) {
            cambios.add(String.format("precio: %s -> %s", plan.getPrecio(), request.getPrecio()));
        }
        if (request.getDescripcion() != null && !Objects.equals(request.getDescripcion(), plan.getDescripcion())) {
            cambios.add("descripcion actualizada");
        }

        membresiaMapper.updatePlanEntity(plan, request);
        plan = planMembresiaRepository.save(plan);

        String descripcionCambios = cambios.isEmpty() ? "Actualización de plan sin cambios detectados" : String.join(", ", cambios);
        auditoriaService.registrar(
                usuarioActual,
                "plan_membresia",
                "UPDATE",
                plan.getId(),
                descripcionCambios
        );

        return membresiaMapper.toPlanDto(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id, User usuarioActual) {
        PlanMembresia plan = planMembresiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de membresía no encontrado con ID: " + id));

        if (membresiaRepository.existsByPlanId(id)) {
            throw new RuntimeException(String.format(
                    "No se puede eliminar el plan '%s' (ID %d) porque existen membresías activas o históricas asociadas a él.",
                    plan.getNombre(), id));
        }

        planMembresiaRepository.delete(plan);

        auditoriaService.registrar(
                usuarioActual,
                "plan_membresia",
                "DELETE",
                id,
                String.format("Eliminación del plan de membresía '%s' (ID: %d)", plan.getNombre(), id)
        );
    }
}
