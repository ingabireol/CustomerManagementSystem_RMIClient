package ui.product;

import model.Product;
import model.Supplier;
import dao.ProductDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.text.NumberFormat;
import ui.UIFactory;

/**
 * Detail view for showing product information.
 * Displays complete product details including supplier information.
 */
public class ProductDetailsView extends JPanel {
    // UI Components
    private JLabel nameValueLabel;
    private JLabel codeValueLabel;
    private JLabel priceValueLabel;
    private JLabel stockValueLabel;
    private JLabel categoryValueLabel;
    private JLabel supplierValueLabel;
    private JTextArea descriptionValueArea;
    
    // Indicators
    private JPanel stockIndicator;
    
    // Product data
    private Product product;
    private ProductDao productDao;
    
    // Callback for view actions
    private DetailsViewCallback callback;
    
    /**
     * Interface for view actions callback
     */
    public interface DetailsViewCallback {
        void onEditProduct(Product product);
        void onClose();
    }
    
    /**
     * Constructor
     * 
     * @param product The product to display
     * @param callback Callback for view actions
     */
    public ProductDetailsView(Product product, DetailsViewCallback callback) {
        this.product = product;
        this.callback = callback;
        this.productDao = new ProductDao();
        
        // Load product with supplier if needed
        if (product != null && product.getSupplier() == null && product.getSupplierId() > 0) {
            this.product = productDao.getProductWithSupplier(product.getId());
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
        
        // Create the main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Create the details panel
        JPanel detailsPanel = createDetailsPanel();
        contentPanel.add(detailsPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Create actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        // Create a header with product name
        JPanel panel = UIFactory.createModuleHeaderPanel("Product Details");
        
        if (product != null) {
            // Add product name to header
            JLabel productNameLabel = new JLabel(product.getName());
            productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            productNameLabel.setForeground(UIFactory.PRIMARY_COLOR);
            panel.add(productNameLabel, BorderLayout.EAST);
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
        
        // Create a main content panel with two sections side by side
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBackground(Color.WHITE);
        
        // Left section - Basic info
        JPanel basicInfoPanel = createBasicInfoPanel();
        contentPanel.add(basicInfoPanel);
        
        // Right section - Additional info
        JPanel additionalInfoPanel = createAdditionalInfoPanel();
        contentPanel.add(additionalInfoPanel);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Description section at the bottom
        JPanel descriptionPanel = createDescriptionPanel();
        panel.add(descriptionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Section title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel sectionTitle = new JLabel("Basic Information");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, gbc);
        
        // Product code
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        panel.add(createLabel("Product Code:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        codeValueLabel = createValueLabel("");
        panel.add(codeValueLabel, gbc);
        
        // Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(createLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameValueLabel = createValueLabel("");
        panel.add(nameValueLabel, gbc);
        
        // Price
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panel.add(createLabel("Price:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        priceValueLabel = createValueLabel("");
        priceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceValueLabel.setForeground(UIFactory.PRIMARY_COLOR);
        panel.add(priceValueLabel, gbc);
        
        // Add glue to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    private JPanel createAdditionalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Section title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel sectionTitle = new JLabel("Inventory Information");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, gbc);
        
        // Stock level with indicator
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        panel.add(createLabel("Stock:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel stockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        stockPanel.setOpaque(false);
        
        stockValueLabel = createValueLabel("");
        stockIndicator = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(12, 12);
            }
        };
        stockIndicator.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        
        stockPanel.add(stockValueLabel);
        stockPanel.add(stockIndicator);
        
        panel.add(stockPanel, gbc);
        
        // Category
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(createLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        categoryValueLabel = createValueLabel("");
        panel.add(categoryValueLabel, gbc);
        
        // Supplier
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panel.add(createLabel("Supplier:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        supplierValueLabel = createValueLabel("");
        panel.add(supplierValueLabel, gbc);
        
        // Add glue to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Title
        JLabel titleLabel = new JLabel("Description");
        titleLabel.setFont(UIFactory.HEADER_FONT);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Description text area
        descriptionValueArea = new JTextArea(5, 20);
        descriptionValueArea.setEditable(false);
        descriptionValueArea.setLineWrap(true);
        descriptionValueArea.setWrapStyleWord(true);
        descriptionValueArea.setBackground(new Color(0xF8F8F8));
        descriptionValueArea.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        
        JScrollPane scrollPane = new JScrollPane(descriptionValueArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        
        JButton closeButton = UIFactory.createSecondaryButton("Close");
        JButton editButton = UIFactory.createWarningButton("Edit Product");
        
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
                if (callback != null && product != null) {
                    callback.onEditProduct(product);
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
        if (product == null) {
            return;
        }
        
        // Set product details
        codeValueLabel.setText(product.getProductCode());
        nameValueLabel.setText(product.getName());
        
        // Format price
        if (product.getPrice() != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            priceValueLabel.setText(currencyFormat.format(product.getPrice()));
        } else {
            priceValueLabel.setText("$0.00");
        }
        
        // Set stock level and indicator color
        stockValueLabel.setText(String.valueOf(product.getStockQuantity()));
        if (product.getStockQuantity() <= 0) {
            stockIndicator.setBackground(UIFactory.ERROR_COLOR);
            stockValueLabel.setText(product.getStockQuantity() + " - Out of stock");
        } else if (product.getStockQuantity() < 10) {
            stockIndicator.setBackground(UIFactory.WARNING_COLOR);
            stockValueLabel.setText(product.getStockQuantity() + " - Low stock");
        } else {
            stockIndicator.setBackground(UIFactory.SUCCESS_COLOR);
            stockValueLabel.setText(String.valueOf(product.getStockQuantity()));
        }
        
        categoryValueLabel.setText(product.getCategory());
        
        // Set supplier info
        Supplier supplier = product.getSupplier();
        if (supplier != null) {
            supplierValueLabel.setText(supplier.getName());
        } else if (product.getSupplierId() > 0) {
            supplierValueLabel.setText("Supplier ID: " + product.getSupplierId());
        } else {
            supplierValueLabel.setText("No supplier");
        }
        
        // Set description
        descriptionValueArea.setText(product.getDescription());
    }
}