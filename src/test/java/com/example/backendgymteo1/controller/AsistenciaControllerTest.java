package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.asistencia.RegistrarAsistenciaDto;
import com.example.backendgymteo1.dto.asistencia.RegistroAsistenciaResponseDto;
import com.example.backendgymteo1.dto.socio.SocioResponseDto;
import com.example.backendgymteo1.dto.user.UserResponseDto;
import com.example.backendgymteo1.entity.Asistencia;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.AsistenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AsistenciaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AsistenciaService asistenciaService;

    @InjectMocks
    private AsistenciaController asistenciaController;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(2)
                .correo("recepcionista@gym.com")
                .nombres("Carlos")
                .apellidos("López")
                .rol(Rol.recepcionista())
                .estado(true)
                .build();

        HandlerMethodArgumentResolver currentUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(CurrentUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return currentUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(asistenciaController)
                .setCustomArgumentResolvers(currentUserResolver)
                .build();
    }

    @Test
    @DisplayName("POST /asistencias con snake_case registra entrada exitosamente y retorna 201")
    void testRegistrarEntrada_Success() throws Exception {
        RegistroAsistenciaResponseDto responseDto = RegistroAsistenciaResponseDto.builder()
                .mensaje("Asistencia registrada exitosamente")
                .idAsistencia(15)
                .fecha(LocalDate.of(2026, 9, 5))
                .horaEntrada(LocalTime.of(8, 0, 0))
                .recepcionistaId(2)
                .recepcionistaNombre("Carlos López")
                .sucursalId(1)
                .sucursalNombre("Sucursal Central")
                .socio(SocioResponseDto.builder()
                        .id(4)
                        .usuario(UserResponseDto.builder()
                                .nombres("Juan")
                                .apellidos("Pérez")
                                .build())
                        .build())
                .build();

        when(asistenciaService.registrarEntrada(any(RegistrarAsistenciaDto.class), eq(currentUser)))
                .thenReturn(responseDto);

        String jsonBody = "{\"id_socio\": 4, \"sucursal_id\": 1}";

        mockMvc.perform(post("/asistencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Asistencia registrada exitosamente"))
                .andExpect(jsonPath("$.idAsistencia").value(15))
                .andExpect(jsonPath("$.recepcionistaId").value(2))
                .andExpect(jsonPath("$.sucursalId").value(1))
                .andExpect(jsonPath("$.socio.id").value(4))
                .andExpect(jsonPath("$.socio.usuario.nombres").value("Juan"));
    }

    @Test
    @DisplayName("POST /asistencias/entrada con camelCase registra entrada exitosamente y retorna 201")
    void testRegistrarEntrada_AliasEndpoint_Success() throws Exception {
        RegistroAsistenciaResponseDto responseDto = RegistroAsistenciaResponseDto.builder()
                .mensaje("Asistencia registrada exitosamente")
                .idAsistencia(16)
                .fecha(LocalDate.of(2026, 9, 5))
                .horaEntrada(LocalTime.of(9, 30, 0))
                .recepcionistaId(2)
                .recepcionistaNombre("Carlos López")
                .sucursalId(1)
                .sucursalNombre("Sucursal Central")
                .socio(SocioResponseDto.builder().id(5).build())
                .build();

        when(asistenciaService.registrarEntrada(any(RegistrarAsistenciaDto.class), eq(currentUser)))
                .thenReturn(responseDto);

        String jsonBody = "{\"idSocio\": 5, \"sucursalId\": 1}";

        mockMvc.perform(post("/asistencias/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAsistencia").value(16))
                .andExpect(jsonPath("$.socio.id").value(5));
    }

    @Test
    @DisplayName("POST /asistencias con datos vacíos o nulos retorna 400 Bad Request")
    void testRegistrarEntrada_ValidationFailure() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/asistencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /asistencias/{id} retorna 200 OK")
    void testFindById() throws Exception {
        Asistencia asistencia = Asistencia.builder()
                .id(10)
                .fecha(LocalDate.of(2026, 9, 5))
                .horaEntrada(LocalTime.of(10, 0))
                .build();

        when(asistenciaService.findById(10)).thenReturn(asistencia);

        mockMvc.perform(get("/asistencias/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET /asistencias/socio/{socioId} retorna 200 OK")
    void testFindBySocioId() throws Exception {
        Asistencia a1 = Asistencia.builder().id(1).build();
        Asistencia a2 = Asistencia.builder().id(2).build();

        when(asistenciaService.findBySocioId(4)).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/asistencias/socio/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
