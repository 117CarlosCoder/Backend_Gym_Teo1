package com.example.backendgymteo1.service;

import com.example.backendgymteo1.dto.pago.PagoResponseDto;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfComprobanteService {

    public byte[] generarComprobantePdf(PagoResponseDto pago) {
        if (pago == null) {
            throw new RuntimeException("No se puede generar comprobante PDF para un pago nulo");
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontHeaderTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
            Font fontHeaderSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(226, 232, 240));
            Font fontSectionHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(30, 41, 59));
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(71, 85, 105));
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(15, 23, 42));
            Font fontAmount = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(22, 101, 52));
            Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(100, 116, 139));

            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(new Color(30, 41, 59));
            headerCell.setPadding(16);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph title = new Paragraph("CLAUDA LOVERS", fontHeaderTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(title);

            Paragraph subtitle = new Paragraph("COMPROBANTE OFICIAL DE PAGO", fontHeaderSubtitle);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(subtitle);

            headerTable.addCell(headerCell);
            document.add(headerTable);

            document.add(new Paragraph(" "));

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{50f, 50f});

            String noComprobante = pago.getIdComprobante() != null ? String.format("%06d", pago.getIdComprobante()) : "N/A";
            String noFactura = pago.getIdFactura() != null ? String.format("%06d", pago.getIdFactura()) : "N/A";
            String fechaPagoStr = pago.getFechaPago() != null ? pago.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A";

            addTableCell(metaTable, "No. Comprobante:", noComprobante, fontLabel, fontValue);
            addTableCell(metaTable, "No. Factura:", noFactura, fontLabel, fontValue);
            addTableCell(metaTable, "Fecha de Pago:", fechaPagoStr, fontLabel, fontValue);
            addTableCell(metaTable, "Estado:", "PAGADO", fontLabel, fontValue);

            document.add(metaTable);
            document.add(new Paragraph(" "));

            document.add(createSectionTitle("DATOS DEL SOCIO", fontSectionHeader));

            PdfPTable socioTable = new PdfPTable(2);
            socioTable.setWidthPercentage(100);
            socioTable.setWidths(new float[]{30f, 70f});

            addTableCell(socioTable, "Nombre Completo:", pago.getNombreSocio() != null ? pago.getNombreSocio() : "N/A", fontLabel, fontValue);
            addTableCell(socioTable, "DPI:", pago.getDpiSocio() != null ? pago.getDpiSocio() : "N/A", fontLabel, fontValue);
            addTableCell(socioTable, "Correo Electrónico:", pago.getCorreoSocio() != null ? pago.getCorreoSocio() : "N/A", fontLabel, fontValue);

            document.add(socioTable);
            document.add(new Paragraph(" "));

            document.add(createSectionTitle("DETALLES DEL PLAN Y MEMBRESÍA", fontSectionHeader));

            PdfPTable planTable = new PdfPTable(2);
            planTable.setWidthPercentage(100);
            planTable.setWidths(new float[]{40f, 60f});

            String duracionStr = pago.getDuracionPlan() != null ? pago.getDuracionPlan() + " días" : "N/A";
            String vencimientoStr = pago.getNuevaFechaVencimiento() != null ? pago.getNuevaFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";

            addTableCell(planTable, "Plan Contratado:", pago.getNombrePlan() != null ? pago.getNombrePlan() : "N/A", fontLabel, fontValue);
            addTableCell(planTable, "Duración del Plan:", duracionStr, fontLabel, fontValue);
            addTableCell(planTable, "Nueva Fecha de Vencimiento:", vencimientoStr, fontLabel, fontValue);

            document.add(planTable);
            document.add(new Paragraph(" "));

            document.add(createSectionTitle("RESUMEN DE PAGO", fontSectionHeader));

            PdfPTable pagoTable = new PdfPTable(2);
            pagoTable.setWidthPercentage(100);
            pagoTable.setWidths(new float[]{40f, 60f});

            String metodoStr = (pago.getMetodoPago() != null && pago.getMetodoPago().getNombre() != null) ? pago.getMetodoPago().getNombre() : "N/A";
            String montoStr = pago.getMontoPagado() != null ? String.format("Q. %.2f", pago.getMontoPagado()) : "Q. 0.00";

            addTableCell(pagoTable, "Método de Pago:", metodoStr, fontLabel, fontValue);
            addTableCell(pagoTable, "Referencia / Voucher:", pago.getReferencia() != null ? pago.getReferencia() : "Sin referencia", fontLabel, fontValue);
            addTableCell(pagoTable, "Atendido por:", pago.getNombreRecepcionista() != null ? pago.getNombreRecepcionista() : "Recepción", fontLabel, fontValue);
            document.add(pagoTable);
            document.add(new Paragraph(" "));

            // Desglose del cobro: precio del plan, descuento (promoción) y recargo por pago tardío (mora)
            document.add(createSectionTitle("DESGLOSE DEL COBRO", fontSectionHeader));
            PdfPTable desgloseTable = new PdfPTable(2);
            desgloseTable.setWidthPercentage(100);
            desgloseTable.setWidths(new float[]{60f, 40f});
            Font fontDescuento = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(22, 101, 52));
            Font fontRecargo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(185, 28, 28));

            BigDecimal descuento = valorOCero(pago.getDescuento());
            BigDecimal recargo = valorOCero(pago.getRecargo());
            BigDecimal base = pago.getMontoBase() != null
                    ? pago.getMontoBase()
                    : valorOCero(pago.getMontoPagado()).add(descuento).subtract(recargo);

            addTableCell(desgloseTable, "Precio del plan:", formatoQuetzales(base), fontLabel, fontValue);
            if (descuento.signum() > 0) {
                String etiqueta = pago.getNombrePromocion() != null
                        ? "Descuento aplicado (" + pago.getNombrePromocion() + "):"
                        : "Descuento aplicado:";
                addTableCell(desgloseTable, etiqueta, "- " + formatoQuetzales(descuento), fontLabel, fontDescuento);
            } else {
                addTableCell(desgloseTable, "Descuento aplicado:", "No aplica", fontLabel, fontValue);
            }
            if (recargo.signum() > 0) {
                String etiqueta = base.signum() > 0
                        ? String.format("Recargo por pago tardío / mora (%s%%):",
                                recargo.multiply(BigDecimal.valueOf(100)).divide(base, 0, RoundingMode.HALF_UP))
                        : "Recargo por pago tardío / mora:";
                addTableCell(desgloseTable, etiqueta, "+ " + formatoQuetzales(recargo), fontLabel, fontRecargo);
            } else {
                addTableCell(desgloseTable, "Recargo por pago tardío / mora:", "No aplica (pago a tiempo)", fontLabel, fontValue);
            }
            boolean enAbonos = (pago.getNumeroAbono() != null && pago.getNumeroAbono() > 1)
                    || (pago.getSaldoPendiente() != null && pago.getSaldoPendiente().signum() > 0);
            if (enAbonos) {
                addTableCell(desgloseTable, "Total de la factura:", formatoQuetzales(pago.getTotalFactura()), fontLabel, fontValue);
                addTableCell(desgloseTable, "Monto pagado en este abono:", montoStr, fontLabel, fontAmount);
            } else {
                addTableCell(desgloseTable, "Monto Total Pagado:", montoStr, fontLabel, fontAmount);
            }
            document.add(desgloseTable);
            document.add(new Paragraph(" "));

            // Pago en cuotas / abonos: cuánto se lleva pagado y el saldo que queda
            if (enAbonos) {
                document.add(createSectionTitle("PAGO EN ABONOS - ESTADO DE LA FACTURA", fontSectionHeader));
                PdfPTable abonosTable = new PdfPTable(2);
                abonosTable.setWidthPercentage(100);
                abonosTable.setWidths(new float[]{60f, 40f});
                BigDecimal saldo = valorOCero(pago.getSaldoPendiente());
                addTableCell(abonosTable, "Número de abono:", String.valueOf(pago.getNumeroAbono() != null ? pago.getNumeroAbono() : 1), fontLabel, fontValue);
                addTableCell(abonosTable, "Total abonado a la fecha:", formatoQuetzales(pago.getTotalAbonado()), fontLabel, fontValue);
                addTableCell(abonosTable, "Saldo pendiente:", saldo.signum() > 0 ? formatoQuetzales(saldo) : "Q. 0.00 (factura liquidada)",
                        fontLabel, saldo.signum() > 0 ? fontRecargo : fontDescuento);
                if (saldo.signum() > 0 && pago.getFechaLimiteSaldo() != null) {
                    addTableCell(abonosTable, "Fecha límite para liquidar el saldo:", pago.getFechaLimiteSaldo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontLabel, fontValue);
                }
                document.add(abonosTable);
                document.add(new Paragraph(" "));
            }
            document.add(new Paragraph(" "));

            Paragraph footer = new Paragraph("¡Gracias por su pago! Este documento sirve como comprobante oficial de su transacción en Gimnasio Teo.", fontFooter);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            log.error("Error al generar comprobante PDF de pago ID {}: {}", pago.getIdComprobante(), e.getMessage());
            throw new RuntimeException("Error al generar el documento PDF del comprobante de pago", e);
        }

        return out.toByteArray();
    }

    private static BigDecimal valorOCero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private static String formatoQuetzales(BigDecimal valor) {
        return String.format("Q. %.2f", valorOCero(valor));
    }

    private Paragraph createSectionTitle(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingAfter(6);
        return p;
    }

    private void addTableCell(PdfPTable table, String label, String value, Font fontLabel, Font fontValue) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBorder(PdfPCell.NO_BORDER);
        cellLabel.setPadding(4);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, fontValue));
        cellValue.setBorder(PdfPCell.NO_BORDER);
        cellValue.setPadding(4);

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }
}
