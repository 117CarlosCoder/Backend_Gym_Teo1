package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.promocion.CreatePromocionDto;
import com.example.backendgymteo1.dto.promocion.PromocionResponseDto;
import com.example.backendgymteo1.dto.promocion.UpdatePromocionDto;
import com.example.backendgymteo1.entity.Promocion;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.FacturaRepository;
import com.example.backendgymteo1.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromocionService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final PromocionRepository promocionRepository;
    private final FacturaRepository facturaRepository;
    private final AuditoriaService auditoriaService;

    public List<PromocionResponseDto> findAll() {
        return promocionRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<PromocionResponseDto> findVigentes() {
        return promocionRepository.findVigentes(LocalDate.now()).stream().map(this::toDto).toList();
    }

    public PromocionResponseDto findById(Integer id) {
        return toDto(buscar(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public PromocionResponseDto create(CreatePromocionDto request, User usuarioActual) {
        String nombre = request.getNombre().trim();
        if (promocionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new RuntimeException("Ya existe una promoción con el nombre: " + nombre);
        }
        validarValor(request.getTipoDescuento(), request.getValorDescuento());
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        Promocion promocion = promocionRepository.save(Promocion.builder()
                .nombre(nombre)
                .descripcion(request.getDescripcion())
                .tipoDescuento(request.getTipoDescuento())
                .valorDescuento(request.getValorDescuento())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .activo(request.getActivo() == null || request.getActivo())
                .build());

        auditoriaService.registrar(usuarioActual, "promocion", "INSERT", promocion.getId(),
                String.format("Promoción creada con ID %d, Nombre: '%s', %s: %s, Vigencia: %s a %s",
                        promocion.getId(), promocion.getNombre(), promocion.getTipoDescuento(),
                        promocion.getValorDescuento(), promocion.getFechaInicio(), promocion.getFechaFin()));
        return toDto(promocion);
    }

    @Transactional(rollbackFor = Exception.class)
    public PromocionResponseDto update(Integer id, UpdatePromocionDto request, User usuarioActual) {
        Promocion promocion = buscar(id);

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            String nuevoNombre = request.getNombre().trim();
            if (!nuevoNombre.equalsIgnoreCase(promocion.getNombre())
                    && promocionRepository.existsByNombreIgnoreCase(nuevoNombre)) {
                throw new RuntimeException("Ya existe una promoción con el nombre: " + nuevoNombre);
            }
            promocion.setNombre(nuevoNombre);
        }
        if (request.getDescripcion() != null) {
            promocion.setDescripcion(request.getDescripcion());
        }
        if (request.getTipoDescuento() != null) {
            promocion.setTipoDescuento(request.getTipoDescuento());
        }
        if (request.getValorDescuento() != null) {
            promocion.setValorDescuento(request.getValorDescuento());
        }
        if (request.getFechaInicio() != null) {
            promocion.setFechaInicio(request.getFechaInicio());
        }
        if (request.getFechaFin() != null) {
            promocion.setFechaFin(request.getFechaFin());
        }
        if (request.getActivo() != null) {
            promocion.setActivo(request.getActivo());
        }
        validarValor(promocion.getTipoDescuento(), promocion.getValorDescuento());
        validarFechas(promocion.getFechaInicio(), promocion.getFechaFin());

        promocion = promocionRepository.save(promocion);
        auditoriaService.registrar(usuarioActual, "promocion", "UPDATE", promocion.getId(),
                String.format("Promoción ID %d actualizada: '%s', %s: %s, Vigencia: %s a %s, Activa: %s",
                        promocion.getId(), promocion.getNombre(), promocion.getTipoDescuento(),
                        promocion.getValorDescuento(), promocion.getFechaInicio(), promocion.getFechaFin(),
                        promocion.isActivo()));
        return toDto(promocion);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id, User usuarioActual) {
        Promocion promocion = buscar(id);
        if (facturaRepository.existsByIdPromocion(id)) {
            throw new RuntimeException(String.format(
                    "No se puede eliminar la promoción '%s' porque ya fue aplicada en facturas. Desactívela en su lugar.",
                    promocion.getNombre()));
        }
        promocionRepository.delete(promocion);
        auditoriaService.registrar(usuarioActual, "promocion", "DELETE", id,
                String.format("Eliminación de la promoción '%s' (ID: %d)", promocion.getNombre(), id));
    }

    private Promocion buscar(Integer id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
    }

    private void validarValor(Promocion.TipoDescuento tipo, BigDecimal valor) {
        if (tipo == Promocion.TipoDescuento.PORCENTAJE && valor.compareTo(CIEN) > 0) {
            throw new RuntimeException("El porcentaje de descuento no puede ser mayor a 100");
        }
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (fin.isBefore(inicio)) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private PromocionResponseDto toDto(Promocion p) {
        return PromocionResponseDto.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .tipoDescuento(p.getTipoDescuento())
                .valorDescuento(p.getValorDescuento())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activo(p.isActivo())
                .vigente(p.estaVigente(LocalDate.now()))
                .build();
    }
}
