package com.example.backendgymteo1.controller;

import com.example.backendgymteo1.config.security.CurrentUser;
import com.example.backendgymteo1.dto.pago.CreatePagoDto;
import com.example.backendgymteo1.dto.pago.MetodoPagoResponseDto;
import com.example.backendgymteo1.dto.pago.PagoResponseDto;
import com.example.backendgymteo1.entity.User;
import com.example.backendgymteo1.service.PagoService;
import com.example.backendgymteo1.service.PdfComprobanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Pagos", description = "Operaciones para el registro de pagos, historial por socio y descarga de comprobantes PDF")
@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PagoController {

    private final PagoService pagoService;
    private final PdfComprobanteService pdfComprobanteService;

    @Operation(summary = "Registrar un nuevo pago de membresía", description = "Registra la factura y el comprobante de pago, y recalcula automáticamente la fechaVencimiento de la membresía según la duración del plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de pago inválidos o monto <= 0", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN o RECEPCIONISTA)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membresía o método de pago no encontrado", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<PagoResponseDto> registrarPago(
            @Valid @RequestBody CreatePagoDto request,
            @CurrentUser User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(request, currentUser));
    }

    @Operation(summary = "Consultar historial de pagos por socio", description = "Retorna la lista paginada de comprobantes de pago pertenecientes a un socio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/socio/{socioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'CLIENTE')")
    public ResponseEntity<Page<PagoResponseDto>> obtenerHistorialPagosPorSocio(
            @PathVariable Integer socioId,
            @PageableDefault(size = 10, sort = "fechaPago") Pageable pageable) {
        return ResponseEntity.ok(pagoService.obtenerHistorialPagosPorSocio(socioId, pageable));
    }

    @Operation(summary = "Obtener detalle de pago por ID", description = "Retorna el comprobante y detalle completo de la transacción.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagoResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'CLIENTE')")
    public ResponseEntity<PagoResponseDto> obtenerPagoPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.obtenerPagoPorId(id));
    }

    @Operation(summary = "Descargar comprobante de pago en formato PDF", description = "Genera y descarga el archivo PDF oficial con el diseño del comprobante de pago.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo PDF generado exitosamente",
                    content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado", content = @Content)
    })
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'CLIENTE')")
    public ResponseEntity<byte[]> descargarComprobantePdf(@PathVariable Integer id) {
        PagoResponseDto pago = pagoService.obtenerPagoPorId(id);
        byte[] pdfBytes = pdfComprobanteService.generarComprobantePdf(pago);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("inline")
                .filename("comprobante_pago_" + id + ".pdf")
                .build());
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


    @Operation(summary = "Listar métodos de pago disponibles", description = "Retorna el catálogo de métodos de pago (Efectivo, Tarjeta, Transferencia).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de métodos de pago",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MetodoPagoResponseDto.class))))
    })
    @GetMapping("/metodos-pago")
    public ResponseEntity<List<MetodoPagoResponseDto>> listarMetodosPago() {
        return ResponseEntity.ok(pagoService.listarMetodosPago());
    }
}
