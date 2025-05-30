package ui.order;

import model.Order;
import model.OrderItem;
import model.Customer;
import model.Invoice;
import dao.OrderDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.text.NumberFormat;
import javax.swing.table.DefaultTableCellRenderer;
import ui.UIFactory;

/**
 * Detail view for showing order information and line items.
 * Displays complete order details including customer information and all items ordered.
 */
public class OrderDetailsView extends JPanel {
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
    private OrderDao orderDao;
    
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
        this.orderDao = new OrderDao();
        
        // Load order with details if needed
        if (order != null && (order.getOrderItems() == null || order.getOrderItems().isEmpty())) {
            this.order = orderDao.getOrderWithDetails(order.getId());
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
            
            // Set color based on status
            String status = order.getStatus();
            if ("Delivered".equals(status)) {
                statusIndicator.setBackground(UIFactory.SUCCESS_COLOR);
            } else if ("Processing".equals(status) || "Shipped".equals(status)) {
                statusIndicator.setBackground(UIFactory.WARNING_COLOR);
            } else if ("Cancelled".equals(status)) {
                statusIndicator.setBackground(UIFactory.ERROR_COLOR);
            } else {
                statusIndicator.setBackground(UIFactory.MEDIUM_GRAY);
            }
            
            JLabel statusLabel = new JLabel(status);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            statusPanel.add(statusIndicator);
            statusPanel.add(statusLabel);
            
            rightPanel.add(orderIdLabel);
            rightPanel.add(statusPanel);
            
            panel.add(rightPanel, BorderLayout.EAST);
        }
        
        return panel;
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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Order ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        orderIdValueLabel = createValueLabel("");
        detailsGrid.add(orderIdValueLabel, gbc);
        
        // Customer
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Customer:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        customerValueLabel = createValueLabel("");
        
        // Add view customer button
        JPanel customerPanel = new JPanel(new BorderLayout(5, 0));
        customerPanel.setOpaque(false);
        customerPanel.add(customerValueLabel, BorderLayout.CENTER);
        
        JButton viewCustomerButton = new JButton("View");
        viewCustomerButton.setFont(UIFactory.SMALL_FONT);
        viewCustomerButton.setPreferredSize(new Dimension(60, 25));
        viewCustomerButton.addActionListener(e -> {
            if (callback != null && order != null && order.getCustomer() != null) {
                callback.onViewCustomer(order.getCustomer());
            }
        });
        customerPanel.add(viewCustomerButton, BorderLayout.EAST);
        
        detailsGrid.add(customerPanel, gbc);
        
        // Order Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Order Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        orderDateValueLabel = createValueLabel("");
        detailsGrid.add(orderDateValueLabel, gbc);
        
        // Status
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Status:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        
        // Status with colored indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.setOpaque(false);
        
        JPanel indicator = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(12, 12);
            }
        };
        indicator.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        
        statusValueLabel = createValueLabel("");
        
        statusPanel.add(indicator);
        statusPanel.add(statusValueLabel);
        
        detailsGrid.add(statusPanel, gbc);
        
        // Total Amount
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Total Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        totalValueLabel = createValueLabel("");
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValueLabel.setForeground(UIFactory.PRIMARY_COLOR);
        detailsGrid.add(totalValueLabel, gbc);
        
        // Payment Method
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Payment Method:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
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
                if (callback != null && order != null) {
                    callback.onEditOrder(order);
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
        if (order == null) {
            return;
        }
        
        // Set order details
        orderIdValueLabel.setText(order.getOrderId());
        
        // Set customer info
        Customer customer = order.getCustomer();
        if (customer != null) {
            customerValueLabel.setText(customer.getFullName());
        } else if (order.getCustomerId() > 0) {
            customerValueLabel.setText("Customer ID: " + order.getCustomerId());
        } else {
            customerValueLabel.setText("No customer");
        }
        
        // Set other order fields
        orderDateValueLabel.setText(order.getOrderDate() != null ? 
                                 order.getOrderDate().format(dateFormatter) : "");
        statusValueLabel.setText(order.getStatus());
        
        // Set status indicator color
        if ("Delivered".equals(order.getStatus())) {
            statusIndicator.setBackground(UIFactory.SUCCESS_COLOR);
        } else if ("Processing".equals(order.getStatus()) || "Shipped".equals(order.getStatus())) {
            statusIndicator.setBackground(UIFactory.WARNING_COLOR);
        } else if ("Cancelled".equals(order.getStatus())) {
            statusIndicator.setBackground(UIFactory.ERROR_COLOR);
        } else {
            statusIndicator.setBackground(UIFactory.MEDIUM_GRAY);
        }
        
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
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            
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
        
        // Populate invoices table
        invoicesTableModel.setRowCount(0);
        
        List<Invoice> invoices = order.getInvoices();
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
}