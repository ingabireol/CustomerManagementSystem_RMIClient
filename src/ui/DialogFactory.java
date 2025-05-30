package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

/**
 * Factory for creating standardized dialogs in the application.
 * Ensures consistent dialog appearance and behavior.
 */
public class DialogFactory {
    
    /**
     * Creates a modal dialog with the specified content panel
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param contentPanel Panel to display in the dialog
     * @param width Preferred width
     * @param height Preferred height
     * @return The configured dialog
     */
    public static JDialog createDialog(Component parent, String title, JPanel contentPanel, int width, int height) {
        // Find the frame parent
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(parent);
        if (parentFrame == null) {
            parentFrame = JOptionPane.getRootFrame();
        }
        
        // Create the dialog
        JDialog dialog = new JDialog(parentFrame, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Set size and position
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        
        // Set the content panel
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        wrapperPanel.add(contentPanel, BorderLayout.CENTER);
        dialog.setContentPane(wrapperPanel);
        
        return dialog;
    }
    
    /**
     * Creates a form dialog for creating or editing an entity
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param formPanel The form panel
     * @param onSave Callback when form is saved
     * @param onCancel Callback when form is cancelled
     * @param width Preferred width
     * @param height Preferred height
     * @return The configured dialog
     */
    public static JDialog createFormDialog(Component parent, String title, JPanel formPanel, 
                                          Runnable onSave, Runnable onCancel, int width, int height) {
        // Create the dialog
        JDialog dialog = createDialog(parent, title, formPanel, width, height);
        
        // Add window listener for cancel action
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        });
        
        return dialog;
    }
    
    /**
     * Creates a confirmation dialog with custom styling
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param message Confirmation message
     * @param onConfirm Callback when confirmed
     * @return The configured dialog
     */
    public static JDialog createConfirmationDialog(Component parent, String title, String message, Runnable onConfirm) {
        // Create the content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Add the message
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(UIFactory.BODY_FONT);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton cancelButton = UIFactory.createSecondaryButton("Cancel");
        JButton confirmButton = UIFactory.createPrimaryButton("Confirm");
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create the dialog
        JDialog dialog = createDialog(parent, title, contentPanel, 400, 200);
        
        // Add button actions
        cancelButton.addActionListener(e -> dialog.dispose());
        
        confirmButton.addActionListener(e -> {
            dialog.dispose();
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        
        return dialog;
    }
    
    /**
     * Creates a warning confirmation dialog with custom styling
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param message Warning message
     * @param onConfirm Callback when confirmed
     * @return The configured dialog
     */
    public static JDialog createWarningDialog(Component parent, String title, String message, Runnable onConfirm) {
        // Create the content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Add the message with warning icon
        JPanel messagePanel = new JPanel(new BorderLayout(10, 0));
        messagePanel.setOpaque(false);
        
        // Would add a warning icon in practice
        // JLabel iconLabel = new JLabel(new ImageIcon("warning.png"));
        JLabel iconLabel = new JLabel("⚠️");  // Unicode warning symbol as fallback
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 32));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(UIFactory.BODY_FONT);
        
        messagePanel.add(iconLabel, BorderLayout.WEST);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(messagePanel, BorderLayout.CENTER);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton cancelButton = UIFactory.createSecondaryButton("Cancel");
        JButton confirmButton = UIFactory.createDangerButton("Delete");
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create the dialog
        JDialog dialog = createDialog(parent, title, contentPanel, 450, 200);
        
        // Add button actions
        cancelButton.addActionListener(e -> dialog.dispose());
        
        confirmButton.addActionListener(e -> {
            dialog.dispose();
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        
        return dialog;
    }
    
    /**
     * Creates a details dialog for viewing an entity's details
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param detailsPanel The panel with entity details
     * @param onEdit Callback when edit button is clicked (can be null)
     * @param width Preferred width
     * @param height Preferred height
     * @return The configured dialog
     */
    public static JDialog createDetailsDialog(Component parent, String title, JPanel detailsPanel, 
                                             Runnable onEdit, int width, int height) {
        // Create a wrapper panel with buttons
        JPanel wrapperPanel = new JPanel(new BorderLayout(0, 10));
        wrapperPanel.setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Add the details panel
        wrapperPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton closeButton = UIFactory.createSecondaryButton("Close");
        
        buttonPanel.add(closeButton);
        
        // Add edit button if callback provided
        if (onEdit != null) {
            JButton editButton = UIFactory.createWarningButton("Edit");
            buttonPanel.add(editButton, 0); // Add before close button
            
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Window window = SwingUtilities.getWindowAncestor(editButton);
                    if (window != null) {
                        window.dispose();
                    }
                    onEdit.run();
                }
            });
        }
        
        wrapperPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create the dialog
        JDialog dialog = createDialog(parent, title, wrapperPanel, width, height);
        
        // Add close button action
        closeButton.addActionListener(e -> dialog.dispose());
        
        return dialog;
    }
    
    /**
     * Creates a dialog for selecting items from a list
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param items Array of items to select from
     * @param onSelect Consumer for selected item
     * @param width Preferred width
     * @param height Preferred height
     * @param <T> Type of items
     * @return The configured dialog
     */
    public static <T> JDialog createSelectionDialog(Component parent, String title, T[] items, 
                                                  Consumer<T> onSelect, int width, int height) {
        // Create the content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(Color.WHITE);
        
        // Create list panel
        JList<T> list = new JList<>(items);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(UIFactory.BODY_FONT);
        
        JScrollPane scrollPane = UIFactory.createScrollPane(list);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton cancelButton = UIFactory.createSecondaryButton("Cancel");
        JButton selectButton = UIFactory.createPrimaryButton("Select");
        selectButton.setEnabled(false);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(selectButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create the dialog
        JDialog dialog = createDialog(parent, title, contentPanel, width, height);
        
        // Add selection listener
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectButton.setEnabled(list.getSelectedValue() != null);
            }
        });
        
        // Add button actions
        cancelButton.addActionListener(e -> dialog.dispose());
        
        selectButton.addActionListener(e -> {
            T selectedItem = list.getSelectedValue();
            dialog.dispose();
            if (selectedItem != null && onSelect != null) {
                onSelect.accept(selectedItem);
            }
        });
        
        // Add double-click selection
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    T selectedItem = list.getSelectedValue();
                    dialog.dispose();
                    if (selectedItem != null && onSelect != null) {
                        onSelect.accept(selectedItem);
                    }
                }
            }
        });
        
        return dialog;
    }
    
    /**
     * Creates an error dialog with custom styling
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param message Error message
     * @return The configured dialog
     */
    public static JDialog createErrorDialog(Component parent, String title, String message) {
        // Create the content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Add the message with error icon
        JPanel messagePanel = new JPanel(new BorderLayout(10, 0));
        messagePanel.setOpaque(false);
        
        // Would add an error icon in practice
        // JLabel iconLabel = new JLabel(new ImageIcon("error.png"));
        JLabel iconLabel = new JLabel("❌");  // Unicode error symbol as fallback
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 32));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setForeground(UIFactory.ERROR_COLOR);
        
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(UIFactory.BODY_FONT);
        
        messagePanel.add(iconLabel, BorderLayout.WEST);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(messagePanel, BorderLayout.CENTER);
        
        // Add OK button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton okButton = UIFactory.createPrimaryButton("OK");
        
        buttonPanel.add(okButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create the dialog
        JDialog dialog = createDialog(parent, title, contentPanel, 450, 200);
        
        // Add button action
        okButton.addActionListener(e -> dialog.dispose());
        
        return dialog;
    }
}