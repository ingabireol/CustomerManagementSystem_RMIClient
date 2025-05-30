package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Factory class for creating standardized UI components with consistent styling.
 * This ensures a cohesive look and feel across the application.
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
     * Creates a header panel for module pages with title and action buttons
     * 
     * @param title Module title
     * @return Header panel with title and space for actions
     */
    public static JPanel createModuleHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create an actions panel (right side)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setOpaque(false);
        headerPanel.add(actionsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Creates a styled table with consistent appearance
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
}