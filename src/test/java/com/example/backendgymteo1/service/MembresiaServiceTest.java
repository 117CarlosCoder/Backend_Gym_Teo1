package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.membresia.CancelarMembresiaRequestDto;
import com.example.backendgymteo1.dto.membresia.CreateMembresiaDto;
import com.example.backendgymteo1.dto.membresia.MembresiaResponseDto;
import com.example.backendgymteo1.dto.membresia.UpdateMembresiaDto;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Rol;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembresiaServiceTest {

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private SocioRepository socioRepository;

    @Mock
    private PlanMembresiaRepository planMembresiaRepository;

    @Mock
    private EstadoMembresiaRepository estadoMembresiaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private MembresiaMapper membresiaMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MembresiaService membresiaService;

    private User adminUser;
    private Socio socioEntity;
    private PlanMembresia planMensual;
    private PlanMembresia planTrimestral;
    private EstadoMembresia estadoActiva;
    private EstadoMembresia estadoCancelada;
    private Sucursal sucursalCentral;
    private Sucursal sucursalNorte;
    private Membresia membresiaEntity;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1)
                .correo("admin@gym.com")
                .nombres("Carlos")
                .apellidos("Admin")
                .rol(Rol.admin())
                .build();

        User socioUser = User.builder()
                .id(4)
                .correo("socio@gym.com")
                .nombres("Pedro")
                .apellidos("Picapiedra")
                .rol(Rol.cliente())
                .estado(true)
                .build();

        sucursalCentral = Sucursal.builder()
                .id(1)
                .nombre("Sucursal Central")
                .activo(true)
                .build();

        sucursalNorte = Sucursal.builder()
                .id(2)
                .nombre("Sucursal Norte")
                .activo(true)
                .build();

        socioEntity = Socio.builder()
                .id(4)
                .usuario(socioUser)
                .sucursal(sucursalCentral)
                .build();

        planMensual = PlanMembresia.builder()
                .id(1)
                .nombre("Plan Mensual")
                .duracion(30)
                .precio(new BigDecimal("250.00"))
                .build();

        planTrimestral = PlanMembresia.builder()
                .id(2)
                .nombre("Plan Trimestral")
                .duracion(90)
                .precio(new BigDecimal("650.00"))
                .build();

        estadoActiva = EstadoMembresia.builder()
                .id(1)
                .nombre("ACTIVA")
                .build();

        estadoCancelada = EstadoMembresia.builder()
                .id(4)
                .nombre("CANCELADA")
                .build();

        membresiaEntity = Membresia.builder()
                .id(50)
                .socio(socioEntity)
                .plan(planMensual)
                .estadoMembresia(estadoActiva)
                .fechaInicio(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .sucursales(new HashSet<>(List.of(sucursalCentral)))
                .build();
    }

    @Test
    @DisplayName("Crear membresía con multisucursal calcula vencimiento y registra auditoría")
    void testCreateMembresia_MultiSucursal() {
        CreateMembresiaDto request = CreateMembresiaDto.builder()
                .idSocio(4)
                .idPlan(1)
                .sucursalIds(List.of(1, 2))
                .fechaInicio(LocalDate.now())
                .build();

        when(socioRepository.findByIdAndActiveWithUser(4)).thenReturn(Optional.of(socioEntity));
        when(planMembresiaRepository.findById(1)).thenReturn(Optional.of(planMensual));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));
        when(membresiaRepository.existsActiveBySocio(eq(4), eq(1), any())).thenReturn(false);
        when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalCentral));
        when(sucursalRepository.findById(2)).thenReturn(Optional.of(sucursalNorte));
        when(membresiaRepository.save(any(Membresia.class))).thenReturn(membresiaEntity);

        MembresiaResponseDto expectedDto = MembresiaResponseDto.builder()
                .id(50)
                .activa(true)
                .build();
        when(membresiaMapper.toDto(any(Membresia.class))).thenReturn(expectedDto);

        MembresiaResponseDto result = membresiaService.create(request, adminUser);

        assertNotNull(result);
        assertEquals(50, result.getId());
        verify(membresiaRepository).save(any(Membresia.class));
        verify(auditoriaService).registrar(eq(adminUser), eq("membresia"), eq("INSERT"), eq(50), anyString());
    }

    @Test
    @DisplayName("Editar membresía actualiza plan, sucursales y fecha, y registra auditoría")
    void testUpdateMembresia_SuccessAndAudit() {
        when(membresiaRepository.findByIdWithDetails(50)).thenReturn(Optional.of(membresiaEntity));
        when(planMembresiaRepository.findById(2)).thenReturn(Optional.of(planTrimestral));
        when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalCentral));
        when(sucursalRepository.findById(2)).thenReturn(Optional.of(sucursalNorte));
        when(membresiaRepository.save(any(Membresia.class))).thenReturn(membresiaEntity);

        UpdateMembresiaDto updateRequest = UpdateMembresiaDto.builder()
                .tipoMembresiaId(2)
                .sucursalIds(List.of(1, 2))
                .fechaVencimiento(LocalDate.now().plusDays(90))
                .build();

        MembresiaResponseDto updatedDto = MembresiaResponseDto.builder().id(50).build();
        when(membresiaMapper.toDto(any(Membresia.class))).thenReturn(updatedDto);

        MembresiaResponseDto result = membresiaService.update(50, updateRequest, adminUser);

        assertNotNull(result);
        verify(membresiaRepository).save(membresiaEntity);
        verify(auditoriaService).registrar(eq(adminUser), eq("membresia"), eq("UPDATE"), eq(50), anyString());
    }

    @Test
    @DisplayName("Editar membresía con fecha vencimiento anterior a inicio debe lanzar error")
    void testUpdateMembresia_InvalidExpirationDate_ThrowsException() {
        when(membresiaRepository.findByIdWithDetails(50)).thenReturn(Optional.of(membresiaEntity));

        UpdateMembresiaDto updateRequest = UpdateMembresiaDto.builder()
                .fechaVencimiento(LocalDate.now().minusDays(10))
                .build();

        assertThrows(RuntimeException.class, () -> membresiaService.update(50, updateRequest, adminUser));
    }

    @Test
    @DisplayName("Cancelar membresía con reembolso cambia estado, registra auditoría y envía correo")
    void testCancelarMembresia_WithRefundAndEmail() {
        when(membresiaRepository.findByIdWithDetails(50)).thenReturn(Optional.of(membresiaEntity));
        when(estadoMembresiaRepository.findById(4)).thenReturn(Optional.of(estadoCancelada));
        when(membresiaRepository.save(any(Membresia.class))).thenReturn(membresiaEntity);

        CancelarMembresiaRequestDto cancelRequest = CancelarMembresiaRequestDto.builder()
                .motivo("Reubicación laboral")
                .comentarios("Solicitó devolución proporcional")
                .reembolso(true)
                .montoReembolso(new BigDecimal("125.00"))
                .build();

        MembresiaResponseDto cancelDto = MembresiaResponseDto.builder()
                .id(50)
                .activa(false)
                .reembolso(true)
                .build();
        when(membresiaMapper.toDto(any(Membresia.class))).thenReturn(cancelDto);

        MembresiaResponseDto result = membresiaService.cancelar(50, cancelRequest, adminUser);

        assertNotNull(result);
        verify(membresiaRepository).save(membresiaEntity);
        verify(auditoriaService).registrar(eq(adminUser), eq("membresia"), eq("CANCEL"), eq(50), anyString());
        verify(emailService).enviarNotificacionCancelacionMembresia(
                eq("socio@gym.com"),
                anyString(),
                eq("Plan Mensual"),
                eq("Reubicación laboral"),
                eq(true),
                eq(new BigDecimal("125.00"))
        );
    }
}
