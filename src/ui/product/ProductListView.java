package ui.product;

import model.Product;
import model.Supplier;
import dao.ProductDao;

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
 * List view for displaying and managing products.
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
    
    // Product data and DAO
    private List<Product> productList;
    private ProductDao productDao;
    
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
     */
    public ProductListView(ProductListCallback callback) {
        this.callback = callback;
        this.productDao = new ProductDao();
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
     * Loads product data from the database
     */
    private void loadData() {
        try {
            this.productList = productDao.findAllProducts();
            refreshTableData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading product data: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = UIFactory.createModuleHeaderPanel("Products");
        
        // Create search and filter section on the right side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        // Category filter combo box
        String[] categoryOptions = {"All Categories", "Electronics", "Clothing", "Food & Beverages", "Home & Garden", "Office Supplies", "Other"};
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
                applyFilter();
            }
        });
        
        // Add filter change action
        categoryFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        
        // Create the table model with column names
        String[] columnNames = {"ID", "Product Code", "Name", "Price", "Stock", "Category"};
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
     * Refreshes the table data with the current product list
     */
    private void refreshTableData() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Populate the table with data
        for (Product product : productList) {
            Object[] rowData = {
                product.getId(),
                product.getProductCode(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory()
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
     * Applies search and filter criteria to the table
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
     * Deletes a product from the database
     * 
     * @param product The product to delete
     */
    private void deleteProduct(Product product) {
        try {
            int result = productDao.deleteProduct(product.getId());
            if (result > 0) {
                productList.removeIf(p -> p.getId() == product.getId());
                refreshTableData();
                JOptionPane.showMessageDialog(this,
                    "Product deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (callback != null) {
                    callback.onDeleteProduct(product);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete product. It may be referenced by other records.",
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error deleting product: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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
        }
    }
}