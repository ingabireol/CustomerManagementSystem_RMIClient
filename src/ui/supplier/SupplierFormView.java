package ui.supplier;

import model.Supplier;
import dao.SupplierDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ui.UIFactory;

/**
 * Form view for creating and editing supplier records.
 * Provides fields for all supplier properties and validates input.
 */
public class SupplierFormView extends JPanel {
    // Form components
    private JTextField supplierCodeField;
    private JTextField nameField;
    private JTextField contactPersonField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    
    // Validation labels
    private JLabel supplierCodeValidationLabel;
    private JLabel nameValidationLabel;
    private JLabel emailValidationLabel;
    
    // Buttons
    private JButton saveButton;
    private JButton cancelButton;
    
    // Mode flag
    private boolean editMode = false;
    private Supplier currentSupplier;
    
    // Data access
    private SupplierDao supplierDao;
    
    // Callback for form submission
    private FormSubmissionCallback callback;
    
    /**
     * Interface for form submission callback
     */
    public interface FormSubmissionCallback {
        void onSave(Supplier supplier);
        void onCancel();
    }
    
    /**
     * Constructor for create mode
     * 
     * @param callback Callback for form actions
     */
    public SupplierFormView(FormSubmissionCallback callback) {
        this.callback = callback;
        this.supplierDao = new SupplierDao();
        this.editMode = false;
        initializeUI();
    }
    
    /**
     * Constructor for edit mode
     * 
     * @param supplier Supplier to edit
     * @param callback Callback for form actions
     */
    public SupplierFormView(Supplier supplier, FormSubmissionCallback callback) {
        this.callback = callback;
        this.supplierDao = new SupplierDao();
        this.editMode = true;
        this.currentSupplier = supplier;
        initializeUI();
        populateFields(supplier);
    }
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 20));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the form header
        JPanel headerPanel = UIFactory.createModuleHeaderPanel(editMode ? "Edit Supplier" : "New Supplier");
        add(headerPanel, BorderLayout.NORTH);
        
        // Create the main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Create form sections
        formPanel.add(createBasicInfoSection());
        formPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacing
        formPanel.add(createContactInfoSection());
        
        // Add the form to a scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create the button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        cancelButton = UIFactory.createSecondaryButton("Cancel");
        saveButton = UIFactory.createPrimaryButton(editMode ? "Update" : "Save");
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Register button actions
        registerActions();
    }
    
    private JPanel createBasicInfoSection() {
        JPanel sectionPanel = UIFactory.createFormSection("Basic Information");
        
        // Use GridBagLayout for form layout
        sectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Supplier Code field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        JLabel supplierCodeLabel = new JLabel("Supplier Code:");
        sectionPanel.add(supplierCodeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        supplierCodeField = new JTextField();
        supplierCodeField.setEnabled(!editMode); // Disable in edit mode
        sectionPanel.add(supplierCodeField, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        supplierCodeValidationLabel = new JLabel("");
        supplierCodeValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        supplierCodeValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(supplierCodeValidationLabel, gbc);
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        JLabel nameLabel = new JLabel("Name:");
        sectionPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        nameField = new JTextField();
        sectionPanel.add(nameField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        nameValidationLabel = new JLabel("");
        nameValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        nameValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(nameValidationLabel, gbc);
        
        return sectionPanel;
    }
    
    private JPanel createContactInfoSection() {
        JPanel sectionPanel = UIFactory.createFormSection("Contact Information");
        
        // Use GridBagLayout for form layout
        sectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Contact Person field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        JLabel contactPersonLabel = new JLabel("Contact Person:");
        sectionPanel.add(contactPersonLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        contactPersonField = new JTextField();
        sectionPanel.add(contactPersonField, gbc);
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        JLabel emailLabel = new JLabel("Email:");
        sectionPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        emailField = new JTextField();
        sectionPanel.add(emailField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        emailValidationLabel = new JLabel("");
        emailValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        emailValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(emailValidationLabel, gbc);
        
        // Phone field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.15;
        JLabel phoneLabel = new JLabel("Phone:");
        sectionPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        phoneField = new JTextField();
        sectionPanel.add(phoneField, gbc);
        
        // Address field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.15;
        JLabel addressLabel = new JLabel("Address:");
        sectionPanel.add(addressLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.85;
        addressArea = new JTextArea(5, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        addressScrollPane.setBorder(BorderFactory.createLineBorder(UIFactory.MEDIUM_GRAY));
        sectionPanel.add(addressScrollPane, gbc);
        
        return sectionPanel;
    }
    
    private void registerActions() {
        // Cancel button action
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null) {
                    callback.onCancel();
                }
            }
        });
        
        // Save button action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    saveSupplier();
                }
            }
        });
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Reset validation messages
        supplierCodeValidationLabel.setText("");
        nameValidationLabel.setText("");
        emailValidationLabel.setText("");
        
        // Validate Supplier Code
        if (!editMode) { // Only validate in create mode
            if (supplierCodeField.getText().trim().isEmpty()) {
                supplierCodeValidationLabel.setText("Supplier code is required");
                isValid = false;
            } else if (!supplierCodeField.getText().matches("[A-Za-z0-9-]+")) {
                supplierCodeValidationLabel.setText("Only letters, numbers, and hyphens allowed");
                isValid = false;
            } else {
                // Check if code already exists
                Supplier existingSupplier = supplierDao.findSupplierByCode(supplierCodeField.getText().trim());
                if (existingSupplier != null) {
                    supplierCodeValidationLabel.setText("Supplier code already exists");
                    isValid = false;
                }
            }
        }
        
        // Validate Name
        if (nameField.getText().trim().isEmpty()) {
            nameValidationLabel.setText("Supplier name is required");
            isValid = false;
        }
        
        // Validate Email (optional but must be valid if provided)
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            emailValidationLabel.setText("Invalid email format");
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean isValidEmail(String email) {
        // Basic email validation using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    private void saveSupplier() {
        try {
            Supplier supplier;
            
            if (editMode && currentSupplier != null) {
                supplier = currentSupplier;
            } else {
                supplier = new Supplier();
                supplier.setSupplierCode(supplierCodeField.getText().trim());
            }
            
            supplier.setName(nameField.getText().trim());
            supplier.setContactPerson(contactPersonField.getText().trim());
            supplier.setEmail(emailField.getText().trim());
            supplier.setPhone(phoneField.getText().trim());
            supplier.setAddress(addressArea.getText().trim());
            
            int result;
            if (editMode) {
                result = supplierDao.updateSupplier(supplier);
            } else {
                result = supplierDao.createSupplier(supplier);
            }
            
            if (result > 0) {
                if (callback != null) {
                    callback.onSave(supplier);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to " + (editMode ? "update" : "create") + " supplier.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error saving supplier: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void populateFields(Supplier supplier) {
        if (supplier != null) {
            supplierCodeField.setText(supplier.getSupplierCode());
            nameField.setText(supplier.getName());
            contactPersonField.setText(supplier.getContactPerson());
            emailField.setText(supplier.getEmail());
            phoneField.setText(supplier.getPhone());
            addressArea.setText(supplier.getAddress());
        }
    }
}