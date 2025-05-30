package ui.supplier;

import model.Supplier;
import util.LogUtil;
import util.RMIConnectionManager;
import service.SupplierService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ui.UIFactory;

/**
 * RMI-based form view for creating and editing supplier records.
 * Provides fields for all supplier properties and validates input using remote services.
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
    
    // RMI Services
    private SupplierService supplierService;
    
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
        this.editMode = false;
        
        // Initialize RMI service
        initializeRMIServices();
        initializeUI();
        addRealTimeValidation();
    }
    
    /**
     * Constructor for edit mode
     * 
     * @param supplier Supplier to edit
     * @param callback Callback for form actions
     */
    public SupplierFormView(Supplier supplier, FormSubmissionCallback callback) {
        this.callback = callback;
        this.editMode = true;
        this.currentSupplier = supplier;
        
        // Initialize RMI service
        initializeRMIServices();
        initializeUI();
        populateFields(supplier);
        addRealTimeValidation();
    }
    
    /**
     * Initializes RMI service connections
     */
    private void initializeRMIServices() {
        try {
            LogUtil.info("Initializing SupplierService for form view");
            supplierService = RMIConnectionManager.getSupplierService();
            
            if (supplierService == null) {
                LogUtil.error("Failed to get SupplierService from RMI");
                showConnectionError();
            } else {
                LogUtil.info("Successfully connected to SupplierService");
            }
        } catch (Exception e) {
            LogUtil.error("Error connecting to SupplierService", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error message
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            this,
            "Failed to connect to the server.\nSome features may not work properly.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
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
            String supplierCode = supplierCodeField.getText().trim();
            if (supplierCode.isEmpty()) {
                supplierCodeValidationLabel.setText("Supplier code is required");
                isValid = false;
            } else if (!supplierCode.matches("[A-Za-z0-9-]+")) {
                supplierCodeValidationLabel.setText("Only letters, numbers, and hyphens allowed");
                isValid = false;
            } else {
                // Check if code already exists using RMI service
                try {
                    if (supplierService != null && supplierService.supplierCodeExists(supplierCode)) {
                        supplierCodeValidationLabel.setText("Supplier code already exists");
                        isValid = false;
                    }
                } catch (Exception e) {
                    LogUtil.error("Error checking supplier code existence", e);
                    supplierCodeValidationLabel.setText("Error validating supplier code");
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
        if (!email.isEmpty()) {
            if (!isValidEmail(email)) {
                emailValidationLabel.setText("Invalid email format");
                isValid = false;
            } else {
                // Check if email already exists (excluding current supplier in edit mode)
                try {
                    if (supplierService != null && supplierService.emailExists(email)) {
                        // In edit mode, allow the current supplier's email
                        if (!editMode || currentSupplier == null || !email.equals(currentSupplier.getEmail())) {
                            emailValidationLabel.setText("Email already exists");
                            isValid = false;
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("Error checking email existence", e);
                    emailValidationLabel.setText("Error validating email");
                    isValid = false;
                }
            }
        }
        
        return isValid;
    }
    
    private boolean isValidEmail(String email) {
        // Basic email validation using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    private void saveSupplier() {
        // Disable save button and show loading state
        saveButton.setEnabled(false);
        saveButton.setText(editMode ? "Updating..." : "Saving...");
        
        SwingWorker<Supplier, Void> worker = new SwingWorker<Supplier, Void>() {
            @Override
            protected Supplier doInBackground() throws Exception {
                if (supplierService == null) {
                    throw new Exception("Server connection not available");
                }
                
                Supplier supplier;
                
                if (editMode && currentSupplier != null) {
                    supplier = currentSupplier;
                } else {
                    supplier = new Supplier();
                    supplier.setSupplierCode(supplierCodeField.getText().trim());
                }
                
                // Set supplier properties from form fields
                supplier.setName(nameField.getText().trim());
                supplier.setContactPerson(contactPersonField.getText().trim());
                supplier.setEmail(emailField.getText().trim());
                supplier.setPhone(phoneField.getText().trim());
                supplier.setAddress(addressArea.getText().trim());
                
                // Save using RMI service
                if (editMode) {
                    LogUtil.info("Updating supplier via RMI: " + supplier.getName());
                    return supplierService.updateSupplier(supplier);
                } else {
                    LogUtil.info("Creating new supplier via RMI: " + supplier.getName());
                    return supplierService.createSupplier(supplier);
                }
            }
            
            @Override
            protected void done() {
                // Reset button state
                saveButton.setEnabled(true);
                saveButton.setText(editMode ? "Update" : "Save");
                
                try {
                    Supplier savedSupplier = get();
                    
                    if (savedSupplier != null) {
                        LogUtil.info("Supplier " + (editMode ? "updated" : "created") + " successfully");
                        
                        // Update current supplier if in edit mode
                        if (editMode) {
                            currentSupplier = savedSupplier;
                        }
                        
                        // Show success message
                        JOptionPane.showMessageDialog(SupplierFormView.this,
                            "Supplier " + (editMode ? "updated" : "created") + " successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        if (callback != null) {
                            callback.onSave(savedSupplier);
                        }
                    } else {
                        LogUtil.warn("Supplier save operation returned null");
                        JOptionPane.showMessageDialog(SupplierFormView.this,
                            "Failed to " + (editMode ? "update" : "create") + " supplier.\nPlease check the data and try again.",
                            "Save Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception ex) {
                    LogUtil.error("Error saving supplier via RMI", ex);
                    JOptionPane.showMessageDialog(SupplierFormView.this,
                        "Error saving supplier: " + ex.getMessage(),
                        "Server Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void populateFields(Supplier supplier) {
        if (supplier != null) {
            supplierCodeField.setText(supplier.getSupplierCode() != null ? supplier.getSupplierCode() : "");
            nameField.setText(supplier.getName() != null ? supplier.getName() : "");
            contactPersonField.setText(supplier.getContactPerson() != null ? supplier.getContactPerson() : "");
            emailField.setText(supplier.getEmail() != null ? supplier.getEmail() : "");
            phoneField.setText(supplier.getPhone() != null ? supplier.getPhone() : "");
            addressArea.setText(supplier.getAddress() != null ? supplier.getAddress() : "");
        }
    }
    
    /**
     * Validates a single field and shows/hides validation message
     * 
     * @param field The field to validate
     * @param validationLabel The label to show validation message
     * @param value The value to validate
     * @param fieldName The name of the field for error messages
     * @param isRequired Whether the field is required
     * @return true if valid
     */
    private boolean validateField(JTextField field, JLabel validationLabel, String value, String fieldName, boolean isRequired) {
        if (isRequired && value.trim().isEmpty()) {
            validationLabel.setText(fieldName + " is required");
            field.requestFocusInWindow();
            return false;
        }
        
        validationLabel.setText("");
        return true;
    }
    
    /**
     * Validates email field with real-time checking
     * 
     * @param email The email to validate
     * @return true if valid
     */
    private boolean validateEmailField(String email) {
        if (!email.isEmpty()) {
            if (!isValidEmail(email)) {
                emailValidationLabel.setText("Invalid email format");
                emailField.requestFocusInWindow();
                return false;
            }
            
            // Check for duplicate email using RMI (asynchronous)
            SwingWorker<Boolean, Void> emailCheckWorker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (supplierService != null) {
                        return supplierService.emailExists(email);
                    }
                    return false;
                }
                
                @Override
                protected void done() {
                    try {
                        boolean exists = get();
                        if (exists) {
                            // In edit mode, allow the current supplier's email
                            if (!editMode || currentSupplier == null || !email.equals(currentSupplier.getEmail())) {
                                emailValidationLabel.setText("Email already exists");
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.warn("Could not check email existence: " + e.getMessage());
                    }
                }
            };
            emailCheckWorker.execute();
        }
        
        emailValidationLabel.setText("");
        return true;
    }
    
    /**
     * Adds real-time validation to form fields
     */
    private void addRealTimeValidation() {
        // Supplier code validation (only in create mode)
        if (!editMode) {
            supplierCodeField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    String code = supplierCodeField.getText().trim();
                    if (!code.isEmpty()) {
                        if (!code.matches("[A-Za-z0-9-]+")) {
                            supplierCodeValidationLabel.setText("Only letters, numbers, and hyphens allowed");
                        } else {
                            // Check for duplicate code asynchronously
                            SwingWorker<Boolean, Void> codeCheckWorker = new SwingWorker<Boolean, Void>() {
                                @Override
                                protected Boolean doInBackground() throws Exception {
                                    if (supplierService != null) {
                                        return supplierService.supplierCodeExists(code);
                                    }
                                    return false;
                                }
                                
                                @Override
                                protected void done() {
                                    try {
                                        boolean exists = get();
                                        if (exists) {
                                            supplierCodeValidationLabel.setText("Supplier code already exists");
                                        } else {
                                            supplierCodeValidationLabel.setText("");
                                        }
                                    } catch (Exception ex) {
                                        LogUtil.warn("Could not check supplier code existence: " + ex.getMessage());
                                    }
                                }
                            };
                            codeCheckWorker.execute();
                        }
                    } else {
                        supplierCodeValidationLabel.setText("");
                    }
                }
            });
        }
        
        // Name field validation
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    nameValidationLabel.setText("Supplier name is required");
                } else {
                    nameValidationLabel.setText("");
                }
            }
        });
        
        // Email field validation
        emailField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String email = emailField.getText().trim();
                validateEmailField(email);
            }
        });
    }
    
    /**
     * Clears all form fields
     */
    public void clearForm() {
        supplierCodeField.setText("");
        nameField.setText("");
        contactPersonField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        
        // Clear validation messages
        clearValidationMessages();
    }
    
    /**
     * Clears all validation messages
     */
    public void clearValidationMessages() {
        supplierCodeValidationLabel.setText("");
        nameValidationLabel.setText("");
        emailValidationLabel.setText("");
    }
    
    /**
     * Gets form data as a Supplier object
     * 
     * @return Supplier object with form data
     */
    public Supplier getSupplierFromForm() {
        Supplier supplier = new Supplier();
        
        if (!editMode) {
            supplier.setSupplierCode(supplierCodeField.getText().trim());
        } else if (currentSupplier != null) {
            supplier.setId(currentSupplier.getId());
            supplier.setSupplierCode(currentSupplier.getSupplierCode());
        }
        
        supplier.setName(nameField.getText().trim());
        supplier.setContactPerson(contactPersonField.getText().trim());
        supplier.setEmail(emailField.getText().trim());
        supplier.setPhone(phoneField.getText().trim());
        supplier.setAddress(addressArea.getText().trim());
        
        return supplier;
    }
    
    /**
     * Sets form data from a Supplier object
     * 
     * @param supplier The supplier to load into the form
     */
    public void setSupplierToForm(Supplier supplier) {
        populateFields(supplier);
        this.currentSupplier = supplier;
        clearValidationMessages();
    }
    
    /**
     * Checks if the form has unsaved changes
     * 
     * @return true if there are unsaved changes
     */
    public boolean hasUnsavedChanges() {
        if (currentSupplier == null && !editMode) {
            // In create mode, check if any field has data
            return !supplierCodeField.getText().trim().isEmpty() ||
                   !nameField.getText().trim().isEmpty() ||
                   !contactPersonField.getText().trim().isEmpty() ||
                   !emailField.getText().trim().isEmpty() ||
                   !phoneField.getText().trim().isEmpty() ||
                   !addressArea.getText().trim().isEmpty();
        } else if (editMode && currentSupplier != null) {
            // In edit mode, check if data differs from original
            return !nameField.getText().trim().equals(currentSupplier.getName() != null ? currentSupplier.getName() : "") ||
                   !contactPersonField.getText().trim().equals(currentSupplier.getContactPerson() != null ? currentSupplier.getContactPerson() : "") ||
                   !emailField.getText().trim().equals(currentSupplier.getEmail() != null ? currentSupplier.getEmail() : "") ||
                   !phoneField.getText().trim().equals(currentSupplier.getPhone() != null ? currentSupplier.getPhone() : "") ||
                   !addressArea.getText().trim().equals(currentSupplier.getAddress() != null ? currentSupplier.getAddress() : "");
        }
        
        return false;
    }
    
    /**
     * Gets the current supplier being edited
     * 
     * @return The current supplier or null if in create mode
     */
    public Supplier getCurrentSupplier() {
        return currentSupplier;
    }
    
    /**
     * Checks if the form is in edit mode
     * 
     * @return true if editing an existing supplier
     */
    public boolean isEditMode() {
        return editMode;
    }
    
    /**
     * Sets the enabled state of the save button
     * 
     * @param enabled true to enable the save button
     */
    public void setSaveButtonEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
    }
    
    /**
     * Sets the enabled state of all form fields
     * 
     * @param enabled true to enable form fields
     */
    public void setFormEnabled(boolean enabled) {
        if (!editMode) {
            supplierCodeField.setEnabled(enabled);
        }
        nameField.setEnabled(enabled);
        contactPersonField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        phoneField.setEnabled(enabled);
        addressArea.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }
    
    /**
     * Refreshes RMI connection if needed
     */
    public void refreshConnection() {
        initializeRMIServices();
    }
    
    /**
     * Focuses on the first editable field
     */
    public void focusFirstField() {
        SwingUtilities.invokeLater(() -> {
            if (!editMode && supplierCodeField.isEnabled()) {
                supplierCodeField.requestFocusInWindow();
            } else {
                nameField.requestFocusInWindow();
            }
        });
    }
}