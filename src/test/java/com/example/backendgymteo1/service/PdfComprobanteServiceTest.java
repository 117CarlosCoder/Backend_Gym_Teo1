package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.pago.MetodoPagoResponseDto;
import com.example.backendgymteo1.dto.pago.PagoResponseDto;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfComprobanteServiceTest {

    private final PdfComprobanteService service = new PdfComprobanteService();

    private PagoResponseDto.PagoResponseDtoBuilder pagoBase() {
        return PagoResponseDto.builder()
                .idComprobante(15)
                .idFactura(15)
                .nombreSocio("Lucía Hernández")
                .dpiSocio("1000000000008")
                .correoSocio("lucia@gym.com")
                .nombrePlan("Plan Mensual")
                .duracionPlan(30)
                .nuevaFechaVencimiento(LocalDate.of(2026, 11, 20))
                .metodoPago(MetodoPagoResponseDto.builder().id(1).nombre("EFECTIVO").build())
                .fechaPago(LocalDateTime.of(2026, 10, 21, 10, 30));
    }

    private String textoDelPdf(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
                texto.append(extractor.getTextFromPage(pagina)).append('\n');
            }
            return texto.toString();
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("El comprobante muestra el recargo por mora cuando el pago fue tardío")
    void comprobanteMuestraRecargoPorMora() throws Exception {
        PagoResponseDto pago = pagoBase()
                .montoBase(new BigDecimal("250.00"))
                .descuento(BigDecimal.ZERO)
                .recargo(new BigDecimal("25.00"))
                .montoPagado(new BigDecimal("275.00"))
                .build();

        String texto = textoDelPdf(service.generarComprobantePdf(pago));

        assertTrue(texto.contains("DESGLOSE DEL COBRO"), texto);
        assertTrue(texto.contains("Q. 250.00"), texto);
        assertTrue(texto.contains("Recargo por pago tardío / mora (10%)"), texto);
        assertTrue(texto.contains("+ Q. 25.00"), texto);
        assertTrue(texto.contains("Q. 275.00"), texto);
    }

    @Test
    @DisplayName("El comprobante muestra el descuento y el nombre de la promoción")
    void comprobanteMuestraDescuento() throws Exception {
        PagoResponseDto pago = pagoBase()
                .montoBase(new BigDecimal("250.00"))
                .descuento(new BigDecimal("50.00"))
                .recargo(BigDecimal.ZERO)
                .idPromocion(3)
                .nombrePromocion("Promo Estudiantes")
                .montoPagado(new BigDecimal("200.00"))
                .build();

        String texto = textoDelPdf(service.generarComprobantePdf(pago));

        assertTrue(texto.contains("Descuento aplicado (Promo Estudiantes)"), texto);
        assertTrue(texto.contains("- Q. 50.00"), texto);
        assertTrue(texto.contains("No aplica (pago a tiempo)"), texto);
        assertTrue(texto.contains("Q. 200.00"), texto);
    }

    @Test
    @DisplayName("Un pago sin descuento ni recargo indica que no aplican")
    void comprobanteSinDescuentoNiRecargo() throws Exception {
        PagoResponseDto pago = pagoBase()
                .montoBase(new BigDecimal("250.00"))
                .descuento(BigDecimal.ZERO)
                .recargo(BigDecimal.ZERO)
                .montoPagado(new BigDecimal("250.00"))
                .build();

        String texto = textoDelPdf(service.generarComprobantePdf(pago));

        assertTrue(texto.contains("Descuento aplicado"), texto);
        assertTrue(texto.contains("No aplica"), texto);
    }

    @Test
    @DisplayName("Un abono muestra el total de la factura, lo abonado y el saldo pendiente con su fecha límite")
    void comprobanteDeAbonoMuestraSaldo() throws Exception {
        PagoResponseDto pago = pagoBase()
                .montoBase(new BigDecimal("250.00"))
                .descuento(BigDecimal.ZERO)
                .recargo(BigDecimal.ZERO)
                .montoPagado(new BigDecimal("150.00"))
                .totalFactura(new BigDecimal("250.00"))
                .numeroAbono(1)
                .totalAbonado(new BigDecimal("150.00"))
                .saldoPendiente(new BigDecimal("100.00"))
                .fechaLimiteSaldo(LocalDate.of(2026, 11, 5))
                .build();

        String texto = textoDelPdf(service.generarComprobantePdf(pago));

        assertTrue(texto.contains("PAGO EN ABONOS"), texto);
        assertTrue(texto.contains("Monto pagado en este abono"), texto);
        assertTrue(texto.contains("Saldo pendiente"), texto);
        assertTrue(texto.contains("Q. 100.00"), texto);
        assertTrue(texto.contains("05/11/2026"), texto);
    }

    @Test
    @DisplayName("No genera comprobante para un pago nulo")
    void rechazaPagoNulo() {
        assertThrows(RuntimeException.class, () -> service.generarComprobantePdf(null));
    }
}
