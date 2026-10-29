package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.configuracion.ReglasAbonoDto;
import com.example.backendgymteo1.dto.pago.CreateAbonoDto;
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
import com.example.backendgymteo1.repository.PromocionRepository;
import com.example.backendgymteo1.dto.pago.CotizacionPagoDto;
import com.example.backendgymteo1.entity.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
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

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private ConfiguracionSistemaService configuracionSistemaService;

    @Mock
    private AvisoPagoService avisoPagoService;

    @Mock
    private EstadoCuentaService estadoCuentaService;

    @Mock
    private CongelamientoService congelamientoService;

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
        org.mockito.Mockito.lenient().when(configuracionSistemaService.obtenerRecargoTardioPorcentaje())
                .thenReturn(new BigDecimal("10.00"));
        org.mockito.Mockito.lenient().when(configuracionSistemaService.consultarReglasAbono())
                .thenReturn(ReglasAbonoDto.builder().porcentajeMinimoInicial(50).plazoDias(15).build());
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
                .monto(new BigDecimal("715.00"))
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

    // ---------------------------------------------------------------------
    // Descuentos, recargo por pago tardío e historial
    // ---------------------------------------------------------------------

    private Membresia membresiaConPlan(int id, String precio, LocalDate vencimiento) {
        PlanMembresia plan = PlanMembresia.builder()
                .id(1).nombre("Plan Mensual").duracion(30).precio(new BigDecimal(precio)).build();
        return Membresia.builder()
                .id(id).socio(socioEntity).plan(plan).estadoMembresia(estadoActiva)
                .fechaInicio(LocalDate.now().minusDays(30)).fechaVencimiento(vencimiento).build();
    }

    private Promocion promocion(int id, Promocion.TipoDescuento tipo, String valor) {
        return Promocion.builder()
                .id(id).nombre("Promo " + id).tipoDescuento(tipo).valorDescuento(new BigDecimal(valor))
                .fechaInicio(LocalDate.now().minusDays(1)).fechaFin(LocalDate.now().plusDays(1)).activo(true).build();
    }

    @Test
    @DisplayName("Cotización sin promoción y membresía vigente: total igual al precio del plan")
    void testCotizacion_SinPromocionNiRecargo() {
        Membresia m = membresiaConPlan(1, "250.00", LocalDate.now().plusDays(3));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());

        CotizacionPagoDto c = pagoService.calcularCotizacion(m, LocalDate.now());

        assertEquals(new BigDecimal("250.00"), c.getMontoBase());
        assertEquals(new BigDecimal("0.00"), c.getDescuento());
        assertEquals(new BigDecimal("0.00"), c.getRecargo());
        assertEquals(new BigDecimal("250.00"), c.getTotal());
        assertFalse(c.isPagoTardio());
        assertNull(c.getIdPromocion());
    }

    @Test
    @DisplayName("Cotización con promoción porcentual aplica el descuento")
    void testCotizacion_PromocionPorcentaje() {
        Membresia m = membresiaConPlan(1, "250.00", LocalDate.now().plusDays(3));
        when(promocionRepository.findVigentes(any(LocalDate.class)))
                .thenReturn(List.of(promocion(7, Promocion.TipoDescuento.PORCENTAJE, "20")));

        CotizacionPagoDto c = pagoService.calcularCotizacion(m, LocalDate.now());

        assertEquals(new BigDecimal("50.00"), c.getDescuento());
        assertEquals(new BigDecimal("200.00"), c.getTotal());
        assertEquals(7, c.getIdPromocion());
    }

    @Test
    @DisplayName("Con varias promociones vigentes se aplica la que otorga mayor descuento (sin acumular)")
    void testCotizacion_ElijeMejorPromocion() {
        Membresia m = membresiaConPlan(1, "250.00", LocalDate.now().plusDays(3));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of(
                promocion(1, Promocion.TipoDescuento.PORCENTAJE, "10"),   // 25.00
                promocion(2, Promocion.TipoDescuento.MONTO_FIJO, "40"),   // 40.00
                promocion(3, Promocion.TipoDescuento.PORCENTAJE, "5")));  // 12.50

        CotizacionPagoDto c = pagoService.calcularCotizacion(m, LocalDate.now());

        assertEquals(2, c.getIdPromocion());
        assertEquals(new BigDecimal("40.00"), c.getDescuento());
        assertEquals(new BigDecimal("210.00"), c.getTotal());
    }

    @Test
    @DisplayName("Un descuento del 100% deja el total en el mínimo Q 0.01 (restricción de la base de datos)")
    void testCotizacion_DescuentoTotalNoDejaTotalEnCero() {
        Membresia m = membresiaConPlan(1, "250.00", LocalDate.now().plusDays(3));
        when(promocionRepository.findVigentes(any(LocalDate.class)))
                .thenReturn(List.of(promocion(1, Promocion.TipoDescuento.PORCENTAJE, "100")));

        CotizacionPagoDto c = pagoService.calcularCotizacion(m, LocalDate.now());

        assertEquals(new BigDecimal("0.01"), c.getTotal());
    }

    @Test
    @DisplayName("Membresía vencida: recargo del 10% sobre el precio del plan y días de atraso")
    void testCotizacion_RecargoPagoTardio() {
        Membresia m = membresiaConPlan(1, "250.00", LocalDate.now().minusDays(4));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());

        CotizacionPagoDto c = pagoService.calcularCotizacion(m, LocalDate.now());

        assertTrue(c.isPagoTardio());
        assertEquals(4, c.getDiasAtraso());
        assertEquals(new BigDecimal("25.00"), c.getRecargo());
        assertEquals(new BigDecimal("275.00"), c.getTotal());
    }

    @Test
    @DisplayName("Vencimiento exactamente hoy NO es pago tardío")
    void testCotizacion_VencimientoHoyNoEsTardio() {
        Membresia m = membresiaConPlan(1, "250.00", LocalDate.now());
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());

        CotizacionPagoDto c = pagoService.calcularCotizacion(m, LocalDate.now());

        assertFalse(c.isPagoTardio());
        assertEquals(new BigDecimal("0.00"), c.getRecargo());
    }

    @Test
    @DisplayName("Descuento y recargo combinados: base - descuento + recargo (recargo sobre el precio base)")
    void testCotizacion_DescuentoYRecargo() {
        Membresia m = membresiaConPlan(1, "250.00", LocalDate.now().minusDays(1));
        when(promocionRepository.findVigentes(any(LocalDate.class)))
                .thenReturn(List.of(promocion(1, Promocion.TipoDescuento.MONTO_FIJO, "50")));

        CotizacionPagoDto c = pagoService.calcularCotizacion(m, LocalDate.now());

        assertEquals(new BigDecimal("250.00"), c.getMontoBase());
        assertEquals(new BigDecimal("50.00"), c.getDescuento());
        assertEquals(new BigDecimal("25.00"), c.getRecargo());
        assertEquals(new BigDecimal("225.00"), c.getTotal());
    }

    @Test
    @DisplayName("Registrar pago con promoción y recargo guarda el desglose en la factura")
    void testRegistrarPago_GuardaDesgloseEnFactura() {
        Membresia m = membresiaConPlan(30, "250.00", LocalDate.now().minusDays(2));
        when(membresiaRepository.findByIdWithDetails(30)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(promocionRepository.findVigentes(any(LocalDate.class)))
                .thenReturn(List.of(promocion(9, Promocion.TipoDescuento.MONTO_FIJO, "50")));
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(i -> i.getArgument(0));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));
        when(pagoMapper.toPagoDto(any(ComprobantePago.class))).thenReturn(PagoResponseDto.builder().build());

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(30).idMetodoPago(1).monto(new BigDecimal("225.00")).build();

        pagoService.registrarPago(request, recepcionistaUser);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(captor.capture());
        Factura f = captor.getValue();
        assertEquals(new BigDecimal("250.00"), f.getMontoBase());
        assertEquals(new BigDecimal("50.00"), f.getDescuento());
        assertEquals(new BigDecimal("25.00"), f.getRecargo());
        assertEquals(new BigDecimal("225.00"), f.getMonto());
        assertEquals(9, f.getIdPromocion());
        verify(avisoPagoService).registrar(eq(f), any(CotizacionPagoDto.class));
    }

    @Test
    @DisplayName("Registrar pago acepta descuento directo enviado en el request y permite cobrar el monto con descuento")
    void testRegistrarPago_ConDescuentoDirecto_AceptaMontoConDescuento() {
        Membresia m = membresiaConPlan(33, "250.00", LocalDate.now().plusDays(5));
        when(membresiaRepository.findByIdWithDetails(33)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(i -> i.getArgument(0));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));
        when(pagoMapper.toPagoDto(any(ComprobantePago.class))).thenReturn(PagoResponseDto.builder().build());

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(33)
                .idMetodoPago(1)
                .descuento(new BigDecimal("50.00"))
                .monto(new BigDecimal("200.00"))
                .build();

        pagoService.registrarPago(request, recepcionistaUser);

        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(facturaCaptor.capture());
        Factura f = facturaCaptor.getValue();
        assertEquals(new BigDecimal("250.00"), f.getMontoBase());
        assertEquals(new BigDecimal("50.00"), f.getDescuento());
        assertEquals(new BigDecimal("200.00"), f.getMonto());
    }

    @Test
    @DisplayName("Registrar pago con promoción específica (idPromocion) aplica el descuento de esa promoción")
    void testRegistrarPago_ConIdPromocionEspecifica() {
        Membresia m = membresiaConPlan(34, "250.00", LocalDate.now().plusDays(5));
        Promocion promoSeleccionada = promocion(5, Promocion.TipoDescuento.PORCENTAJE, "10");
        when(membresiaRepository.findByIdWithDetails(34)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(promocionRepository.findById(5)).thenReturn(Optional.of(promoSeleccionada));
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(i -> i.getArgument(0));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));
        when(pagoMapper.toPagoDto(any(ComprobantePago.class))).thenReturn(PagoResponseDto.builder().build());

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(34)
                .idMetodoPago(1)
                .idPromocion(5)
                .monto(new BigDecimal("225.00"))
                .build();

        pagoService.registrarPago(request, recepcionistaUser);

        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(facturaCaptor.capture());
        Factura f = facturaCaptor.getValue();
        assertEquals(5, f.getIdPromocion());
        assertEquals(new BigDecimal("25.00"), f.getDescuento());
        assertEquals(new BigDecimal("225.00"), f.getMonto());
    }

    @Test
    @DisplayName("Registrar pago en membresía vencida con promoción activa acepta el monto con descuento del plan (base - descuento)")
    void testRegistrarPago_MembresiaVencidaConPromocion_AceptaMontoConDescuentoSinRecargo() {
        Membresia m = membresiaConPlan(35, "250.00", LocalDate.now().minusDays(3));
        when(membresiaRepository.findByIdWithDetails(35)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(promocionRepository.findVigentes(any(LocalDate.class)))
                .thenReturn(List.of(promocion(11, Promocion.TipoDescuento.PORCENTAJE, "10")));
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(i -> i.getArgument(0));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));
        when(pagoMapper.toPagoDto(any(ComprobantePago.class))).thenReturn(PagoResponseDto.builder().build());

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(35)
                .idMetodoPago(1)
                .monto(new BigDecimal("225.00"))
                .build();

        pagoService.registrarPago(request, recepcionistaUser);

        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(facturaCaptor.capture());
        Factura f = facturaCaptor.getValue();
        assertEquals(new BigDecimal("250.00"), f.getMontoBase());
        assertEquals(new BigDecimal("25.00"), f.getDescuento());
        assertEquals(new BigDecimal("0.00"), f.getRecargo());
        assertEquals(new BigDecimal("225.00"), f.getMonto());
    }

    @Test
    @DisplayName("Un primer abono menor al mínimo (50% del total con recargo) es rechazado y no persiste nada")
    void testRegistrarPago_MontoInsuficienteConRecargo() {
        Membresia m = membresiaConPlan(31, "250.00", LocalDate.now().minusDays(2));
        when(membresiaRepository.findByIdWithDetails(31)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(31).idMetodoPago(1).monto(new BigDecimal("100.00")).build();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoService.registrarPago(request, recepcionistaUser));

        assertTrue(ex.getMessage().contains("137.50"), ex.getMessage());
        assertTrue(ex.getMessage().contains("275.00"), ex.getMessage());
        verify(facturaRepository, never()).save(any(Factura.class));
        verify(membresiaRepository, never()).save(any(Membresia.class));
        org.mockito.Mockito.verifyNoInteractions(avisoPagoService);
    }

    @Test
    @DisplayName("La validación del total aplica también a pagos que no son en efectivo")
    void testRegistrarPago_MontoInsuficienteTarjeta() {
        Membresia m = membresiaConPlan(32, "250.00", LocalDate.now().plusDays(5));
        MetodoPago tarjeta = MetodoPago.builder().id(2).nombre("TARJETA").build();
        when(membresiaRepository.findByIdWithDetails(32)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(2)).thenReturn(Optional.of(tarjeta));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(32).idMetodoPago(2).monto(new BigDecimal("100.00")).build();

        assertThrows(RuntimeException.class, () -> pagoService.registrarPago(request, recepcionistaUser));
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("Historial: rango de fechas invertido es rechazado")
    void testHistorial_RangoInvalido() {
        assertThrows(RuntimeException.class, () -> pagoService.buscarHistorialPagos(
                null, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 1), null, PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("Historial: fechaHasta se convierte en límite exclusivo del día siguiente para incluir todo el día")
    void testHistorial_ConvierteRangoDeFechas() {
        when(comprobantePagoRepository.buscarHistorial(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PagoResponseDto> result = pagoService.buscarHistorialPagos(
                4, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 1, PageRequest.of(0, 10));

        assertNotNull(result);
        verify(comprobantePagoRepository).buscarHistorial(eq(4),
                eq(LocalDateTime.of(2026, 10, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 11, 1, 0, 0)),
                eq(1), any());
    }

    @Test
    @DisplayName("Historial sin filtros delega con parámetros nulos")
    void testHistorial_SinFiltros() {
        when(comprobantePagoRepository.buscarHistorial(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        pagoService.buscarHistorialPagos(null, null, null, null, PageRequest.of(0, 10));

        verify(comprobantePagoRepository).buscarHistorial(eq(null), eq(null), eq(null), eq(null), any());
    }

    // --- Ajuste del PO: pagos en cuotas / abonos ---

    @Test
    @DisplayName("Un pago menor al total pero sobre el mínimo es un primer abono: factura PENDIENTE con plazo y membresía extendida")
    void testRegistrarPago_PrimerAbonoDejaSaldoPendiente() {
        Membresia m = membresiaConPlan(40, "250.00", LocalDate.now().plusDays(5));
        EstadoFactura pendiente = EstadoFactura.builder().id(1).nombre("PENDIENTE").build();
        when(membresiaRepository.findByIdWithDetails(40)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(1)).thenReturn(Optional.of(pendiente));
        when(estadoMembresiaRepository.findById(1)).thenReturn(Optional.of(estadoActiva));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(40).idMetodoPago(1).monto(new BigDecimal("150.00")).build();

        pagoService.registrarPago(request, recepcionistaUser);

        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(facturaCaptor.capture());
        Factura f = facturaCaptor.getValue();
        assertEquals("PENDIENTE", f.getEstadoFactura().getNombre());
        assertEquals(new BigDecimal("250.00"), f.getMonto());
        assertEquals(LocalDate.now().plusDays(15), f.getFechaVencimiento());

        ArgumentCaptor<ComprobantePago> comprobanteCaptor = ArgumentCaptor.forClass(ComprobantePago.class);
        verify(comprobantePagoRepository).save(comprobanteCaptor.capture());
        assertEquals(new BigDecimal("150.00"), comprobanteCaptor.getValue().getMontoPagado());
        assertEquals(LocalDate.now().plusDays(35), m.getFechaVencimiento());
    }

    @Test
    @DisplayName("No se cobra un nuevo periodo mientras la membresía tenga saldo pendiente")
    void testRegistrarPago_BloqueadoConSaldoPendiente() {
        Membresia m = membresiaConPlan(41, "250.00", LocalDate.now().plusDays(5));
        when(membresiaRepository.findByIdWithDetails(41)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        org.mockito.Mockito.doThrow(new RuntimeException("La membresía tiene un saldo pendiente de Q. 100.00 en la factura #9."))
                .when(estadoCuentaService).validarSinSaldoPendiente(41);

        CreatePagoDto request = CreatePagoDto.builder()
                .idMembresia(41).idMetodoPago(1).monto(new BigDecimal("250.00")).build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pagoService.registrarPago(request, recepcionistaUser));
        assertTrue(ex.getMessage().contains("saldo pendiente"));
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("Un pago que deja la membresía congelada conserva el estado CONGELADA")
    void testRegistrarPago_MembresiaCongeladaSigueCongelada() {
        Membresia m = membresiaConPlan(42, "250.00", LocalDate.now().plusDays(20));
        EstadoMembresia congelada = EstadoMembresia.builder().id(3).nombre("CONGELADA").build();
        when(membresiaRepository.findByIdWithDetails(42)).thenReturn(Optional.of(m));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(promocionRepository.findVigentes(any(LocalDate.class))).thenReturn(List.of());
        when(recepcionistaRepository.findById(2)).thenReturn(Optional.of(recepcionistaEntity));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(congelamientoService.estaCongeladaEn(eq(42), any(LocalDate.class))).thenReturn(true);
        when(estadoMembresiaRepository.findById(3)).thenReturn(Optional.of(congelada));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.registrarPago(CreatePagoDto.builder()
                .idMembresia(42).idMetodoPago(1).monto(new BigDecimal("250.00")).build(), recepcionistaUser);

        assertEquals("CONGELADA", m.getEstadoMembresia().getNombre());
    }

    private Factura facturaPendiente(int id, String total) {
        return Factura.builder()
                .id(id)
                .membresia(membresiaConPlan(50, total, LocalDate.now().plusDays(20)))
                .estadoFactura(EstadoFactura.builder().id(1).nombre("PENDIENTE").build())
                .monto(new BigDecimal(total))
                .montoBase(new BigDecimal(total))
                .fechaEmision(LocalDate.now().minusDays(3))
                .fechaVencimiento(LocalDate.now().plusDays(12))
                .build();
    }

    @Test
    @DisplayName("El abono que cubre todo el saldo deja la factura PAGADA")
    void testRegistrarAbono_LiquidaFactura() {
        Factura factura = facturaPendiente(9, "250.00");
        when(facturaRepository.findByIdWithDetails(9)).thenReturn(Optional.of(factura));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(estadoCuentaService.saldoDe(factura)).thenReturn(new BigDecimal("100.00"));
        when(estadoFacturaRepository.findById(2)).thenReturn(Optional.of(estadoPagada));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.registrarAbono(CreateAbonoDto.builder()
                .idFactura(9).idMetodoPago(1).monto(new BigDecimal("100.00")).build(), recepcionistaUser);

        assertEquals("PAGADA", factura.getEstadoFactura().getNombre());
        verify(facturaRepository).save(factura);
    }

    @Test
    @DisplayName("Un abono parcial mantiene la factura PENDIENTE")
    void testRegistrarAbono_ParcialSiguePendiente() {
        Factura factura = facturaPendiente(10, "250.00");
        when(facturaRepository.findByIdWithDetails(10)).thenReturn(Optional.of(factura));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(estadoCuentaService.saldoDe(factura)).thenReturn(new BigDecimal("100.00"));
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.registrarAbono(CreateAbonoDto.builder()
                .idFactura(10).idMetodoPago(1).monto(new BigDecimal("40.00")).build(), recepcionistaUser);

        assertEquals("PENDIENTE", factura.getEstadoFactura().getNombre());
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("Un abono mayor al saldo es rechazado")
    void testRegistrarAbono_SuperaSaldo() {
        Factura factura = facturaPendiente(11, "250.00");
        when(facturaRepository.findByIdWithDetails(11)).thenReturn(Optional.of(factura));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));
        when(estadoCuentaService.saldoDe(factura)).thenReturn(new BigDecimal("100.00"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pagoService.registrarAbono(CreateAbonoDto.builder()
                .idFactura(11).idMetodoPago(1).monto(new BigDecimal("120.00")).build(), recepcionistaUser));

        assertTrue(ex.getMessage().contains("supera el saldo"));
        verify(comprobantePagoRepository, never()).save(any(ComprobantePago.class));
    }

    @Test
    @DisplayName("No se puede abonar a una factura ya pagada")
    void testRegistrarAbono_FacturaPagada() {
        Factura factura = facturaPendiente(12, "250.00");
        factura.setEstadoFactura(estadoPagada);
        when(facturaRepository.findByIdWithDetails(12)).thenReturn(Optional.of(factura));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoEfectivo));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pagoService.registrarAbono(CreateAbonoDto.builder()
                .idFactura(12).idMetodoPago(1).monto(new BigDecimal("10.00")).build(), recepcionistaUser));

        assertTrue(ex.getMessage().contains("no tiene saldo pendiente"));
    }
}
