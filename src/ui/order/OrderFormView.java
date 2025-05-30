package ui.order;

import model.Order;
import model.OrderItem;
import model.Product;
import model.Customer;
import service.OrderService;
import service.CustomerService;
import service.ProductService;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import ui.UIFactory;
import ui.DialogFactory;

/**
 * RMI-based Form view for creating and editing order records.
 * Provides fields for all order properties and manages order items using RMI services.
 */
public class OrderFormView extends JPanel {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private OrderService orderService;
    private CustomerService customerService;
    private ProductService productService;
    
    // Form components
    private JTextField orderIdField;
    private JComboBox<Customer> customerComboBox;
    private JTextField orderDateField;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> paymentMethodComboBox;
    
    // Order items table
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // Total display
    private JLabel totalValueLabel;
    
    // Validation labels
    private JLabel orderIdValidationLabel;
    private JLabel customerValidationLabel;
    private JLabel dateValidationLabel;
    
    // Action buttons
    private JButton saveButton;
    private JButton cancelButton;
    private JButton addItemButton;
    private JButton removeItemButton;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Mode flag
    private boolean editMode = false;
    private Order currentOrder;
    
    // Callback for form submission
    private FormSubmissionCallback callback;
    
    /**
     * Interface for form submission callback
     */
    public interface FormSubmissionCallback {
        void onSave(Order order);
        void onCancel();
    }
    
    /**
     * Constructor for create mode
     * 
     * @param callback Callback for form actions
     */
    public OrderFormView(FormSubmissionCallback callback) {
        this.callback = callback;
        this.editMode = false;
        
        // Initialize RMI connections
        initializeRMIConnection();
        
        initializeUI();
        loadFormData();
        setupInitialValues();
    }
    
    /**
     * Constructor for edit mode
     * 
     * @param order Order to edit
     * @param callback Callback for form actions
     */
    public OrderFormView(Order order, FormSubmissionCallback callback) {
        this.callback = callback;
        this.editMode = true;
        this.currentOrder = order;
        
        // Initialize RMI connections
        initializeRMIConnection();
        
        // Load complete order details if needed
        if (order != null && orderService != null) {
            loadOrderDetails();
        }
        
        initializeUI();
        loadFormData();
        populateFields(this.currentOrder);
    }
    
    /**
     * Initializes the RMI connections to the server
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to Order services at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            orderService = (OrderService) registry.lookup("orderService");
            customerService = (CustomerService) registry.lookup("customerService");
            productService = (ProductService) registry.lookup("productService");
            LogUtil.info("Successfully connected to Order services");
        } catch (Exception e) {
            LogUtil.error("Failed to connect to Order services", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            this,
            "Failed to connect to the Order Service.\nPlease ensure the server is running and try again.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Loads order details with all related data
     */
    private void loadOrderDetails() {
        try {
            if (currentOrder != null && orderService != null) {
                // Load complete order details using RMI
                Order detailedOrder = orderService.getOrderWithDetails(currentOrder.getId());
                if (detailedOrder != null) {
                    this.currentOrder = detailedOrder;
                    LogUtil.info("Order details loaded successfully for order ID: " + currentOrder.getId());
                }
            }
        } catch (Exception e) {
            LogUtil.error("Failed to load order details", e);
            JOptionPane.showMessageDialog(
                this,
                "Error loading order details: " + e.getMessage(),
                "Data Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 20));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the form header
        JPanel headerPanel = UIFactory.createModuleHeaderPanel(editMode ? "Edit Order" : "New Order");
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
        formPanel.add(createOrderItemsSection());
        
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
        JPanel sectionPanel = UIFactory.createFormSection("Order Information");
        
        // Use GridBagLayout for form layout
        sectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Order ID field
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.15;
        JLabel orderIdLabel = new JLabel("Order ID:");
        sectionPanel.add(orderIdLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.35;
        orderIdField = new JTextField();
        orderIdField.setEnabled(!editMode); // Disable in edit mode
        sectionPanel.add(orderIdField, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.5;
        orderIdValidationLabel = new JLabel("");
        orderIdValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        orderIdValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(orderIdValidationLabel, gbc);
        
        // Customer field
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.15;
        JLabel customerLabel = new JLabel("Customer:");
        sectionPanel.add(customerLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.35;
        
        // Add customer combo box
        customerComboBox = new JComboBox<>();
        // Set the renderer to display customer name
        customerComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (value instanceof Customer) {
                    setText(((Customer) value).getFullName());
                }
                
                return this;
            }
        });
        
        sectionPanel.add(customerComboBox, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.5;
        customerValidationLabel = new JLabel("");
        customerValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        customerValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(customerValidationLabel, gbc);
        
        // Order date field
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        JLabel orderDateLabel = new JLabel("Order Date:");
        sectionPanel.add(orderDateLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.35;
        orderDateField = UIFactory.createDateField("YYYY-MM-DD");
        sectionPanel.add(orderDateField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.5;
        dateValidationLabel = new JLabel("");
        dateValidationLabel.setForeground(UIFactory.ERROR_COLOR);
        dateValidationLabel.setFont(UIFactory.SMALL_FONT);
        sectionPanel.add(dateValidationLabel, gbc);
        
        // Status field
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        JLabel statusLabel = new JLabel("Status:");
        sectionPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.35;
        String[] statuses = {
            "Pending",
            "Processing", 
            "Shipped",
            "Delivered",
            "Cancelled"
        };
        statusComboBox = UIFactory.createComboBox(statuses);
        sectionPanel.add(statusComboBox, gbc);
        
        // Payment method field
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.15;
        JLabel paymentMethodLabel = new JLabel("Payment Method:");
        sectionPanel.add(paymentMethodLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.35;
        String[] paymentMethods = {
            "Credit Card",
            "Debit Card", 
            "PayPal",
            "Bank Transfer",
            "Cash on Delivery"
        };
        paymentMethodComboBox = UIFactory.createComboBox(paymentMethods);
        sectionPanel.add(paymentMethodComboBox, gbc);
        
        return sectionPanel;
    }
    
    private JPanel createOrderItemsSection() {
        JPanel sectionPanel = UIFactory.createFormSection("Order Items");
        sectionPanel.setLayout(new BorderLayout(0, 10));
        
        // Create table for order items
        String[] columnNames = {"Product", "Quantity", "Unit Price", "Subtotal"};
        itemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only quantity is editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Integer.class; // Quantity
                if (columnIndex == 2 || columnIndex == 3) return BigDecimal.class; // Price columns
                return Object.class;
            }
            
            @Override
            public void setValueAt(Object value, int row, int column) {
                super.setValueAt(value, row, column);
                
                // Update subtotal when quantity changes
                if (column == 1 && row < orderItems.size()) {
                    try {
                        int quantity = (Integer) value;
                        if (quantity <= 0) {
                            quantity = 1;
                            super.setValueAt(quantity, row, column);
                        }
                        
                        OrderItem item = orderItems.get(row);
                        item.setQuantity(quantity);
                        
                        // Update subtotal in table
                        super.setValueAt(item.getSubtotal(), row, 3);
                        
                        // Recalculate order total
                        updateOrderTotal();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        
        itemsTable = UIFactory.createStyledTable(itemsTableModel);
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(250); // Product column wider
        
        JScrollPane scrollPane = UIFactory.createScrollPane(itemsTable);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        sectionPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create panel for buttons and total
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        
        // Buttons for adding/removing items
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        
        addItemButton = UIFactory.createPrimaryButton("Add Item");
        removeItemButton = UIFactory.createDangerButton("Remove Item");
        removeItemButton.setEnabled(false); // Disabled until selection
        
        buttonPanel.add(addItemButton);
        buttonPanel.add(removeItemButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        
        // Total amount display
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setOpaque(false);
        
        JLabel totalLabel = new JLabel("Order Total: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        totalValueLabel = new JLabel("$0.00");
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValueLabel.setForeground(UIFactory.PRIMARY_COLOR);
        
        totalPanel.add(totalLabel);
        totalPanel.add(totalValueLabel);
        
        bottomPanel.add(totalPanel, BorderLayout.EAST);
        
        sectionPanel.add(bottomPanel, BorderLayout.SOUTH);
        
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
                    saveOrder();
                }
            }
        });
        
        // Add item button action
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddItemDialog();
            }
        });
        
        // Remove item button action
        removeItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });
        
        // Table selection listener
        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                removeItemButton.setEnabled(itemsTable.getSelectedRow() >= 0);
            }
        });
    }
    
    /**
     * Loads form data from RMI services
     */
    private void loadFormData() {
        loadCustomers();
    }
    
    /**
     * Loads all customers for the combo box using RMI
     */
    private void loadCustomers() {
        try {
            if (customerService != null) {
                List<Customer> customers = customerService.findAllCustomers();
                
                DefaultComboBoxModel<Customer> model = new DefaultComboBoxModel<>();
                for (Customer customer : customers) {
                    model.addElement(customer);
                }
                
                customerComboBox.setModel(model);
                LogUtil.info("Loaded " + customers.size() + " customers");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading customers", ex);
            JOptionPane.showMessageDialog(this,
                "Error loading customers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows dialog to add a new item to the order using RMI
     */
    private void showAddItemDialog() {
        // Create a product selection dialog
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog; 

        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Add Product", true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "Add Product", true);
        } else {
            dialog = new JDialog();
            dialog.setTitle("Add Product");
            dialog.setModal(true);
        }
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Product selection
        JPanel selectionPanel = new JPanel(new BorderLayout(0, 5));
        selectionPanel.add(new JLabel("Select Product:"), BorderLayout.NORTH);
        
        // Create product list with search
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        JTextField searchField = UIFactory.createSearchField("Search products...", 0);
        JButton searchButton = UIFactory.createSecondaryButton("Search");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        selectionPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Product list
        DefaultListModel<Product> productListModel = new DefaultListModel<>();
        JList<Product> productList = new JList<>(productListModel);
        
        // Set the renderer to display product name and code
        productList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (value instanceof Product) {
                    Product product = (Product) value;
                    setText(product.getName() + " (" + product.getProductCode() + ") - " + 
                           NumberFormat.getCurrencyInstance().format(product.getPrice()));
                }
                
                return this;
            }
        });
        
        // Load products using RMI
        loadProductsForDialog(productListModel);
        
        JScrollPane scrollPane = UIFactory.createScrollPane(productList);
        selectionPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Quantity panel
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityPanel.add(new JLabel("Quantity:"));
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 1000, 1);
        JSpinner quantitySpinner = new JSpinner(spinnerModel);
        quantityPanel.add(quantitySpinner);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = UIFactory.createSecondaryButton("Cancel");
        JButton addBtn = UIFactory.createPrimaryButton("Add to Order");
        addBtn.setEnabled(false); // Disabled until selection
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(addBtn);
        
        // Enable add button when product is selected
        productList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                addBtn.setEnabled(productList.getSelectedValue() != null);
            }
        });
        
        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            searchProducts(productListModel, searchText);
        });
        
        // Register button actions
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        addBtn.addActionListener(e -> {
            Product selectedProduct = productList.getSelectedValue();
            if (selectedProduct != null) {
                int quantity = (Integer) quantitySpinner.getValue();
                addOrderItem(selectedProduct, quantity);
                dialog.dispose();
            }
        });
        
        // Add double-click selection
        productList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Product selectedProduct = productList.getSelectedValue();
                    if (selectedProduct != null) {
                        int quantity = (Integer) quantitySpinner.getValue();
                        addOrderItem(selectedProduct, quantity);
                        dialog.dispose();
                    }
                }
            }
        });
        
        panel.add(selectionPanel, BorderLayout.CENTER);
        panel.add(quantityPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Loads products using RMI service
     */
    private void loadProductsForDialog(DefaultListModel<Product> model) {
        try {
            if (productService != null) {
                List<Product> products = productService.findAllProducts();
                model.clear();
                for (Product product : products) {
                    model.addElement(product);
                }
                LogUtil.info("Loaded " + products.size() + " products for dialog");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading products", ex);
            JOptionPane.showMessageDialog(this,
                "Error loading products: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Searches products using RMI service
     */
    private void searchProducts(DefaultListModel<Product> model, String searchText) {
        try {
            if (productService != null) {
                List<Product> products;
                if (searchText.isEmpty()) {
                    products = productService.findAllProducts();
                } else {
                    products = productService.findProductsByName(searchText);
                }
                
                model.clear();
                for (Product product : products) {
                    model.addElement(product);
                }
                LogUtil.info("Search completed, found " + products.size() + " products");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching products", ex);
        }
    }
    
    /**
     * Adds an order item to the order
     * 
     * @param product The product to add
     * @param quantity The quantity to add
     */
    private void addOrderItem(Product product, int quantity) {
        // Check if product already exists in order
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem item = orderItems.get(i);
            if (item.getProduct() != null && item.getProduct().getId() == product.getId()) {
                // Update existing item quantity
                int newQuantity = item.getQuantity() + quantity;
                item.setQuantity(newQuantity);
                itemsTableModel.setValueAt(newQuantity, i, 1);
                itemsTableModel.setValueAt(item.getSubtotal(), i, 3);
                updateOrderTotal();
                return;
            }
        }
        
        // Create new order item
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        orderItems.add(item);
        
        // Add to table
        Object[] rowData = {
            product.getName() + " (" + product.getProductCode() + ")",
            quantity,
            product.getPrice(),
            item.getSubtotal()
        };
        itemsTableModel.addRow(rowData);
        
        // Update order total
        updateOrderTotal();
    }
    
    /**
     * Removes the selected item from the order
     */
    private void removeSelectedItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Remove from list
            orderItems.remove(selectedRow);
            
            // Remove from table
            itemsTableModel.removeRow(selectedRow);
            
            // Update order total
            updateOrderTotal();
            
            // Disable remove button
            removeItemButton.setEnabled(false);
        }
    }
    
    /**
     * Updates the order total based on all items
     */
    private void updateOrderTotal() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItem item : orderItems) {
            total = total.add(item.getSubtotal());
        }
        
        // Update total label
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        totalValueLabel.setText(currencyFormat.format(total));
    }
    
    /**
     * Initializes form with default values for new order
     */
    private void setupInitialValues() {
        // Set default date to today
        orderDateField.setText(LocalDate.now().format(dateFormatter));
        
        // Set default status to Pending
        statusComboBox.setSelectedItem("Pending");
        
        // Set default payment method
        paymentMethodComboBox.setSelectedIndex(0);
        
        // Generate order ID (could be more sophisticated in real app)
        String orderId = "ORD-" + LocalDate.now().getYear() + "-" + 
                        String.format("%04d", (int)(Math.random() * 10000));
        orderIdField.setText(orderId);
    }
    
    /**
     * Validates the form before saving
     * 
     * @return true if form is valid
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Reset validation messages
        orderIdValidationLabel.setText("");
        customerValidationLabel.setText("");
        dateValidationLabel.setText("");
        
        // Validate Order ID
        if (!editMode) { // Only validate in create mode
            if (orderIdField.getText().trim().isEmpty()) {
                orderIdValidationLabel.setText("Order ID is required");
                isValid = false;
            } else {
                // Check if order ID already exists using RMI
                try {
                    if (orderService != null && orderService.orderIdExists(orderIdField.getText().trim())) {
                        orderIdValidationLabel.setText("Order ID already exists");
                        isValid = false;
                    }
                } catch (Exception e) {
                    LogUtil.error("Error checking order ID existence", e);
                }
            }
        }
        
        // Validate Customer
        if (customerComboBox.getSelectedItem() == null) {
            customerValidationLabel.setText("Customer is required");
            isValid = false;
        }
        
        // Validate Order Date
        if (orderDateField.getText().trim().isEmpty()) {
            dateValidationLabel.setText("Order date is required");
            isValid = false;
        } else {
            try {
                LocalDate.parse(orderDateField.getText().trim(), dateFormatter);
            } catch (Exception e) {
                dateValidationLabel.setText("Invalid date format (YYYY-MM-DD)");
                isValid = false;
            }
        }
        
        // Validate Order Items
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Order must have at least one item.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Saves the order using RMI service
     */
    private void saveOrder() {
        try {
            Order order;
            
            if (editMode && currentOrder != null) {
                order = currentOrder;
            } else {
                order = new Order();
                order.setOrderId(orderIdField.getText().trim());
            }
            
            // Set values from form
            Customer selectedCustomer = (Customer) customerComboBox.getSelectedItem();
            order.setCustomer(selectedCustomer);
            order.setOrderDate(LocalDate.parse(orderDateField.getText().trim(), dateFormatter));
            order.setStatus((String) statusComboBox.getSelectedItem());
            order.setPaymentMethod((String) paymentMethodComboBox.getSelectedItem());
            
            // Set order items
            order.setOrderItems(new ArrayList<>(orderItems));
            
            // Calculate total amount
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItem item : orderItems) {
                total = total.add(item.getSubtotal());
            }
            order.setTotalAmount(total);
            
            Order savedOrder;
            
            if (editMode) {
                savedOrder = orderService.updateOrder(order);
            } else {
                savedOrder = orderService.createOrder(order);
            }
            
            if (savedOrder != null) {
                if (callback != null) {
                    callback.onSave(savedOrder);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to " + (editMode ? "update" : "create") + " order.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            LogUtil.error("Error saving order", ex);
            JOptionPane.showMessageDialog(this,
                "Error saving order: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Populates form fields with order data
     * 
     * @param order The order to populate from
     */
    private void populateFields(Order order) {
        if (order == null) {
            return;
        }
        
        // Set field values
        orderIdField.setText(order.getOrderId());
        
        // Set customer
        if (order.getCustomer() != null) {
            for (int i = 0; i < customerComboBox.getItemCount(); i++) {
                Customer customer = customerComboBox.getItemAt(i);
                if (customer.getId() == order.getCustomer().getId()) {
                    customerComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        // Set other fields
        orderDateField.setText(order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "");
        statusComboBox.setSelectedItem(order.getStatus());
        paymentMethodComboBox.setSelectedItem(order.getPaymentMethod());
        
        // Set order items
        orderItems.clear();
        itemsTableModel.setRowCount(0);
        
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                orderItems.add(item);
                
                String productName = "";
                String productCode = "";
                
                if (item.getProduct() != null) {
                    productName = item.getProduct().getName();
                    productCode = item.getProduct().getProductCode();
                }
                
                Object[] rowData = {
                    productName + " (" + productCode + ")",
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
                };
                itemsTableModel.addRow(rowData);
            }
        }
        
        // Update order total
        updateOrderTotal();
    }
    
    /**
     * Checks if the RMI connection is available
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return orderService != null && customerService != null && productService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
        if (isConnected()) {
            loadFormData();
        }
    }
}