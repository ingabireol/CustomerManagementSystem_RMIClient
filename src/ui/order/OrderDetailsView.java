package ui.order;

import model.Order;
import model.OrderItem;
import model.Customer;
import model.Invoice;
import service.OrderService;
import service.InvoiceService;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.text.NumberFormat;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import ui.UIFactory;

/**
 * RMI-based Detail view for showing order information and line items.
 * Displays complete order details including customer information and all items ordered.
 */
public class OrderDetailsView extends JPanel {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private OrderService orderService;
    private InvoiceService invoiceService;
    
    // UI Components
    private JLabel orderIdValueLabel;
    private JLabel customerValueLabel;
    private JLabel orderDateValueLabel;
    private JLabel statusValueLabel;
    private JLabel totalValueLabel;
    private JLabel paymentMethodValueLabel;
    
    // Status indicator
    private JPanel statusIndicator;
    
    // Order items table
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    
    // Invoices table
    private JTable invoicesTable;
    private DefaultTableModel invoicesTableModel;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Order data
    private Order order;
    
    // Callback for view actions
    private DetailsViewCallback callback;
    
    /**
     * Interface for view actions callback
     */
    public interface DetailsViewCallback {
        void onEditOrder(Order order);
        void onViewCustomer(Customer customer);
        void onCreateInvoice(Order order);
        void onClose();
    }
    
    /**
     * Constructor
     * 
     * @param order The order to display
     * @param callback Callback for view actions
     */
    public OrderDetailsView(Order order, DetailsViewCallback callback) {
        this.order = order;
        this.callback = callback;
        
        // Initialize RMI connections
        initializeRMIConnection();
        
        // Load order with details if needed
        if (order != null && orderService != null) {
            loadOrderDetails();
        }
        
        initializeUI();
        populateData();
    }
    
    /**
     * Initializes the RMI connections to the server
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to OrderService and InvoiceService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            orderService = (OrderService) registry.lookup("orderService");
            invoiceService = (InvoiceService) registry.lookup("invoiceService");
            LogUtil.info("Successfully connected to OrderService and InvoiceService");
        } catch (Exception e) {
            LogUtil.error("Failed to connect to Order/Invoice services", e);
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
            if (order != null && orderService != null) {
                // Load complete order details using RMI
                Order detailedOrder = orderService.getOrderWithDetails(order.getId());
                if (detailedOrder != null) {
                    this.order = detailedOrder;
                    LogUtil.info("Order details loaded successfully for order ID: " + order.getId());
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
        setLayout(new BorderLayout(0, 15));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create a tabbed pane for order details and invoices
        JTabbedPane tabbedPane = new JTabbedPane();
        UIFactory.styleTabbedPane(tabbedPane);
        
        // Create the details panel
        JPanel detailsPanel = createMainDetailsPanel();
        tabbedPane.addTab("Order Details", detailsPanel);
        
        // Create invoices panel
        JPanel invoicesPanel = createInvoicesPanel();
        tabbedPane.addTab("Invoices", invoicesPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        // Create a header with order ID and status
        JPanel panel = UIFactory.createModuleHeaderPanel("Order Details");
        
        if (order != null) {
            // Create a panel for the right side with order ID and status
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightPanel.setOpaque(false);
            
            // Order ID
            JLabel orderIdLabel = new JLabel("Order: " + order.getOrderId());
            orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            orderIdLabel.setForeground(UIFactory.PRIMARY_COLOR);
            
            // Status with colored indicator
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            statusPanel.setOpaque(false);
            
            statusIndicator = new JPanel() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(15, 15);
                }
            };
            statusIndicator.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            statusIndicator.setOpaque(true);
            
            // Set color based on status
            updateStatusIndicator(order.getStatus());
            
            JLabel statusLabel = new JLabel(order.getStatus());
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            statusPanel.add(statusIndicator);
            statusPanel.add(statusLabel);
            
            rightPanel.add(orderIdLabel);
            rightPanel.add(statusPanel);
            
            panel.add(rightPanel, BorderLayout.EAST);
        }
        
        return panel;
    }
    
    /**
     * Updates the status indicator color based on status
     */
    private void updateStatusIndicator(String status) {
        if (statusIndicator != null && status != null) {
            switch (status) {
                case "Delivered":
                    statusIndicator.setBackground(UIFactory.SUCCESS_COLOR);
                    break;
                case "Processing":
                case "Shipped":
                    statusIndicator.setBackground(UIFactory.WARNING_COLOR);
                    break;
                case "Cancelled":
                    statusIndicator.setBackground(UIFactory.ERROR_COLOR);
                    break;
                default:
                    statusIndicator.setBackground(UIFactory.MEDIUM_GRAY);
                    break;
            }
        }
    }
    
    private JPanel createMainDetailsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Create order info section
        JPanel orderInfoPanel = createOrderInfoPanel();
        mainPanel.add(orderInfoPanel, BorderLayout.NORTH);
        
        // Create order items section
        JPanel orderItemsPanel = createOrderItemsPanel();
        mainPanel.add(orderItemsPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createOrderInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title
        JLabel sectionTitle = new JLabel("Order Information");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, BorderLayout.NORTH);
        
        // Create details grid
        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Order ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Order ID:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.4;
        orderIdValueLabel = createValueLabel("");
        detailsGrid.add(orderIdValueLabel, gbc);
        
        // Customer
        gbc.gridx = 2; gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Customer:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.4;
        customerValueLabel = createValueLabel("");
        
        // Add view customer button
        JPanel customerPanel = new JPanel(new BorderLayout(5, 0));
        customerPanel.setOpaque(false);
        customerPanel.add(customerValueLabel, BorderLayout.CENTER);
        
        JButton viewCustomerButton = UIFactory.createSecondaryButton("View");
        viewCustomerButton.setPreferredSize(new Dimension(60, 25));
        viewCustomerButton.addActionListener(e -> {
            if (callback != null && order != null && order.getCustomer() != null) {
                callback.onViewCustomer(order.getCustomer());
            }
        });
        customerPanel.add(viewCustomerButton, BorderLayout.EAST);
        
        detailsGrid.add(customerPanel, gbc);
        
        // Order Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Order Date:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.4;
        orderDateValueLabel = createValueLabel("");
        detailsGrid.add(orderDateValueLabel, gbc);
        
        // Status
        gbc.gridx = 2; gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Status:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.4;
        statusValueLabel = createValueLabel("");
        detailsGrid.add(statusValueLabel, gbc);
        
        // Total Amount
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Total Amount:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.4;
        totalValueLabel = createValueLabel("");
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValueLabel.setForeground(UIFactory.PRIMARY_COLOR);
        detailsGrid.add(totalValueLabel, gbc);
        
        // Payment Method
        gbc.gridx = 2; gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Payment Method:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.4;
        paymentMethodValueLabel = createValueLabel("");
        detailsGrid.add(paymentMethodValueLabel, gbc);
        
        panel.add(detailsGrid, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOrderItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title
        JLabel sectionTitle = new JLabel("Order Items");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, BorderLayout.NORTH);
        
        // Create order items table
        String[] columnNames = {"Product Code", "Product Name", "Quantity", "Unit Price", "Subtotal"};
        itemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class; // Quantity
                if (columnIndex == 3 || columnIndex == 4) return BigDecimal.class; // Prices
                return String.class;
            }
        };
        
        itemsTable = UIFactory.createStyledTable(itemsTableModel);
        
        // Set column widths
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Product Code
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Product Name
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(70);  // Quantity
        
        JScrollPane scrollPane = UIFactory.createScrollPane(itemsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add order total summary at the bottom
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setOpaque(false);
        totalPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel totalLabel = new JLabel("Order Total: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel totalAmountLabel = new JLabel();
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalAmountLabel.setForeground(UIFactory.PRIMARY_COLOR);
        
        // Set total amount
        if (order != null && order.getTotalAmount() != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            totalAmountLabel.setText(currencyFormat.format(order.getTotalAmount()));
        } else {
            totalAmountLabel.setText("$0.00");
        }
        
        totalPanel.add(totalLabel);
        totalPanel.add(totalAmountLabel);
        
        panel.add(totalPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createInvoicesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create panel header with title and buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel sectionTitle = new JLabel("Order Invoices");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        headerPanel.add(sectionTitle, BorderLayout.WEST);
        
        // Add create invoice button
        JButton createInvoiceButton = UIFactory.createPrimaryButton("Create Invoice");
        createInvoiceButton.addActionListener(e -> {
            if (callback != null && order != null) {
                callback.onCreateInvoice(order);
            }
        });
        
        // Only enable if order status is Delivered
        if (order != null) {
            createInvoiceButton.setEnabled("Delivered".equals(order.getStatus()));
        } else {
            createInvoiceButton.setEnabled(false);
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createInvoiceButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create invoices table
        String[] columnNames = {"Invoice #", "Issue Date", "Due Date", "Amount", "Status"};
        invoicesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        invoicesTable = UIFactory.createStyledTable(invoicesTableModel);
        
        // Add color rendering for status column
        invoicesTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    String status = value.toString();
                    
                    // Choose colors based on status
                    if ("Paid".equals(status)) {
                        c.setForeground(UIFactory.SUCCESS_COLOR);
                    } else if ("Overdue".equals(status)) {
                        c.setForeground(UIFactory.ERROR_COLOR);
                    } else if ("Cancelled".equals(status)) {
                        c.setForeground(UIFactory.ERROR_COLOR);
                    } else {
                        c.setForeground(UIFactory.MEDIUM_GRAY);
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = UIFactory.createScrollPane(invoicesTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        
        JButton closeButton = UIFactory.createSecondaryButton("Close");
        JButton editButton = UIFactory.createWarningButton("Edit Order");
        JButton refreshButton = UIFactory.createSecondaryButton("Refresh");
        
        panel.add(refreshButton);
        panel.add(closeButton);
        panel.add(editButton);
        
        // Add button actions
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshOrderData();
            }
        });
        
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
                if (callback != null && order != null) {
                    callback.onEditOrder(order);
                }
            }
        });
        
        return panel;
    }
    
    /**
     * Refreshes order data from the server
     */
    private void refreshOrderData() {
        try {
            if (order != null && orderService != null) {
                Order refreshedOrder = orderService.getOrderWithDetails(order.getId());
                if (refreshedOrder != null) {
                    this.order = refreshedOrder;
                    populateData();
                    loadInvoices();
                    LogUtil.info("Order data refreshed successfully");
                } else {
                    LogUtil.warn("Failed to refresh order data - order not found");
                }
            }
        } catch (Exception e) {
            LogUtil.error("Error refreshing order data", e);
            JOptionPane.showMessageDialog(
                this,
                "Error refreshing order data: " + e.getMessage(),
                "Refresh Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Loads invoices for the current order using RMI
     */
    private void loadInvoices() {
        try {
            if (order != null && invoiceService != null) {
                List<Invoice> invoices = invoiceService.findInvoicesByOrder(order);
                populateInvoicesTable(invoices);
                LogUtil.info("Invoices loaded successfully for order: " + order.getOrderId());
            }
        } catch (Exception e) {
            LogUtil.error("Error loading invoices", e);
            // Don't show error dialog for invoices as it's not critical
        }
    }
    
    /**
     * Populates the invoices table
     */
    private void populateInvoicesTable(List<Invoice> invoices) {
        invoicesTableModel.setRowCount(0);
        
        if (invoices != null) {
            for (Invoice invoice : invoices) {
                Object[] rowData = {
                    invoice.getInvoiceNumber(),
                    invoice.getIssueDate() != null ? invoice.getIssueDate().format(dateFormatter) : "",
                    invoice.getDueDate() != null ? invoice.getDueDate().format(dateFormatter) : "",
                    invoice.getAmount(),
                    invoice.getStatus()
                };
                invoicesTableModel.addRow(rowData);
            }
        }
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
        if (order == null) {
            return;
        }
        
        // Set order details
        orderIdValueLabel.setText(order.getOrderId());
        
        // Set customer info
        Customer customer = order.getCustomer();
        if (customer != null) {
            customerValueLabel.setText(customer.getFullName());
        } else {
            customerValueLabel.setText("No customer");
        }
        
        // Set other order fields
        orderDateValueLabel.setText(order.getOrderDate() != null ? 
                                 order.getOrderDate().format(dateFormatter) : "");
        statusValueLabel.setText(order.getStatus());
        
        // Update status indicator color
        updateStatusIndicator(order.getStatus());
        
        // Format total amount
        if (order.getTotalAmount() != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            totalValueLabel.setText(currencyFormat.format(order.getTotalAmount()));
        } else {
            totalValueLabel.setText("$0.00");
        }
        
        paymentMethodValueLabel.setText(order.getPaymentMethod());
        
        // Populate order items table
        itemsTableModel.setRowCount(0);
        
        List<OrderItem> items = order.getOrderItems();
        if (items != null) {
            for (OrderItem item : items) {
                String productCode = "";
                String productName = "";
                
                if (item.getProduct() != null) {
                    productCode = item.getProduct().getProductCode();
                    productName = item.getProduct().getName();
                }
                
                Object[] rowData = {
                    productCode,
                    productName,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
                };
                itemsTableModel.addRow(rowData);
            }
        }
        
        // Load invoices
        loadInvoices();
    }
    
    /**
     * Gets the current order
     * 
     * @return The order being displayed
     */
    public Order getOrder() {
        return order;
    }
    
    /**
     * Checks if the RMI connection is available
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return orderService != null && invoiceService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
        if (isConnected()) {
            refreshOrderData();
        }
    }
}