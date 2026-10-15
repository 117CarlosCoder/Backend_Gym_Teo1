package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.promocion.CreatePromocionDto;
import com.example.backendgymteo1.dto.promocion.PromocionResponseDto;
import com.example.backendgymteo1.dto.promocion.UpdatePromocionDto;
import com.example.backendgymteo1.entity.Promocion;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.FacturaRepository;
import com.example.backendgymteo1.repository.PromocionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {

    @Mock
    private PromocionRepository promocionRepository;
    @Mock
    private FacturaRepository facturaRepository;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PromocionService promocionService;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1).correo("admin@gym.com").nombres("Admin").apellidos("Gym").rol(Rol.admin()).build();
    }

    private CreatePromocionDto createDto() {
        return CreatePromocionDto.builder()
                .nombre("  Promo Fin de Año ")
                .tipoDescuento(Promocion.TipoDescuento.PORCENTAJE)
                .valorDescuento(new BigDecimal("10"))
                .fechaInicio(LocalDate.now().minusDays(1))
                .fechaFin(LocalDate.now().plusDays(10))
                .build();
    }

    private Promocion existente() {
        return Promocion.builder().id(5).nombre("Existente").tipoDescuento(Promocion.TipoDescuento.MONTO_FIJO)
                .valorDescuento(new BigDecimal("25")).fechaInicio(LocalDate.now().minusDays(1))
                .fechaFin(LocalDate.now().plusDays(1)).activo(true).build();
    }

    @Test
    @DisplayName("Crear promoción válida: se recorta el nombre, queda activa por defecto y se audita")
    void create_ok() {
        when(promocionRepository.existsByNombreIgnoreCase("Promo Fin de Año")).thenReturn(false);
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(i -> {
            Promocion p = i.getArgument(0);
            p.setId(1);
            return p;
        });

        PromocionResponseDto result = promocionService.create(createDto(), admin);

        assertEquals("Promo Fin de Año", result.getNombre());
        assertTrue(result.isActivo());
        assertTrue(result.isVigente());
        verify(auditoriaService).registrar(any(User.class), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Crear promoción con nombre duplicado es rechazado")
    void create_nombreDuplicado() {
        when(promocionRepository.existsByNombreIgnoreCase("Promo Fin de Año")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> promocionService.create(createDto(), admin));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear promoción con porcentaje mayor a 100 es rechazado")
    void create_porcentajeMayorA100() {
        CreatePromocionDto dto = createDto();
        dto.setValorDescuento(new BigDecimal("101"));
        when(promocionRepository.existsByNombreIgnoreCase(anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> promocionService.create(dto, admin));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear promoción con fecha fin anterior a fecha inicio es rechazado")
    void create_fechasInvertidas() {
        CreatePromocionDto dto = createDto();
        dto.setFechaFin(dto.getFechaInicio().minusDays(1));
        when(promocionRepository.existsByNombreIgnoreCase(anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> promocionService.create(dto, admin));
    }

    @Test
    @DisplayName("Un monto fijo mayor a 100 sí es válido (la regla de 100 aplica solo a porcentajes)")
    void create_montoFijoMayorA100() {
        CreatePromocionDto dto = createDto();
        dto.setTipoDescuento(Promocion.TipoDescuento.MONTO_FIJO);
        dto.setValorDescuento(new BigDecimal("150"));
        when(promocionRepository.existsByNombreIgnoreCase(anyString())).thenReturn(false);
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(i -> i.getArgument(0));

        PromocionResponseDto result = promocionService.create(dto, admin);

        assertEquals(new BigDecimal("150"), result.getValorDescuento());
    }

    @Test
    @DisplayName("Actualización parcial: solo cambia los campos enviados")
    void update_parcial() {
        Promocion p = existente();
        when(promocionRepository.findById(5)).thenReturn(Optional.of(p));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(i -> i.getArgument(0));

        PromocionResponseDto result = promocionService.update(5,
                UpdatePromocionDto.builder().activo(false).build(), admin);

        assertFalse(result.isActivo());
        assertFalse(result.isVigente());
        assertEquals("Existente", result.getNombre());
        assertEquals(new BigDecimal("25"), result.getValorDescuento());
    }

    @Test
    @DisplayName("Actualizar a porcentaje > 100 combinando con el valor existente es rechazado")
    void update_cambiarATipoPorcentajeConValorGrande() {
        Promocion p = existente();
        p.setValorDescuento(new BigDecimal("150"));
        when(promocionRepository.findById(5)).thenReturn(Optional.of(p));

        assertThrows(RuntimeException.class, () -> promocionService.update(5,
                UpdatePromocionDto.builder().tipoDescuento(Promocion.TipoDescuento.PORCENTAJE).build(), admin));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar promoción inexistente lanza ResourceNotFoundException")
    void update_noExiste() {
        when(promocionRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promocionService.update(99, new UpdatePromocionDto(), admin));
    }

    @Test
    @DisplayName("Eliminar una promoción ya usada en facturas es rechazado")
    void delete_usadaEnFacturas() {
        when(promocionRepository.findById(5)).thenReturn(Optional.of(existente()));
        when(facturaRepository.existsByIdPromocion(5)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> promocionService.delete(5, admin));
        verify(promocionRepository, never()).delete(any(Promocion.class));
    }

    @Test
    @DisplayName("Eliminar una promoción sin uso la borra y audita")
    void delete_ok() {
        Promocion p = existente();
        when(promocionRepository.findById(5)).thenReturn(Optional.of(p));
        when(facturaRepository.existsByIdPromocion(5)).thenReturn(false);

        promocionService.delete(5, admin);

        verify(promocionRepository).delete(p);
        verify(auditoriaService).registrar(any(User.class), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("findVigentes delega en el repositorio con la fecha de hoy")
    void findVigentes() {
        when(promocionRepository.findVigentes(LocalDate.now())).thenReturn(List.of(existente()));

        List<PromocionResponseDto> result = promocionService.findVigentes();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isVigente());
    }
}
