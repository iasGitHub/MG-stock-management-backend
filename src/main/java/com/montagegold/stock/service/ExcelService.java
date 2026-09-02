package com.montagegold.stock.service;

import com.montagegold.stock.dto.StockMovementResponse;
import com.montagegold.stock.dto.SupplierRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void exportMovements(List<StockMovementResponse> movements, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=mouvements_stock.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mouvements");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] headers = {"ID", "Produit", "Référence", "Type", "Quantité", "Prix Unitaire (MRU)",
                "Fournisseur/Destinataire", "Raison", "Réf. Externe", "Date", "Utilisateur"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (StockMovementResponse m : movements) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(m.getId());
            row.createCell(1).setCellValue(m.getProductName());
            row.createCell(2).setCellValue(m.getProductReference());
            row.createCell(3).setCellValue(m.getType().toString());
            row.createCell(4).setCellValue(m.getQuantity());
            row.createCell(5).setCellValue(m.getUnitPrice() != null ? m.getUnitPrice() : 0);

            String supplierRecipient = m.getType().toString().equals("IN")
                    ? (m.getSupplierName() != null ? m.getSupplierName() : "")
                    : (m.getRecipient() != null ? m.getRecipient() : "");
            row.createCell(6).setCellValue(supplierRecipient);
            row.createCell(7).setCellValue(m.getReason() != null ? m.getReason() : "");
            row.createCell(8).setCellValue(m.getExternalReference() != null ? m.getExternalReference() : "");
            row.createCell(9).setCellValue(m.getMovementDate() != null ? m.getMovementDate().format(DATE_FMT) : "");
            row.createCell(10).setCellValue(m.getUserName() != null ? m.getUserName() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public List<SupplierRequest> parseSupplierImport(MultipartFile file) throws IOException {
        List<SupplierRequest> suppliers = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String nif = getCellStringValue(row.getCell(0));
            String name = getCellStringValue(row.getCell(1));
            if (nif == null || nif.isBlank() || name == null || name.isBlank()) continue;

            SupplierRequest request = new SupplierRequest();
            request.setNif(nif.trim());
            request.setName(name.trim());

            String phone = getCellStringValue(row.getCell(2));
            request.setPhone(phone != null && !phone.isBlank() ? phone.trim() : null);

            String address = getCellStringValue(row.getCell(3));
            request.setAddress(address != null && !address.isBlank() ? address.trim() : null);

            suppliers.add(request);
        }

        workbook.close();
        return suppliers;
    }

    public void exportSupplierTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=modele_fournisseurs.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Fournisseurs");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] headers = {"NIF *", "Nom *", "Téléphone", "Adresse"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Row example = sheet.createRow(1);
        example.createCell(0).setCellValue("FRS-001");
        example.createCell(1).setCellValue("Fournisseur Exemple");
        example.createCell(2).setCellValue("+222 XX XX XX XX");
        example.createCell(3).setCellValue("Adresse du fournisseur");

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
}
