package ui.product;

import model.Product;
import model.Supplier;
import controller.ProductController;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import ui.UIFactory;

/**
 * RMI-based list view for displaying and managing products.
 * Provides functionality for searching, filtering, and performing CRUD operations.
 */
public class ProductListView extends JPanel {
    // Table components
    private JTable productTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    
    // Search and filter components
    private JTextField searchField;
    private JComboBox<String> categoryFilterComboBox;
    
    // Action buttons
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton viewDetailsButton;
    
    // Product data and RMI controller
    private List<Product> productList;
    private ProductController productController;
    
    // Callback for list actions
    private ProductListCallback callback;
    
    /**
     * Interface for product list actions
     */
    public interface ProductListCallback {
        void onAddProduct();
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
        void onViewProductDetails(Product product);
    }
    
    /**
     * Constructor
     * 
     * @param productController The RMI-based product controller
     * @param callback Callback for list actions
     */
    public ProductListView(ProductController productController, ProductListCallback callback) {
        this.productController = productController;
        this.callback = callback;
        this.productList = new ArrayList<>();
        
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 10));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create the table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Create the actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Loads product data using RMI service
     */
    private void loadData() {
        try {
            LogUtil.info("Loading products via RMI service...");
            this.productList = productController.getProductService().findAllProducts();
            if (productList == null) {
                LogUtil.warn("Received null product list from RMI service");
                this.productList = new ArrayList<>();
            } else {
                LogUtil.info("Loaded " + productList.size() + " products via RMI service");
            }
            refreshTableData();
        } catch (Exception ex) {
            LogUtil.error("Error loading product data via RMI", ex);
            JOptionPane.showMessageDialog(this,
                "Error loading product data: " + ex.getMessage(),
                "RMI Service Error",
                JOptionPane.ERROR_MESSAGE);
            this.productList = new ArrayList<>();
            refreshTableData();
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = UIFactory.createModuleHeaderPanel("Products");
        
        // Create search and filter section on the right side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        // Category filter combo box - load from RMI service
        String[] categoryOptions = loadCategoryOptions();
        categoryFilterComboBox = UIFactory.createComboBox(categoryOptions);
        categoryFilterComboBox.setPreferredSize(new Dimension(150, 30));
        
        // Search field
        searchField = UIFactory.createSearchField("Search products...", 200);
        
        // Add search button
        JButton searchButton = UIFactory.createSecondaryButton("Search");
        
        // Add components to search panel
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryFilterComboBox);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Add search panel to the header
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        // Add search action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Add filter change action
        categoryFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
        
        // Add Enter key support for search field
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        return headerPanel;
    }
    
    /**
     * Load category options from RMI service
     */
    private String[] loadCategoryOptions() {
        try {
            List<String> categories = productController.getAllCategories();
            if (categories != null && !categories.isEmpty()) {
                List<String> options = new ArrayList<>();
                options.add("All Categories");
                options.addAll(categories);
                return options.toArray(new String[0]);
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading categories from RMI service", ex);
        }
        
        // Return default categories if RMI call fails
        return new String[]{"All Categories", "Electronics", "Clothing", "Food & Beverages", "Home & Garden", "Office Supplies", "Other"};
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        
        // Create the table model with column names
        String[] columnNames = {"ID", "Product Code", "Name", "Price", "Stock", "Category", "Supplier"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class; // ID
                    case 3: return BigDecimal.class; // Price
                    case 4: return Integer.class; // Stock
                    default: return String.class;
                }
            }
        };
        
        // Create and set up the table
        productTable = UIFactory.createStyledTable(tableModel);
        
        // Add row sorting
        tableSorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(tableSorter);
        
        // Set column widths
        productTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID column
        productTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Product Code
        productTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Name
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Price
        productTable.getColumnModel().getColumn(4).setPreferredWidth(80); // Stock
        productTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Category
        productTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Supplier
        
        // Add double-click listener for viewing details
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = productTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        selectedRow = productTable.convertRowIndexToModel(selectedRow);
                        Product selectedProduct = getProductAtRow(selectedRow);
                        if (selectedProduct != null && callback != null) {
                            callback.onViewProductDetails(selectedProduct);
                        }
                    }
                }
            }
        });
        
        // Add selection listener to enable/disable action buttons
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = productTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                viewDetailsButton.setEnabled(hasSelection);
            }
        });
        
        // Add the table to a scroll pane
        JScrollPane scrollPane = UIFactory.createScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Information label (left side)
        JLabel infoLabel = new JLabel("Double-click a row to view product details");
        infoLabel.setFont(UIFactory.SMALL_FONT);
        infoLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
        // Action buttons (right side)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        
        refreshButton = UIFactory.createSecondaryButton("Refresh");
        
        viewDetailsButton = UIFactory.createSecondaryButton("View Details");
        viewDetailsButton.setEnabled(false); // Disabled until selection
        
        deleteButton = UIFactory.createDangerButton("Delete");
        deleteButton.setEnabled(false); // Disabled until selection
        
        editButton = UIFactory.createWarningButton("Edit");
        editButton.setEnabled(false); // Disabled until selection
        
        addButton = UIFactory.createPrimaryButton("Add New");
        
        // Add buttons to panel
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(viewDetailsButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(addButton);
        
        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);
        
        // Register button actions
        registerButtonActions();
        
        return panel;
    }
    
    private void registerButtonActions() {
        // Add button action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null) {
                    callback.onAddProduct();
                }
            }
        });
        
        // Edit button action
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = productTable.convertRowIndexToModel(selectedRow);
                    Product selectedProduct = getProductAtRow(selectedRow);
                    if (selectedProduct != null) {
                        callback.onEditProduct(selectedProduct);
                    }
                }
            }
        });
        
        // Delete button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = productTable.convertRowIndexToModel(selectedRow);
                    Product selectedProduct = getProductAtRow(selectedRow);
                    if (selectedProduct != null) {
                        int confirm = JOptionPane.showConfirmDialog(
                            ProductListView.this,
                            "Are you sure you want to delete product: " + selectedProduct.getName() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteProduct(selectedProduct);
                        }
                    }
                }
            }
        });
        
        // View Details button action
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = productTable.convertRowIndexToModel(selectedRow);
                    Product selectedProduct = getProductAtRow(selectedRow);
                    if (selectedProduct != null) {
                        callback.onViewProductDetails(selectedProduct);
                    }
                }
            }
        });
        
        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
    }
    
    /**
     * Performs search using RMI service
     */
    private void performSearch() {
        String searchText = searchField.getText().trim();
        String selectedCategory = (String) categoryFilterComboBox.getSelectedItem();
        
        if (searchText.isEmpty() && "All Categories".equals(selectedCategory)) {
            // Load all products
            loadData();
            return;
        }
        
        try {
            LogUtil.info("Performing search via RMI - Text: '" + searchText + "', Category: '" + selectedCategory + "'");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            List<Product> searchResults = new ArrayList<>();
            
            if (!searchText.isEmpty()) {
                // Search by name using RMI service
                List<Product> nameResults = productController.getProductService().findProductsByName(searchText);
                if (nameResults != null) {
                    searchResults.addAll(nameResults);
                }
            }
            
            if (!"All Categories".equals(selectedCategory)) {
                // Search by category using RMI service
                List<Product> categoryResults = productController.getProductService().findProductsByCategory(selectedCategory);
                if (categoryResults != null) {
                    if (searchResults.isEmpty()) {
                        searchResults.addAll(categoryResults);
                    } else {
                        // Intersect the results if both search text and category are specified
                        searchResults.retainAll(categoryResults);
                    }
                }
            }
            
            this.productList = searchResults;
            refreshTableData();
            
            LogUtil.info("Search completed via RMI - Found " + searchResults.size() + " products");
            
        } catch (Exception ex) {
            LogUtil.error("Error performing search via RMI", ex);
            JOptionPane.showMessageDialog(this,
                "Error performing search: " + ex.getMessage(),
                "RMI Service Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Applies local filter to the table data
     */
    private void applyFilter() {
        RowFilter<DefaultTableModel, Object> filter = null;
        
        // Get search text
        String searchText = searchField.getText().trim().toLowerCase();
        
        // Get category filter selection
        String categorySelection = (String) categoryFilterComboBox.getSelectedItem();
        
        // Combined filter for search text and category selection
        if (!searchText.isEmpty() || !"All Categories".equals(categorySelection)) {
            filter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    boolean matchesSearch = true;
                    boolean matchesCategory = true;
                    
                    // Apply search text filter
                    if (!searchText.isEmpty()) {
                        matchesSearch = false;
                        for (int i = 1; i < entry.getValueCount(); i++) { // Skip ID column
                            Object value = entry.getValue(i);
                            if (value != null && value.toString().toLowerCase().contains(searchText)) {
                                matchesSearch = true;
                                break;
                            }
                        }
                    }
                    
                    // Apply category filter
                    if (!"All Categories".equals(categorySelection)) {
                        String categoryValue = entry.getStringValue(5); // Category column (index 5)
                        matchesCategory = categorySelection.equals(categoryValue);
                    }
                    
                    return matchesSearch && matchesCategory;
                }
            };
        }
        
        tableSorter.setRowFilter(filter);
    }
    
    /**
     * Refreshes the table data with the current product list
     */
    private void refreshTableData() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Populate the table with data
        for (Product product : productList) {
            String supplierName = "N/A";
            if (product.getSupplier() != null) {
                supplierName = product.getSupplier().getName();
            } else if (product.getSupplierId() > 0) {
                supplierName = "ID: " + product.getSupplierId();
            }
            
            Object[] rowData = {
                product.getId(),
                product.getProductCode(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory(),
                supplierName
            };
            tableModel.addRow(rowData);
        }
        
        // Reset selection and filters
        productTable.clearSelection();
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        viewDetailsButton.setEnabled(false);
        
        // Apply any current filter
        applyFilter();
    }
    
    /**
     * Gets the Product object corresponding to a specific table row
     * 
     * @param modelRow Row index in the table model
     * @return The Product object or null if not found
     */
    private Product getProductAtRow(int modelRow) {
        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
            int productId = (int) tableModel.getValueAt(modelRow, 0);
            for (Product product : productList) {
                if (product.getId() == productId) {
                    return product;
                }
            }
        }
        return null;
    }
    
    /**
     * Deletes a product using RMI service
     * 
     * @param product The product to delete
     */
    private void deleteProduct(Product product) {
        try {
            LogUtil.info("Deleting product via RMI: " + product.getName());
            Product deletedProduct = productController.getProductService().deleteProduct(product);
            
            if (deletedProduct != null) {
                productList.removeIf(p -> p.getId() == product.getId());
                refreshTableData();
                LogUtil.info("Product deleted successfully via RMI");
                
                JOptionPane.showMessageDialog(this,
                    "Product deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (callback != null) {
                    callback.onDeleteProduct(product);
                }
            } else {
                LogUtil.warn("Failed to delete product via RMI");
                JOptionPane.showMessageDialog(this,
                    "Failed to delete product. It may be referenced by other records.",
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            LogUtil.error("Error deleting product via RMI", ex);
            JOptionPane.showMessageDialog(this,
                "Error deleting product: " + ex.getMessage(),
                "RMI Service Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Updates the product list and refreshes the table
     * 
     * @param products New list of products
     */
    public void updateProducts(List<Product> products) {
        this.productList = products != null ? products : new ArrayList<>();
        refreshTableData();
        LogUtil.info("Product list updated with " + this.productList.size() + " products");
    }
    
    /**
     * Adds a product to the list and refreshes the table
     * 
     * @param product Product to add
     */
    public void addProduct(Product product) {
        if (product != null && product.getId() > 0) {
            this.productList.add(product);
            refreshTableData();
            LogUtil.info("Product added to list: " + product.getName());
        }
    }
    
    /**
     * Updates a product in the list and refreshes the table
     * 
     * @param product Product to update
     */
    public void updateProduct(Product product) {
        if (product != null) {
            for (int i = 0; i < productList.size(); i++) {
                if (productList.get(i).getId() == product.getId()) {
                    productList.set(i, product);
                    break;
                }
            }
            refreshTableData();
            LogUtil.info("Product updated in list: " + product.getName());
        }
    }
    
    /**
     * Removes a product from the list and refreshes the table
     * 
     * @param product Product to remove
     */
    public void removeProduct(Product product) {
        if (product != null) {
            productList.removeIf(p -> p.getId() == product.getId());
            refreshTableData();
            LogUtil.info("Product removed from list: " + product.getName());
        }
    }
    
    /**
     * Gets the current product list
     * 
     * @return List of current products
     */
    public List<Product> getProductList() {
        return new ArrayList<>(productList);
    }
    
    /**
     * Gets the currently selected product
     * 
     * @return Selected product or null if none selected
     */
    public Product getSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedRow = productTable.convertRowIndexToModel(selectedRow);
            return getProductAtRow(selectedRow);
        }
        return null;
    }
    
    /**
     * Refreshes the category filter options from RMI service
     */
    public void refreshCategoryFilter() {
        try {
            String[] newCategories = loadCategoryOptions();
            String currentSelection = (String) categoryFilterComboBox.getSelectedItem();
            
            // Update combo box model
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(newCategories);
            categoryFilterComboBox.setModel(model);
            
            // Try to restore previous selection
            if (currentSelection != null) {
                for (int i = 0; i < newCategories.length; i++) {
                    if (newCategories[i].equals(currentSelection)) {
                        categoryFilterComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            LogUtil.info("Category filter refreshed");
        } catch (Exception ex) {
            LogUtil.error("Error refreshing category filter", ex);
        }
    }
    
    /**
     * Clears the search and filter criteria
     */
    public void clearSearchAndFilter() {
        searchField.setText("");
        categoryFilterComboBox.setSelectedIndex(0); // "All Categories"
        loadData(); // Reload all data
        LogUtil.info("Search and filter cleared");
    }
    
    /**
     * Gets low stock products using RMI service
     * 
     * @param threshold Stock threshold
     */
    public void showLowStockProducts(int threshold) {
        try {
            LogUtil.info("Loading low stock products via RMI with threshold: " + threshold);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            List<Product> lowStockProducts = productController.getProductService().findLowStockProducts(threshold);
            if (lowStockProducts != null) {
                this.productList = lowStockProducts;
                refreshTableData();
                
                JOptionPane.showMessageDialog(this,
                    "Found " + lowStockProducts.size() + " products with stock below " + threshold,
                    "Low Stock Products",
                    JOptionPane.INFORMATION_MESSAGE);
                
                LogUtil.info("Low stock search completed - Found " + lowStockProducts.size() + " products");
            } else {
                LogUtil.warn("Received null low stock products list from RMI service");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading low stock products via RMI", ex);
            JOptionPane.showMessageDialog(this,
                "Error loading low stock products: " + ex.getMessage(),
                "RMI Service Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Shows products by supplier using RMI service
     * 
     * @param supplier The supplier to filter by
     */
    public void showProductsBySupplier(Supplier supplier) {
        if (supplier == null) {
            return;
        }
        
        try {
            LogUtil.info("Loading products by supplier via RMI: " + supplier.getName());
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            List<Product> supplierProducts = productController.getProductService().findProductsBySupplier(supplier);
            if (supplierProducts != null) {
                this.productList = supplierProducts;
                refreshTableData();
                
                JOptionPane.showMessageDialog(this,
                    "Found " + supplierProducts.size() + " products from supplier: " + supplier.getName(),
                    "Products by Supplier",
                    JOptionPane.INFORMATION_MESSAGE);
                
                LogUtil.info("Supplier products search completed - Found " + supplierProducts.size() + " products");
            } else {
                LogUtil.warn("Received null supplier products list from RMI service");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading products by supplier via RMI", ex);
            JOptionPane.showMessageDialog(this,
                "Error loading products by supplier: " + ex.getMessage(),
                "RMI Service Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Gets the product controller
     * 
     * @return The ProductController instance
     */
    public ProductController getProductController() {
        return productController;
    }
    
    /**
     * Sets the product controller (useful for dependency injection)
     * 
     * @param productController The new ProductController instance
     */
    public void setProductController(ProductController productController) {
        this.productController = productController;
        LogUtil.info("Product controller updated in list view");
    }
    
    /**
     * Exports the current product list to CSV format
     * 
     * @return CSV content as string
     */
    public String exportToCSV() {
        StringBuilder csv = new StringBuilder();
        
        // Add header
        csv.append("ID,Product Code,Name,Price,Stock,Category,Supplier\n");
        
        // Add data rows
        for (Product product : productList) {
            String supplierName = "N/A";
            if (product.getSupplier() != null) {
                supplierName = product.getSupplier().getName().replace(",", ";"); // Escape commas
            } else if (product.getSupplierId() > 0) {
                supplierName = "ID: " + product.getSupplierId();
            }
            
            csv.append(product.getId()).append(",")
               .append(product.getProductCode()).append(",")
               .append(product.getName().replace(",", ";")).append(",") // Escape commas
               .append(product.getPrice()).append(",")
               .append(product.getStockQuantity()).append(",")
               .append(product.getCategory().replace(",", ";")).append(",") // Escape commas
               .append(supplierName).append("\n");
        }
        
        LogUtil.info("Product list exported to CSV format - " + productList.size() + " rows");
        return csv.toString();
    }
    
    /**
     * Gets table statistics
     * 
     * @return Statistics string
     */
    public String getTableStatistics() {
        int totalProducts = productList.size();
        int displayedRows = productTable.getRowCount();
        
        // Calculate low stock count
        int lowStockCount = 0;
        for (Product product : productList) {
            if (product.getStockQuantity() < 10) {
                lowStockCount++;
            }
        }
        
        return String.format("Total: %d products | Displayed: %d | Low stock: %d", 
                           totalProducts, displayedRows, lowStockCount);
    }
}