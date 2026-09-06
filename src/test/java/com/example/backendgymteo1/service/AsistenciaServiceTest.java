package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.asistencia.RegistrarAsistenciaDto;
import com.example.backendgymteo1.dto.asistencia.RegistroAsistenciaResponseDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.sucursal.SucursalResponseDto;
import com.example.backendgymteo1.dto.user.UserResponseDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Recepcionista;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.Sucursal;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.ResourceNotFoundException;
import com.example.backendgymteo1.repository.AsistenciaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.RecepcionistaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import com.example.backendgymteo1.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsistenciaServiceTest {

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private SocioRepository socioRepository;

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private RecepcionistaRepository recepcionistaRepository;

    @Mock
    private SocioService socioService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AsistenciaService asistenciaService;

    private User recepcionistaUser;
    private Recepcionista recepcionistaEntity;
    private User socioUser;
    private Socio socioEntity;
    private Sucursal sucursalCentral;
    private Sucursal sucursalNorte;
    private Membresia membresiaActiva;
    private SocioResponseDto socioResponseDto;

    @BeforeEach
    void setUp() {
        recepcionistaUser = User.builder()
                .id(2)
                .correo("recepcion@gym.com")
                .nombres("Carlos")
                .apellidos("López")
                .rol(Rol.recepcionista())
                .estado(true)
                .build();

        recepcionistaEntity = Recepcionista.builder()
                .id(2)
                .usuario(recepcionistaUser)
                .fechaContratacion(LocalDate.of(2024, 1, 15))
                .build();

        sucursalCentral = Sucursal.builder()
                .id(1)
                .nombre("Sucursal Central")
                .direccion("12 Calle 5-45 Zona 1")
                .activo(true)
                .build();

        sucursalNorte = Sucursal.builder()
                .id(2)
                .nombre("Sucursal Norte")
                .direccion("Avenida Las Américas 8-20 Zona 10")
                .activo(true)
                .build();

        socioUser = User.builder()
                .id(4)
                .correo("socio@gym.com")
                .nombres("Juan")
                .apellidos("Pérez")
                .rol(Rol.cliente())
                .estado(true)
                .build();

        socioEntity = Socio.builder()
                .id(4)
                .usuario(socioUser)
                .sucursal(sucursalCentral)
                .fechaRegistro(LocalDate.of(2024, 3, 1))
                .build();

        Set<Sucursal> sucursales = new HashSet<>();
        sucursales.add(sucursalCentral);

        membresiaActiva = Membresia.builder()
                .id(10)
                .socio(socioEntity)
                .plan(PlanMembresia.builder().id(1).nombre("Plan Mensual").duracion(30).build())
                .estadoMembresia(EstadoMembresia.builder().id(1).nombre("ACTIVA").build())
                .fechaInicio(LocalDate.now().minusDays(5))
                .fechaVencimiento(LocalDate.now().plusDays(25))
                .sucursales(sucursales)
                .build();

        socioResponseDto = SocioResponseDto.builder()
                .id(4)
                .estado(true)
                .fechaRegistro(LocalDate.of(2024, 3, 1))
                .usuario(UserResponseDto.builder()
                        .id(4)
                        .nombres("Juan")
                        .apellidos("Pérez")
                        .correo("socio@gym.com")
                        .build())
                .sucursal(SucursalResponseDto.builder()
                        .id(1)
                        .nombre("Sucursal Central")
                        .build())
                .build();
    }

    @Test
    @DisplayName("Registrar entrada exitosamente: valida socio, membresía, sucursal, registra hora y recepcionista, y retorna confirmación con socio")
    void testRegistrarEntrada_Success() {
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(1)
                .build();

        when(socioRepository.findById(4)).thenReturn(Optional.of(socioEntity));
        when(membresiaRepository.findActiveBySocioId(eq(4), any(LocalDate.class))).thenReturn(Optional.of(membresiaActiva));
        when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalCentral));
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));

        Asistencia asistenciaGuardada = Asistencia.builder()
                .id(100)
                .socio(socioEntity)
                .recepcionista(recepcionistaEntity)
                .sucursal(sucursalCentral)
                .fecha(LocalDate.now())
                .horaEntrada(LocalTime.of(8, 30))
                .build();
        when(asistenciaRepository.save(any(Asistencia.class))).thenReturn(asistenciaGuardada);
        when(socioService.findById(4)).thenReturn(socioResponseDto);

        RegistroAsistenciaResponseDto response = asistenciaService.registrarEntrada(request, recepcionistaUser);

        assertNotNull(response);
        assertEquals("Asistencia registrada exitosamente", response.getMensaje());
        assertEquals(100, response.getIdAsistencia());
        assertEquals(LocalDate.now(), response.getFecha());
        assertEquals(LocalTime.of(8, 30), response.getHoraEntrada());
        assertEquals(2, response.getRecepcionistaId());
        assertEquals("Carlos López", response.getRecepcionistaNombre());
        assertEquals(1, response.getSucursalId());
        assertEquals("Sucursal Central", response.getSucursalNombre());
        assertNotNull(response.getSocio());
        assertEquals(4, response.getSocio().getId());
        assertEquals("Juan", response.getSocio().getUsuario().getNombres());

        ArgumentCaptor<Asistencia> captor = ArgumentCaptor.forClass(Asistencia.class);
        verify(asistenciaRepository).save(captor.capture());
        Asistencia captured = captor.getValue();
        assertEquals(socioEntity, captured.getSocio());
        assertEquals(recepcionistaEntity, captured.getRecepcionista());
        assertEquals(sucursalCentral, captured.getSucursal());
        assertNotNull(captured.getHoraEntrada());
        assertNotNull(captured.getFecha());

        verify(auditoriaService).registrar(eq(recepcionistaUser), eq("asistencia"), eq("INSERT"), eq(100), anyString());
    }

    @Test
    @DisplayName("Registrar entrada cuando usuario autenticado no tiene perfil de recepcionista: crea perfil y registra asistencia")
    void testRegistrarEntrada_CreatesRecepcionistaProfileWhenMissing() {
        User adminUser = User.builder()
                .id(1)
                .correo("admin@gym.com")
                .nombres("Admin")
                .apellidos("Gym")
                .rol(Rol.admin())
                .estado(true)
                .build();

        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(1)
                .build();

        when(socioRepository.findById(4)).thenReturn(Optional.of(socioEntity));
        when(membresiaRepository.findActiveBySocioId(eq(4), any(LocalDate.class))).thenReturn(Optional.of(membresiaActiva));
        when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalCentral));
        when(recepcionistaRepository.findById(1)).thenReturn(Optional.empty());

        Recepcionista nuevoRecep = Recepcionista.builder()
                .id(1)
                .usuario(adminUser)
                .fechaContratacion(LocalDate.now())
                .build();
        when(recepcionistaRepository.save(any(Recepcionista.class))).thenReturn(nuevoRecep);

        Asistencia asistenciaGuardada = Asistencia.builder()
                .id(101)
                .socio(socioEntity)
                .recepcionista(nuevoRecep)
                .sucursal(sucursalCentral)
                .fecha(LocalDate.now())
                .horaEntrada(LocalTime.of(9, 0))
                .build();
        when(asistenciaRepository.save(any(Asistencia.class))).thenReturn(asistenciaGuardada);
        when(socioService.findById(4)).thenReturn(socioResponseDto);

        RegistroAsistenciaResponseDto response = asistenciaService.registrarEntrada(request, adminUser);

        assertNotNull(response);
        assertEquals(101, response.getIdAsistencia());
        assertEquals(1, response.getRecepcionistaId());
        assertEquals("Admin Gym", response.getRecepcionistaNombre());
        verify(recepcionistaRepository).save(any(Recepcionista.class));
    }

    @Test
    @DisplayName("Validar socio: debe lanzar ResourceNotFoundException (404) si el socio no existe")
    void testRegistrarEntrada_ThrowsWhenSocioNotFound() {
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(99)
                .sucursalId(1)
                .build();

        when(socioRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> asistenciaService.registrarEntrada(request, recepcionistaUser));
        assertTrue(ex.getMessage().contains("Socio no encontrado con ID: 99"));
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validar socio: debe lanzar RuntimeException si el usuario del socio está inactivo")
    void testRegistrarEntrada_ThrowsWhenSocioInactive() {
        socioUser.setEstado(false);
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(1)
                .build();

        when(socioRepository.findById(4)).thenReturn(Optional.of(socioEntity));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> asistenciaService.registrarEntrada(request, recepcionistaUser));
        assertTrue(ex.getMessage().contains("inactivo o dado de baja"));
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validar membresía: debe lanzar RuntimeException si no cuenta con membresía activa y vigente")
    void testRegistrarEntrada_ThrowsWhenNoActiveMembership() {
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(1)
                .build();

        when(socioRepository.findById(4)).thenReturn(Optional.of(socioEntity));
        when(membresiaRepository.findActiveBySocioId(eq(4), any(LocalDate.class))).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> asistenciaService.registrarEntrada(request, recepcionistaUser));
        assertTrue(ex.getMessage().contains("no cuenta con una membresía activa y vigente"));
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validar sucursal: debe lanzar ResourceNotFoundException si la sucursal no existe")
    void testRegistrarEntrada_ThrowsWhenSucursalNotFound() {
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(999)
                .build();

        when(socioRepository.findById(4)).thenReturn(Optional.of(socioEntity));
        when(membresiaRepository.findActiveBySocioId(eq(4), any(LocalDate.class))).thenReturn(Optional.of(membresiaActiva));
        when(sucursalRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> asistenciaService.registrarEntrada(request, recepcionistaUser));
        assertTrue(ex.getMessage().contains("Sucursal no encontrada con ID: 999"));
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validar sucursal: debe lanzar RuntimeException si la sucursal está inactiva")
    void testRegistrarEntrada_ThrowsWhenSucursalInactive() {
        sucursalCentral.setActivo(false);
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(1)
                .build();

        when(socioRepository.findById(4)).thenReturn(Optional.of(socioEntity));
        when(membresiaRepository.findActiveBySocioId(eq(4), any(LocalDate.class))).thenReturn(Optional.of(membresiaActiva));
        when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursalCentral));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> asistenciaService.registrarEntrada(request, recepcionistaUser));
        assertTrue(ex.getMessage().contains("no se encuentra activa"));
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validar sucursal: debe lanzar RuntimeException si la membresía activa no autoriza acceso a esa sucursal")
    void testRegistrarEntrada_ThrowsWhenMembershipDoesNotCoverSucursal() {
        // Socio's membership is only for sucursalCentral (id=1), but request is for sucursalNorte (id=2)
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(2)
                .build();

        when(socioRepository.findById(4)).thenReturn(Optional.of(socioEntity));
        when(membresiaRepository.findActiveBySocioId(eq(4), any(LocalDate.class))).thenReturn(Optional.of(membresiaActiva));
        when(sucursalRepository.findById(2)).thenReturn(Optional.of(sucursalNorte));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> asistenciaService.registrarEntrada(request, recepcionistaUser));
        assertTrue(ex.getMessage().contains("no autoriza el acceso a la sucursal 'Sucursal Norte'"));
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validar usuario autenticado: debe lanzar RuntimeException si el usuario es null")
    void testRegistrarEntrada_ThrowsWhenUserNull() {
        RegistrarAsistenciaDto request = RegistrarAsistenciaDto.builder()
                .idSocio(4)
                .sucursalId(1)
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> asistenciaService.registrarEntrada(request, null));
        assertTrue(ex.getMessage().contains("Se requiere un usuario autenticado"));
    }
}
