package controller;

import model.Supplier;
import service.SupplierService;
import ui.DialogFactory;
import ui.supplier.SupplierDetailsView;
import ui.supplier.SupplierFormView;
import ui.supplier.SupplierListView;
import util.LogUtil;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * RMI-based Controller for Supplier module operations.
 * Manages interactions between the supplier views and the remote supplier service.
 */
public class SupplierController {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote service
    private SupplierService supplierService;
    
    // Views
    private SupplierListView listView;
    private Component parentComponent;
    
    /**
     * Constructor
     * 
     * @param parentComponent Parent component for dialogs
     */
    public SupplierController(Component parentComponent) {
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
            LogUtil.info("Connecting to SupplierService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            supplierService = (SupplierService) registry.lookup("supplierService");
            LogUtil.info("Successfully connected to SupplierService");
        } catch (Exception e) {
            LogUtil.error("Failed to connect to SupplierService", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            parentComponent,
            "Failed to connect to the Supplier Service.\nPlease ensure the server is running and try again.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Gets the supplier list view
     * 
     * @return The supplier list view
     */
    public SupplierListView getListView() {
        return listView;
    }
    
    /**
     * Initializes the supplier list view with callbacks
     */
    private void initializeListView() {
        listView = new SupplierListView(new SupplierListView.SupplierListCallback() {
            @Override
            public void onAddSupplier() {
                showAddSupplierDialog();
            }
            
            @Override
            public void onEditSupplier(Supplier supplier) {
                showEditSupplierDialog(supplier);
            }
            
            @Override
            public void onDeleteSupplier(Supplier supplier) {
                deleteSupplier(supplier);
            }
            
            @Override
            public void onViewSupplierDetails(Supplier supplier) {
                showSupplierDetailsDialog(supplier);
            }
            
            @Override
            public void onViewSupplierProducts(Supplier supplier) {
                showSupplierWithProducts(supplier);
            }
        });
    }
    
    /**
     * Shows the add supplier dialog
     */
    private void showAddSupplierDialog() {
        final SupplierFormView[] formView = new SupplierFormView[1];
        formView[0] = new SupplierFormView(new SupplierFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Supplier supplier) {
                try {
                    // Validate supplier data
                    if (!validateSupplier(supplier)) {
                        return;
                    }
                    
                    // Check if supplier code already exists
                    if (supplierService.supplierCodeExists(supplier.getSupplierCode())) {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Supplier code already exists. Please use a different code.",
                            "Duplicate Supplier Code",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    
                    // Check if email already exists
                    if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
                        if (supplierService.emailExists(supplier.getEmail())) {
                            JOptionPane.showMessageDialog(
                                formView[0],
                                "Email address already exists. Please use a different email.",
                                "Duplicate Email",
                                JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }
                    }
                    
                    // Create the supplier using RMI service
                    Supplier createdSupplier = supplierService.createSupplier(supplier);
                    
                    if (createdSupplier != null) {
                        // Update the list view with the new supplier
                        listView.addSupplier(createdSupplier);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Supplier created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to create supplier. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error creating supplier", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error creating supplier: " + e.getMessage(),
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
            "Add New Supplier",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            600,
            500
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Shows the edit supplier dialog
     * 
     * @param supplier The supplier to edit
     */
    private void showEditSupplierDialog(Supplier supplier) {
        SupplierFormView[] formView = new SupplierFormView[1];
        formView[0] = new SupplierFormView(supplier, new SupplierFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Supplier updatedSupplier) {
                try {
                    // Validate supplier data
                    if (!validateSupplier(updatedSupplier)) {
                        return;
                    }
                    
                    // Update the supplier using RMI service
                    Supplier savedSupplier = supplierService.updateSupplier(updatedSupplier);
                    
                    if (savedSupplier != null) {
                        // Update the list view with the modified supplier
                        listView.updateSupplier(savedSupplier);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Supplier updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to update supplier. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error updating supplier", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error updating supplier: " + e.getMessage(),
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
            "Edit Supplier",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            600,
            500
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Deletes a supplier
     * 
     * @param supplier The supplier to delete
     */
    private void deleteSupplier(Supplier supplier) {
        int result = JOptionPane.showConfirmDialog(
            parentComponent,
            "Are you sure you want to delete supplier: " + supplier.getName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                Supplier deletedSupplier = supplierService.deleteSupplier(supplier);
                
                if (deletedSupplier != null) {
                    // Remove from list view
                    listView.removeSupplier(supplier);
                    
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Supplier deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Failed to delete supplier. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                
            } catch (Exception e) {
                LogUtil.error("Error deleting supplier", e);
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Error deleting supplier: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Shows the supplier details dialog
     * 
     * @param supplier The supplier to view
     */
    private void showSupplierDetailsDialog(Supplier supplier) {
        try {
            final SupplierDetailsView[] detailsView = new SupplierDetailsView[1];
            detailsView[0] = new SupplierDetailsView(supplier, new SupplierDetailsView.DetailsViewCallback() {
                @Override
                public void onEditSupplier(Supplier supplierToEdit) {
                    // Close the details dialog
                    Window window = SwingUtilities.getWindowAncestor(detailsView[0]);
                    if (window instanceof JDialog) {
                        ((JDialog) window).dispose();
                    }
                    
                    // Show the edit dialog
                    showEditSupplierDialog(supplierToEdit);
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
                "Supplier Details",
                detailsView[0],
                null, // onEdit handled in callback
                700,
                600
            );
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            LogUtil.error("Error loading supplier details", e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading supplier details: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Shows the supplier with its products
     * 
     * @param supplier The supplier to view with products
     */
    private void showSupplierWithProducts(Supplier supplier) {
        try {
            // Load the supplier with products
            Supplier supplierWithProducts = supplierService.getSupplierWithProducts(supplier.getId());
            
            if (supplierWithProducts != null) {
                showSupplierDetailsDialog(supplierWithProducts);
            } else {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Failed to load supplier products.",
                    "Data Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading supplier products", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading supplier products: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Validates supplier data
     * 
     * @param supplier The supplier to validate
     * @return true if valid, false otherwise
     */
    private boolean validateSupplier(Supplier supplier) {
        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Supplier code is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Supplier name is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        // Validate email format if provided
        if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
            String email = supplier.getEmail().trim();
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
     * Refreshes the supplier list with data from the server
     */
    public void refreshSupplierList() {
        try {
            List<Supplier> suppliers = supplierService.findAllSuppliers();
            if (suppliers != null) {
                listView.updateSuppliers(suppliers);
                LogUtil.info("Supplier list refreshed with " + suppliers.size() + " suppliers");
            } else {
                LogUtil.warn("Received null supplier list from server");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading suppliers", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading suppliers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for suppliers by name
     * 
     * @param name The name to search for
     */
    public void searchSuppliers(String name) {
        try {
            List<Supplier> suppliers = supplierService.findSuppliersByName(name);
            if (suppliers != null) {
                listView.updateSuppliers(suppliers);
                LogUtil.info("Search completed, found " + suppliers.size() + " suppliers");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching suppliers", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching suppliers: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for suppliers by contact person
     * 
     * @param contactPerson The contact person to search for
     */
    public void searchSuppliersByContact(String contactPerson) {
        try {
            List<Supplier> suppliers = supplierService.findSuppliersByContactPerson(contactPerson);
            if (suppliers != null) {
                listView.updateSuppliers(suppliers);
                LogUtil.info("Contact search completed, found " + suppliers.size() + " suppliers");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching suppliers by contact", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching suppliers by contact: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Gets the supplier service instance
     * 
     * @return The SupplierService
     */
    public SupplierService getSupplierService() {
        return supplierService;
    }
    
    /**
     * Checks if the connection to the server is available
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return supplierService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
    }
}