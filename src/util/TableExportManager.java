package util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for exporting JTable data to various formats (CSV, Excel, PDF)
 * without requiring external libraries. Provides beautiful formatting and styling.
 */
public class TableExportManager {
    
    private static final String COMPANY_NAME = "Business Management System";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Export formats enumeration
     */
    public enum ExportFormat {
        CSV("CSV Files", "csv"),
        EXCEL("Excel Files", "xls"),
        PDF("PDF Files", "pdf"),
        HTML("HTML Files", "html");
        
        private final String description;
        private final String extension;
        
        ExportFormat(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }
        
        public String getDescription() { return description; }
        public String getExtension() { return extension; }
    }
    
    /**
     * Export configuration class
     */
    public static class ExportConfig {
        private String title = "Data Export";
        private String subtitle = "";
        private boolean includeHeader = true;
        private boolean includeFooter = true;
        private boolean includeTimestamp = true;
        private String[] selectedColumns = null; // null means all columns
        private Component parentComponent = null;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        
        public boolean isIncludeHeader() { return includeHeader; }
        public void setIncludeHeader(boolean includeHeader) { this.includeHeader = includeHeader; }
        
        public boolean isIncludeFooter() { return includeFooter; }
        public void setIncludeFooter(boolean includeFooter) { this.includeFooter = includeFooter; }
        
        public boolean isIncludeTimestamp() { return includeTimestamp; }
        public void setIncludeTimestamp(boolean includeTimestamp) { this.includeTimestamp = includeTimestamp; }
        
        public String[] getSelectedColumns() { return selectedColumns; }
        public void setSelectedColumns(String[] selectedColumns) { this.selectedColumns = selectedColumns; }
        
        public Component getParentComponent() { return parentComponent; }
        public void setParentComponent(Component parentComponent) { this.parentComponent = parentComponent; }
    }
    
    /**
     * Main export method that shows a dialog and exports data
     */
    public static void exportTable(JTable table, String defaultTitle) {
        ExportConfig config = new ExportConfig();
        config.setTitle(defaultTitle);
        config.setParentComponent(SwingUtilities.getWindowAncestor(table));
        
        // Show export configuration dialog
        if (showExportDialog(config, table.getModel())) {
            // Show file chooser
            JFileChooser fileChooser = createFileChooser();
            
            if (fileChooser.showSaveDialog(config.getParentComponent()) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                ExportFormat format = getSelectedFormat(fileChooser);
                
                // Ensure file has correct extension
                if (!selectedFile.getName().toLowerCase().endsWith("." + format.getExtension())) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + "." + format.getExtension());
                }
                
                // Perform export
                try {
                    exportToFile(table.getModel(), selectedFile, format, config);
                    
                    // Show success message
                    JOptionPane.showMessageDialog(
                        config.getParentComponent(),
                        "Export completed successfully!\nFile saved to: " + selectedFile.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Ask if user wants to open the file
                    int result = JOptionPane.showConfirmDialog(
                        config.getParentComponent(),
                        "Would you like to open the exported file?",
                        "Open File",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (result == JOptionPane.YES_OPTION) {
                        openFile(selectedFile);
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        config.getParentComponent(),
                        "Error exporting data: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    
    /**
     * Creates a file chooser with multiple format filters
     */
    private static JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Table Data");
        
        // Add file filters for each export format
        for (ExportFormat format : ExportFormat.values()) {
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith("." + format.getExtension());
                }
                
                @Override
                public String getDescription() {
                    return format.getDescription() + " (*." + format.getExtension() + ")";
                }
            });
        }
        
        // Set default filter to CSV
        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[1]); // Skip "All files"
        
        return fileChooser;
    }
    
    /**
     * Gets the selected export format based on file chooser filter
     */
    private static ExportFormat getSelectedFormat(JFileChooser fileChooser) {
        String description = fileChooser.getFileFilter().getDescription();
        
        for (ExportFormat format : ExportFormat.values()) {
            if (description.contains(format.getDescription())) {
                return format;
            }
        }
        
        return ExportFormat.CSV; // Default
    }
    
    /**
     * Shows export configuration dialog
     */
    private static boolean showExportDialog(ExportConfig config, TableModel model) {
        JDialog dialog = new JDialog((Window) config.getParentComponent(), "Export Configuration", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(config.getParentComponent());
        dialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField titleField = new JTextField(config.getTitle(), 20);
        formPanel.add(titleField, gbc);
        
        // Subtitle field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Subtitle:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField subtitleField = new JTextField(config.getSubtitle(), 20);
        formPanel.add(subtitleField, gbc);
        
        // Checkboxes
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JCheckBox headerCheckBox = new JCheckBox("Include header information", config.isIncludeHeader());
        formPanel.add(headerCheckBox, gbc);
        
        gbc.gridy = 3;
        JCheckBox footerCheckBox = new JCheckBox("Include footer information", config.isIncludeFooter());
        formPanel.add(footerCheckBox, gbc);
        
        gbc.gridy = 4;
        JCheckBox timestampCheckBox = new JCheckBox("Include timestamp", config.isIncludeTimestamp());
        formPanel.add(timestampCheckBox, gbc);
        
        // Column selection
        gbc.gridy = 5;
        formPanel.add(new JLabel("Columns to export:"), gbc);
        
        gbc.gridy = 6; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        JList<String> columnList = new JList<>();
        String[] columnNames = new String[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            columnNames[i] = model.getColumnName(i);
        }
        columnList.setListData(columnNames);
        columnList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        columnList.setSelectedIndices(getAllIndices(columnNames.length)); // Select all by default
        
        JScrollPane scrollPane = new JScrollPane(columnList);
        scrollPane.setPreferredSize(new Dimension(200, 80));
        formPanel.add(scrollPane, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("Export");
        JButton cancelButton = new JButton("Cancel");
        
        final boolean[] result = {false};
        
        okButton.addActionListener(e -> {
            // Update config
            config.setTitle(titleField.getText());
            config.setSubtitle(subtitleField.getText());
            config.setIncludeHeader(headerCheckBox.isSelected());
            config.setIncludeFooter(footerCheckBox.isSelected());
            config.setIncludeTimestamp(timestampCheckBox.isSelected());
            
            // Get selected columns
            List<String> selectedColumns = columnList.getSelectedValuesList();
            if (selectedColumns.size() < columnNames.length) {
                config.setSelectedColumns(selectedColumns.toArray(new String[0]));
            }
            
            result[0] = true;
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
        return result[0];
    }
    
    /**
     * Helper method to get all indices
     */
    private static int[] getAllIndices(int count) {
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            indices[i] = i;
        }
        return indices;
    }
    
    /**
     * Main export method that delegates to specific format exporters
     */
    private static void exportToFile(TableModel model, File file, ExportFormat format, ExportConfig config) throws IOException {
        switch (format) {
            case CSV:
                exportToCSV(model, file, config);
                break;
            case EXCEL:
                exportToExcel(model, file, config);
                break;
            case PDF:
                exportToPDF(model, file, config);
                break;
            case HTML:
                exportToHTML(model, file, config);
                break;
        }
    }
    
    /**
     * Export to CSV format
     */
    private static void exportToCSV(TableModel model, File file, ExportConfig config) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header if enabled
            if (config.isIncludeHeader()) {
                writer.println("# " + config.getTitle());
                if (!config.getSubtitle().isEmpty()) {
                    writer.println("# " + config.getSubtitle());
                }
                writer.println("# " + COMPANY_NAME);
                if (config.isIncludeTimestamp()) {
                    writer.println("# Generated on: " + LocalDateTime.now().format(DATE_FORMATTER));
                }
                writer.println("#");
            }
            
            // Get column indices to export
            int[] columnIndices = getColumnIndices(model, config);
            
            // Write column headers
            for (int i = 0; i < columnIndices.length; i++) {
                if (i > 0) writer.print(",");
                writer.print("\"" + model.getColumnName(columnIndices[i]) + "\"");
            }
            writer.println();
            
            // Write data rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int i = 0; i < columnIndices.length; i++) {
                    if (i > 0) writer.print(",");
                    Object value = model.getValueAt(row, columnIndices[i]);
                    String stringValue = value != null ? value.toString() : "";
                    // Escape quotes and wrap in quotes
                    stringValue = stringValue.replace("\"", "\"\"");
                    writer.print("\"" + stringValue + "\"");
                }
                writer.println();
            }
            
            // Write footer if enabled
            if (config.isIncludeFooter()) {
                writer.println("#");
                writer.println("# Total rows: " + model.getRowCount());
                writer.println("# Export completed: " + LocalDateTime.now().format(DATE_FORMATTER));
            }
        }
    }
    
    /**
     * Export to Excel format (HTML table that Excel can open)
     */
    private static void exportToExcel(TableModel model, File file, ExportConfig config) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("<?xml version=\"1.0\"?>");
            writer.println("<?mso-application progid=\"Excel.Sheet\"?>");
            writer.println("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
            writer.println(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
            writer.println(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
            writer.println(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");
            
            writer.println("<DocumentProperties xmlns=\"urn:schemas-microsoft-com:office:office\">");
            writer.println("<Title>" + escapeXml(config.getTitle()) + "</Title>");
            writer.println("<Author>" + escapeXml(COMPANY_NAME) + "</Author>");
            writer.println("<Created>" + LocalDateTime.now().format(DATE_FORMATTER) + "</Created>");
            writer.println("</DocumentProperties>");
            
            // Styles
            writer.println("<Styles>");
            writer.println("<Style ss:ID=\"Header\">");
            writer.println("<Font ss:Bold=\"1\" ss:Size=\"12\"/>");
            writer.println("<Interior ss:Color=\"#4472C4\" ss:Pattern=\"Solid\"/>");
            writer.println("<Font ss:Color=\"#FFFFFF\"/>");
            writer.println("</Style>");
            writer.println("<Style ss:ID=\"Title\">");
            writer.println("<Font ss:Bold=\"1\" ss:Size=\"16\"/>");
            writer.println("<Alignment ss:Horizontal=\"Center\"/>");
            writer.println("</Style>");
            writer.println("<Style ss:ID=\"Data\">");
            writer.println("<Borders>");
            writer.println("<Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#D0D0D0\"/>");
            writer.println("</Borders>");
            writer.println("</Style>");
            writer.println("</Styles>");
            
            writer.println("<Worksheet ss:Name=\"" + escapeXml(config.getTitle()) + "\">");
            writer.println("<Table>");
            
            int currentRow = 1;
            
            // Title and header info
            if (config.isIncludeHeader()) {
                int[] columnIndices = getColumnIndices(model, config);
                
                writer.println("<Row ss:Index=\"" + currentRow + "\">");
                writer.println("<Cell ss:MergeAcross=\"" + (columnIndices.length - 1) + "\" ss:StyleID=\"Title\">");
                writer.println("<Data ss:Type=\"String\">" + escapeXml(config.getTitle()) + "</Data>");
                writer.println("</Cell>");
                writer.println("</Row>");
                currentRow++;
                
                if (!config.getSubtitle().isEmpty()) {
                    writer.println("<Row ss:Index=\"" + currentRow + "\">");
                    writer.println("<Cell ss:MergeAcross=\"" + (columnIndices.length - 1) + "\">");
                    writer.println("<Data ss:Type=\"String\">" + escapeXml(config.getSubtitle()) + "</Data>");
                    writer.println("</Cell>");
                    writer.println("</Row>");
                    currentRow++;
                }
                
                if (config.isIncludeTimestamp()) {
                    writer.println("<Row ss:Index=\"" + currentRow + "\">");
                    writer.println("<Cell ss:MergeAcross=\"" + (columnIndices.length - 1) + "\">");
                    writer.println("<Data ss:Type=\"String\">Generated: " + LocalDateTime.now().format(DATE_FORMATTER) + "</Data>");
                    writer.println("</Cell>");
                    writer.println("</Row>");
                    currentRow++;
                }
                
                // Empty row
                currentRow++;
            }
            
            // Column headers
            int[] columnIndices = getColumnIndices(model, config);
            writer.println("<Row ss:Index=\"" + currentRow + "\">");
            for (int colIndex : columnIndices) {
                writer.println("<Cell ss:StyleID=\"Header\">");
                writer.println("<Data ss:Type=\"String\">" + escapeXml(model.getColumnName(colIndex)) + "</Data>");
                writer.println("</Cell>");
            }
            writer.println("</Row>");
            currentRow++;
            
            // Data rows
            for (int row = 0; row < model.getRowCount(); row++) {
                writer.println("<Row ss:Index=\"" + currentRow + "\">");
                for (int colIndex : columnIndices) {
                    Object value = model.getValueAt(row, colIndex);
                    String stringValue = value != null ? value.toString() : "";
                    String dataType = "String";
                    
                    // Try to determine data type
                    if (value instanceof Number) {
                        dataType = "Number";
                    }
                    
                    writer.println("<Cell ss:StyleID=\"Data\">");
                    writer.println("<Data ss:Type=\"" + dataType + "\">" + escapeXml(stringValue) + "</Data>");
                    writer.println("</Cell>");
                }
                writer.println("</Row>");
                currentRow++;
            }
            
            writer.println("</Table>");
            writer.println("</Worksheet>");
            writer.println("</Workbook>");
        }
    }
    
    /**
     * Export to PDF format (HTML that can be printed/saved as PDF)
     */
    private static void exportToPDF(TableModel model, File file, ExportConfig config) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset=\"UTF-8\">");
            writer.println("<title>" + escapeHtml(config.getTitle()) + "</title>");
            writer.println("<style>");
            writer.println("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; }");
            writer.println(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #1976D2; padding-bottom: 10px; }");
            writer.println(".title { font-size: 24px; font-weight: bold; color: #1976D2; margin-bottom: 5px; }");
            writer.println(".subtitle { font-size: 16px; color: #666; margin-bottom: 5px; }");
            writer.println(".info { font-size: 12px; color: #999; }");
            writer.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            writer.println("th { background-color: #1976D2; color: white; padding: 12px 8px; text-align: left; font-weight: bold; }");
            writer.println("td { padding: 8px; border-bottom: 1px solid #ddd; }");
            writer.println("tr:nth-child(even) { background-color: #f9f9f9; }");
            writer.println("tr:hover { background-color: #f5f5f5; }");
            writer.println(".footer { margin-top: 30px; border-top: 1px solid #ddd; padding-top: 10px; font-size: 12px; color: #666; }");
            writer.println("@media print { body { margin: 0; } .no-print { display: none; } }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            // Header
            if (config.isIncludeHeader()) {
                writer.println("<div class=\"header\">");
                writer.println("<div class=\"title\">" + escapeHtml(config.getTitle()) + "</div>");
                if (!config.getSubtitle().isEmpty()) {
                    writer.println("<div class=\"subtitle\">" + escapeHtml(config.getSubtitle()) + "</div>");
                }
                writer.println("<div class=\"info\">" + escapeHtml(COMPANY_NAME));
                if (config.isIncludeTimestamp()) {
                    writer.println(" - Generated on " + LocalDateTime.now().format(DATE_FORMATTER));
                }
                writer.println("</div>");
                writer.println("</div>");
            }
            
            // Table
            writer.println("<table>");
            
            // Column headers
            int[] columnIndices = getColumnIndices(model, config);
            writer.println("<thead>");
            writer.println("<tr>");
            for (int colIndex : columnIndices) {
                writer.println("<th>" + escapeHtml(model.getColumnName(colIndex)) + "</th>");
            }
            writer.println("</tr>");
            writer.println("</thead>");
            
            // Data rows
            writer.println("<tbody>");
            for (int row = 0; row < model.getRowCount(); row++) {
                writer.println("<tr>");
                for (int colIndex : columnIndices) {
                    Object value = model.getValueAt(row, colIndex);
                    String stringValue = value != null ? value.toString() : "";
                    writer.println("<td>" + escapeHtml(stringValue) + "</td>");
                }
                writer.println("</tr>");
            }
            writer.println("</tbody>");
            writer.println("</table>");
            
            // Footer
            if (config.isIncludeFooter()) {
                writer.println("<div class=\"footer\">");
                writer.println("<strong>Summary:</strong> " + model.getRowCount() + " records exported");
                if (config.isIncludeTimestamp()) {
                    writer.println(" | Export completed: " + LocalDateTime.now().format(DATE_FORMATTER));
                }
                writer.println("</div>");
            }
            
            // Print button (will not show when printed)
            writer.println("<script>");
            writer.println("function printReport() { window.print(); }");
            writer.println("</script>");
            writer.println("<div class=\"no-print\" style=\"margin-top: 20px; text-align: center;\">");
            writer.println("<button onclick=\"printReport()\" style=\"padding: 10px 20px; background-color: #1976D2; color: white; border: none; border-radius: 4px; cursor: pointer;\">Print / Save as PDF</button>");
            writer.println("</div>");
            
            writer.println("</body>");
            writer.println("</html>");
        }
    }
    
    /**
     * Export to HTML format
     */
    private static void exportToHTML(TableModel model, File file, ExportConfig config) throws IOException {
        exportToPDF(model, file, config); // Same as PDF but without print optimization
    }
    
    /**
     * Get column indices to export based on configuration
     */
    private static int[] getColumnIndices(TableModel model, ExportConfig config) {
        if (config.getSelectedColumns() == null) {
            // Return all columns
            int[] indices = new int[model.getColumnCount()];
            for (int i = 0; i < model.getColumnCount(); i++) {
                indices[i] = i;
            }
            return indices;
        } else {
            // Return selected columns
            List<Integer> indices = new ArrayList<>();
            for (String columnName : config.getSelectedColumns()) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    if (model.getColumnName(i).equals(columnName)) {
                        indices.add(i);
                        break;
                    }
                }
            }
            return indices.stream().mapToInt(i -> i).toArray();
        }
    }
    
    /**
     * Escape XML special characters
     */
    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
    
    /**
     * Escape HTML special characters
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;");
    }
    
    /**
     * Open file with system default application
     */
    private static void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            // Silently fail - not critical
        }
    }
    
    /**
     * Quick export method for CSV (no dialog)
     */
    public static void quickExportCSV(JTable table, String title, File file) throws IOException {
        ExportConfig config = new ExportConfig();
        config.setTitle(title);
        exportToCSV(table.getModel(), file, config);
    }
    
    /**
     * Quick export method for Excel (no dialog)
     */
    public static void quickExportExcel(JTable table, String title, File file) throws IOException {
        ExportConfig config = new ExportConfig();
        config.setTitle(title);
        exportToExcel(table.getModel(), file, config);
    }
    
    /**
     * Quick export method for PDF/HTML (no dialog)
     */
    public static void quickExportPDF(JTable table, String title, File file) throws IOException {
        ExportConfig config = new ExportConfig();
        config.setTitle(title);
        exportToPDF(table.getModel(), file, config);
    }
}