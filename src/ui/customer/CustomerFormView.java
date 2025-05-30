package ui.customer;

import model.Customer;
import service.CustomerService;
import util.ValidationUtil;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import ui.UIFactory;

/**
 * Form view for creating and editing customer records using RMI services.
 * Provides fields for all customer properties and validates input with remote validation.
 */
public class CustomerFormView extends JPanel {
    
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote service
    private CustomerService customerService;
    
    // Form components
    private JTextField customerIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    
    // Validation labels
    private JLabel customerIdValidationLabel;
    private JLabel firstNameValidationLabel;
    private JLabel lastNameValidationLabel;
    private JLabel emailValidationLabel;
    
    // Buttons
    private JButton saveButton;
    private JButton cancelButton;
    
    // Mode flag
    private boolean editMode = false;
    private Customer currentCustomer;
    
    // Callback for form submission
    private FormSubmissionCallback callback;
    
    // Connection status
    private boolean isConnected = false;
    
    /**
     * Interface for form submission callback
     */
    public interface FormSubmissionCallback {
        void onSave(Customer customer);
        void onCancel();
    }
    
    /**
     * Constructor for create mode
     * 
     * @param callback Callback for form actions
     */
    public CustomerFormView(FormSubmissionCallback callback) {
        this.callback = callback;
        this.editMode = false;
        
        // Initialize RMI connection
        initializeRMIConnection();
        
        // Initialize UI
        if (isConnected) {
            initializeUI();
        } else {
            initializeErrorUI();
        }
    }
    
    /**
     * Constructor for edit mode
     * 
     * @param customer Customer to edit
     * @param callback Callback for form actions
     */
    public CustomerFormView(Customer customer, FormSubmissionCallback callback) {
        this.callback = callback;
        this.editMode = true;
        this.currentCustomer = customer;
        
        // Initialize RMI connection
        initializeRMIConnection();
        
        // Initialize UI
        if (isConnected) {
            initializeUI();
            populateFields(customer);
        } else {
            initializeErrorUI();
        }
    }
    
    /**
     * Initializes RMI connection to CustomerService
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to CustomerService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            customerService = (CustomerService) registry.lookup("customerService");
            isConnected = true;
            LogUtil.info("Successfully connected to CustomerService");
            
        } catch (Exception e) {
            LogUtil.error("Failed to connect to CustomerService", e);
            isConnected = false;
        }
    }
    
    /**
     * Initializes error UI when connection fails
     */
    private void initializeErrorUI() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        
        JLabel errorLabel = new JLabel("Unable to connect to server");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        errorLabel.setForeground(UIFactory.ERROR_COLOR);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel messageLabel = new JLabel("Cannot load customer form. Please ensure the server is running.");
        messageLabel.setFont(UIFactory.BODY_FONT);
        messageLabel.setForeground(UIFactory.MEDIUM_GRAY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton retryButton = UIFactory.createPrimaryButton("Retry Connection");
        retryButton.addActionListener(e -> retryConnection());
        
        JButton cancelButton = UIFactory.createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> {
            if (callback != null) {
                callback.onCancel();
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(retryButton);
        buttonPanel.add(cancelButton);
        
        errorPanel.add(errorLabel, BorderLayout.NORTH);
        errorPanel.add(messageLabel, BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(errorPanel, BorderLayout.CENTER);
    }
    
    /**
     * Retries the RMI connection
     */
    private void retryConnection() {
        initializeRMIConnection();
        if (isConnected) {
            // Reinitialize the UI
            removeAll();
            initializeUI();
            if (editMode && currentCustomer != null) {
                populateFields(currentCustomer);
            }
            revalidate();
            repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                "Still unable to connect to server. Please check if the server is running.",
                "Connection Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Initializes the main UI components
     */
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 20));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the form header
        JPanel headerPanel = UIFactory.createModuleHeaderPanel(editMode ? "Edit Customer" : "New Customer");
        
        // Add connection status to header
        JLabel statusLabel = new JLabel("Connected to Server");
        statusLabel.setFont(UIFactory.SMALL_FONT);
        statusLabel.setForeground(UIFactory.SUCCESS_COLOR);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
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
    
    /**
     * Creates the basic information section
     */
    private JPanel createBasicInfoSection() {
        JPanel sectionPanel = UIFactory.createFormSection("Basic Information");
        
        // Use GridBagLayout for form layout
        sectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Customer ID field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        JLabel customerIdLabel = new JLabel("Customer ID:");
        sectionPanel.add(customerIdLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        customerIdField = new JTextField();
        customerIdField.setEnabled(!editMode); // Disable in edit mode
        if (editMode) {
            customerIdField.setBackground(new Color(0xF5F5F5));
            customerIdField.setToolTipText("Customer ID cannot be changed");
        }
        sectionPanel.add(customerIdField, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        customerIdValidationLabel = new JLabel("");
        customerIdValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        customerIdValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(customerIdValidationLabel, gbc);
        
        // First Name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        JLabel firstNameLabel = new JLabel("First Name:");
        sectionPanel.add(firstNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        firstNameField = new JTextField();
        sectionPanel.add(firstNameField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        firstNameValidationLabel = new JLabel("");
        firstNameValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        firstNameValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(firstNameValidationLabel, gbc);
        
        // Last Name field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.15;
        JLabel lastNameLabel = new JLabel("Last Name:");
        sectionPanel.add(lastNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        lastNameField = new JTextField();
        sectionPanel.add(lastNameField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        lastNameValidationLabel = new JLabel("");
        lastNameValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        lastNameValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(lastNameValidationLabel, gbc);
        
        return sectionPanel;
    }
    
    /**
     * Creates the contact information section
     */
    private JPanel createContactInfoSection() {
        JPanel sectionPanel = UIFactory.createFormSection("Contact Information");
        
        // Use GridBagLayout for form layout
        sectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
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
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        JLabel phoneLabel = new JLabel("Phone:");
        sectionPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        phoneField = new JTextField();
        sectionPanel.add(phoneField, gbc);
        
        // Address field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.15;
        JLabel addressLabel = new JLabel("Address:");
        sectionPanel.add(addressLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
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
    
    /**
     * Registers button actions
     */
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
                if (isConnected) {
                    if (validateForm()) {
                        saveCustomer();
                    }
                } else {
                    JOptionPane.showMessageDialog(CustomerFormView.this,
                        "Cannot save customer - not connected to server.\nPlease retry connection.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    /**
     * Validates the form using both local and remote validation
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Reset validation messages
        customerIdValidationLabel.setText("");
        firstNameValidationLabel.setText("");
        lastNameValidationLabel.setText("");
        emailValidationLabel.setText("");
        
        // Disable save button during validation
        saveButton.setEnabled(false);
        saveButton.setText("Validating...");
        
        try {
            // Validate Customer ID
            if (!editMode) { // Only validate in create mode
                if (customerIdField.getText().trim().isEmpty()) {
                    customerIdValidationLabel.setText("Customer ID is required");
                    isValid = false;
                } else if (!ValidationUtil.validateAlphanumeric(customerIdField.getText().trim())) {
                    customerIdValidationLabel.setText("Only letters and numbers allowed");
                    isValid = false;
                } else {
                    // Check if ID already exists using RMI service
                    boolean exists = customerService.customerIdExists(customerIdField.getText().trim());
                    if (exists) {
                        customerIdValidationLabel.setText("Customer ID already exists");
                        isValid = false;
                    }
                }
            }
            
            // Validate First Name
            if (firstNameField.getText().trim().isEmpty()) {
                firstNameValidationLabel.setText("First name is required");
                isValid = false;
            }
            
            // Validate Last Name
            if (lastNameField.getText().trim().isEmpty()) {
                lastNameValidationLabel.setText("Last name is required");
                isValid = false;
            }
            
            // Validate Email
            if (emailField.getText().trim().isEmpty()) {
                emailValidationLabel.setText("Email is required");
                isValid = false;
            } else if (!ValidationUtil.validateEmail(emailField.getText().trim())) {
                emailValidationLabel.setText("Invalid email format");
                isValid = false;
            } else if (!editMode || !emailField.getText().equals(currentCustomer.getEmail())) {
                // Check if email already exists using RMI service (but skip if it's the same as current in edit mode)
                boolean exists = customerService.emailExists(emailField.getText().trim());
                if (exists && (editMode ? true : true)) { // Need to check if it's a different customer's email in edit mode
                    if (editMode) {
                        // In edit mode, check if the email belongs to a different customer
                        Customer existingCustomer = customerService.findCustomerByEmail(emailField.getText().trim());
                        if (existingCustomer != null && existingCustomer.getId() != currentCustomer.getId()) {
                            emailValidationLabel.setText("Email already exists for another customer");
                            isValid = false;
                        }
                    } else {
                        emailValidationLabel.setText("Email already exists");
                        isValid = false;
                    }
                }
            }
            
        } catch (Exception e) {
            LogUtil.error("Error during form validation", e);
            JOptionPane.showMessageDialog(this,
                "Validation error: " + e.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            isValid = false;
        } finally {
            // Re-enable save button
            saveButton.setEnabled(true);
            saveButton.setText(editMode ? "Update" : "Save");
        }
        
        return isValid;
    }
    
    /**
     * Saves the customer using RMI service
     */
    private void saveCustomer() {
        // Disable save button during save operation
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");
        
        SwingWorker<Customer, Void> worker = new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() throws Exception {
                Customer customer;
                
                if (editMode && currentCustomer != null) {
                    customer = currentCustomer;
                } else {
                    customer = new Customer();
                    customer.setCustomerId(customerIdField.getText().trim());
                    customer.setRegistrationDate(LocalDate.now());
                }
                
                customer.setFirstName(firstNameField.getText().trim());
                customer.setLastName(lastNameField.getText().trim());
                customer.setEmail(emailField.getText().trim());
                customer.setPhone(phoneField.getText().trim());
                customer.setAddress(addressArea.getText().trim());
                
                if (editMode) {
                    // Update existing customer using RMI service
                    return customerService.updateCustomer(customer);
                } else {
                    // Create new customer using RMI service
                    return customerService.createCustomer(customer);
                }
            }
            
            @Override
            protected void done() {
                try {
                    Customer savedCustomer = get();
                    
                    if (savedCustomer != null) {
                        LogUtil.info("Customer " + (editMode ? "updated" : "created") + " successfully");
                        
                        if (callback != null) {
                            callback.onSave(savedCustomer);
                        }
                    } else {
                        JOptionPane.showMessageDialog(CustomerFormView.this,
                            "Failed to " + (editMode ? "update" : "create") + " customer.\nPlease try again.",
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error saving customer", e);
                    JOptionPane.showMessageDialog(CustomerFormView.this,
                        "Error saving customer: " + e.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable save button
                    saveButton.setEnabled(true);
                    saveButton.setText(editMode ? "Update" : "Save");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Populates the form fields with customer data
     */
    private void populateFields(Customer customer) {
        if (customer != null) {
            customerIdField.setText(customer.getCustomerId() != null ? customer.getCustomerId() : "");
            firstNameField.setText(customer.getFirstName() != null ? customer.getFirstName() : "");
            lastNameField.setText(customer.getLastName() != null ? customer.getLastName() : "");
            emailField.setText(customer.getEmail() != null ? customer.getEmail() : "");
            phoneField.setText(customer.getPhone() != null ? customer.getPhone() : "");
            addressArea.setText(customer.getAddress() != null ? customer.getAddress() : "");
        }
    }
    
    /**
     * Clears all form fields
     */
    public void clearFields() {
        customerIdField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        
        // Clear validation messages
        customerIdValidationLabel.setText("");
        firstNameValidationLabel.setText("");
        lastNameValidationLabel.setText("");
        emailValidationLabel.setText("");
    }
    
    /**
     * Checks if connected to RMI service
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Reconnects to RMI service
     */
    public void reconnect() {
        retryConnection();
    }
    
    /**
     * Gets the current customer (for edit mode)
     */
    public Customer getCurrentCustomer() {
        return currentCustomer;
    }
    
    /**
     * Checks if the form is in edit mode
     */
    public boolean isEditMode() {
        return editMode;
    }
}