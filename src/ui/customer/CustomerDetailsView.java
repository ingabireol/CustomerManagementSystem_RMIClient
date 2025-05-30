package ui.customer;

import model.Customer;
import model.Order;
import service.CustomerService;
import service.OrderService;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ui.UIFactory;

/**
 * Detail view for showing customer information and order history using RMI services.
 * Displays complete customer details and their related orders from remote services.
 */
public class CustomerDetailsView extends JPanel {
    
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private CustomerService customerService;
    private OrderService orderService;
    
    // UI Components
    private JLabel nameValueLabel;
    private JLabel customerIdValueLabel;
    private JLabel emailValueLabel;
    private JLabel phoneValueLabel;
    private JLabel registrationDateValueLabel;
    private JTextArea addressValueArea;
    
    // Orders table
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Customer data
    private Customer customer;
    private List<Order> customerOrders;
    
    // Callback for view actions
    private DetailsViewCallback callback;
    
    // Connection status
    private boolean isConnected = false;
    
    /**
     * Interface for view actions callback
     */
    public interface DetailsViewCallback {
        void onEditCustomer(Customer customer);
        void onViewOrders(Customer customer);
        void onClose();
    }
    
    /**
     * Constructor
     * 
     * @param customer The customer to display
     * @param callback Callback for view actions
     */
    public CustomerDetailsView(Customer customer, DetailsViewCallback callback) {
        this.customer = customer;
        this.callback = callback;
        
        // Initialize RMI connections
        initializeRMIConnections();
        
        // Initialize UI
        initializeUI();
        
        // Load customer data
        if (isConnected) {
            loadCustomerData();
        } else {
            showConnectionError();
        }
    }
    
    /**
     * Initializes RMI connections to required services
     */
    private void initializeRMIConnections() {
        try {
            LogUtil.info("Connecting to CustomerService and OrderService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            
            customerService = (CustomerService) registry.lookup("customerService");
            orderService = (OrderService) registry.lookup("orderService");
            
            isConnected = true;
            LogUtil.info("Successfully connected to Customer and Order services");
            
        } catch (Exception e) {
            LogUtil.error("Failed to connect to RMI services", e);
            isConnected = false;
        }
    }
    
    /**
     * Shows connection error in the details view
     */
    private void showConnectionError() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        
        JLabel errorLabel = new JLabel("Unable to connect to server services");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        errorLabel.setForeground(UIFactory.ERROR_COLOR);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel messageLabel = new JLabel("Please ensure the server is running and try again.");
        messageLabel.setFont(UIFactory.BODY_FONT);
        messageLabel.setForeground(UIFactory.MEDIUM_GRAY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton retryButton = UIFactory.createPrimaryButton("Retry Connection");
        retryButton.addActionListener(e -> retryConnection());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(retryButton);
        
        errorPanel.add(errorLabel, BorderLayout.NORTH);
        errorPanel.add(messageLabel, BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(errorPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    /**
     * Retries the RMI connection
     */
    private void retryConnection() {
        initializeRMIConnections();
        if (isConnected) {
            // Reinitialize the entire UI
            removeAll();
            initializeUI();
            loadCustomerData();
            revalidate();
            repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                "Still unable to connect to server. Please check if the server is running.",
                "Connection Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 15));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create a split panel for details and orders
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.4); // 40% to details, 60% to orders
        
        // Create the details panel
        JPanel detailsPanel = createDetailsPanel();
        splitPane.setTopComponent(detailsPanel);
        
        // Create the orders panel
        JPanel ordersPanel = createOrdersPanel();
        splitPane.setBottomComponent(ordersPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Create actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Loads customer data from RMI services
     */
    private void loadCustomerData() {
        if (!isConnected || customer == null) {
            return;
        }
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    LogUtil.info("Loading customer details and orders from RMI services...");
                    
                    // Load fresh customer data
                    Customer refreshedCustomer = customerService.findCustomerById(customer.getId());
                    if (refreshedCustomer != null) {
                        customer = refreshedCustomer;
                    }
                    
                    // Load customer orders
                    customerOrders = orderService.findOrdersByCustomer(customer);
                    
                    LogUtil.info("Customer data loaded successfully");
                    
                } catch (Exception e) {
                    LogUtil.error("Error loading customer data from RMI services", e);
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    populateData();
                } catch (Exception e) {
                    LogUtil.error("Error in background task", e);
                    showDataLoadError();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows data loading error
     */
    private void showDataLoadError() {
        JOptionPane.showMessageDialog(this,
            "Failed to load customer data from server.\nPlease check your connection and try again.",
            "Data Loading Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Creates the header panel
     */
    private JPanel createHeaderPanel() {
        // Create a header with customer name
        JPanel panel = UIFactory.createModuleHeaderPanel("Customer Details");
        
        if (customer != null) {
            // Add customer name to header
            JLabel customerNameLabel = new JLabel(customer.getFullName());
            customerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            customerNameLabel.setForeground(UIFactory.PRIMARY_COLOR);
            panel.add(customerNameLabel, BorderLayout.EAST);
            
            // Add connection status
            String status = isConnected ? "Connected" : "Disconnected";
            Color statusColor = isConnected ? UIFactory.SUCCESS_COLOR : UIFactory.ERROR_COLOR;
            
            JLabel statusLabel = new JLabel(status);
            statusLabel.setFont(UIFactory.SMALL_FONT);
            statusLabel.setForeground(statusColor);
            
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.add(customerNameLabel, BorderLayout.NORTH);
            rightPanel.add(statusLabel, BorderLayout.SOUTH);
            
            panel.add(rightPanel, BorderLayout.EAST);
        }
        
        return panel;
    }
    
    /**
     * Creates the customer details panel
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title
        JLabel sectionTitle = new JLabel("Customer Information");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, BorderLayout.NORTH);
        
        // Create details grid
        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Customer ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Customer ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        customerIdValueLabel = createValueLabel("");
        detailsGrid.add(customerIdValueLabel, gbc);
        
        // Name
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Name:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        nameValueLabel = createValueLabel("");
        detailsGrid.add(nameValueLabel, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        emailValueLabel = createValueLabel("");
        detailsGrid.add(emailValueLabel, gbc);
        
        // Phone
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Phone:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        phoneValueLabel = createValueLabel("");
        detailsGrid.add(phoneValueLabel, gbc);
        
        // Registration Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Registration Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        registrationDateValueLabel = createValueLabel("");
        detailsGrid.add(registrationDateValueLabel, gbc);
        
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
    
    /**
     * Creates the orders panel
     */
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title with count
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel sectionTitle = new JLabel("Order History");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        headerPanel.add(sectionTitle, BorderLayout.WEST);
        
        // Add view all orders button
        JButton viewOrdersButton = UIFactory.createSecondaryButton("View All Orders");
        viewOrdersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null && customer != null) {
                    callback.onViewOrders(customer);
                }
            }
        });
        
        // Add refresh button
        JButton refreshButton = UIFactory.createSecondaryButton("Refresh");
        refreshButton.addActionListener(e -> refreshOrderData());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewOrdersButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create orders table
        String[] columnNames = {"Order ID", "Date", "Total", "Status"};
        ordersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        ordersTable = UIFactory.createStyledTable(ordersTableModel);
        
        // Add double-click listener to view order details
        ordersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && ordersTable.getSelectedRow() != -1) {
                    int selectedRow = ordersTable.getSelectedRow();
                    if (selectedRow >= 0 && customerOrders != null && selectedRow < customerOrders.size()) {
                        Order selectedOrder = customerOrders.get(selectedRow);
                        viewOrderDetails(selectedOrder);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = UIFactory.createScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add info label
        JLabel infoLabel = new JLabel("Double-click an order to view details");
        infoLabel.setFont(UIFactory.SMALL_FONT);
        infoLabel.setForeground(UIFactory.MEDIUM_GRAY);
        infoLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Refreshes order data from the server
     */
    private void refreshOrderData() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this,
                "Not connected to server. Please retry connection.",
                "Connection Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SwingWorker<List<Order>, Void> worker = new SwingWorker<List<Order>, Void>() {
            @Override
            protected List<Order> doInBackground() throws Exception {
                return orderService.findOrdersByCustomer(customer);
            }
            
            @Override
            protected void done() {
                try {
                    customerOrders = get();
                    populateOrdersTable();
                    LogUtil.info("Order data refreshed successfully");
                } catch (Exception e) {
                    LogUtil.error("Error refreshing order data", e);
                    JOptionPane.showMessageDialog(CustomerDetailsView.this,
                        "Failed to refresh order data: " + e.getMessage(),
                        "Refresh Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Views order details (placeholder for future implementation)
     */
    private void viewOrderDetails(Order order) {
        JOptionPane.showMessageDialog(this,
            "Order Details:\n" +
            "Order ID: " + order.getOrderId() + "\n" +
            "Date: " + (order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "N/A") + "\n" +
            "Total: " + (order.getTotalAmount() != null ? order.getTotalAmount().toString() : "N/A") + "\n" +
            "Status: " + order.getStatus(),
            "Order Details",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Creates the actions panel
     */
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        
        JButton closeButton = UIFactory.createSecondaryButton("Close");
        JButton editButton = UIFactory.createWarningButton("Edit Customer");
        
        // Only enable edit button if connected
        editButton.setEnabled(isConnected);
        
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
                if (callback != null && customer != null && isConnected) {
                    callback.onEditCustomer(customer);
                } else if (!isConnected) {
                    JOptionPane.showMessageDialog(CustomerDetailsView.this,
                        "Cannot edit customer - not connected to server.",
                        "Connection Error",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        return panel;
    }
    
    /**
     * Creates a label for the details grid
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFactory.BODY_FONT);
        label.setForeground(UIFactory.MEDIUM_GRAY);
        return label;
    }
    
    /**
     * Creates a value label for the details grid
     */
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFactory.BODY_FONT);
        return label;
    }
    
    /**
     * Populates the UI with customer data
     */
    private void populateData() {
        if (customer == null) {
            return;
        }
        
        try {
            // Set customer details
            customerIdValueLabel.setText(customer.getCustomerId() != null ? customer.getCustomerId() : "");
            nameValueLabel.setText(customer.getFullName() != null ? customer.getFullName() : "");
            emailValueLabel.setText(customer.getEmail() != null ? customer.getEmail() : "");
            phoneValueLabel.setText(customer.getPhone() != null ? customer.getPhone() : "");
            registrationDateValueLabel.setText(customer.getRegistrationDate() != null ? 
                                             customer.getRegistrationDate().format(dateFormatter) : "");
            addressValueArea.setText(customer.getAddress() != null ? customer.getAddress() : "");
            
            // Populate orders table
            populateOrdersTable();
            
            LogUtil.info("Customer details populated successfully");
            
        } catch (Exception e) {
            LogUtil.error("Error populating customer data", e);
        }
    }
    
    /**
     * Populates the orders table with customer order data
     */
    private void populateOrdersTable() {
        // Clear the table
        ordersTableModel.setRowCount(0);
        
        if (customerOrders != null && !customerOrders.isEmpty()) {
            for (Order order : customerOrders) {
                Object[] rowData = {
                    order.getOrderId() != null ? order.getOrderId() : "",
                    order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "",
                    order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0.00",
                    order.getStatus() != null ? order.getStatus() : ""
                };
                ordersTableModel.addRow(rowData);
            }
        } else {
            // Show "No orders" message in table
            ordersTableModel.addRow(new Object[]{"No orders found", "", "", ""});
        }
        
        // Update the section title to show order count
        Component[] components = ((JPanel)((BorderLayout)createOrdersPanel().getLayout())
            .getLayoutComponent(BorderLayout.NORTH)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            JLabel titleLabel = (JLabel) components[0];
            int orderCount = customerOrders != null ? customerOrders.size() : 0;
            titleLabel.setText("Order History (" + orderCount + " orders)");
        }
    }
    
    /**
     * Refreshes the customer data from the server
     */
    public void refreshCustomerData() {
        if (isConnected) {
            loadCustomerData();
        } else {
            retryConnection();
        }
    }
    
    /**
     * Gets the current customer
     */
    public Customer getCustomer() {
        return customer;
    }
    
    /**
     * Sets a new customer and refreshes the view
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (isConnected) {
            loadCustomerData();
        } else {
            populateData(); // Show what we have even if not connected
        }
    }
    
    /**
     * Checks if connected to RMI services
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Reconnects to RMI services
     */
    public void reconnect() {
        retryConnection();
    }
}