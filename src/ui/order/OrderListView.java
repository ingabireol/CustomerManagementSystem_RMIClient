package ui.order;

import model.Order;
import model.Customer;
import service.OrderService;
import service.CustomerService;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import ui.UIFactory;

/**
 * RMI-based List view for displaying and managing orders.
 * Provides functionality for searching, filtering, and performing CRUD operations using RMI services.
 */
public class OrderListView extends JPanel {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private OrderService orderService;
    private CustomerService customerService;
    
    // Table components
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    
    // Search and filter components
    private JTextField searchField;
    private JComboBox<String> statusFilterComboBox;
    private JComboBox<String> dateRangeComboBox;
    private JTextField fromDateField;
    private JTextField toDateField;
    
    // Action buttons
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton viewDetailsButton;
    private JButton createInvoiceButton;
    
    // Order data
    private List<Order> orderList;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Callback for list actions
    private OrderListCallback callback;
    
    /**
     * Interface for order list actions
     */
    public interface OrderListCallback {
        void onAddOrder();
        void onEditOrder(Order order);
        void onDeleteOrder(Order order);
        void onViewOrderDetails(Order order);
        void onCreateInvoice(Order order);
    }
    
    /**
     * Constructor
     */
    public OrderListView(OrderListCallback callback) {
        this.callback = callback;
        this.orderList = new ArrayList<>();
        
        // Initialize RMI connections
        initializeRMIConnection();
        
        initializeUI();
        loadData();
    }
    
    /**
     * Initializes the RMI connections to the server
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to OrderService and CustomerService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            orderService = (OrderService) registry.lookup("orderService");
            customerService = (CustomerService) registry.lookup("customerService");
            LogUtil.info("Successfully connected to OrderService and CustomerService");
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
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 10));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create a split panel for table and summary
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.8); // 80% to table, 20% to summary
        
        // Create the table panel
        JPanel tablePanel = createTablePanel();
        splitPane.setLeftComponent(tablePanel);
        
        // Create the summary panel
        JPanel summaryPanel = createSummaryPanel();
        splitPane.setRightComponent(summaryPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Create the actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Loads order data from RMI service
     */
    private void loadData() {
        try {
            if (orderService != null) {
                this.orderList = orderService.findAllOrders();
                refreshTableData();
                LogUtil.info("Loaded " + orderList.size() + " orders");
            } else {
                LogUtil.warn("OrderService is not available");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading order data", ex);
            JOptionPane.showMessageDialog(this,
                "Error loading order data: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = UIFactory.createModuleHeaderPanel("Orders");
        
        // Create search and filter section on the right side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        // Status filter combo box
        String[] statusOptions = {
            "All Orders", 
            "Pending", 
            "Processing", 
            "Shipped",
            "Delivered",
            "Cancelled"
        };
        statusFilterComboBox = UIFactory.createComboBox(statusOptions);
        statusFilterComboBox.setPreferredSize(new Dimension(120, 30));
        
        // Date range filter
        String[] dateRangeOptions = {
            "All Time",
            "Today",
            "Last 7 Days",
            "Last 30 Days",
            "This Month",
            "Last Month",
            "Custom Range"
        };
        dateRangeComboBox = UIFactory.createComboBox(dateRangeOptions);
        dateRangeComboBox.setPreferredSize(new Dimension(120, 30));
        
        // Custom date fields (initially hidden)
        fromDateField = UIFactory.createDateField("From Date");
        fromDateField.setPreferredSize(new Dimension(100, 30));
        fromDateField.setVisible(false);
        
        toDateField = UIFactory.createDateField("To Date");
        toDateField.setPreferredSize(new Dimension(100, 30));
        toDateField.setVisible(false);
        
        // Search field
        searchField = UIFactory.createSearchField("Search orders...", 200);
        
        // Add search button
        JButton searchButton = UIFactory.createSecondaryButton("Search");
        
        // Add components to search panel
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusFilterComboBox);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(new JLabel("Period:"));
        searchPanel.add(dateRangeComboBox);
        searchPanel.add(fromDateField);
        searchPanel.add(toDateField);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Add search panel to the header
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        // Add search action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        
        // Add filter change actions
        statusFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        
        dateRangeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) dateRangeComboBox.getSelectedItem();
                boolean isCustomRange = "Custom Range".equals(selected);
                fromDateField.setVisible(isCustomRange);
                toDateField.setVisible(isCustomRange);
                
                // Apply filter immediately if not custom range
                if (!isCustomRange) {
                    applyDateRangeFilter(selected);
                }
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
        String[] columnNames = {"ID", "Order ID", "Customer", "Date", "Amount", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID column
                if (columnIndex == 4) return BigDecimal.class; // Amount column
                return String.class;
            }
        };
        
        // Create and set up the table
        orderTable = UIFactory.createStyledTable(tableModel);
        
        // Add row sorting
        tableSorter = new TableRowSorter<>(tableModel);
        orderTable.setRowSorter(tableSorter);
        
        // Set column widths
        orderTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID column
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Order ID
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Customer
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Date
        
        // Set the renderer for the status column to show colored status
        orderTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    String status = value.toString();
                    
                    // Choose colors based on status
                    if ("Delivered".equals(status)) {
                        c.setForeground(UIFactory.SUCCESS_COLOR);
                    } else if ("Processing".equals(status) || "Shipped".equals(status)) {
                        c.setForeground(UIFactory.WARNING_COLOR);
                    } else if ("Cancelled".equals(status)) {
                        c.setForeground(UIFactory.ERROR_COLOR);
                    } else if ("Pending".equals(status)) {
                        c.setForeground(UIFactory.MEDIUM_GRAY);
                    } else {
                        c.setForeground(UIFactory.DARK_GRAY);
                    }
                }
                
                return c;
            }
        });
        
        // Add double-click listener for viewing details
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = orderTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        selectedRow = orderTable.convertRowIndexToModel(selectedRow);
                        Order selectedOrder = getOrderAtRow(selectedRow);
                        if (selectedOrder != null && callback != null) {
                            callback.onViewOrderDetails(selectedOrder);
                        }
                    }
                }
            }
        });
        
        // Add selection listener to enable/disable action buttons
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = orderTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                viewDetailsButton.setEnabled(hasSelection);
                createInvoiceButton.setEnabled(hasSelection);
                
                // Only enable the create invoice button for orders with status "Delivered"
                if (hasSelection) {
                    int selectedRow = orderTable.getSelectedRow();
                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
                    Order selectedOrder = getOrderAtRow(selectedRow);
                    if (selectedOrder != null) {
                        createInvoiceButton.setEnabled("Delivered".equals(selectedOrder.getStatus()));
                    }
                }
            }
        });
        
        // Add the table to a scroll pane
        JScrollPane scrollPane = UIFactory.createScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Summary title
        JLabel titleLabel = new JLabel("Order Summary");
        titleLabel.setFont(UIFactory.HEADER_FONT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Add summary cards based on current orders
        JPanel totalOrdersPanel = createSummaryItem("Total Orders", "0");
        totalOrdersPanel.setName("totalOrders");
        panel.add(totalOrdersPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel ordersThisMonthPanel = createSummaryItem("Orders This Month", "0");
        ordersThisMonthPanel.setName("ordersThisMonth");
        panel.add(ordersThisMonthPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel pendingOrdersPanel = createSummaryItem("Pending Orders", "0");
        pendingOrdersPanel.setName("pendingOrders");
        panel.add(pendingOrdersPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel deliveredOrdersPanel = createSummaryItem("Delivered Orders", "0");
        deliveredOrdersPanel.setName("deliveredOrders");
        panel.add(deliveredOrdersPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel avgOrderValuePanel = createSummaryItem("Average Order Value", "$0.00");
        avgOrderValuePanel.setName("avgOrderValue");
        panel.add(avgOrderValuePanel);
        
        // Add glue to push everything to the top
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createSummaryItem(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(8, 8, 8, 8)
        ));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UIFactory.BODY_FONT);
        labelComponent.setForeground(UIFactory.MEDIUM_GRAY);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        panel.add(labelComponent, BorderLayout.NORTH);
        panel.add(valueComponent, BorderLayout.CENTER);
        
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Information label (left side)
        JLabel infoLabel = new JLabel("Double-click a row to view order details");
        infoLabel.setFont(UIFactory.SMALL_FONT);
        infoLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
        // Action buttons (right side)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        
        refreshButton = UIFactory.createSecondaryButton("Refresh");
        
        createInvoiceButton = UIFactory.createSecondaryButton("Create Invoice");
        createInvoiceButton.setEnabled(false); // Disabled until selection
        
        viewDetailsButton = UIFactory.createSecondaryButton("View Details");
        viewDetailsButton.setEnabled(false); // Disabled until selection
        
        deleteButton = UIFactory.createDangerButton("Delete");
        deleteButton.setEnabled(false); // Disabled until selection
        
        editButton = UIFactory.createWarningButton("Edit");
        editButton.setEnabled(false); // Disabled until selection
        
        addButton = UIFactory.createPrimaryButton("New Order");
        
        // Add buttons to panel
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(createInvoiceButton);
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
                    callback.onAddOrder();
                }
            }
        });
        
        // Edit button action
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
                    Order selectedOrder = getOrderAtRow(selectedRow);
                    if (selectedOrder != null) {
                        callback.onEditOrder(selectedOrder);
                    }
                }
            }
        });
        
        // Delete button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
                    Order selectedOrder = getOrderAtRow(selectedRow);
                    if (selectedOrder != null) {
                        int confirm = JOptionPane.showConfirmDialog(
                            OrderListView.this,
                            "Are you sure you want to delete order: " + selectedOrder.getOrderId() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteOrder(selectedOrder);
                        }
                    }
                }
            }
        });
        
        // View Details button action
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
                    Order selectedOrder = getOrderAtRow(selectedRow);
                    if (selectedOrder != null) {
                        callback.onViewOrderDetails(selectedOrder);
                    }
                }
            }
        });
        
        // Create Invoice button action
        createInvoiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
                    Order selectedOrder = getOrderAtRow(selectedRow);
                    if (selectedOrder != null) {
                        callback.onCreateInvoice(selectedOrder);
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
     * Refreshes the table data with the current order list
     */
    private void refreshTableData() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Populate the table with data
        for (Order order : orderList) {
            // Get customer name
            String customerName = "";
            if (order.getCustomer() != null) {
                customerName = order.getCustomer().getFullName();
            }
            
            Object[] rowData = {
                order.getId(),
                order.getOrderId(),
                customerName,
                order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "",
                order.getTotalAmount(),
                order.getStatus()
            };
            tableModel.addRow(rowData);
        }
        
        // Reset selection and buttons
        orderTable.clearSelection();
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        viewDetailsButton.setEnabled(false);
        createInvoiceButton.setEnabled(false);
        
        // Update summary statistics
        updateSummaryStatistics();
        
        // Apply existing filters
        applyFilters();
    }
    
    /**
     * Update summary statistics based on current orders
     */
    private void updateSummaryStatistics() {
        if (orderList == null || orderList.isEmpty()) {
            return;
        }
        
        int totalOrders = orderList.size();
        int pendingOrders = 0;
        int deliveredOrders = 0;
        int ordersThisMonth = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        
        for (Order order : orderList) {
            // Count by status
            if ("Pending".equals(order.getStatus())) {
                pendingOrders++;
            } else if ("Delivered".equals(order.getStatus())) {
                deliveredOrders++;
            }
            
            // Orders this month
            if (order.getOrderDate() != null && !order.getOrderDate().isBefore(firstDayOfMonth)) {
                ordersThisMonth++;
            }
            
            // Sum total amount
            if (order.getTotalAmount() != null) {
                totalAmount = totalAmount.add(order.getTotalAmount());
            }
        }
        
        // Calculate average order value
        BigDecimal avgOrderValue = totalOrders > 0 
            ? totalAmount.divide(new BigDecimal(totalOrders), 2, BigDecimal.ROUND_HALF_UP) 
            : BigDecimal.ZERO;
        
        // Update the summary panels - look for components by name
        Component[] components = this.getComponents();
        for (Component component : components) {
            if (component instanceof JSplitPane) {
                JSplitPane splitPane = (JSplitPane) component;
                Component rightComponent = splitPane.getRightComponent();
                
                if (rightComponent instanceof JPanel) {
                    JPanel summaryPanel = (JPanel) rightComponent;
                    Component[] summaryComponents = summaryPanel.getComponents();
                    
                    for (Component summaryComp : summaryComponents) {
                        if (summaryComp instanceof JPanel && summaryComp.getName() != null) {
                            JPanel card = (JPanel) summaryComp;
                            
                            // Find the value label (should be the second component)
                            if (card.getComponentCount() >= 2 && card.getComponent(1) instanceof JLabel) {
                                JLabel valueLabel = (JLabel) card.getComponent(1);
                                
                                switch (card.getName()) {
                                    case "totalOrders":
                                        valueLabel.setText(String.valueOf(totalOrders));
                                        break;
                                    case "ordersThisMonth":
                                        valueLabel.setText(String.valueOf(ordersThisMonth));
                                        break;
                                    case "pendingOrders":
                                        valueLabel.setText(String.valueOf(pendingOrders));
                                        break;
                                    case "deliveredOrders":
                                        valueLabel.setText(String.valueOf(deliveredOrders));
                                        break;
                                    case "avgOrderValue":
                                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                                        valueLabel.setText(currencyFormat.format(avgOrderValue));
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Delete an order using RMI service
     */
    private void deleteOrder(Order order) {
        try {
            if (orderService != null) {
                Order deletedOrder = orderService.deleteOrder(order);
                if (deletedOrder != null) {
                    orderList.removeIf(o -> o.getId() == order.getId());
                    refreshTableData();
                    JOptionPane.showMessageDialog(this,
                        "Order deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    if (callback != null) {
                        callback.onDeleteOrder(order);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to delete order. It may be referenced by other records.",
                        "Delete Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            LogUtil.error("Error deleting order", ex);
            JOptionPane.showMessageDialog(this,
                "Error deleting order: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Apply date range filter
     */
    private void applyDateRangeFilter(String rangeOption) {
        LocalDate fromDate = null;
        LocalDate toDate = null;
        LocalDate today = LocalDate.now();
        
        switch (rangeOption) {
            case "Today":
                fromDate = today;
                toDate = today;
                break;
            case "Last 7 Days":
                fromDate = today.minusDays(6);
                toDate = today;
                break;
            case "Last 30 Days":
                fromDate = today.minusDays(29);
                toDate = today;
                break;
            case "This Month":
                fromDate = today.withDayOfMonth(1);
                toDate = today;
                break;
            case "Last Month":
                fromDate = today.minusMonths(1).withDayOfMonth(1);
                toDate = today.withDayOfMonth(1).minusDays(1);
                break;
            case "Custom Range":
                // Parse from custom date fields
                try {
                    if (!fromDateField.getText().isEmpty()) {
                        fromDate = LocalDate.parse(fromDateField.getText(), dateFormatter);
                    }
                    if (!toDateField.getText().isEmpty()) {
                        toDate = LocalDate.parse(toDateField.getText(), dateFormatter);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid date format. Use format YYYY-MM-DD.",
                        "Format Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "All Time":
            default:
                // No date filtering
                break;
        }
        
        // Update visible date fields
        if (fromDateField.isVisible()) {
            fromDateField.setText(fromDate != null ? fromDate.format(dateFormatter) : "");
            toDateField.setText(toDate != null ? toDate.format(dateFormatter) : "");
        }
        
        // Apply all filters
        applyFilters();
    }
    
    /**
     * Apply all filters to the table
     */
    private void applyFilters() {
        RowFilter<DefaultTableModel, Object> filter = null;
        
        // Get search text
        String searchText = searchField.getText().trim().toLowerCase();
        
        // Get status selection
        String statusSelection = (String) statusFilterComboBox.getSelectedItem();
        
        // Get date range selection
        String dateRangeSelection = (String) dateRangeComboBox.getSelectedItem();
        LocalDate fromDate = null;
        LocalDate toDate = null;
        
        // Process date range
        if (!"All Time".equals(dateRangeSelection)) {
            // For custom range, get dates from fields
            if ("Custom Range".equals(dateRangeSelection)) {
                try {
                    if (!fromDateField.getText().isEmpty()) {
                        fromDate = LocalDate.parse(fromDateField.getText(), dateFormatter);
                    }
                    if (!toDateField.getText().isEmpty()) {
                        toDate = LocalDate.parse(toDateField.getText(), dateFormatter);
                    }
                } catch (Exception e) {
                    // Invalid date format - handled in UI
                }
            } else {
                // For predefined ranges
                LocalDate today = LocalDate.now();
                
                switch (dateRangeSelection) {
                    case "Today":
                        fromDate = today;
                        toDate = today;
                        break;
                    case "Last 7 Days":
                        fromDate = today.minusDays(6);
                        toDate = today;
                        break;
                    case "Last 30 Days":
                        fromDate = today.minusDays(29);
                        toDate = today;
                        break;
                    case "This Month":
                        fromDate = today.withDayOfMonth(1);
                        toDate = today;
                        break;
                    case "Last Month":
                        fromDate = today.minusMonths(1).withDayOfMonth(1);
                        toDate = today.withDayOfMonth(1).minusDays(1);
                        break;
                }
            }
        }
        
        // Final date range for use in filter
        final LocalDate finalFromDate = fromDate;
        final LocalDate finalToDate = toDate;
        
        // Combined filter for search text, status, and date range
        if (!searchText.isEmpty() || !"All Orders".equals(statusSelection) || 
            !"All Time".equals(dateRangeSelection) && (finalFromDate != null || finalToDate != null)) {
            
            filter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    boolean matchesSearch = true;
                    boolean matchesStatus = true;
                    boolean matchesDateRange = true;
                    
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
                    
                    // Apply status filter
                    if (!"All Orders".equals(statusSelection)) {
                        String statusValue = entry.getStringValue(5); // Status column (index 5)
                        matchesStatus = statusSelection.equals(statusValue);
                    }
                    
                    // Apply date range filter
                    if (!"All Time".equals(dateRangeSelection) && (finalFromDate != null || finalToDate != null)) {
                        String dateString = entry.getStringValue(3); // Date column (index 3)
                        if (dateString == null || dateString.isEmpty()) {
                            matchesDateRange = false;
                        } else {
                            try {
                                LocalDate orderDate = LocalDate.parse(dateString, dateFormatter);
                                if (finalFromDate != null) {
                                    matchesDateRange = !orderDate.isBefore(finalFromDate);
                                }
                                if (finalToDate != null) {
                                    matchesDateRange = matchesDateRange && !orderDate.isAfter(finalToDate);
                                }
                            } catch (Exception e) {
                                matchesDateRange = false;
                            }
                        }
                    }
                    
                    // Return combined filter result
                    return matchesSearch && matchesStatus && matchesDateRange;
                }
            };
        }
        
        // Apply the filter to the table sorter
        tableSorter.setRowFilter(filter);
    }
    
    /**
     * Get the Order object at the specified row
     */
    private Order getOrderAtRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orderList.size()) {
            int orderId = ((Integer) tableModel.getValueAt(rowIndex, 0)).intValue();
            for (Order order : orderList) {
                if (order.getId() == orderId) {
                    return order;
                }
            }
        }
        return null;
    }
    
    /**
     * Updates the order list with a new/edited order
     */
    public void updateOrder(Order order) {
        // Check if order already exists in the list
        boolean found = false;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getId() == order.getId()) {
                orderList.set(i, order);
                found = true;
                break;
            }
        }
        
        // If not found, add as new
        if (!found) {
            orderList.add(order);
        }
        
        // Refresh the table
        refreshTableData();
    }
    
    /**
     * Adds a new order to the list view
     */
    public void addOrder(Order order) {
        orderList.add(order);
        refreshTableData();
    }
    
    /**
     * Removes an order from the list view
     */
    public void removeOrder(Order order) {
        orderList.removeIf(o -> o.getId() == order.getId());
        refreshTableData();
    }
    
    /**
     * Updates multiple orders in the list view
     */
    public void updateOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        
        this.orderList = orders;
        refreshTableData();
    }
    
    /**
     * Selects an order in the table by ID
     */
    public void selectOrder(int orderId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int id = ((Integer) tableModel.getValueAt(i, 0)).intValue();
            if (id == orderId) {
                int viewIndex = orderTable.convertRowIndexToView(i);
                orderTable.getSelectionModel().setSelectionInterval(viewIndex, viewIndex);
                orderTable.scrollRectToVisible(orderTable.getCellRect(viewIndex, 0, true));
                break;
            }
        }
    }
    
    /**
     * Searches for orders by various criteria using RMI
     */
    public void searchOrders(String searchText) {
        try {
            if (orderService != null && searchText != null && !searchText.trim().isEmpty()) {
                // You can implement specific search methods in the OrderService
                // For now, we'll filter the current list
                applyFilters();
                LogUtil.info("Search applied for: " + searchText);
            } else {
                // Reset to show all orders
                loadData();
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching orders", ex);
            JOptionPane.showMessageDialog(
                this,
                "Error searching orders: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Finds orders by status using RMI
     */
    public void findOrdersByStatus(String status) {
        try {
            if (orderService != null && status != null && !status.equals("All Orders")) {
                List<Order> orders = orderService.findOrdersByStatus(status);
                if (orders != null) {
                    updateOrders(orders);
                    LogUtil.info("Found " + orders.size() + " orders with status: " + status);
                }
            } else {
                loadData(); // Load all orders
            }
        } catch (Exception ex) {
            LogUtil.error("Error finding orders by status", ex);
            JOptionPane.showMessageDialog(
                this,
                "Error finding orders by status: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Finds orders by date range using RMI
     */
    public void findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            if (orderService != null && startDate != null && endDate != null) {
                List<Order> orders = orderService.findOrdersByDateRange(startDate, endDate);
                if (orders != null) {
                    updateOrders(orders);
                    LogUtil.info("Found " + orders.size() + " orders in date range: " + 
                               startDate + " to " + endDate);
                }
            }
        } catch (Exception ex) {
            LogUtil.error("Error finding orders by date range", ex);
            JOptionPane.showMessageDialog(
                this,
                "Error finding orders by date range: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Finds orders by customer using RMI
     */
    public void findOrdersByCustomer(Customer customer) {
        try {
            if (orderService != null && customer != null) {
                List<Order> orders = orderService.findOrdersByCustomer(customer);
                if (orders != null) {
                    updateOrders(orders);
                    LogUtil.info("Found " + orders.size() + " orders for customer: " + 
                               customer.getFullName());
                }
            }
        } catch (Exception ex) {
            LogUtil.error("Error finding orders by customer", ex);
            JOptionPane.showMessageDialog(
                this,
                "Error finding orders by customer: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Gets the current order list
     * 
     * @return List of orders currently displayed
     */
    public List<Order> getOrderList() {
        return new ArrayList<>(orderList);
    }
    
    /**
     * Gets the selected order
     * 
     * @return The currently selected order, or null if none selected
     */
    public Order getSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedRow = orderTable.convertRowIndexToModel(selectedRow);
            return getOrderAtRow(selectedRow);
        }
        return null;
    }
    
    /**
     * Refreshes the data by reloading from the server
     */
    public void refreshData() {
        loadData();
    }
    
    /**
     * Checks if the RMI connection is available
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return orderService != null && customerService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
        if (isConnected()) {
            loadData();
        }
    }
    
    /**
     * Gets the OrderService instance
     * 
     * @return The OrderService
     */
    public OrderService getOrderService() {
        return orderService;
    }
    
    /**
     * Gets the CustomerService instance
     * 
     * @return The CustomerService
     */
    public CustomerService getCustomerService() {
        return customerService;
    }
}