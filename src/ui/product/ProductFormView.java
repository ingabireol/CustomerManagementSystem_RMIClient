package ui.product;

import model.Product;
import model.Supplier;
import controller.ProductController;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import ui.UIFactory;

/**
 * RMI-based form view for creating and editing product records.
 * Provides fields for all product properties and validates input.
 */
public class ProductFormView extends JPanel {
    // Form components
    private JTextField productCodeField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JFormattedTextField priceField;
    private JSpinner stockQuantitySpinner;
    private JComboBox<String> categoryComboBox;
    private JComboBox<Supplier> supplierComboBox;
    
    // Validation labels
    private JLabel productCodeValidationLabel;
    private JLabel nameValidationLabel;
    private JLabel priceValidationLabel;
    
    // Buttons
    private JButton saveButton;
    private JButton cancelButton;
    
    // Mode flag
    private boolean editMode = false;
    private Product currentProduct;
    
    // RMI Controller
    private ProductController productController;
    private List<Supplier> supplierList;
    
    // Callback for form submission
    private FormSubmissionCallback callback;
    
    /**
     * Interface for form submission callback
     */
    public interface FormSubmissionCallback {
        void onSave(Product product);
        void onCancel();
    }
    
    /**
     * Constructor for create mode
     * 
     * @param productController The RMI-based product controller
     * @param callback Callback for form actions
     */
    public ProductFormView(ProductController productController, FormSubmissionCallback callback) {
        this.productController = productController;
        this.callback = callback;
        this.editMode = false;
        
        loadSuppliers();
        initializeUI();
    }
    
    /**
     * Constructor for edit mode
     * 
     * @param productController The RMI-based product controller
     * @param product Product to edit
     * @param callback Callback for form actions
     */
    public ProductFormView(ProductController productController, Product product, FormSubmissionCallback callback) {
        this.productController = productController;
        this.callback = callback;
        this.editMode = true;
        this.currentProduct = product;
        
        loadSuppliers();
        initializeUI();
        populateFields(product);
    }
    
    /**
     * Loads the list of suppliers using RMI service
     */
    private void loadSuppliers() {
        try {
            LogUtil.info("Loading suppliers from RMI service...");
            this.supplierList = productController.getAllSuppliers();
            if (supplierList == null) {
                LogUtil.warn("Received null supplier list from RMI service");
                this.supplierList = new ArrayList<>();
            } else {
                LogUtil.info("Loaded " + supplierList.size() + " suppliers from RMI service");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading suppliers from RMI service", ex);
            JOptionPane.showMessageDialog(this,
                "Error loading suppliers: " + ex.getMessage(),
                "RMI Service Error",
                JOptionPane.ERROR_MESSAGE);
            this.supplierList = new ArrayList<>();
        }
    }
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 20));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the form header
        JPanel headerPanel = UIFactory.createModuleHeaderPanel(editMode ? "Edit Product" : "New Product");
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
        formPanel.add(createInventorySection());
        formPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacing
        formPanel.add(createDetailsSection());
        
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
        
        // Product Code field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        JLabel productCodeLabel = new JLabel("Product Code:");
        sectionPanel.add(productCodeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        productCodeField = new JTextField();
        productCodeField.setEnabled(!editMode); // Disable in edit mode
        sectionPanel.add(productCodeField, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        productCodeValidationLabel = new JLabel("");
        productCodeValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        productCodeValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(productCodeValidationLabel, gbc);
        
        // Product Name field
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
        
        // Price field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.15;
        JLabel priceLabel = new JLabel("Price:");
        sectionPanel.add(priceLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        
        // Create a formatted price field
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setMinimumFractionDigits(2);
        NumberFormatter currencyFormatter = new NumberFormatter(currencyFormat);
        currencyFormatter.setMinimum(0.0);
        currencyFormatter.setAllowsInvalid(false);
        
        priceField = new JFormattedTextField(currencyFormat);
        priceField.setValue(0.0);
        sectionPanel.add(priceField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        priceValidationLabel = new JLabel("");
        priceValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        priceValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(priceValidationLabel, gbc);
        
        // Category ComboBox
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.15;
        JLabel categoryLabel = new JLabel("Category:");
        sectionPanel.add(categoryLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        
        // Load categories from RMI service
        String[] categories = loadCategories();
        categoryComboBox = UIFactory.createComboBox(categories);
        sectionPanel.add(categoryComboBox, gbc);
        
        return sectionPanel;
    }
    
    /**
     * Load categories from RMI service
     */
    private String[] loadCategories() {
        try {
            List<String> categoryList = productController.getAllCategories();
            if (categoryList != null && !categoryList.isEmpty()) {
                return categoryList.toArray(new String[0]);
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading categories from RMI service", ex);
        }
        
        // Return default categories if RMI call fails
        return new String[]{"Electronics", "Clothing", "Food & Beverages", "Home & Garden", "Office Supplies", "Other"};
    }
    
    private JPanel createInventorySection() {
        JPanel sectionPanel = UIFactory.createFormSection("Inventory Information");
        
        // Use GridBagLayout for form layout
        sectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Stock Quantity field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        JLabel stockQuantityLabel = new JLabel("Stock Quantity:");
        sectionPanel.add(stockQuantityLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 10000, 1);
        stockQuantitySpinner = new JSpinner(spinnerModel);
        sectionPanel.add(stockQuantitySpinner, gbc);
        
        // Supplier ComboBox
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        JLabel supplierLabel = new JLabel("Supplier:");
        sectionPanel.add(supplierLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        supplierComboBox = new JComboBox<>();
        
        // Add suppliers to combo box
        DefaultComboBoxModel<Supplier> supplierModel = new DefaultComboBoxModel<>();
        if (supplierList != null) {
            for (Supplier supplier : supplierList) {
                supplierModel.addElement(supplier);
            }
        }
        supplierComboBox.setModel(supplierModel);
        
        // Set the renderer to display supplier name
        supplierComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (value instanceof Supplier) {
                    setText(((Supplier) value).getName());
                }
                
                return this;
            }
        });
        
        sectionPanel.add(supplierComboBox, gbc);
        
        return sectionPanel;
    }
    
    private JPanel createDetailsSection() {
        JPanel sectionPanel = UIFactory.createFormSection("Product Details");
        
        // Use GridBagLayout for form layout
        sectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Description Text Area
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.15;
        JLabel descriptionLabel = new JLabel("Description:");
        sectionPanel.add(descriptionLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.85;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createLineBorder(UIFactory.MEDIUM_GRAY));
        sectionPanel.add(descriptionScrollPane, gbc);
        
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
                    saveProduct();
                }
            }
        });
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Reset validation messages
        productCodeValidationLabel.setText("");
        nameValidationLabel.setText("");
        priceValidationLabel.setText("");
        
        // Validate Product Code
        if (!editMode) { // Only validate in create mode
            if (productCodeField.getText().trim().isEmpty()) {
                productCodeValidationLabel.setText("Product code is required");
                isValid = false;
            } else if (!productCodeField.getText().matches("[A-Za-z0-9-]+")) {
                productCodeValidationLabel.setText("Only letters, numbers, and hyphens allowed");
                isValid = false;
            } else {
                // Check if code already exists using RMI service
                try {
                    boolean exists = productController.getProductService().productCodeExists(productCodeField.getText().trim());
                    if (exists) {
                        productCodeValidationLabel.setText("Product code already exists");
                        isValid = false;
                    }
                } catch (Exception ex) {
                    LogUtil.error("Error checking product code existence", ex);
                    productCodeValidationLabel.setText("Error validating product code");
                    isValid = false;
                }
            }
        }
        
        // Validate Name
        if (nameField.getText().trim().isEmpty()) {
            nameValidationLabel.setText("Product name is required");
            isValid = false;
        }
        
        // Validate Price
        try {
            Number price = (Number) priceField.getValue();
            if (price == null || price.doubleValue() <= 0) {
                priceValidationLabel.setText("Price must be greater than zero");
                isValid = false;
            }
        } catch (Exception e) {
            priceValidationLabel.setText("Invalid price format");
            isValid = false;
        }
        
        return isValid;
    }
    
    private void saveProduct() {
        try {
            Product product;
            
            if (editMode && currentProduct != null) {
                product = currentProduct;
            } else {
                product = new Product();
                product.setProductCode(productCodeField.getText().trim());
            }
            
            product.setName(nameField.getText().trim());
            product.setDescription(descriptionArea.getText().trim());
            
            // Parse price value - handle formatting
            try {
                Number priceValue = (Number) priceField.getValue();
                product.setPrice(new BigDecimal(priceValue.toString()));
            } catch (Exception e) {
                product.setPrice(BigDecimal.ZERO);
            }
            
            // Get stock quantity from spinner
            product.setStockQuantity((Integer) stockQuantitySpinner.getValue());
            
            // Get category
            product.setCategory((String) categoryComboBox.getSelectedItem());
            
            // Get supplier
            Supplier selectedSupplier = (Supplier) supplierComboBox.getSelectedItem();
            if (selectedSupplier != null) {
                product.setSupplierId(selectedSupplier.getId());
                product.setSupplier(selectedSupplier);
            }
            
            // Use RMI service to save the product
            Product savedProduct;
            if (editMode) {
                LogUtil.info("Updating product via RMI: " + product.getName());
                savedProduct = productController.getProductService().updateProduct(product);
            } else {
                LogUtil.info("Creating product via RMI: " + product.getName());
                savedProduct = productController.getProductService().createProduct(product);
            }
            
            if (savedProduct != null) {
                LogUtil.info("Product " + (editMode ? "updated" : "created") + " successfully via RMI");
                if (callback != null) {
                    callback.onSave(savedProduct);
                }
            } else {
                String message = "Failed to " + (editMode ? "update" : "create") + " product.";
                LogUtil.warn(message);
                JOptionPane.showMessageDialog(this,
                    message,
                    "RMI Service Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            LogUtil.error("Error saving product via RMI", ex);
            JOptionPane.showMessageDialog(this,
                "Error saving product: " + ex.getMessage(),
                "RMI Service Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateFields(Product product) {
        if (product != null) {
            productCodeField.setText(product.getProductCode());
            nameField.setText(product.getName());
            descriptionArea.setText(product.getDescription());
            
            // Set price field
            if (product.getPrice() != null) {
                priceField.setValue(product.getPrice().doubleValue());
            } else {
                priceField.setValue(0.0);
            }
            
            // Set stock quantity
            stockQuantitySpinner.setValue(product.getStockQuantity());
            
            // Set category
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                if (categoryComboBox.getItemAt(i).equals(product.getCategory())) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
            
            // Set supplier
            if (product.getSupplierId() > 0) {
                for (int i = 0; i < supplierComboBox.getItemCount(); i++) {
                    Supplier supplier = (Supplier) supplierComboBox.getItemAt(i);
                    if (supplier.getId() == product.getSupplierId()) {
                        supplierComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Refreshes the supplier list from RMI service
     */
    public void refreshSuppliers() {
        loadSuppliers();
        
        // Update supplier combo box
        DefaultComboBoxModel<Supplier> model = (DefaultComboBoxModel<Supplier>) supplierComboBox.getModel();
        model.removeAllElements();
        
        if (supplierList != null) {
            for (Supplier supplier : supplierList) {
                model.addElement(supplier);
            }
        }
        
        supplierComboBox.repaint();
    }
    
    /**
     * Gets the current product controller
     * 
     * @return The ProductController instance
     */
    public ProductController getProductController() {
        return productController;
    }
}