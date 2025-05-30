package ui.supplier;

import model.Supplier;
import model.Product;
import dao.SupplierDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import ui.UIFactory;

/**
 * Detailed view for a supplier with associated products.
 * Shows all supplier information and its related products.
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
    private SupplierDao supplierDao;
    
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
        this.supplierDao = new SupplierDao();
        
        // Load supplier with products if needed
        if (supplier != null && (supplier.getProducts() == null || supplier.getProducts().isEmpty())) {
            this.supplier = supplierDao.getSupplierWithProducts(supplier.getId());
        }
        
        initializeUI();
        populateData();
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
        codeValueLabel.setText(supplier.getSupplierCode());
        nameValueLabel.setText(supplier.getName());
        contactPersonValueLabel.setText(supplier.getContactPerson());
        emailValueLabel.setText(supplier.getEmail());
        phoneValueLabel.setText(supplier.getPhone());
        addressValueArea.setText(supplier.getAddress());
        
        // Populate products table
        productsTableModel.setRowCount(0);
        
        List<Product> products = supplier.getProducts();
        if (products != null) {
            for (Product product : products) {
                Object[] rowData = {
                    product.getId(),
                    product.getProductCode(),
                    product.getName(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getCategory()
                };
                productsTableModel.addRow(rowData);
            }
        }
    }
}