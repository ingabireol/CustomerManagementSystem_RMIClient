package controller;

import model.Product;
import model.Supplier;
import service.ProductService;
import service.SupplierService;
import ui.DialogFactory;
import ui.product.ProductDetailsView;
import ui.product.ProductFormView;
import ui.product.ProductListView;
import util.LogUtil;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * RMI-based Controller for Product module operations.
 * Manages interactions between the product views and the remote product service.
 */
public class ProductController {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private ProductService productService;
    private SupplierService supplierService;
    
    // Views
    private ProductListView listView;
    private Component parentComponent;
    
    /**
     * Constructor
     * 
     * @param parentComponent Parent component for dialogs
     */
    public ProductController(Component parentComponent) {
        this.parentComponent = parentComponent;
        
        // Initialize RMI connections
        initializeRMIConnection();
        
        // Initialize the list view
        initializeListView();
    }
    
    /**
     * Initializes the RMI connections to the server
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to ProductService and SupplierService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            productService = (ProductService) registry.lookup("productService");
            supplierService = (SupplierService) registry.lookup("supplierService");
            LogUtil.info("Successfully connected to ProductService and SupplierService");
        } catch (Exception e) {
            LogUtil.error("Failed to connect to Product/Supplier services", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            parentComponent,
            "Failed to connect to the Product Service.\nPlease ensure the server is running and try again.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Gets the product list view
     * 
     * @return The product list view
     */
    public ProductListView getListView() {
        return listView;
    }
    
    /**
     * Initializes the product list view with callbacks
     */
    private void initializeListView() {
        listView = new ProductListView(new ProductListView.ProductListCallback() {
            @Override
            public void onAddProduct() {
                showAddProductDialog();
            }
            
            @Override
            public void onEditProduct(Product product) {
                showEditProductDialog(product);
            }
            
            @Override
            public void onDeleteProduct(Product product) {
                deleteProduct(product);
            }
            
            @Override
            public void onViewProductDetails(Product product) {
                showProductDetailsDialog(product);
            }
        });
    }
    
    /**
     * Shows the add product dialog
     */
    private void showAddProductDialog() {
        ProductFormView[] formView = new ProductFormView[1];
        formView[0] = new ProductFormView(new ProductFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Product product) {
                try {
                    // Validate product data
                    if (!validateProduct(product)) {
                        return;
                    }
                    
                    // Check if product code already exists
                    if (productService.productCodeExists(product.getProductCode())) {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Product code already exists. Please use a different code.",
                            "Duplicate Product Code",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    
                    // Create the product using RMI service
                    Product createdProduct = productService.createProduct(product);
                    
                    if (createdProduct != null) {
                        // Update the list view with the new product
                        listView.addProduct(createdProduct);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Product created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to create product. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error creating product", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error creating product: " + e.getMessage(),
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
            "Add New Product",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            600,
            550
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Shows the edit product dialog
     * 
     * @param product The product to edit
     */
    private void showEditProductDialog(Product product) {
        ProductFormView[] formView = new ProductFormView[1];
        formView[0] = new ProductFormView(product, new ProductFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Product updatedProduct) {
                try {
                    // Validate product data
                    if (!validateProduct(updatedProduct)) {
                        return;
                    }
                    
                    // Update the product using RMI service
                    Product savedProduct = productService.updateProduct(updatedProduct);
                    
                    if (savedProduct != null) {
                        // Update the list view with the modified product
                        listView.updateProduct(savedProduct);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Product updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to update product. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error updating product", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error updating product: " + e.getMessage(),
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
            "Edit Product",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            600,
            550
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Deletes a product
     * 
     * @param product The product to delete
     */
    private void deleteProduct(Product product) {
        int result = JOptionPane.showConfirmDialog(
            parentComponent,
            "Are you sure you want to delete product: " + product.getName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                Product deletedProduct = productService.deleteProduct(product);
                
                if (deletedProduct != null) {
                    // Remove from list view
                    listView.removeProduct(product);
                    
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Product deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Failed to delete product. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                
            } catch (Exception e) {
                LogUtil.error("Error deleting product", e);
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Error deleting product: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Shows the product details dialog
     * 
     * @param product The product to view
     */
    private void showProductDetailsDialog(Product product) {
        try {
            // First, load the product with supplier information
            Product productWithSupplier = productService.getProductWithSupplier(product.getId());
            if (productWithSupplier == null) {
                productWithSupplier = product;
            }
            
            // Create the details view with the product that has supplier information
            final ProductDetailsView[] detailsView = new ProductDetailsView[1];
            detailsView[0] = new ProductDetailsView(productWithSupplier, new ProductDetailsView.DetailsViewCallback() {
                @Override
                public void onEditProduct(Product productToEdit) {
                    // Close the details dialog
                    Window window = SwingUtilities.getWindowAncestor(detailsView[0]);
                    if (window instanceof JDialog) {
                        ((JDialog) window).dispose();
                    }
                    
                    // Show the edit dialog
                    showEditProductDialog(productToEdit);
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
                "Product Details",
                detailsView[0],
                null, // onEdit handled in callback
                700,
                600
            );
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            LogUtil.error("Error loading product details", e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading product details: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Validates product data
     * 
     * @param product The product to validate
     * @return true if valid, false otherwise
     */
    private boolean validateProduct(Product product) {
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Product code is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Product name is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Price must be a positive number.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (product.getStockQuantity() < 0) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Stock quantity cannot be negative.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        return true;
    }
    
    /**
     * Refreshes the product list with data from the server
     */
    public void refreshProductList() {
        try {
            List<Product> products = productService.findAllProducts();
            if (products != null) {
                listView.updateProducts(products);
                LogUtil.info("Product list refreshed with " + products.size() + " products");
            } else {
                LogUtil.warn("Received null product list from server");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading products", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading products: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for products by name
     * 
     * @param name The name to search for
     */
    public void searchProducts(String name) {
        try {
            List<Product> products = productService.findProductsByName(name);
            if (products != null) {
                listView.updateProducts(products);
                LogUtil.info("Search completed, found " + products.size() + " products");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching products", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching products: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for products by category
     * 
     * @param category The category to search for
     */
    public void searchProductsByCategory(String category) {
        try {
            List<Product> products = productService.findProductsByCategory(category);
            if (products != null) {
                listView.updateProducts(products);
                LogUtil.info("Category search completed, found " + products.size() + " products");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching products by category", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching products by category: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Gets low stock products
     * 
     * @param threshold The stock threshold
     */
    public void getLowStockProducts(int threshold) {
        try {
            List<Product> products = productService.findLowStockProducts(threshold);
            if (products != null) {
                listView.updateProducts(products);
                LogUtil.info("Low stock search completed, found " + products.size() + " products");
            }
        } catch (Exception ex) {
            LogUtil.error("Error getting low stock products", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error getting low stock products: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Gets all available categories
     * 
     * @return List of categories
     */
    public List<String> getAllCategories() {
        try {
            return productService.findAllCategories();
        } catch (Exception ex) {
            LogUtil.error("Error getting categories", ex);
            return null;
        }
    }
    
    /**
     * Gets all suppliers for product forms
     * 
     * @return List of suppliers
     */
    public List<Supplier> getAllSuppliers() {
        try {
            return supplierService.findAllSuppliers();
        } catch (Exception ex) {
            LogUtil.error("Error getting suppliers", ex);
            return null;
        }
    }
    
    /**
     * Gets the product service instance
     * 
     * @return The ProductService
     */
    public ProductService getProductService() {
        return productService;
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
        return productService != null && supplierService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
    }
}