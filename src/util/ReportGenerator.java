//package util;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//import javax.swing.table.TableModel;
//import org.apache.pdfbox.pdmodel.font.Standard14Font;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.common.PDRectangle;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
///**
// * Utility class for generating reports in various formats (PDF, Excel).
// */
//public class ReportGenerator {
//    private static final String REPORTS_DIRECTORY = "reports";
//    
//    /**
//     * Initializes the reports directory if it doesn't exist
//     */
//    private static void initializeReportsDirectory() {
//        File reportsDir = new File(REPORTS_DIRECTORY);
//        if (!reportsDir.exists()) {
//            reportsDir.mkdir();
//        }
//    }
//    
//    /**
//     * Generates a PDF report from a TableModel
//     * 
//     * @param tableModel The table model containing report data
//     * @param title Report title
//     * @param filename Output filename (without extension)
//     * @return Full path to the generated file
//     * @throws IOException If there's an error writing the file
//     */
//    public static String generatePdfReport(TableModel tableModel, String title, String filename) throws IOException {
//        initializeReportsDirectory();
//        
//        String filePath = REPORTS_DIRECTORY + File.separator + filename + ".pdf";
//        
//        try (PDDocument document = new PDDocument()) {
//            PDPage page = new PDPage(PDRectangle.A4);
//            document.addPage(page);
//            
//            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
//                // Draw title
//                contentStream.beginText();
//                contentStream.setFont(PDType1Font.getInstance(Standard14Fonts.FontName.HELVETICA), 16);
//                contentStream.newLineAtOffset(50, 750);
//                contentStream.showText(title);
//                contentStream.endText();
//                
//                // Draw table headers
//                float yPosition = 700;
//                float margin = 50;
//                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
//                float tableHeight = 20f;
//                float cellMargin = 5f;
//                
//                int numColumns = tableModel.getColumnCount();
//                float cellWidth = tableWidth / numColumns;
//                
//                // Draw column headers
//                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
//                contentStream.beginText();
//                contentStream.newLineAtOffset(margin, yPosition);
//                
//                for (int col = 0; col < numColumns; col++) {
//                    String header = tableModel.getColumnName(col);
//                    contentStream.showText(header);
//                    contentStream.newLineAtOffset(cellWidth, 0);
//                }
//                
//                contentStream.endText();
//                yPosition -= tableHeight;
//                
//                // Draw table content
//                contentStream.setFont(PDType1Font.HELVETICA, 10);
//                
//                int numRows = tableModel.getRowCount();
//                for (int row = 0; row < numRows; row++) {
//                    if (yPosition <= 100) {
//                        // Start a new page if we're near the bottom
//                        contentStream.close();
//                        page = new PDPage(PDRectangle.A4);
//                        document.addPage(page);
////                        contentStream = new PDPageContentStream(document, page);
//                        yPosition = 750;
//                    }
//                    
//                    contentStream.beginText();
//                    contentStream.newLineAtOffset(margin, yPosition);
//                    
//                    for (int col = 0; col < numColumns; col++) {
//                        Object value = tableModel.getValueAt(row, col);
//                        String text = value != null ? value.toString() : "";
//                        contentStream.showText(text);
//                        contentStream.newLineAtOffset(cellWidth, 0);
//                    }
//                    
//                    contentStream.endText();
//                    yPosition -= tableHeight;
//                }
//            }
//            
//            document.save(filePath);
//            LogUtil.info("PDF report generated: " + filePath);
//        }
//        
//        return filePath;
//    }
//    
//    /**
//     * Generates an Excel report from a TableModel
//     * 
//     * @param tableModel The table model containing report data
//     * @param title Sheet title
//     * @param filename Output filename (without extension)
//     * @return Full path to the generated file
//     * @throws IOException If there's an error writing the file
//     */
//    public static String generateExcelReport(TableModel tableModel, String title, String filename) throws IOException {
//        initializeReportsDirectory();
//        
//        String filePath = REPORTS_DIRECTORY + File.separator + filename + ".xlsx";
//        
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet(title);
//            
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            for (int col = 0; col < tableModel.getColumnCount(); col++) {
//                Cell cell = headerRow.createCell(col);
//                cell.setCellValue(tableModel.getColumnName(col));
//            }
//            
//            // Create data rows
//            for (int row = 0; row < tableModel.getRowCount(); row++) {
//                Row dataRow = sheet.createRow(row + 1);
//                for (int col = 0; col < tableModel.getColumnCount(); col++) {
//                    Cell cell = dataRow.createCell(col);
//                    
//                    Object value = tableModel.getValueAt(row, col);
//                    if (value != null) {
//                        if (value instanceof String) {
//                            cell.setCellValue((String) value);
//                        } else if (value instanceof Number) {
//                            cell.setCellValue(((Number) value).doubleValue());
//                        } else if (value instanceof Boolean) {
//                            cell.setCellValue((Boolean) value);
//                        } else {
//                            cell.setCellValue(value.toString());
//                        }
//                    }
//                }
//            }
//            
//            // Auto-size columns
//            for (int col = 0; col < tableModel.getColumnCount(); col++) {
//                sheet.autoSizeColumn(col);
//            }
//            
//            // Write to file
//            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
//                workbook.write(fileOut);
//            }
//            
//            LogUtil.info("Excel report generated: " + filePath);
//        }
//        
//        return filePath;
//    }
//    
//    /**
//     * Generates a PDF report from a list of maps
//     * 
//     * @param data List of maps containing row data
//     * @param columns Map of column names to their display titles
//     * @param title Report title
//     * @param filename Output filename (without extension)
//     * @return Full path to the generated file
//     * @throws IOException If there's an error writing the file
//     */
//    public static String generatePdfReport(List<Map<String, Object>> data, Map<String, String> columns, 
//                                           String title, String filename) throws IOException {
//        initializeReportsDirectory();
//        
//        String filePath = REPORTS_DIRECTORY + File.separator + filename + ".pdf";
//        
//        try (PDDocument document = new PDDocument()) {
//            PDPage page = new PDPage(PDRectangle.A4);
//            document.addPage(page);
//            
//            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
//                // Draw title
//                contentStream.beginText();
//                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
//                contentStream.newLineAtOffset(50, 750);
//                contentStream.showText(title);
//                contentStream.endText();
//                
//                // Draw table
//                float yPosition = 700;
//                float margin = 50;
//                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
//                float tableHeight = 20f;
//                
//                int numColumns = columns.size();
//                float cellWidth = tableWidth / numColumns;
//                
//                // Draw column headers
//                contentStream.setFont(PDType1Font.class, 12);
//                contentStream.beginText();
//                contentStream.newLineAtOffset(margin, yPosition);
//                
//                for (String displayName : columns.values()) {
//                    contentStream.showText(displayName);
//                    contentStream.newLineAtOffset(cellWidth, 0);
//                }
//                
//                contentStream.endText();
//                yPosition -= tableHeight;
//                
//                // Draw table content
//                contentStream.setFont(PDType1Font.HELVETICA, 10);
//                
//                for (Map<String, Object> row : data) {
//                    if (yPosition <= 100) {
//                        // Start a new page if we're near the bottom
//                        contentStream.close();
//                        page = new PDPage(PDRectangle.A4);
//                        document.addPage(page);
//                        contentStream = new PDPageContentStream(document, page);
//                        yPosition = 750;
//                    }
//                    
//                    contentStream.beginText();
//                    contentStream.newLineAtOffset(margin, yPosition);
//                    
//                    for (String columnKey : columns.keySet()) {
//                        Object value = row.get(columnKey);
//                        String text = value != null ? value.toString() : "";
//                        contentStream.showText(text);
//                        contentStream.newLineAtOffset(cellWidth, 0);
//                    }
//                    
//                    contentStream.endText();
//                    yPosition -= tableHeight;
//                }
//            }
//            
//            document.save(filePath);
//            LogUtil.info("PDF report generated: " + filePath);
//        }
//        
//        return filePath;
//    }
//    
//    /**
//     * Generates an Excel report from a list of maps
//     * 
//     * @param data List of maps containing row data
//     * @param columns Map of column names to their display titles
//     * @param title Sheet title
//     * @param filename Output filename (without extension)
//     * @return Full path to the generated file
//     * @throws IOException If there's an error writing the file
//     */
//    public static String generateExcelReport(List<Map<String, Object>> data, Map<String, String> columns, 
//                                            String title, String filename) throws IOException {
//        initializeReportsDirectory();
//        
//        String filePath = REPORTS_DIRECTORY + File.separator + filename + ".xlsx";
//        
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet(title);
//            
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            int colIndex = 0;
//            for (String displayName : columns.values()) {
//                Cell cell = headerRow.createCell(colIndex++);
//                cell.setCellValue(displayName);
//            }
//            
//            // Create data rows
//            int rowIndex = 1;
//            for (Map<String, Object> row : data) {
//                Row dataRow = sheet.createRow(rowIndex++);
//                colIndex = 0;
//                
//                for (String columnKey : columns.keySet()) {
//                    Cell cell = dataRow.createCell(colIndex++);
//                    
//                    Object value = row.get(columnKey);
//                    if (value != null) {
//                        if (value instanceof String) {
//                            cell.setCellValue((String) value);
//                        } else if (value instanceof Number) {
//                            cell.setCellValue(((Number) value).doubleValue());
//                        } else if (value instanceof Boolean) {
//                            cell.setCellValue((Boolean) value);
//                        } else {
//                            cell.setCellValue(value.toString());
//                        }
//                    }
//                }
//            }
//            
//            // Auto-size columns
//            for (int col = 0; col < columns.size(); col++) {
//                sheet.autoSizeColumn(col);
//            }
//            
//            // Write to file
//            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
//                workbook.write(fileOut);
//            }
//            
//            LogUtil.info("Excel report generated: " + filePath);
//        }
//        
//        return filePath;
//    }
//}