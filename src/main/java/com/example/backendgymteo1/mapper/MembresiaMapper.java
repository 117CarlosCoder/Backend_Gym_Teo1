package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.membresia.EstadoMembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.MembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.PlanMembresiaResponseDto;
import com.example.backendgymteo1.dto.socio.MembresiaResumenDto;
import com.example.backendgymteo1.dto.sucursal.SucursalResponseDto;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Sucursal;
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

        List<SucursalResponseDto> sucursalesDto = Collections.emptyList();
        if (membresia.getSucursales() != null && !membresia.getSucursales().isEmpty()) {
            sucursalesDto = membresia.getSucursales().stream()
                    .map(this::toSucursalDto)
                    .toList();
        }

        String nombreUsuarioCancelo = null;
        if (membresia.getUsuarioCancelo() != null) {
            nombreUsuarioCancelo = membresia.getUsuarioCancelo().getNombres() + " " + membresia.getUsuarioCancelo().getApellidos();
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
                .sucursales(sucursalesDto)
                .fechaCancelacion(membresia.getFechaCancelacion())
                .usuarioCancelo(nombreUsuarioCancelo)
                .motivoCancelacion(membresia.getMotivoCancelacion())
                .comentariosCancelacion(membresia.getComentariosCancelacion())
                .reembolso(membresia.isReembolso())
                .montoReembolso(membresia.getMontoReembolso())
                .build();
    }

    public MembresiaResumenDto toResumenDto(Membresia membresia) {
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

        return MembresiaResumenDto.builder()
                .idMembresia(membresia.getId())
                .plan(membresia.getPlan() != null ? membresia.getPlan().getNombre() : "N/A")
                .estado(membresia.getEstadoMembresia() != null ? membresia.getEstadoMembresia().getNombre() : "N/A")
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

    public SucursalResponseDto toSucursalDto(Sucursal sucursal) {
        if (sucursal == null) {
            return null;
        }
        return SucursalResponseDto.builder()
                .id(sucursal.getId())
                .nombre(sucursal.getNombre())
                .direccion(sucursal.getDireccion())
                .telefono(sucursal.getTelefono())
                .activo(sucursal.isActivo())
                .build();
    }
}
