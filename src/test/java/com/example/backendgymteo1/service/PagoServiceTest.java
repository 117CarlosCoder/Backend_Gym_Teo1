package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.pago.CreatePagoDto;
import com.example.backendgymteo1.dto.pago.PagoResponseDto;
import com.example.backendgymteo1.entity.ComprobantePago;
import com.example.backendgymteo1.entity.EstadoFactura;
import com.example.backendgymteo1.entity.EstadoMembresia;
import com.example.backendgymteo1.entity.Factura;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.MetodoPago;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Recepcionista;
import com.example.backendgymteo1.entity.Rol;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.mapper.PagoMapper;
import com.example.backendgymteo1.repository.ComprobantePagoRepository;
import com.example.backendgymteo1.repository.EstadoFacturaRepository;
import com.example.backendgymteo1.repository.EstadoMembresiaRepository;
import com.example.backendgymteo1.repository.FacturaRepository;
import com.example.backendgymteo1.repository.MembresiaRepository;
import com.example.backendgymteo1.repository.MetodoPagoRepository;
import com.example.backendgymteo1.repository.RecepcionistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private ComprobantePagoRepository comprobantePagoRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private EstadoFacturaRepository estadoFacturaRepository;

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private EstadoMembresiaRepository estadoMembresiaRepository;

    @Mock
    private RecepcionistaRepository recepcionistaRepository;

    @Mock
    private PagoMapper pagoMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PagoService pagoService;

    private User recepcionistaUser;
    private Recepcionista recepcionistaEntity;
    private Socio socioEntity;
    private MetodoPago metodoEfectivo;
    private EstadoFactura estadoPagada;
    private EstadoMembresia estadoActiva;

    @BeforeEach
    void setUp() {
        recepcionistaUser = User.builder()
                .id(2)
                .correo("recepcion@gym.com")
                .nombres("Ana")
                .apellidos("García")
                .rol(Rol.recepcionista())
                .build();

        recepcionistaEntity = Recepcionista.builder()
                .id(2)
                .usuario(recepcionistaUser)
                .fechaContratacion(LocalDate.now())
                .build();

        User socioUser = User.builder()
                .id(4)
                .correo("denilmonterroso22@gmail.com")
                .nombres("Denil")
                .apellidos("Monterroso")
                .dpi("1234567890101")
                .rol(Rol.cliente())
                .estado(true)
                .build();

        socioEntity = Socio.builder()
                .id(4)
                .usuario(socioUser)
                .build();

        metodoEfectivo = MetodoPago.builder()
                .id(1)
                .nombre("EFECTIVO")
                .descripcion("Pago en efectivo")
                .build();

        estadoPagada = EstadoFactura.builder()
                .id(2)
                .nombre("PAGADA")
                .build();

        estadoActiva = EstadoMembresia.builder()
                .id(1)
                .nombre("ACTIVA")
                .build();
    }

    @Test
    @DisplayName("Registrar pago para Plan Mensual (30 días) recalcula exactamente la fecha de vencimiento")
    void testRegistrarPago_PlanMensual_CalculaVencimientoExacto() {
        PlanMembresia planMensual = PlanMembresia.builder()
                .id(1)
                .nombre("Plan Mensual")
                .duracion(30)
                .precio(new BigDecimal("250.00"))
                .build();

        LocalDate fechaInicio = LocalDate.now();
        LocalDate vencimientoActual = fechaInicio.plusDays(10);

        Membresia membresiaPrevia = Membresia.builder()
                .id(10)
                .socio(socioEntity)
                .plan(planMensual)
                .estadoMembresia(estadoActiva)
                .fechaInicio(fechaInicio)
                .fechaVencimiento(vencimientoActual)
                .build();

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(10)
                .idMetodoPago(1)
                .monto(new BigDecimal("250.00"))
                .referencia("EFEC-100")
                .observacion("Pago mensual")
                .build();

        when(membresiaRepository.findByIdWithDetails(10)).thenReturn(Optional.of(membresiaPrevia));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> {
            Factura f = i.getArgument(0);
            f.setId(100);
            return f;
        });
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(i -> {
            ComprobantePago c = i.getArgument(0);
            c.setId(200);
            return c;
        });
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));

        LocalDate vencimientoEsperadoCalculado = vencimientoActual.plusDays(30);

        PagoResponseDto expectedDto = PagoResponseDto.builder()
                .idComprobante(200)
                .idFactura(100)
                .nuevaFechaVencimiento(vencimientoEsperadoCalculado)
                .build();
        when(pagoMapper.toPagoDto(any(ComprobantePago.class))).thenReturn(expectedDto);

        PagoResponseDto result = pagoService.registrarPago(request, recepcionistaUser);

        assertNotNull(result);
        assertEquals(200, result.getIdComprobante());
        assertEquals(vencimientoEsperadoCalculado, result.getNuevaFechaVencimiento());

        ArgumentCaptor<Membresia> membresiaCaptor = ArgumentCaptor.forClass(Membresia.class);
        verify(membresiaRepository).save(membresiaCaptor.capture());

        Membresia membresiaGuardada = membresiaCaptor.getValue();
        assertEquals(vencimientoEsperadoCalculado, membresiaGuardada.getFechaVencimiento());
        assertEquals(1, membresiaGuardada.getEstadoMembresia().getId());
    }

    @Test
    @DisplayName("Registrar pago para Plan Trimestral (90 días) en membresía vencida recalcula desde hoy")
    void testRegistrarPago_PlanTrimestral_MembresiaVencida() {
        PlanMembresia planTrimestral = PlanMembresia.builder()
                .id(2)
                .nombre("Plan Trimestral")
                .duracion(90)
                .precio(new BigDecimal("650.00"))
                .build();

        LocalDate vencimientoVencido = LocalDate.now().minusDays(5);

        Membresia membresiaVencida = Membresia.builder()
                .id(15)
                .socio(socioEntity)
                .plan(planTrimestral)
                .estadoMembresia(EstadoMembresia.builder().id(3).nombre("VENCIDA").build())
                .fechaInicio(LocalDate.now().minusDays(95))
                .fechaVencimiento(vencimientoVencido)
                .build();

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(15)
                .idMetodoPago(1)
                .monto(new BigDecimal("650.00"))
                .referencia("TRANS-TRIMESTRAL")
                .build();

        when(membresiaRepository.findByIdWithDetails(15)).thenReturn(Optional.of(membresiaVencida));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(i -> i.getArgument(0));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));

        LocalDate vencimientoEsperadoCalculado = LocalDate.now().plusDays(90);

        PagoResponseDto expectedDto = PagoResponseDto.builder()
                .idComprobante(201)
                .nuevaFechaVencimiento(vencimientoEsperadoCalculado)
                .build();
        when(pagoMapper.toPagoDto(any(ComprobantePago.class))).thenReturn(expectedDto);

        PagoResponseDto result = pagoService.registrarPago(request, recepcionistaUser);

        assertNotNull(result);
        ArgumentCaptor<Membresia> membresiaCaptor = ArgumentCaptor.forClass(Membresia.class);
        verify(membresiaRepository).save(membresiaCaptor.capture());

        Membresia membresiaGuardada = membresiaCaptor.getValue();
        assertEquals(vencimientoEsperadoCalculado, membresiaGuardada.getFechaVencimiento());
        assertEquals("ACTIVA", membresiaGuardada.getEstadoMembresia().getNombre());
    }

    @Test
    @DisplayName("Registrar pago para Plan Anual (365 días) recalcula fecha correctamente")
    void testRegistrarPago_PlanAnual() {
        PlanMembresia planAnual = PlanMembresia.builder()
                .id(3)
                .nombre("Plan Anual")
                .duracion(365)
                .precio(new BigDecimal("2200.00"))
                .build();

        LocalDate hoy = LocalDate.now();

        Membresia membresia = Membresia.builder()
                .id(20)
                .socio(socioEntity)
                .plan(planAnual)
                .estadoMembresia(estadoActiva)
                .fechaInicio(hoy)
                .fechaVencimiento(hoy)
                .build();

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(20)
                .idMetodoPago(1)
                .monto(new BigDecimal("2200.00"))
                .build();

        when(membresiaRepository.findByIdWithDetails(20)).thenReturn(Optional.of(membresia));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(i -> i.getArgument(0));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));

        LocalDate vencimientoEsperadoCalculado = hoy.plusDays(365);

        PagoResponseDto expectedDto = PagoResponseDto.builder()
                .nuevaFechaVencimiento(vencimientoEsperadoCalculado)
                .build();
        when(pagoMapper.toPagoDto(any(ComprobantePago.class))).thenReturn(expectedDto);

        pagoService.registrarPago(request, recepcionistaUser);

        ArgumentCaptor<Membresia> membresiaCaptor = ArgumentCaptor.forClass(Membresia.class);
        verify(membresiaRepository).save(membresiaCaptor.capture());

        Membresia membresiaGuardada = membresiaCaptor.getValue();
        assertEquals(vencimientoEsperadoCalculado, membresiaGuardada.getFechaVencimiento());
    }
}
