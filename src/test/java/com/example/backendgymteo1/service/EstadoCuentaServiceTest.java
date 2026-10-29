package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.pago.EstadoCuentaDto;
import com.example.backendgymteo1.entity.ComprobantePago;
import com.example.backendgymteo1.entity.EstadoFactura;
import com.example.backendgymteo1.entity.Factura;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.MetodoPago;
import com.example.backendgymteo1.entity.PlanMembresia;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.repository.ComprobantePagoRepository;
import com.example.backendgymteo1.repository.FacturaRepository;
import com.example.backendgymteo1.repository.SocioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoCuentaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;
    @Mock
    private ComprobantePagoRepository comprobantePagoRepository;
    @Mock
    private SocioRepository socioRepository;

    @InjectMocks
    private EstadoCuentaService service;

    private final LocalDate hoy = LocalDate.now();
    private final EstadoFactura pendiente = EstadoFactura.builder().id(1).nombre("PENDIENTE").build();
    private final EstadoFactura pagada = EstadoFactura.builder().id(2).nombre("PAGADA").build();
    private final MetodoPago efectivo = MetodoPago.builder().id(1).nombre("EFECTIVO").build();

    private Factura factura(int id, String total, EstadoFactura estado, LocalDate limite) {
        PlanMembresia plan = PlanMembresia.builder().id(1).nombre("Plan Mensual").duracion(30).precio(new BigDecimal(total)).build();
        Membresia membresia = Membresia.builder().id(10).plan(plan).build();
        return Factura.builder().id(id).membresia(membresia).estadoFactura(estado)
                .monto(new BigDecimal(total)).montoBase(new BigDecimal(total))
                .fechaEmision(hoy.minusDays(20)).fechaVencimiento(limite).build();
    }

    private ComprobantePago pago(int id, Factura factura, String monto) {
        return ComprobantePago.builder().id(id).factura(factura).metodoPago(efectivo)
                .fechaPago(LocalDateTime.now()).montoPagado(new BigDecimal(monto)).build();
    }

    @Test
    @DisplayName("El estado de cuenta suma facturado, pagado y saldo, y marca VENCIDA la factura fuera de plazo")
    void estadoCuentaConAbonos() {
        Socio socio = Socio.builder().id(4).usuario(User.builder().nombres("Lucía").apellidos("Hernández").build()).build();
        Factura liquidada = factura(1, "250.00", pagada, hoy.minusDays(20));
        Factura conSaldo = factura(2, "650.00", pendiente, hoy.minusDays(2));
        when(socioRepository.findById(4)).thenReturn(Optional.of(socio));
        when(facturaRepository.findEstadoCuentaBySocioId(4)).thenReturn(List.of(conSaldo, liquidada));
        when(comprobantePagoRepository.findByFacturaIds(anyList())).thenReturn(List.of(
                pago(1, liquidada, "250.00"), pago(2, conSaldo, "400.00"), pago(3, conSaldo, "100.00")));

        EstadoCuentaDto cuenta = service.estadoCuenta(4);

        assertEquals(new BigDecimal("900.00"), cuenta.getTotalFacturado());
        assertEquals(new BigDecimal("750.00"), cuenta.getTotalPagado());
        assertEquals(new BigDecimal("150.00"), cuenta.getSaldoPendiente());
        assertEquals(new BigDecimal("150.00"), cuenta.getSaldoVencido());
        EstadoCuentaDto.FacturaCuentaDto primera = cuenta.getFacturas().get(0);
        assertEquals("VENCIDA", primera.getEstado());
        assertEquals(2, primera.getAbonos().size());
        assertEquals("PAGADA", cuenta.getFacturas().get(1).getEstado());
    }

    @Test
    @DisplayName("Con saldo vencido el socio no puede ingresar; con saldo dentro del plazo sí")
    void validaSaldoVencido() {
        Factura vencida = factura(3, "250.00", pendiente, hoy.minusDays(1));
        Factura enPlazo = factura(4, "250.00", pendiente, hoy.plusDays(5));
        when(facturaRepository.findPendientesBySocioId(4)).thenReturn(List.of(vencida));
        when(facturaRepository.findPendientesBySocioId(5)).thenReturn(List.of(enPlazo));
        when(comprobantePagoRepository.sumPagadoPorFactura(3)).thenReturn(new BigDecimal("125.00"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.validarSinSaldoVencido(4, hoy));
        assertTrue(ex.getMessage().contains("125.00"), ex.getMessage());
        assertDoesNotThrow(() -> service.validarSinSaldoVencido(5, hoy));
    }

    @Test
    @DisplayName("No se cobra otro periodo si la membresía tiene saldo")
    void validaSaldoPendienteDeMembresia() {
        Factura conSaldo = factura(5, "250.00", pendiente, hoy.plusDays(5));
        when(facturaRepository.findPendientesByMembresiaId(10)).thenReturn(List.of(conSaldo));
        when(comprobantePagoRepository.sumPagadoPorFactura(5)).thenReturn(new BigDecimal("150.00"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.validarSinSaldoPendiente(10));
        assertTrue(ex.getMessage().contains("100.00"), ex.getMessage());
    }
}
