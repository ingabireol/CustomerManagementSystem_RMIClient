package controller;

import model.Customer;
import service.CustomerService;
import ui.DialogFactory;
import ui.customer.CustomerDetailsView;
import ui.customer.CustomerFormView;
import ui.customer.CustomerListView;
import util.LogUtil;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * RMI-based Controller for Customer module operations.
 * Manages interactions between the customer views and the remote customer service.
 */
public class CustomerController {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote service
    private CustomerService customerService;
    
    // Views
    private CustomerListView listView;
    private Component parentComponent;
    
    /**
     * Constructor
     * 
     * @param parentComponent Parent component for dialogs
     */
    public CustomerController(Component parentComponent) {
        this.parentComponent = parentComponent;
        
        // Initialize RMI connection
        initializeRMIConnection();
        
        // Initialize the list view
        initializeListView();
    }
    
    /**
     * Initializes the RMI connection to the server
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to CustomerService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            customerService = (CustomerService) registry.lookup("customerService");
            LogUtil.info("Successfully connected to CustomerService");
        } catch (Exception e) {
            LogUtil.error("Failed to connect to CustomerService", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            parentComponent,
            "Failed to connect to the Customer Service.\nPlease ensure the server is running and try again.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Gets the customer list view
     * 
     * @return The customer list view
     */
    public CustomerListView getListView() {
        return listView;
    }
    
    /**
     * Initializes the customer list view with callbacks
     */
    private void initializeListView() {
        listView = new CustomerListView(new CustomerListView.CustomerListCallback() {
            @Override
            public void onAddCustomer() {
                showAddCustomerDialog();
            }
            
            @Override
            public void onEditCustomer(Customer customer) {
                showEditCustomerDialog(customer);
            }
            
            @Override
            public void onDeleteCustomer(Customer customer) {
                deleteCustomer(customer);
            }
            
            @Override
            public void onViewCustomerDetails(Customer customer) {
                showCustomerDetailsDialog(customer);
            }
            
            @Override
            public void onViewCustomerOrders(Customer customer) {
                showCustomerWithOrders(customer);
            }
        });
    }
    
    /**
     * Shows the add customer dialog
     */
    private void showAddCustomerDialog() {
        final CustomerFormView[] formView = new CustomerFormView[1];
        formView[0] = new CustomerFormView(new CustomerFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Customer customer) {
                try {
                    // Validate customer data
                    if (!validateCustomer(customer)) {
                        return;
                    }
                    
                    // Check if customer ID already exists
                    if (customerService.customerIdExists(customer.getCustomerId())) {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Customer ID already exists. Please use a different ID.",
                            "Duplicate Customer ID",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    
                    // Check if email already exists
                    if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                        if (customerService.emailExists(customer.getEmail())) {
                            JOptionPane.showMessageDialog(
                                formView[0],
                                "Email address already exists. Please use a different email.",
                                "Duplicate Email",
                                JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }
                    }
                    
                    // Create the customer using RMI service
                    Customer createdCustomer = customerService.createCustomer(customer);
                    
                    if (createdCustomer != null) {
                        // Update the list view with the new customer
                        listView.addCustomer(createdCustomer);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Customer created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to create customer. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error creating customer", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error creating customer: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            
            @Override
            public void onCancel() {
                // Close the dialog
                Window window = SwingUtilities.getWindowAncestor(formView[0]);
                if (window instanceof JDialog) {
                    ((JDialog) window).dispose();
                }
            }
        });
        
        // Create and show the dialog
        JDialog dialog = DialogFactory.createFormDialog(
            parentComponent,
            "Add New Customer",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            600,
            550
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Shows the edit customer dialog
     * 
     * @param customer The customer to edit
     */
    private void showEditCustomerDialog(Customer customer) {
        CustomerFormView[] formView = new CustomerFormView[1];
        formView[0] = new CustomerFormView(customer, new CustomerFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Customer updatedCustomer) {
                try {
                    // Validate customer data
                    if (!validateCustomer(updatedCustomer)) {
                        return;
                    }
                    
                    // Update the customer using RMI service
                    Customer savedCustomer = customerService.updateCustomer(updatedCustomer);
                    
                    if (savedCustomer != null) {
                        // Update the list view with the modified customer
                        listView.updateCustomer(savedCustomer);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Customer updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to update customer. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error updating customer", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error updating customer: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            
            @Override
            public void onCancel() {
                // Close the dialog
                Window window = SwingUtilities.getWindowAncestor(formView[0]);
                if (window instanceof JDialog) {
                    ((JDialog) window).dispose();
                }
            }
        });
        
        // Create and show the dialog
        JDialog dialog = DialogFactory.createFormDialog(
            parentComponent,
            "Edit Customer",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            600,
            550
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Deletes a customer
     * 
     * @param customer The customer to delete
     */
    private void deleteCustomer(Customer customer) {
        int result = JOptionPane.showConfirmDialog(
            parentComponent,
            "Are you sure you want to delete customer: " + customer.getFullName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                Customer deletedCustomer = customerService.deleteCustomer(customer);
                
                if (deletedCustomer != null) {
                    // Remove from list view
                    listView.removeCustomer(customer);
                    
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Customer deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Failed to delete customer. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                
            } catch (Exception e) {
                LogUtil.error("Error deleting customer", e);
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Error deleting customer: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Shows the customer details dialog
     * 
     * @param customer The customer to view
     */
    private void showCustomerDetailsDialog(Customer customer) {
        try {
            // First, load the customer with orders information
            Customer customerWithOrders = customerService.getCustomerWithOrders(customer.getId());
            if (customerWithOrders == null) {
                customerWithOrders = customer;
            }
            
            // Create the details view with the customer that has orders information
            final CustomerDetailsView[] detailsView = new CustomerDetailsView[1];
            detailsView[0] = new CustomerDetailsView(customerWithOrders, 
                    new CustomerDetailsView.DetailsViewCallback() {
                        @Override
                        public void onEditCustomer(Customer customerToEdit) {
                            // Close the details dialog
                            Window window = SwingUtilities.getWindowAncestor(detailsView[0]);
                            if (window instanceof JDialog) {
                                ((JDialog) window).dispose();
                            }
                            
                            // Show the edit dialog
                            showEditCustomerDialog(customerToEdit);
                        }
                        
                        @Override
                        public void onViewOrders(Customer customerWithOrders) {
                            // This would navigate to the orders view for this customer
                            JOptionPane.showMessageDialog(
                                    parentComponent,
                                    "View Orders functionality would show all orders for " +
                                            customerWithOrders.getFullName(),
                                    "View Orders",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                        
                        @Override
                        public void onClose() {
                            // Close the dialog
                            Window window = SwingUtilities.getWindowAncestor(detailsView[0]);
                            if (window instanceof JDialog) {
                                ((JDialog) window).dispose();
                            }
                        }
                    });
            
            // Create and show the dialog
            JDialog dialog = DialogFactory.createDetailsDialog(
                parentComponent,
                "Customer Details",
                detailsView[0],
                null, // onEdit handled in callback
                700,
                600
            );
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            LogUtil.error("Error loading customer details", e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading customer details: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Shows the customer with their order history
     * 
     * @param customer The customer to view with orders
     */
    private void showCustomerWithOrders(Customer customer) {
        try {
            // Load the customer with orders
            Customer customerWithOrders = customerService.getCustomerWithOrders(customer.getId());
            
            if (customerWithOrders != null) {
                showCustomerDetailsDialog(customerWithOrders);
            } else {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Failed to load customer orders.",
                    "Data Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading customer orders", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading customer orders: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Validates customer data
     * 
     * @param customer The customer to validate
     * @return true if valid, false otherwise
     */
    private boolean validateCustomer(Customer customer) {
        if (customer.getCustomerId() == null || customer.getCustomerId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Customer ID is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "First name is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Last name is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        // Validate email format if provided
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            String email = customer.getEmail().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Please enter a valid email address.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Refreshes the customer list with data from the server
     */
    public void refreshCustomerList() {
        try {
            List<Customer> customers = customerService.findAllCustomers();
            if (customers != null) {
                listView.updateCustomers(customers);
                LogUtil.info("Customer list refreshed with " + customers.size() + " customers");
            } else {
                LogUtil.debug("Received null customer list from server");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading customers", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading customers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for customers by name
     * 
     * @param name The name to search for
     */
    public void searchCustomers(String name) {
        try {
            List<Customer> customers = customerService.findCustomersByName(name);
            if (customers != null) {
                listView.updateCustomers(customers);
                LogUtil.info("Search completed, found " + customers.size() + " customers");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching customers", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching customers: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Gets the customer service instance
     * 
     * @return The CustomerService
     */
    public CustomerService getCustomerService() {
        return customerService;
    }
    
    /**
     * Checks if the connection to the server is available
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return customerService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
    }
}