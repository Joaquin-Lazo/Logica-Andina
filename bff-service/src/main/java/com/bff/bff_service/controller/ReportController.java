package com.bff.bff_service.controller;

import com.bff.bff_service.service.AuthService;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AuthService authService;

    @Value("${ROUTE_SERVICE_URL:http://localhost:8081}")
    private String routeServiceUrl;

    // ========== JSON ENDPOINT (for the dashboard) ==========

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getReportSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        List<Map<String, Object>> routes = fetchList("/api/routes");
        List<Map<String, Object>> invoices = fetchList("/api/invoices");
        List<Map<String, Object>> trucks = fetchList("/api/camiones");
        List<Map<String, Object>> cargo = fetchList("/api/cargo");

        // --- Operational Stats ---
        Map<String, Object> operational = new LinkedHashMap<>();
        operational.put("totalRutas", routes.size());

        long pendientes = routes.stream().filter(r -> "Pendiente".equals(r.get("estado"))).count();
        long enTransito = routes.stream().filter(r -> "En Transito".equals(r.get("estado"))).count();
        long completadas = routes.stream().filter(r -> "Completada".equals(r.get("estado"))).count();
        operational.put("pendientes", pendientes);
        operational.put("enTransito", enTransito);
        operational.put("completadas", completadas);

        double totalKm = routes.stream()
                .filter(r -> r.get("distanciaEstimadaKm") != null)
                .mapToDouble(r -> ((Number) r.get("distanciaEstimadaKm")).doubleValue())
                .sum();
        operational.put("totalKmEstimados", Math.round(totalKm * 10.0) / 10.0);

        operational.put("totalCamiones", trucks.size());
        long trucksActivos = trucks.stream().filter(t -> "Activo".equals(t.get("estadoOperativo"))).count();
        operational.put("camionesActivos", trucksActivos);

        double totalToneladas = cargo.stream()
                .filter(c -> c.get("pesoToneladas") != null)
                .mapToDouble(c -> ((Number) c.get("pesoToneladas")).doubleValue())
                .sum();
        operational.put("totalToneladasTransportadas", Math.round(totalToneladas * 10.0) / 10.0);
        operational.put("totalCargamentos", cargo.size());

        summary.put("operational", operational);

        // --- Financial Stats ---
        Map<String, Object> financial = new LinkedHashMap<>();
        double totalNeto = invoices.stream()
                .filter(i -> i.get("montoNeto") != null)
                .mapToDouble(i -> ((Number) i.get("montoNeto")).doubleValue()).sum();
        double totalImpuestos = invoices.stream()
                .filter(i -> i.get("impuestos") != null)
                .mapToDouble(i -> ((Number) i.get("impuestos")).doubleValue()).sum();
        double totalPagar = invoices.stream()
                .filter(i -> i.get("totalPagar") != null)
                .mapToDouble(i -> ((Number) i.get("totalPagar")).doubleValue()).sum();

        long pagadas = invoices.stream().filter(i -> "Pagada".equals(i.get("estadoPago"))).count();
        long porCobrar = invoices.size() - pagadas;

        double montoPendiente = invoices.stream()
                .filter(i -> !"Pagada".equals(i.get("estadoPago")) && i.get("totalPagar") != null)
                .mapToDouble(i -> ((Number) i.get("totalPagar")).doubleValue()).sum();

        financial.put("totalFacturas", invoices.size());
        financial.put("ingresoNeto", Math.round(totalNeto));
        financial.put("totalImpuestos", Math.round(totalImpuestos));
        financial.put("ingresoTotal", Math.round(totalPagar));
        financial.put("facturasPagadas", pagadas);
        financial.put("facturasPorCobrar", porCobrar);
        financial.put("montoPendienteCobro", Math.round(montoPendiente));

        // Revenue by client
        Map<String, Double> revenueByClient = new LinkedHashMap<>();
        for (Map<String, Object> inv : invoices) {
            @SuppressWarnings("unchecked")
            Map<String, Object> client = (Map<String, Object>) inv.get("client");
            String clientName = client != null ? (String) client.get("razonSocial") : "Sin Cliente";
            double monto = inv.get("totalPagar") != null ? ((Number) inv.get("totalPagar")).doubleValue() : 0;
            revenueByClient.merge(clientName, monto, Double::sum);
        }
        financial.put("ingresoPorCliente", revenueByClient);

        summary.put("financial", financial);
        summary.put("routes", routes);
        summary.put("invoices", invoices);

        return ResponseEntity.ok(summary);
    }

    // ========== EXCEL EXPORT ==========

    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel() throws Exception {
        List<Map<String, Object>> routes = fetchList("/api/routes");
        List<Map<String, Object>> invoices = fetchList("/api/invoices");

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- Sheet 1: Operational ---
            Sheet opSheet = wb.createSheet("Reporte Operacional");
            String[] opHeaders = { "ID", "Origen", "Destino", "Estado", "Km Estimados", "Camion", "Fecha Creacion" };
            Row opHead = opSheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font hFont = wb.createFont();
            hFont.setBold(true);
            headerStyle.setFont(hFont);
            for (int i = 0; i < opHeaders.length; i++) {
                Cell c = opHead.createCell(i);
                c.setCellValue(opHeaders[i]);
                c.setCellStyle(headerStyle);
            }

            int idx = 1;
            for (Map<String, Object> r : routes) {
                Row row = opSheet.createRow(idx++);
                row.createCell(0).setCellValue(r.get("idRuta") != null ? ((Number) r.get("idRuta")).intValue() : 0);
                row.createCell(1).setCellValue(str(r.get("origenDireccion")));
                row.createCell(2).setCellValue(str(r.get("destinoDireccion")));
                row.createCell(3).setCellValue(str(r.get("estado")));
                row.createCell(4)
                        .setCellValue(r.get("distanciaEstimadaKm") != null
                                ? ((Number) r.get("distanciaEstimadaKm")).doubleValue()
                                : 0);

                @SuppressWarnings("unchecked")
                Map<String, Object> truck = (Map<String, Object>) r.get("truck");
                row.createCell(5).setCellValue(
                        truck != null ? str(truck.get("patente")) + " " + str(truck.get("marcaModelo")) : "—");
                row.createCell(6).setCellValue(str(r.get("fechaCreacion")));
            }
            for (int i = 0; i < opHeaders.length; i++)
                opSheet.autoSizeColumn(i);

            // --- Sheet 2: Financial ---
            Sheet finSheet = wb.createSheet("Reporte Financiero");
            String[] finHeaders = { "ID Factura", "Ruta", "Cliente", "Monto Neto", "IVA", "Total", "Estado Pago" };
            Row finHead = finSheet.createRow(0);
            for (int i = 0; i < finHeaders.length; i++) {
                Cell c = finHead.createCell(i);
                c.setCellValue(finHeaders[i]);
                c.setCellStyle(headerStyle);
            }

            idx = 1;
            for (Map<String, Object> inv : invoices) {
                Row row = finSheet.createRow(idx++);
                row.createCell(0)
                        .setCellValue(inv.get("idFactura") != null ? ((Number) inv.get("idFactura")).intValue() : 0);

                @SuppressWarnings("unchecked")
                Map<String, Object> route = (Map<String, Object>) inv.get("route");
                row.createCell(1).setCellValue(route != null ? "#" + route.get("idRuta") : "—");

                @SuppressWarnings("unchecked")
                Map<String, Object> client = (Map<String, Object>) inv.get("client");
                row.createCell(2).setCellValue(client != null ? str(client.get("razonSocial")) : "—");

                row.createCell(3)
                        .setCellValue(inv.get("montoNeto") != null ? ((Number) inv.get("montoNeto")).doubleValue() : 0);
                row.createCell(4)
                        .setCellValue(inv.get("impuestos") != null ? ((Number) inv.get("impuestos")).doubleValue() : 0);
                row.createCell(5).setCellValue(
                        inv.get("totalPagar") != null ? ((Number) inv.get("totalPagar")).doubleValue() : 0);
                row.createCell(6).setCellValue(str(inv.get("estadoPago")));
            }
            for (int i = 0; i < finHeaders.length; i++)
                finSheet.autoSizeColumn(i);

            wb.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_logistica_andina.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    // ========== PDF EXPORT ==========

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf() throws Exception {
        List<Map<String, Object>> routes = fetchList("/api/routes");
        List<Map<String, Object>> invoices = fetchList("/api/invoices");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate()); // Landscape
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph("Logistica Andina - Reporte General", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            doc.add(title);

            Paragraph date = new Paragraph(
                    "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    normalFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            doc.add(date);

            // --- Operational Table ---
            doc.add(new Paragraph("Reporte Operacional", sectionFont));
            doc.add(new Paragraph(" "));

            PdfPTable opTable = new PdfPTable(5);
            opTable.setWidthPercentage(100);
            String[] opH = { "ID", "Origen", "Destino", "Estado", "Km" };
            for (String h : opH)
                opTable.addCell(new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9))));
            for (Map<String, Object> r : routes) {
                opTable.addCell(new Phrase(String.valueOf(r.get("idRuta")), normalFont));
                opTable.addCell(new Phrase(str(r.get("origenDireccion")), normalFont));
                opTable.addCell(new Phrase(str(r.get("destinoDireccion")), normalFont));
                opTable.addCell(new Phrase(str(r.get("estado")), normalFont));
                opTable.addCell(new Phrase(String.valueOf(r.get("distanciaEstimadaKm")), normalFont));
            }
            doc.add(opTable);
            doc.add(new Paragraph(" "));

            // --- Financial Table ---
            doc.add(new Paragraph("Reporte Financiero", sectionFont));
            doc.add(new Paragraph(" "));

            PdfPTable finTable = new PdfPTable(5);
            finTable.setWidthPercentage(100);
            String[] finH = { "Factura", "Cliente", "Neto", "Total", "Estado" };
            for (String h : finH)
                finTable.addCell(new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9))));
            for (Map<String, Object> inv : invoices) {
                finTable.addCell(new Phrase("#" + inv.get("idFactura"), normalFont));

                @SuppressWarnings("unchecked")
                Map<String, Object> cl = (Map<String, Object>) inv.get("client");
                finTable.addCell(new Phrase(cl != null ? str(cl.get("razonSocial")) : "—", normalFont));
                finTable.addCell(new Phrase("$" + inv.get("montoNeto"), normalFont));
                finTable.addCell(new Phrase("$" + inv.get("totalPagar"), normalFont));
                finTable.addCell(new Phrase(str(inv.get("estadoPago")), normalFont));
            }
            doc.add(finTable);

            doc.close();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_logistica_andina.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());
        }
    }

    // ========== HELPERS ==========

    private List<Map<String, Object>> fetchList(String path) {
        try {
            String token = authService.getRouteServiceToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                    routeServiceUrl + path, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            return res.getBody() != null ? res.getBody() : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching " + path + ": " + e.getMessage());
            return List.of();
        }
    }

    private String str(Object o) {
        return o != null ? o.toString() : "—";
    }
}