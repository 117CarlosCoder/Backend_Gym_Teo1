package com.example.backendgymteo1.mapper;

import com.example.backendgymteo1.dto.pago.FacturaResponseDto;
import com.example.backendgymteo1.dto.pago.MetodoPagoResponseDto;
import com.example.backendgymteo1.dto.pago.PagoResponseDto;
import com.example.backendgymteo1.entity.ComprobantePago;
import com.example.backendgymteo1.entity.Factura;
import com.example.backendgymteo1.entity.MetodoPago;
import com.example.backendgymteo1.entity.Membresia;
import com.example.backendgymteo1.entity.Socio;
import com.example.backendgymteo1.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PagoMapper {

    public MetodoPagoResponseDto toMetodoPagoDto(MetodoPago metodoPago) {
        if (metodoPago == null) {
            return null;
        }
        return MetodoPagoResponseDto.builder()
                .id(metodoPago.getId())
                .nombre(metodoPago.getNombre())
                .descripcion(metodoPago.getDescripcion())
                .build();
    }

    public FacturaResponseDto toFacturaDto(Factura factura) {
        if (factura == null) {
            return null;
        }

        String nombreSocio = null;
        String nombrePlan = null;
        Integer idMembresia = null;

        if (factura.getMembresia() != null) {
            Membresia m = factura.getMembresia();
            idMembresia = m.getId();

            if (m.getPlan() != null) {
                nombrePlan = m.getPlan().getNombre();
            }

            if (m.getSocio() != null && m.getSocio().getUsuario() != null) {
                User u = m.getSocio().getUsuario();
                nombreSocio = u.getNombres() + " " + u.getApellidos();
            }
        }

        String estadoFactura = factura.getEstadoFactura() != null ? factura.getEstadoFactura().getNombre() : null;

        return FacturaResponseDto.builder()
                .id(factura.getId())
                .idMembresia(idMembresia)
                .nombreSocio(nombreSocio)
                .nombrePlan(nombrePlan)
                .estadoFactura(estadoFactura)
                .monto(factura.getMonto())
                .fechaEmision(factura.getFechaEmision())
                .fechaVencimiento(factura.getFechaVencimiento())
                .observacion(factura.getObservacion())
                .build();
    }

    public PagoResponseDto toPagoDto(ComprobantePago comprobante) {
        if (comprobante == null) {
            return null;
        }

        Factura factura = comprobante.getFactura();
        Membresia membresia = factura != null ? factura.getMembresia() : null;
        Socio socio = membresia != null ? membresia.getSocio() : null;
        User usuarioSocio = socio != null ? socio.getUsuario() : null;

        String nombreSocio = usuarioSocio != null ? usuarioSocio.getNombres() + " " + usuarioSocio.getApellidos() : null;
        String correoSocio = usuarioSocio != null ? usuarioSocio.getCorreo() : null;
        String dpiSocio = usuarioSocio != null ? usuarioSocio.getDpi() : null;

        String nombrePlan = (membresia != null && membresia.getPlan() != null) ? membresia.getPlan().getNombre() : null;
        Integer duracionPlan = (membresia != null && membresia.getPlan() != null) ? membresia.getPlan().getDuracion() : null;

        String nombreRecepcionista = null;
        if (factura != null && factura.getRecepcionista() != null && factura.getRecepcionista().getUsuario() != null) {
            User uRecep = factura.getRecepcionista().getUsuario();
            nombreRecepcionista = uRecep.getNombres() + " " + uRecep.getApellidos();
        }

        return PagoResponseDto.builder()
                .idComprobante(comprobante.getId())
                .idFactura(factura != null ? factura.getId() : null)
                .idSocio(socio != null ? socio.getId() : null)
                .nombreSocio(nombreSocio)
                .correoSocio(correoSocio)
                .dpiSocio(dpiSocio)
                .idMembresia(membresia != null ? membresia.getId() : null)
                .nombrePlan(nombrePlan)
                .duracionPlan(duracionPlan)
                .nuevaFechaVencimiento(membresia != null ? membresia.getFechaVencimiento() : null)
                .metodoPago(toMetodoPagoDto(comprobante.getMetodoPago()))
                .fechaPago(comprobante.getFechaPago())
                .montoPagado(comprobante.getMontoPagado())
                .referencia(comprobante.getReferencia())
                .nombreRecepcionista(nombreRecepcionista)
                .observacion(factura != null ? factura.getObservacion() : null)
                .build();
    }

    public List<PagoResponseDto> toPagoDtoList(List<ComprobantePago> comprobantes) {
        if (comprobantes == null) {
            return Collections.emptyList();
        }
        return comprobantes.stream()
                .map(this::toPagoDto)
                .toList();
    }
}
