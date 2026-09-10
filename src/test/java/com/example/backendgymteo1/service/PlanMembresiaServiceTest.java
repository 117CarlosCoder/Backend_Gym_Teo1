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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanMembresiaServiceTest {

    @Mock
    private PlanMembresiaRepository planMembresiaRepository;

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private MembresiaMapper membresiaMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PlanMembresiaService planMembresiaService;

    private PlanMembresia samplePlan;
    private PlanMembresiaResponseDto sampleResponseDto;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1)
                .correo("admin@gym.com")
                .nombres("Admin")
                .apellidos("Principal")
                .build();

        samplePlan = PlanMembresia.builder()
                .id(1)
                .nombre("Plan Mensual")
                .duracion(30)
                .precio(new BigDecimal("250.00"))
                .descripcion("Acceso mensual")
                .build();

        sampleResponseDto = PlanMembresiaResponseDto.builder()
                .id(1)
                .nombre("Plan Mensual")
                .duracion(30)
                .precio(new BigDecimal("250.00"))
                .descripcion("Acceso mensual")
                .build();
    }

    @Test
    @DisplayName("findAll - Retorna lista de planes")
    void testFindAll() {
        when(planMembresiaRepository.findAll()).thenReturn(List.of(samplePlan));
        when(membresiaMapper.toPlanDto(samplePlan)).thenReturn(sampleResponseDto);

        List<PlanMembresiaResponseDto> result = planMembresiaService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Plan Mensual", result.get(0).getNombre());
    }

    @Test
    @DisplayName("findById - Retorna plan cuando existe")
    void testFindById_Success() {
        when(planMembresiaRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(membresiaMapper.toPlanDto(samplePlan)).thenReturn(sampleResponseDto);

        PlanMembresiaResponseDto result = planMembresiaService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Plan Mensual", result.getNombre());
    }

    @Test
    @DisplayName("findById - Lanza ResourceNotFoundException cuando no existe")
    void testFindById_NotFound() {
        when(planMembresiaRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> planMembresiaService.findById(999));
    }

    @Test
    @DisplayName("create - Crea plan exitosamente y registra auditoría")
    void testCreate_Success() {
        CreatePlanMembresiaDto request = CreatePlanMembresiaDto.builder()
                .nombre("Plan Anual Pro")
                .duracion(365)
                .precio(new BigDecimal("2200.00"))
                .descripcion("Acceso anual completo")
                .build();

        when(planMembresiaRepository.existsByNombreIgnoreCase("Plan Anual Pro")).thenReturn(false);
        when(membresiaMapper.toPlanEntity(request)).thenReturn(samplePlan);
        when(planMembresiaRepository.save(samplePlan)).thenReturn(samplePlan);
        when(membresiaMapper.toPlanDto(samplePlan)).thenReturn(sampleResponseDto);

        PlanMembresiaResponseDto result = planMembresiaService.create(request, adminUser);

        assertNotNull(result);
        verify(planMembresiaRepository).save(samplePlan);
        verify(auditoriaService).registrar(any(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("create - Lanza excepción si el nombre ya existe")
    void testCreate_DuplicateName() {
        CreatePlanMembresiaDto request = CreatePlanMembresiaDto.builder()
                .nombre("Plan Mensual")
                .duracion(30)
                .precio(new BigDecimal("250.00"))
                .build();

        when(planMembresiaRepository.existsByNombreIgnoreCase("Plan Mensual")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> planMembresiaService.create(request, adminUser));
        verify(planMembresiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - Actualiza plan exitosamente")
    void testUpdate_Success() {
        UpdatePlanMembresiaDto request = UpdatePlanMembresiaDto.builder()
                .nombre("Plan Mensual VIP")
                .precio(new BigDecimal("280.00"))
                .build();

        when(planMembresiaRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(planMembresiaRepository.existsByNombreIgnoreCase("Plan Mensual VIP")).thenReturn(false);
        when(planMembresiaRepository.save(samplePlan)).thenReturn(samplePlan);
        when(membresiaMapper.toPlanDto(samplePlan)).thenReturn(sampleResponseDto);

        PlanMembresiaResponseDto result = planMembresiaService.update(1, request, adminUser);

        assertNotNull(result);
        verify(membresiaMapper).updatePlanEntity(samplePlan, request);
        verify(planMembresiaRepository).save(samplePlan);
        verify(auditoriaService).registrar(any(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("delete - Elimina plan si no tiene membresías asociadas")
    void testDelete_Success() {
        when(planMembresiaRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(membresiaRepository.existsByPlanId(1)).thenReturn(false);

        planMembresiaService.delete(1, adminUser);

        verify(planMembresiaRepository).delete(samplePlan);
        verify(auditoriaService).registrar(any(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("delete - Bloquea eliminación si existen membresías asociadas")
    void testDelete_ReferentialIntegrityConflict() {
        when(planMembresiaRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(membresiaRepository.existsByPlanId(1)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> planMembresiaService.delete(1, adminUser));
        assertNotNull(ex.getMessage());
        verify(planMembresiaRepository, never()).delete(any());
    }
}
