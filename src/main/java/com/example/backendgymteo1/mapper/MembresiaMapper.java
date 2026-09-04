package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.membresia.EstadoMembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.MembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.PlanMembresiaResponseDto;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MembresiaMapper {

    private final SocioMapper socioMapper;

    public MembresiaResponseDto toDto(Membresia membresia) {
        if (membresia == null) {
            return null;
        }

        LocalDate hoy = LocalDate.now();
        boolean isActiva = membresia.getEstadoMembresia() != null
                && "ACTIVA".equalsIgnoreCase(membresia.getEstadoMembresia().getNombre())
                && membresia.getFechaVencimiento() != null
                && !membresia.getFechaVencimiento().isBefore(hoy);

        Long diasRestantes = null;
        if (membresia.getFechaVencimiento() != null) {
            diasRestantes = ChronoUnit.DAYS.between(hoy, membresia.getFechaVencimiento());
        }

        return MembresiaResponseDto.builder()
                .id(membresia.getId())
                .socio(socioMapper.toDto(membresia.getSocio()))
                .plan(toPlanDto(membresia.getPlan()))
                .estado(toEstadoDto(membresia.getEstadoMembresia()))
                .fechaInicio(membresia.getFechaInicio())
                .fechaVencimiento(membresia.getFechaVencimiento())
                .activa(isActiva)
                .diasRestantes(diasRestantes)
                .build();
    }

    public List<MembresiaResponseDto> toDtoList(List<Membresia> membresias) {
        if (membresias == null) {
            return Collections.emptyList();
        }
        return membresias.stream()
                .map(this::toDto)
                .toList();
    }

    public PlanMembresiaResponseDto toPlanDto(PlanMembresia plan) {
        if (plan == null) {
            return null;
        }
        return PlanMembresiaResponseDto.builder()
                .id(plan.getId())
                .nombre(plan.getNombre())
                .duracion(plan.getDuracion())
                .precio(plan.getPrecio())
                .descripcion(plan.getDescripcion())
                .build();
    }

    public EstadoMembresiaResponseDto toEstadoDto(EstadoMembresia estado) {
        if (estado == null) {
            return null;
        }
        return EstadoMembresiaResponseDto.builder()
                .id(estado.getId())
                .nombre(estado.getNombre())
                .descripcion(estado.getDescripcion())
                .build();
    }
}
