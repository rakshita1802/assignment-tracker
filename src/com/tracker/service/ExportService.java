package com.tracker.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.tracker.model.Assignment;
import com.tracker.model.Enrollment;
import com.tracker.model.Grade;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports data to PDF (iText) or Excel (Apache POI).
 */
public class ExportService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ── EXCEL EXPORT ──────────────────────────────────────────────

    /** Exports a list of assignments to an Excel file. */
    public boolean exportAssignmentsToExcel(List<Assignment> assignments,
                                            String filePath) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Assignments");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font hFont = wb.createFont();
            hFont.setColor(IndexedColors.WHITE.getIndex());
            hFont.setBold(true);
            headerStyle.setFont(hFont);

            // Header row
            String[] headers = {"#","Title","Subject","Deadline","Teacher","Description"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            CellStyle altStyle = wb.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 1;
            for (Assignment a : assignments) {
                Row row = sheet.createRow(rowNum);
                if (rowNum % 2 == 0) {
                    for (int c = 0; c < headers.length; c++)
                        row.createCell(c).setCellStyle(altStyle);
                }
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(a.getTitle());
                row.createCell(2).setCellValue(a.getSubject());
                row.createCell(3).setCellValue(a.getDeadline().format(FMT));
                row.createCell(4).setCellValue(a.getCreatedByName() != null ?
                        a.getCreatedByName() : "");
                row.createCell(5).setCellValue(a.getDescription() != null ?
                        a.getDescription() : "");
                rowNum++;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
            System.out.println("[Export] Excel saved to: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("[ExportService.Excel] " + e.getMessage());
            return false;
        }
    }

    /** Exports grades to Excel. */
    public boolean exportGradesToExcel(List<Grade> grades, String filePath) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Grades");

            String[] headers = {"#","Student","Assignment","Marks","Max Marks","Percentage","Grade","Feedback"};
            Row header = sheet.createRow(0);

            CellStyle hs = wb.createCellStyle();
            hs.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            hs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font hf = wb.createFont();
            hf.setColor(IndexedColors.WHITE.getIndex());
            hf.setBold(true);
            hs.setFont(hf);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(hs);
            }

            int rowNum = 1;
            for (Grade g : grades) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(g.getStudentName());
                row.createCell(2).setCellValue(g.getAssignmentTitle());
                row.createCell(3).setCellValue(g.getMarks());
                row.createCell(4).setCellValue(g.getMaxMarks());
                row.createCell(5).setCellValue(
                        String.format("%.1f%%", g.getMarks() * 100.0 / g.getMaxMarks()));
                row.createCell(6).setCellValue(g.getGradeLetter());
                row.createCell(7).setCellValue(g.getFeedback() != null ? g.getFeedback() : "");
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
            return true;

        } catch (Exception e) {
            System.err.println("[ExportService.GradesExcel] " + e.getMessage());
            return false;
        }
    }

    // ── PDF EXPORT ────────────────────────────────────────────────

    /** Exports assignments to a styled PDF. */
    public boolean exportAssignmentsToPdf(List<Assignment> assignments,
                                          String filePath) {
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            // Title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 18,
                    new BaseColor(67, 97, 238));
            doc.add(new Paragraph("Assignment Report", titleFont));
            doc.add(new Paragraph("Generated: " +
                    java.time.LocalDateTime.now().format(FMT),
                    FontFactory.getFont(FontFactory.HELVETICA, 10,
                            new BaseColor(108, 117, 125))));
            doc.add(Chunk.NEWLINE);

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f, 2f, 2.5f, 2f});

            // Table header
            BaseColor headerBg = new BaseColor(52, 58, 64);
            String[] cols = {"#","Title","Subject","Deadline","Teacher"};
            for (String col : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(col,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11,
                                BaseColor.WHITE)));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(8);
                cell.setBorderColor(BaseColor.DARK_GRAY);
                table.addCell(cell);
            }

            // Data rows
            int idx = 1;
            for (Assignment a : assignments) {
                boolean alt = idx % 2 == 0;
                BaseColor rowBg = alt ? new BaseColor(248, 249, 252) : BaseColor.WHITE;
                com.itextpdf.text.Font rowFont =
                        FontFactory.getFont(FontFactory.HELVETICA, 10);

                addPdfCell(table, String.valueOf(idx++), rowFont, rowBg);
                addPdfCell(table, a.getTitle(),          rowFont, rowBg);
                addPdfCell(table, a.getSubject(),        rowFont, rowBg);
                addPdfCell(table, a.getDeadline().format(FMT), rowFont, rowBg);
                addPdfCell(table, a.getCreatedByName() != null ?
                        a.getCreatedByName() : "", rowFont, rowBg);
            }

            doc.add(table);
            doc.close();
            System.out.println("[Export] PDF saved to: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("[ExportService.PDF] " + e.getMessage());
            if (doc.isOpen()) doc.close();
            return false;
        }
    }

    private void addPdfCell(PdfPTable table, String text,
                            com.itextpdf.text.Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setBorderColor(new BaseColor(220, 225, 235));
        table.addCell(cell);
    }
}