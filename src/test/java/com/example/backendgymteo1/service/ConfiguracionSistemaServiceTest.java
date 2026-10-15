package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.configuracion.RecargoTardioResponseDto;
import com.example.backendgymteo1.entity.ConfiguracionSistema;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.ConfiguracionSistemaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracionSistemaServiceTest {

    private static final String CLAVE = ConfiguracionSistema.RECARGO_TARDIO_PORCENTAJE;

    @Mock
    private ConfiguracionSistemaRepository repository;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ConfiguracionSistemaService service;

    private final User admin = User.builder().id(1).correo("admin@gym.com").rol(Rol.admin()).build();

    private ConfiguracionSistema fila(String valor) {
        return ConfiguracionSistema.builder().id(1).clave(CLAVE).valor(valor)
                .fechaActualizacion(LocalDateTime.of(2026, 10, 1, 8, 0)).build();
    }

    @Test
    @DisplayName("Lee el porcentaje guardado en base de datos")
    void obtener_ok() {
        when(repository.findByClave(CLAVE)).thenReturn(Optional.of(fila("15")));

        assertEquals(new BigDecimal("15.00"), service.obtenerRecargoTardioPorcentaje());
    }

    @Test
    @DisplayName("Sin fila de configuración se usa 10% por defecto")
    void obtener_sinFila() {
        when(repository.findByClave(CLAVE)).thenReturn(Optional.empty());

        assertEquals(new BigDecimal("10.00"), service.obtenerRecargoTardioPorcentaje());
    }

    @Test
    @DisplayName("Valor corrupto o fuera de rango en base de datos: se usa el valor por defecto")
    void obtener_valorInvalido() {
        when(repository.findByClave(CLAVE)).thenReturn(Optional.of(fila("abc")));
        assertEquals(new BigDecimal("10.00"), service.obtenerRecargoTardioPorcentaje());

        when(repository.findByClave(CLAVE)).thenReturn(Optional.of(fila("250")));
        assertEquals(new BigDecimal("10.00"), service.obtenerRecargoTardioPorcentaje());

        when(repository.findByClave(CLAVE)).thenReturn(Optional.of(fila("-5")));
        assertEquals(new BigDecimal("10.00"), service.obtenerRecargoTardioPorcentaje());
    }

    @Test
    @DisplayName("Consultar sin fila retorna el valor por defecto sin fecha de actualización")
    void consultar_sinFila() {
        when(repository.findByClave(CLAVE)).thenReturn(Optional.empty());

        RecargoTardioResponseDto r = service.consultarRecargoTardio();

        assertEquals(new BigDecimal("10.00"), r.getPorcentaje());
        assertNull(r.getFechaActualizacion());
    }

    @Test
    @DisplayName("Actualizar guarda el nuevo valor con 2 decimales, actualiza la fecha y audita")
    void actualizar_ok() {
        ConfiguracionSistema existente = fila("10.00");
        when(repository.findByClave(CLAVE)).thenReturn(Optional.of(existente));
        when(repository.save(any(ConfiguracionSistema.class))).thenAnswer(i -> i.getArgument(0));

        RecargoTardioResponseDto r = service.actualizarRecargoTardio(new BigDecimal("12.5"), admin);

        assertEquals(new BigDecimal("12.50"), r.getPorcentaje());
        assertNotNull(r.getFechaActualizacion());
        ArgumentCaptor<ConfiguracionSistema> captor = ArgumentCaptor.forClass(ConfiguracionSistema.class);
        verify(repository).save(captor.capture());
        assertEquals("12.50", captor.getValue().getValor());
        verify(auditoriaService).registrar(eq(admin), eq("configuracion_sistema"), eq("UPDATE"), anyInt(), anyString());
    }

    @Test
    @DisplayName("Actualizar cuando no existe la fila la crea")
    void actualizar_creaFila() {
        when(repository.findByClave(CLAVE)).thenReturn(Optional.empty());
        when(repository.save(any(ConfiguracionSistema.class))).thenAnswer(i -> {
            ConfiguracionSistema c = i.getArgument(0);
            c.setId(7);
            return c;
        });

        RecargoTardioResponseDto r = service.actualizarRecargoTardio(new BigDecimal("0"), admin);

        assertEquals(new BigDecimal("0.00"), r.getPorcentaje());
    }

    @Test
    @DisplayName("Porcentajes fuera de 0-100 son rechazados y no se persisten")
    void actualizar_fueraDeRango() {
        assertThrows(RuntimeException.class, () -> service.actualizarRecargoTardio(new BigDecimal("100.01"), admin));
        assertThrows(RuntimeException.class, () -> service.actualizarRecargoTardio(new BigDecimal("-1"), admin));
        assertThrows(RuntimeException.class, () -> service.actualizarRecargoTardio(null, admin));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Los extremos 0 y 100 son válidos")
    void actualizar_extremos() {
        when(repository.findByClave(CLAVE)).thenReturn(Optional.of(fila("10")));
        when(repository.save(any(ConfiguracionSistema.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(new BigDecimal("100.00"), service.actualizarRecargoTardio(new BigDecimal("100"), admin).getPorcentaje());
        assertEquals(new BigDecimal("0.00"), service.actualizarRecargoTardio(BigDecimal.ZERO, admin).getPorcentaje());
    }
}
