package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.socio.CreateSocioDto;
import com.example.backendgymteo1.dto.socio.MembresiaResumenDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.socio.UltimaAsistenciaDto;
import com.example.backendgymteo1.dto.socio.UpdateSocioDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Recepcionista;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.mapper.MembresiaMapper;
import com.example.backendgymteo1.mapper.SocioMapper;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import com.example.backendgymteo1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocioServiceTest {

    @Mock
    private SocioRepository socioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private SocioMapper socioMapper;

    @Mock
    private MembresiaMapper membresiaMapper;

    @Mock
    private PasswordGeneratorService passwordGeneratorService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private SocioService socioService;

    private User adminUser;
    private User socioUser;
    private Socio socioEntity;
    private Sucursal sucursalCentral;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1)
                .correo("admin@gym.com")
                .nombres("Admin")
                .apellidos("Principal")
                .rol(Rol.admin())
                .build();

        sucursalCentral = Sucursal.builder()
                .id(1)
                .nombre("Sucursal Central")
                .direccion("12 Calle 5-45 Zona 1")
                .activo(true)
                .build();

        socioUser = User.builder()
                .id(10)
                .dpi("1000000000010")
                .nombres("Carlos")
                .apellidos("López")
                .correo("carlos@gymdemo.com")
                .telefono("55551234")
                .direccion("Avenida Las Américas")
                .fechaNacimiento(LocalDate.of(1995, 5, 10))
                .rol(Rol.cliente())
                .estado(true)
                .build();

        socioEntity = Socio.builder()
                .id(10)
                .usuario(socioUser)
                .sucursal(sucursalCentral)
                .fechaRegistro(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Crear socio exitosamente: autogenera contraseña, asigna sucursal y registra auditoría")
    void testCreateSocio_Success() {
        CreateSocioDto request = CreateSocioDto.builder()
                .dpi("1000000000010")
                .nombres("Carlos")
                .apellidos("López")
                .correo("carlos@gymdemo.com")
                .telefono("55551234")
                .direccion("Avenida Las Américas")
                .fechaNacimiento(LocalDate.of(1995, 5, 10))
                .sucursalId(1)
                .fechaRegistro(LocalDate.now())
                .build();

        when(userRepository.existsByCorreo("carlos@gymdemo.com")).thenReturn(false);
        when(userRepository.existsByDpi("1000000000010")).thenReturn(false);
        when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalCentral));
        when(passwordGeneratorService.generarContraseniaAleatoria()).thenReturn("AutoPass123*");
        when(passwordEncoder.encode("AutoPass123*")).thenReturn("encodedPassword");
        when(socioMapper.toUserEntity(eq(request), eq("encodedPassword"))).thenReturn(socioUser);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(socioUser);
        when(socioMapper.toSocioEntity(eq(socioUser), eq(sucursalCentral), any())).thenReturn(socioEntity);
        when(socioRepository.save(any(Socio.class))).thenReturn(socioEntity);

        SocioResponseDto expectedDto = SocioResponseDto.builder()
                .id(10)
                .estado(true)
                .build();
        when(socioMapper.toDto(eq(socioEntity), eq("AutoPass123*"))).thenReturn(expectedDto);

        SocioResponseDto result = socioService.create(request, adminUser);

        assertNotNull(result);
        assertEquals(10, result.getId());
        verify(emailService).enviarCredenciales(eq("carlos@gymdemo.com"), anyString(), eq("AutoPass123*"), eq("CLIENTE"));
        verify(auditoriaService).registrar(eq(adminUser), eq("socio"), eq("INSERT"), eq(10), anyString());
    }

    @Test
    @DisplayName("Crear socio con correo duplicado debe lanzar excepción")
    void testCreateSocio_DuplicateEmail_ThrowsException() {
        CreateSocioDto request = CreateSocioDto.builder()
                .dpi("1000000000010")
                .correo("existente@gymdemo.com")
                .build();

        when(userRepository.existsByCorreo("existente@gymdemo.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> socioService.create(request, adminUser));
        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(socioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Buscar socio por ID debe retornar perfil completo con membresía actual y última asistencia")
    void testFindById_ReturnsFullProfile() {
        when(socioRepository.findByIdAndActiveWithUser(10)).thenReturn(Optional.of(socioEntity));

        Membresia membresiaMock = Membresia.builder()
                .id(5)
                .plan(PlanMembresia.builder().nombre("Plan Anual").build())
                .fechaInicio(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusMonths(12))
                .build();
        when(membresiaRepository.findActiveBySocioId(eq(10), any(LocalDate.class))).thenReturn(Optional.of(membresiaMock));

        MembresiaResumenDto membresiaResumen = MembresiaResumenDto.builder()
                .idMembresia(5)
                .plan("Plan Anual")
                .activa(true)
                .build();
        when(membresiaMapper.toResumenDto(membresiaMock)).thenReturn(membresiaResumen);

        Asistencia asistenciaMock = Asistencia.builder()
                .id(88)
                .fecha(LocalDate.now().minusDays(1))
                .horaEntrada(LocalTime.of(8, 0))
                .horaSalida(LocalTime.of(9, 30))
                .recepcionista(Recepcionista.builder().usuario(adminUser).build())
                .build();
        when(asistenciaRepository.findUltimaAsistenciaBySocioId(10)).thenReturn(Optional.of(asistenciaMock));

        SocioResponseDto fullDto = SocioResponseDto.builder()
                .id(10)
                .membresiaActual(membresiaResumen)
                .ultimaAsistencia(UltimaAsistenciaDto.builder().idAsistencia(88).build())
                .build();
        when(socioMapper.toDto(eq(socioEntity), eq(null), eq(membresiaResumen), any())).thenReturn(fullDto);

        SocioResponseDto result = socioService.findById(10);

        assertNotNull(result);
        assertNotNull(result.getMembresiaActual());
        assertNotNull(result.getUltimaAsistencia());
    }

    @Test
    @DisplayName("Buscar socio inexistente debe lanzar ResourceNotFoundException (404)")
    void testFindById_NotFound_Throws404() {
        when(socioRepository.findByIdAndActiveWithUser(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> socioService.findById(999));
    }

    @Test
    @DisplayName("Actualizar socio debe modificar datos permitidos, validar email y registrar auditoría detallada")
    void testUpdateSocio_AuditLogsChanges() {
        when(socioRepository.findByIdAndActiveWithUser(10)).thenReturn(Optional.of(socioEntity));
        when(userRepository.existsByCorreo("carlos.nuevo@gymdemo.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(socioUser);
        when(socioRepository.save(any(Socio.class))).thenReturn(socioEntity);

        UpdateSocioDto updateRequest = UpdateSocioDto.builder()
                .nombres("Carlos Modificado")
                .correo("carlos.nuevo@gymdemo.com")
                .telefono("55559999")
                .direccion("Nueva Direccion 45")
                .fechaNacimiento(LocalDate.of(1995, 5, 12))
                .build();

        SocioResponseDto updatedDto = SocioResponseDto.builder().id(10).build();
        when(socioMapper.toDto(eq(socioEntity), eq(null), any(), any())).thenReturn(updatedDto);

        SocioResponseDto result = socioService.update(10, updateRequest, adminUser);

        assertNotNull(result);
        verify(userRepository).save(socioUser);
        verify(socioRepository).save(socioEntity);
        verify(auditoriaService).registrar(eq(adminUser), eq("socio"), eq("UPDATE"), eq(10), anyString());
    }
}
