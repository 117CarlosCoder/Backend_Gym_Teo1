package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.configuracion.RecargoTardioResponseDto;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.exception.GlobalExceptionHandler;
import com.example.backendgymteo1.service.ConfiguracionSistemaService;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConfiguracionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConfiguracionSistemaService service;

    @InjectMocks
    private ConfiguracionController controller;

    private final User admin = User.builder().id(1).correo("admin@gym.com").rol(Rol.admin()).build();

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver currentUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(CurrentUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return admin;
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(currentUserResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /configuracion/recargo-tardio retorna el porcentaje vigente")
    void consultar() throws Exception {
        when(service.consultarRecargoTardio())
                .thenReturn(RecargoTardioResponseDto.builder().porcentaje(new BigDecimal("10.00")).build());

        mockMvc.perform(get("/configuracion/recargo-tardio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentaje").value(10.0));
    }

    @Test
    @DisplayName("PUT /configuracion/recargo-tardio actualiza y retorna el nuevo porcentaje")
    void actualizar() throws Exception {
        when(service.actualizarRecargoTardio(eq(new BigDecimal("15.5")), any(User.class)))
                .thenReturn(RecargoTardioResponseDto.builder().porcentaje(new BigDecimal("15.50")).build());

        mockMvc.perform(put("/configuracion/recargo-tardio")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"porcentaje\": 15.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentaje").value(15.5));
    }

    @Test
    @DisplayName("PUT con porcentaje > 100, negativo o ausente: 400 sin llegar al servicio")
    void actualizar_invalido() throws Exception {
        for (String body : new String[]{"{\"porcentaje\": 101}", "{\"porcentaje\": -1}", "{}"}) {
            mockMvc.perform(put("/configuracion/recargo-tardio")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verify(service, never()).actualizarRecargoTardio(any(), any());
    }
}
