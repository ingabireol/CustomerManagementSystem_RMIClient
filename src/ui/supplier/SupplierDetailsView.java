package ui.supplier;

import model.Supplier;
import model.Product;
import util.LogUtil;
import util.RMIConnectionManager;
import service.SupplierService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import ui.UIFactory;

/**
 * RMI-based detailed view for a supplier with associated products.
 * Shows all supplier information and its related products using remote services.
 */
public class SupplierDetailsView extends JPanel {
    // UI Components
    private JLabel nameValueLabel;
    private JLabel codeValueLabel;
    private JLabel contactPersonValueLabel;
    private JLabel emailValueLabel;
    private JLabel phoneValueLabel;
    private JTextArea addressValueArea;
    
    // Products table
    private JTable productsTable;
    private DefaultTableModel productsTableModel;
    
    // Supplier data
    private Supplier supplier;
    
    // RMI Services
    private SupplierService supplierService;
    
    // Callback for view actions
    private DetailsViewCallback callback;
    
    /**
     * Interface for view actions callback
     */
    public interface DetailsViewCallback {
        void onEditSupplier(Supplier supplier);
        void onClose();
    }
    
    /**
     * Constructor
     * 
     * @param supplier The supplier to display
     * @param callback Callback for view actions
     */
    public SupplierDetailsView(Supplier supplier, DetailsViewCallback callback) {
        this.supplier = supplier;
        this.callback = callback;
        
        // Initialize RMI service
        initializeRMIServices();
        
        // Load supplier with products if needed
        loadSupplierDetails();
        
        initializeUI();
        populateData();
    }
    
    /**
     * Initializes RMI service connections
     */
    private void initializeRMIServices() {
        try {
            LogUtil.info("Initializing SupplierService for details view");
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
    
    /**
     * Loads supplier details with products from the server
     */
    private void loadSupplierDetails() {
        if (supplier != null && supplierService != null) {
            try {
                // Load supplier with products using RMI
                Supplier detailedSupplier = supplierService.getSupplierWithProducts(supplier.getId());
                if (detailedSupplier != null) {
                    this.supplier = detailedSupplier;
                    LogUtil.info("Loaded supplier details with " + 
                        (supplier.getProducts() != null ? supplier.getProducts().size() : 0) + " products");
                } else {
                    LogUtil.warn("Could not load detailed supplier information from server");
                }
            } catch (Exception e) {
                LogUtil.error("Error loading supplier details from server", e);
                JOptionPane.showMessageDialog(
                    this,
                    "Error loading supplier details: " + e.getMessage(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 15));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create a split panel for details and products
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.4); // 40% to details, 60% to products
        
        // Create the details panel
        JPanel detailsPanel = createDetailsPanel();
        splitPane.setTopComponent(detailsPanel);
        
        // Create the products panel
        JPanel productsPanel = createProductsPanel();
        splitPane.setBottomComponent(productsPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Create actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        // Create a header with supplier name and code
        JPanel panel = UIFactory.createModuleHeaderPanel("Supplier Details");
        
        if (supplier != null) {
            // Add supplier name to header
            JLabel supplierNameLabel = new JLabel(supplier.getName());
            supplierNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            supplierNameLabel.setForeground(UIFactory.PRIMARY_COLOR);
            panel.add(supplierNameLabel, BorderLayout.EAST);
        }
        
        return panel;
    }
    
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title
        JLabel sectionTitle = new JLabel("Supplier Information");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, BorderLayout.NORTH);
        
        // Create details grid
        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Supplier code
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Supplier Code:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        codeValueLabel = createValueLabel("");
        detailsGrid.add(codeValueLabel, gbc);
        
        // Name
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Name:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        nameValueLabel = createValueLabel("");
        detailsGrid.add(nameValueLabel, gbc);
        
        // Contact Person
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Contact Person:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        contactPersonValueLabel = createValueLabel("");
        detailsGrid.add(contactPersonValueLabel, gbc);
        
        // Email
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Email:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        emailValueLabel = createValueLabel("");
        detailsGrid.add(emailValueLabel, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        phoneValueLabel = createValueLabel("");
        detailsGrid.add(phoneValueLabel, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        detailsGrid.add(createLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.9;
        gbc.fill = GridBagConstraints.BOTH;
        addressValueArea = new JTextArea(4, 20);
        addressValueArea.setEditable(false);
        addressValueArea.setLineWrap(true);
        addressValueArea.setWrapStyleWord(true);
        addressValueArea.setBackground(new Color(0xF8F8F8));
        addressValueArea.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        JScrollPane addressScrollPane = new JScrollPane(addressValueArea);
        addressScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailsGrid.add(addressScrollPane, gbc);
        
        panel.add(detailsGrid, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title with count
        JLabel sectionTitle = new JLabel("Products Supplied");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, BorderLayout.NORTH);
        
        // Create products table
        String[] columnNames = {"ID", "Product Code", "Name", "Price", "Stock", "Category"};
        productsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        productsTable = UIFactory.createStyledTable(productsTableModel);
        
        // Set column widths
        productsTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID column
        productsTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Product Code
        productsTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Name
        
        JScrollPane scrollPane = UIFactory.createScrollPane(productsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        
        JButton closeButton = UIFactory.createSecondaryButton("Close");
        JButton editButton = UIFactory.createWarningButton("Edit Supplier");
        
        panel.add(closeButton);
        panel.add(editButton);
        
        // Add button actions
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null) {
                    callback.onClose();
                }
            }
        });
        
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null && supplier != null) {
                    callback.onEditSupplier(supplier);
                }
            }
        });
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFactory.BODY_FONT);
        label.setForeground(UIFactory.MEDIUM_GRAY);
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFactory.BODY_FONT);
        return label;
    }
    
    private void populateData() {
        if (supplier == null) {
            return;
        }
        
        // Set supplier details
        codeValueLabel.setText(supplier.getSupplierCode() != null ? supplier.getSupplierCode() : "N/A");
        nameValueLabel.setText(supplier.getName() != null ? supplier.getName() : "N/A");
        contactPersonValueLabel.setText(supplier.getContactPerson() != null ? supplier.getContactPerson() : "N/A");
        emailValueLabel.setText(supplier.getEmail() != null ? supplier.getEmail() : "N/A");
        phoneValueLabel.setText(supplier.getPhone() != null ? supplier.getPhone() : "N/A");
        addressValueArea.setText(supplier.getAddress() != null ? supplier.getAddress() : "");
        
        // Populate products table
        populateProductsTable();
    }
    
    /**
     * Populates the products table with supplier's products
     */
    private void populateProductsTable() {
        // Clear existing data
        productsTableModel.setRowCount(0);
        
        List<Product> products = supplier.getProducts();
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                Object[] rowData = {
                    product.getId(),
                    product.getProductCode() != null ? product.getProductCode() : "N/A",
                    product.getName() != null ? product.getName() : "N/A",
                    product.getPrice() != null ? product.getPrice() : "N/A",
                    product.getStockQuantity(),
                    product.getCategory() != null ? product.getCategory() : "N/A"
                };
                productsTableModel.addRow(rowData);
            }
            
            // Update section title with count
            Container parent = productsTable.getParent().getParent().getParent();
            if (parent instanceof JPanel) {
                Component[] components = ((JPanel) parent).getComponents();
                for (Component comp : components) {
                    if (comp instanceof JLabel && ((JLabel) comp).getText().startsWith("Products Supplied")) {
                        ((JLabel) comp).setText("Products Supplied (" + products.size() + ")");
                        break;
                    }
                }
            }
        } else {
            // Show no products message
            Object[] noDataRow = {"", "", "No products found for this supplier", "", "", ""};
            productsTableModel.addRow(noDataRow);
        }
    }
    
    /**
     * Refreshes the supplier data from the server
     */
    public void refreshData() {
        if (supplier != null && supplierService != null) {
            try {
                LogUtil.info("Refreshing supplier details from server");
                Supplier refreshedSupplier = supplierService.getSupplierWithProducts(supplier.getId());
                if (refreshedSupplier != null) {
                    this.supplier = refreshedSupplier;
                    populateData();
                    LogUtil.info("Supplier details refreshed successfully");
                } else {
                    LogUtil.warn("Could not refresh supplier details - supplier not found");
                }
            } catch (Exception e) {
                LogUtil.error("Error refreshing supplier details", e);
                JOptionPane.showMessageDialog(
                    this,
                    "Error refreshing supplier details: " + e.getMessage(),
                    "Refresh Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Gets the current supplier
     * 
     * @return The supplier being displayed
     */
    public Supplier getSupplier() {
        return supplier;
    }
}