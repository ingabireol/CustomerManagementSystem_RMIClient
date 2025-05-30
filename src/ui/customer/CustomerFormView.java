package ui.customer;

import model.Customer;
import dao.CustomerDao;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import ui.UIFactory;

/**
 * Form view for creating and editing customer records.
 * Provides fields for all customer properties and validates input.
 */
public class CustomerFormView extends JPanel {
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
    
    // Data access
    private CustomerDao customerDao;
    
    // Callback for form submission
    private FormSubmissionCallback callback;
    
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
        this.customerDao = new CustomerDao();
        this.editMode = false;
        initializeUI();
    }
    
    /**
     * Constructor for edit mode
     * 
     * @param customer Customer to edit
     * @param callback Callback for form actions
     */
    public CustomerFormView(Customer customer, FormSubmissionCallback callback) {
        this.callback = callback;
        this.customerDao = new CustomerDao();
        this.editMode = true;
        this.currentCustomer = customer;
        initializeUI();
        populateFields(customer);
    }
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 20));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the form header
        JPanel headerPanel = UIFactory.createModuleHeaderPanel(editMode ? "Edit Customer" : "New Customer");
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
                    saveCustomer();
                }
            }
        });
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Reset validation messages
        customerIdValidationLabel.setText("");
        firstNameValidationLabel.setText("");
        lastNameValidationLabel.setText("");
        emailValidationLabel.setText("");
        
        // Validate Customer ID
        if (!editMode) { // Only validate in create mode
            if (customerIdField.getText().trim().isEmpty()) {
                customerIdValidationLabel.setText("Customer ID is required");
                isValid = false;
            } else if (!ValidationUtil.validateAlphanumeric(customerIdField.getText().trim())) {
                customerIdValidationLabel.setText("Only letters and numbers allowed");
                isValid = false;
            } else {
                // Check if ID already exists
                Customer existingCustomer = customerDao.findCustomerByCustomerId(customerIdField.getText().trim());
                if (existingCustomer != null) {
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
            // Check if email already exists (but skip if it's the same as current in edit mode)
            Customer existingCustomer = customerDao.findCustomerByEmail(emailField.getText().trim());
            if (existingCustomer != null && (editMode ? existingCustomer.getId() != currentCustomer.getId() : true)) {
                emailValidationLabel.setText("Email already exists");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void saveCustomer() {
        try {
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
                // Use controller's method to update the customer
                customer = customerDao.updateCustomer(customer) > 0 ? customer : null;
            } else {
                // Use controller's method to create a new customer
                customer = customerDao.createCustomer(customer) > 0 ? customer : null;
            }
            
            if (customer != null) {
                if (callback != null) {
                    callback.onSave(customer);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to " + (editMode ? "update" : "create") + " customer.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error saving customer: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void populateFields(Customer customer) {
        if (customer != null) {
            customerIdField.setText(customer.getCustomerId());
            firstNameField.setText(customer.getFirstName());
            lastNameField.setText(customer.getLastName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhone());
            addressArea.setText(customer.getAddress());
        }
    }
}