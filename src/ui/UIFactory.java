package ui;

import util.TableExportManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Enhanced Factory class for creating standardized UI components with export functionality.
 * This ensures a cohesive look and feel across the application with modern export capabilities.
 */
public class UIFactory {
    // Color palette
    public static final Color PRIMARY_COLOR = new Color(0x1976D2);
    public static final Color SECONDARY_COLOR = new Color(0xFF5722);
    public static final Color BACKGROUND_COLOR = new Color(0xF5F5F5);
    public static final Color DARK_GRAY = new Color(0x424242);
    public static final Color MEDIUM_GRAY = new Color(0x9E9E9E);
    public static final Color LIGHT_GRAY = new Color(0xF5F5F5);
    public static final Color SUCCESS_COLOR = new Color(0x4CAF50);
    public static final Color WARNING_COLOR = new Color(0xFFC107);
    public static final Color ERROR_COLOR = new Color(0xF44336);
    
    // Typography
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    
    /**
     * Creates a primary action button with standard styling
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BODY_FONT);
        return button;
    }
    
    /**
     * Creates a secondary action button with standard styling
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(LIGHT_GRAY);
        button.setForeground(DARK_GRAY);
        button.setFocusPainted(false);
        button.setFont(BODY_FONT);
        return button;
    }
    
    /**
     * Creates a warning/caution button with standard styling
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createWarningButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(WARNING_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BODY_FONT);
        return button;
    }
    
    /**
     * Creates a danger/delete button with standard styling
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ERROR_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BODY_FONT);
        return button;
    }
    
    /**
     * Creates a success/confirm button with standard styling
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createSuccessButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BODY_FONT);
        return button;
    }
    
    /**
     * Creates an export button with dropdown menu for different formats
     * 
     * @param table The table to export
     * @param exportTitle Default title for exports
     * @return Export button with dropdown menu
     */
    public static JButton createExportButton(JTable table, String exportTitle) {
        JButton exportButton = createSecondaryButton("Export ▼");
        exportButton.setToolTipText("Export table data to various formats");
        
        // Create popup menu for export options
        JPopupMenu exportMenu = new JPopupMenu();
        
        // CSV export option
        JMenuItem csvItem = new JMenuItem("Export to CSV");
        csvItem.setIcon(createColoredIcon("📄", SUCCESS_COLOR));
        csvItem.addActionListener(e -> TableExportManager.exportTable(table, exportTitle + " - CSV Export"));
        exportMenu.add(csvItem);
        
        // Excel export option
        JMenuItem excelItem = new JMenuItem("Export to Excel");
        excelItem.setIcon(createColoredIcon("📊", new Color(0x107C41))); // Excel green
        excelItem.addActionListener(e -> TableExportManager.exportTable(table, exportTitle + " - Excel Export"));
        exportMenu.add(excelItem);
        
        // PDF export option
        JMenuItem pdfItem = new JMenuItem("Export to PDF");
        pdfItem.setIcon(createColoredIcon("📄", ERROR_COLOR));
        pdfItem.addActionListener(e -> TableExportManager.exportTable(table, exportTitle + " - PDF Report"));
        exportMenu.add(pdfItem);
        
        // HTML export option
        JMenuItem htmlItem = new JMenuItem("Export to HTML");
        htmlItem.setIcon(createColoredIcon("🌐", WARNING_COLOR));
        htmlItem.addActionListener(e -> TableExportManager.exportTable(table, exportTitle + " - HTML Report"));
        exportMenu.add(htmlItem);
        
        exportMenu.addSeparator();
        
        // Quick export options (no dialog)
        JMenuItem quickCsvItem = new JMenuItem("Quick CSV Export");
        quickCsvItem.setFont(SMALL_FONT);
        quickCsvItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(exportTitle.replaceAll("[^a-zA-Z0-9]", "_") + ".csv"));
            if (fileChooser.showSaveDialog(exportButton) == JFileChooser.APPROVE_OPTION) {
                try {
                    TableExportManager.quickExportCSV(table, exportTitle, fileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(exportButton, "CSV export completed successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(exportButton, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        exportMenu.add(quickCsvItem);
        
        // Add click listener to show popup menu
        exportButton.addActionListener(e -> {
            exportMenu.show(exportButton, 0, exportButton.getHeight());
        });
        
        return exportButton;
    }
    
    /**
     * Creates a simple colored icon (emoji-based)
     */
    private static Icon createColoredIcon(String emoji, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                g2.drawString(emoji, x, y + 12);
                g2.dispose();
            }
            
            @Override
            public int getIconWidth() { return 16; }
            
            @Override
            public int getIconHeight() { return 16; }
        };
    }
    
    /**
     * Creates a standard panel with white background and padding
     * 
     * @return Styled JPanel
     */
    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }
    
    /**
     * Creates a card-style panel with drop shadow effect
     * 
     * @param title Optional card title (can be null)
     * @return Styled JPanel with card appearance
     */
    public static JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        
        // Add title if provided
        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(HEADER_FONT);
            card.add(titleLabel, BorderLayout.NORTH);
        }
        
        // Add subtle border and padding
        Border roundedBorder = new EmptyBorder(15, 15, 15, 15);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            roundedBorder
        ));
        
        return card;
    }
    
    /**
     * Creates a metric card for dashboard display
     * 
     * @param title Card title
     * @param value Main value to display
     * @param change Change indicator text
     * @param indicatorColor Color for change indicator
     * @return Styled card panel
     */
    public static JPanel createMetricCard(String title, String value, String change, Color indicatorColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        
        // Add a subtle border and padding
        Border roundedBorder = new EmptyBorder(15, 15, 15, 15);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            roundedBorder
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BODY_FONT);
        titleLabel.setForeground(MEDIUM_GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(BODY_FONT);
        changeLabel.setForeground(indicatorColor);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(changeLabel, BorderLayout.WEST);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    /**
     * Creates a header panel for module pages with title, export button, and action buttons
     * 
     * @param title Module title
     * @param table Table to export (can be null to disable export)
     * @return Header panel with title, export, and space for actions
     */
    public static JPanel createModuleHeaderPanel(String title, JTable table) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create an actions panel (right side) with export button
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setOpaque(false);
        
        // Add export button if table is provided
        if (table != null) {
            JButton exportButton = createExportButton(table, title);
            actionsPanel.add(exportButton);
        }
        
        headerPanel.add(actionsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Creates a header panel for module pages with title and action buttons (no export)
     * 
     * @param title Module title
     * @return Header panel with title and space for actions
     */
    public static JPanel createModuleHeaderPanel(String title) {
        return createModuleHeaderPanel(title, null);
    }
    
    /**
     * Creates a styled table with consistent appearance and export functionality
     * 
     * @param model Table data model
     * @return Styled JTable
     */
    public static JTable createStyledTable(TableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xE3F2FD));
        table.setSelectionForeground(DARK_GRAY);
        table.getTableHeader().setBackground(LIGHT_GRAY);
        table.getTableHeader().setFont(HEADER_FONT);
        table.setFont(BODY_FONT);
        
        // Add context menu for quick export
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem exportCsvItem = new JMenuItem("Export to CSV");
        exportCsvItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("table_export.csv"));
            if (fileChooser.showSaveDialog(table) == JFileChooser.APPROVE_OPTION) {
                try {
                    TableExportManager.quickExportCSV(table, "Table Export", fileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(table, "Export completed successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(table, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JMenuItem exportExcelItem = new JMenuItem("Export to Excel");
        exportExcelItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("table_export.xls"));
            if (fileChooser.showSaveDialog(table) == JFileChooser.APPROVE_OPTION) {
                try {
                    TableExportManager.quickExportExcel(table, "Table Export", fileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(table, "Export completed successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(table, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        contextMenu.add(exportCsvItem);
        contextMenu.add(exportExcelItem);
        contextMenu.addSeparator();
        
        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.addActionListener(e -> table.selectAll());
        contextMenu.add(selectAllItem);
        
        JMenuItem copyItem = new JMenuItem("Copy Selection");
        copyItem.addActionListener(e -> {
            // Copy selected cells to clipboard
            StringBuilder sb = new StringBuilder();
            int[] selectedRows = table.getSelectedRows();
            int[] selectedCols = table.getSelectedColumns();
            
            for (int i = 0; i < selectedRows.length; i++) {
                for (int j = 0; j < selectedCols.length; j++) {
                    if (j > 0) sb.append("\t");
                    Object value = table.getValueAt(selectedRows[i], selectedCols[j]);
                    sb.append(value != null ? value.toString() : "");
                }
                if (i < selectedRows.length - 1) sb.append("\n");
            }
            
            java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(sb.toString());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
        });
        contextMenu.add(copyItem);
        
        table.setComponentPopupMenu(contextMenu);
        
        return table;
    }
    
    /**
     * Creates a styled scroll pane for tables and other components
     * 
     * @param view The component to scroll
     * @return Styled JScrollPane
     */
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }
    
    /**
     * Creates an enhanced table panel with export functionality
     * 
     * @param table The table to wrap
     * @param title Title for the table panel
     * @return Panel containing table with export controls
     */
    public static JPanel createTablePanelWithExport(JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        
        // Create header with export button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(LIGHT_GRAY);
        headerPanel.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Export controls on the right
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        exportPanel.setOpaque(false);
        
        JButton exportButton = createExportButton(table, title);
        exportButton.setPreferredSize(new Dimension(100, 25));
        exportPanel.add(exportButton);
        
        // Add row count label
        JLabel rowCountLabel = new JLabel(table.getRowCount() + " rows");
        rowCountLabel.setFont(SMALL_FONT);
        rowCountLabel.setForeground(MEDIUM_GRAY);
        exportPanel.add(rowCountLabel);
        
        headerPanel.add(exportPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Add the table
        JScrollPane scrollPane = createScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates a form field with label and text field
     * 
     * @param labelText Label text
     * @param fieldWidth Preferred width for text field
     * @return Panel containing the labeled field
     */
    public static JPanel createFormField(String labelText, int fieldWidth) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(BODY_FONT);
        
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(fieldWidth, 30));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates a search field with placeholder text
     * 
     * @param placeholderText Text to display when field is empty
     * @param width Preferred width
     * @return Styled search field
     */
    public static JTextField createSearchField(String placeholderText, int width) {
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(width, 30));
        
        // In Java 8, there's no built-in placeholder support, so we'd need a custom implementation
        // For this example, we'll use client properties which work with some look and feels
        searchField.putClientProperty("JTextField.placeholderText", placeholderText);
        
        return searchField;
    }
    
    /**
     * Creates a styled combo box
     * 
     * @param items Items to display in the combo box
     * @return Styled JComboBox
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(BODY_FONT);
        comboBox.setBackground(Color.WHITE);
        return comboBox;
    }
    
    /**
     * Creates a styled date picker (using a text field with formatting)
     * 
     * @param placeholderText Placeholder text
     * @return Text field configured for date input
     */
    public static JTextField createDateField(String placeholderText) {
        JTextField dateField = new JTextField();
        dateField.setPreferredSize(new Dimension(120, 30));
        dateField.putClientProperty("JTextField.placeholderText", placeholderText);
        
        // In a real implementation, we would use a date picker component
        // or add a formatter to the text field
        
        return dateField;
    }
    
    /**
     * Creates a form section panel with title
     * 
     * @param title Section title
     * @return Panel for organizing form sections
     */
    public static JPanel createFormSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MEDIUM_GRAY, 1, true),
                title,
                0,
                0,
                HEADER_FONT
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        return panel;
    }
    
    /**
     * Creates a standard form button panel with save and cancel buttons
     * 
     * @return Panel with aligned buttons
     */
    public static JPanel createFormButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        
        JButton cancelButton = createSecondaryButton("Cancel");
        JButton saveButton = createPrimaryButton("Save");
        
        panel.add(cancelButton);
        panel.add(saveButton);
        
        return panel;
    }
    
    /**
     * Creates a dialog with standardized styling
     * 
     * @param parent Parent component
     * @param title Dialog title
     * @param modal Whether dialog is modal
     * @return Styled JDialog
     */
    public static JDialog createDialog(Component parent, String title, boolean modal) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, modal);
        dialog.setBackground(Color.WHITE);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        
        dialog.setContentPane(contentPanel);
        
        return dialog;
    }
    
    /**
     * Applies standard styling to a tabbed pane
     * 
     * @param tabbedPane The tabbed pane to style
     */
    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setFont(BODY_FONT);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(DARK_GRAY);
    }
    
    /**
     * Creates a navigation button for the side panel
     * 
     * @param text Button text
     * @param isSelected Whether the button is selected
     * @return Styled navigation button
     */
    public static JButton createNavButton(String text, boolean isSelected) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        
        if (isSelected) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(DARK_GRAY);
            button.setForeground(Color.WHITE);
            
            // Add hover effect
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x555555));
                }
                
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (!button.isSelected()) {
                        button.setBackground(DARK_GRAY);
                    }
                }
            });
        }
        
        return button;
    }
    
    /**
     * Creates a toolbar with export functionality for list views
     * 
     * @param table The table associated with this toolbar
     * @param title Title for exports
     * @return Toolbar panel with export and other common actions
     */
    public static JPanel createListViewToolbar(JTable table, String title) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolbar.setBackground(LIGHT_GRAY);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Export button
        JButton exportButton = createExportButton(table, title);
        exportButton.setPreferredSize(new Dimension(90, 28));
        toolbar.add(exportButton);
        
        toolbar.add(Box.createHorizontalStrut(10));
        
        // Refresh button
        JButton refreshButton = createSecondaryButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(80, 28));
        toolbar.add(refreshButton);
        
        // Print button
        JButton printButton = createSecondaryButton("Print");
        printButton.setPreferredSize(new Dimension(70, 28));
        printButton.addActionListener(e -> {
            try {
                table.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(table, "Print failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        toolbar.add(printButton);
        
        toolbar.add(Box.createHorizontalGlue());
        
        // Row count indicator
        JLabel rowCountLabel = new JLabel(table.getRowCount() + " rows");
        rowCountLabel.setFont(SMALL_FONT);
        rowCountLabel.setForeground(MEDIUM_GRAY);
        toolbar.add(rowCountLabel);
        
        return toolbar;
    }
}