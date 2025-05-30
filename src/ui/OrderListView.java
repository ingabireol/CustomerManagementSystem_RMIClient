//package ui;
//
//import model.Order;
//import model.Customer;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.table.TableRowSorter;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * List view for displaying and managing orders.
// * Demonstrates an enhanced list view with summary statistics and advanced filtering.
// */
//public class OrderListView extends JPanel {
//    // Table components
//    private JTable orderTable;
//    private DefaultTableModel tableModel;
//    private TableRowSorter<DefaultTableModel> tableSorter;
//    
//    // Search and filter components
//    private JTextField searchField;
//    private JComboBox<String> statusFilterComboBox;
//    private JComboBox<String> dateRangeComboBox;
//    private JTextField fromDateField;
//    private JTextField toDateField;
//    
//    // Action buttons
//    private JButton addButton;
//    private JButton viewButton;
//    private JButton editButton;
//    private JButton deleteButton;
//    private JButton refreshButton;
//    
//    // Date formatter
//    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    
//    // Order data
//    private List<Order> orderList;
//    
//    // Callback for list actions
//    private OrderListCallback callback;
//    
//    /**
//     * Interface for order list actions
//     */
//    public interface OrderListCallback {
//        void onAddOrder();
//        void onEditOrder(Order order);
//        void onDeleteOrder(Order order);
//        void onViewOrderDetails(Order order);
//    }
//    
//    /**
//     * Constructor
//     * 
//     * @param orders Initial list of orders
//     * @param callback Callback for list actions
//     */
//    public OrderListView(List<Order> orders, OrderListCallback callback) {
//        this.orderList = orders != null ? orders : new ArrayList<>();
//        this.callback = callback;
//        initializeUI();
//    }
//    
//    private void initializeUI() {
//        // Set up the panel
//        setLayout(new BorderLayout(0, 10));
//        setBackground(UIFactory.BACKGROUND_COLOR);
//        setBorder(new EmptyBorder(10, 10, 10, 10));
//        
//        // Create the header panel
//        JPanel headerPanel = createHeaderPanel();
//        add(headerPanel, BorderLayout.NORTH);
//        
//        // Create a split panel for table and summary
//        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//        splitPane.setBorder(BorderFactory.createEmptyBorder());
//        splitPane.setResizeWeight(0.8); // 80% to table, 20% to summary
//        
//        // Create the table panel
//        JPanel tablePanel = createTablePanel();
//        splitPane.setLeftComponent(tablePanel);
//        
//        // Create the summary panel
//        JPanel summaryPanel = createSummaryPanel();
//        splitPane.setRightComponent(summaryPanel);
//        
//        add(splitPane, BorderLayout.CENTER);
//        
//        // Create the actions panel
//        JPanel actionsPanel = createActionsPanel();
//        add(actionsPanel, BorderLayout.SOUTH);
//        
//        // Load the order data
//        refreshTableData();
//    }
//    
//    private JPanel createHeaderPanel() {
//        JPanel headerPanel = UIFactory.createModuleHeaderPanel("Orders");
//        
//        // Create search and filter section
//        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        searchPanel.setOpaque(false);
//        
//        // Status filter
//        JLabel statusLabel = new JLabel("Status:");
//        statusFilterComboBox = new JComboBox<>(new String[] {
//            "All Orders",
//            "Pending",
//            "Processing",
//            "Shipped",
//            "Delivered",
//            "Cancelled"
//        });
//        statusFilterComboBox.setPreferredSize(new Dimension(120, 30));
//        
//        // Date range filter
//        JLabel dateRangeLabel = new JLabel("Period:");
//        dateRangeComboBox = new JComboBox<>(new String[] {
//            "All Time",
//            "Today",
//            "Last 7 Days",
//            "Last 30 Days",
//            "This Month",
//            "Last Month",
//            "Custom Range"
//        });
//        dateRangeComboBox.setPreferredSize(new Dimension(120, 30));
//        
//        // Date fields for custom range (initially hidden)
//        fromDateField = UIFactory.createDateField("From Date");
//        fromDateField.setPreferredSize(new Dimension(100, 30));
//        fromDateField.setVisible(false);
//        
//        toDateField = UIFactory.createDateField("To Date");
//        toDateField.setPreferredSize(new Dimension(100, 30));
//        toDateField.setVisible(false);
//        
//        // Search field
//        searchField = UIFactory.createSearchField("Search orders...", 200);
//        
//        // Search button
//        JButton searchButton = UIFactory.createSecondaryButton("Search");
//        
//        // Add components to search panel
//        searchPanel.add(statusLabel);
//        searchPanel.add(statusFilterComboBox);
//        searchPanel.add(Box.createHorizontalStrut(10));
//        searchPanel.add(dateRangeLabel);
//        searchPanel.add(dateRangeComboBox);
//        searchPanel.add(fromDateField);
//        searchPanel.add(toDateField);
//        searchPanel.add(Box.createHorizontalStrut(10));
//        searchPanel.add(searchField);
//        searchPanel.add(searchButton);
//        
//        // Add search panel to the header
//        headerPanel.add(searchPanel, BorderLayout.EAST);
//        
//        // Register filter actions
//        dateRangeComboBox.addActionListener(e -> {
//            String selected = (String) dateRangeComboBox.getSelectedItem();
//            boolean isCustomRange = "Custom Range".equals(selected);
//            fromDateField.setVisible(isCustomRange);
//            toDateField.setVisible(isCustomRange);
//            
//            // Apply filter immediately if not custom range
//            if (!isCustomRange) {
//                applyDateRangeFilter(selected);
//            }
//        });
//        
//        statusFilterComboBox.addActionListener(e -> applyFilters());
//        
//        searchButton.addActionListener(e -> applyFilters());
//        
//        return headerPanel;
//    }
//    
//    private JPanel createTablePanel() {
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
//            new EmptyBorder(0, 0, 0, 0)
//        ));
//        
//        // Create the table model with column names
//        String[] columnNames = {"ID", "Order ID", "Customer", "Date", "Amount", "Status"};
//        tableModel = new DefaultTableModel(columnNames, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false; // Make all cells non-editable
//            }
//            
//            @Override
//            public Class<?> getColumnClass(int columnIndex) {
//                // Set specific column types for proper sorting
//                if (columnIndex == 0) return Integer.class;
//                if (columnIndex == 4) return BigDecimal.class;
//                return String.class;
//            }
//        };
//        
//        // Create and set up the table
//        orderTable = UIFactory.createStyledTable(tableModel);
//        
//        // Add row sorting
//        tableSorter = new TableRowSorter<>(tableModel);
//        orderTable.setRowSorter(tableSorter);
//        
//        // Set column widths
//        orderTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID column
//        orderTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Order ID
//        orderTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Customer
//        
//        // Set the renderer for the status column to show colored status
//        orderTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                
//                if (value != null) {
//                    String status = value.toString();
//                    
//                    // Choose colors based on status
//                    if ("Delivered".equals(status)) {
//                        c.setForeground(UIFactory.SUCCESS_COLOR);
//                    } else if ("Processing".equals(status) || "Shipped".equals(status)) {
//                        c.setForeground(UIFactory.WARNING_COLOR);
//                    } else if ("Cancelled".equals(status)) {
//                        c.setForeground(UIFactory.ERROR_COLOR);
//                    } else if ("Pending".equals(status)) {
//                        c.setForeground(UIFactory.MEDIUM_GRAY);
//                    } else {
//                        c.setForeground(UIFactory.DARK_GRAY);
//                    }
//                }
//                
//                return c;
//            }
//        });
//        
//        // Add double-click listener for viewing details
//        orderTable.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    int selectedRow = orderTable.getSelectedRow();
//                    if (selectedRow >= 0) {
//                        selectedRow = orderTable.convertRowIndexToModel(selectedRow);
//                        Order selectedOrder = getOrderAtRow(selectedRow);
//                        if (selectedOrder != null && callback != null) {
//                            callback.onViewOrderDetails(selectedOrder);
//                        }
//                    }
//                }
//            }
//        });
//        
//        // Add the table to a scroll pane
//        JScrollPane scrollPane = UIFactory.createScrollPane(orderTable);
//        panel.add(scrollPane, BorderLayout.CENTER);
//        
//        return panel;
//    }
//    
//    private JPanel createSummaryPanel() {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
//            new EmptyBorder(15, 15, 15, 15)
//        ));
//        
//        // Summary title
//        JLabel titleLabel = new JLabel("Order Summary");
//        titleLabel.setFont(UIFactory.HEADER_FONT);
//        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        
//        panel.add(titleLabel);
//        panel.add(Box.createRigidArea(new Dimension(0, 15)));
//        
//        // Add summary cards
//        panel.add(createSummaryItem("Total Orders", "156"));
//        panel.add(Box.createRigidArea(new Dimension(0, 10)));
//        panel.add(createSummaryItem("Orders This Month", "32"));
//        panel.add(Box.createRigidArea(new Dimension(0, 10)));
//        panel.add(createSummaryItem("Pending Orders", "12"));
//        panel.add(Box.createRigidArea(new Dimension(0, 10)));
//        panel.add(createSummaryItem("Delivered Orders", "18"));
//        panel.add(Box.createRigidArea(new Dimension(0, 10)));
//        panel.add(createSummaryItem("Cancelled Orders", "3"));
//        panel.add(Box.createRigidArea(new Dimension(0, 10)));
//        panel.add(createSummaryItem("Average Order Value", "$243.67"));
//        
//        // Add glue to push everything to the top
//        panel.add(Box.createVerticalGlue());
//        
//        return panel;
//    }
//    
//    private JPanel createSummaryItem(String label, String value) {
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
//            new EmptyBorder(8, 8, 8, 8)
//        ));
//        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
//        
//        JLabel labelComponent = new JLabel(label);
//        labelComponent.setFont(UIFactory.BODY_FONT);
//        labelComponent.setForeground(UIFactory.MEDIUM_GRAY);
//        
//        JLabel valueComponent = new JLabel(value);
//        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 18));
//        
//        panel.add(labelComponent, BorderLayout.NORTH);
//        panel.add(valueComponent, BorderLayout.CENTER);
//        
//        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        
//        return panel;
//    }
//    
//    private JPanel createActionsPanel() {
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.setOpaque(false);
//        
//        // Information label (left side)
//        JLabel infoLabel = new JLabel("Double-click a row to view order details");
//        infoLabel.setFont(UIFactory.SMALL_FONT);
//        infoLabel.setForeground(UIFactory.MEDIUM_GRAY);
//        
//        // Action buttons (right side)
//        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
//        buttonsPanel.setOpaque(false);
//        
//        refreshButton = UIFactory.createSecondaryButton("Refresh");
//        
//        deleteButton = UIFactory.createDangerButton("Delete");
//        deleteButton.setEnabled(false); // Disabled until selection
//        
//        editButton = UIFactory.createWarningButton("Edit");
//        editButton.setEnabled(false); // Disabled until selection
//        
//        viewButton = UIFactory.createSecondaryButton("View Details");
//        viewButton.setEnabled(false); // Disabled until selection
//        
//        addButton = UIFactory.createPrimaryButton("New Order");
//        
//        // Add buttons to panel
//        buttonsPanel.add(refreshButton);
//        buttonsPanel.add(deleteButton);
//        buttonsPanel.add(editButton);
//        buttonsPanel.add(viewButton);
//        buttonsPanel.add(addButton);
//        
//        panel.add(infoLabel, BorderLayout.WEST);
//        panel.add(buttonsPanel, BorderLayout.EAST);
//        
//        // Register button actions
//        registerButtonActions();
//        
//        // Add table selection listener
//        orderTable.getSelectionModel().addListSelectionListener(e -> {
//            if (!e.getValueIsAdjusting()) {
//                boolean hasSelection = orderTable.getSelectedRow() >= 0;
//                editButton.setEnabled(hasSelection);
//                deleteButton.setEnabled(hasSelection);
//                viewButton.setEnabled(hasSelection);
//            }
//        });
//        
//        return panel;
//    }
//    
//    private void registerButtonActions() {
//        // Add button action
//        addButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (callback != null) {
//                    callback.onAddOrder();
//                }
//            }
//        });
//        
//        // Edit button action
//        editButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int selectedRow = orderTable.getSelectedRow();
//                if (selectedRow >= 0 && callback != null) {
//                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
//                    Order selectedOrder = getOrderAtRow(selectedRow);
//                    if (selectedOrder != null) {
//                        callback.onEditOrder(selectedOrder);
//                    }
//                }
//            }
//        });
//        
//        // View button action
//        viewButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int selectedRow = orderTable.getSelectedRow();
//                if (selectedRow >= 0 && callback != null) {
//                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
//                    Order selectedOrder = getOrderAtRow(selectedRow);
//                    if (selectedOrder != null) {
//                        callback.onViewOrderDetails(selectedOrder);
//                    }
//                }
//            }
//        });
//        
//        // Delete button action
//        deleteButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int selectedRow = orderTable.getSelectedRow();
//                if (selectedRow >= 0 && callback != null) {
//                    selectedRow = orderTable.convertRowIndexToModel(selectedRow);
//                    Order selectedOrder = getOrderAtRow(selectedRow);
//                    if (selectedOrder != null) {
//                        int confirm = JOptionPane.showConfirmDialog(
//                            OrderListView.this,
//                            "Are you sure you want to delete order: " + selectedOrder.getOrderId() + "?",
//                            "Confirm Delete",
//                            JOptionPane.YES_NO_OPTION,
//                            JOptionPane.WARNING_MESSAGE
//                        );
//                        
//                        if (confirm == JOptionPane.YES_OPTION) {
//                            callback.onDeleteOrder(selectedOrder);
//                        }
//                    }
//                }
//            }
//        });
//        
//        // Refresh button action
//        refreshButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                refreshTableData();
//            }
//        });
//    }
//    
//    private void refreshTableData() {
//        // Clear the table
//        tableModel.setRowCount(0);
//        
//        // Populate the table with data
//        for (Order order : orderList) {
//            // Get customer name
//            String customerName = "";
//            if (order.getCustomer() != null) {
//                customerName = order.getCustomer().getFullName();
//            }
//            
//            Object[] rowData = {
//                order.getId(),
//                order.getOrderId(),
//                customerName,
//                order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "",
//                order.getTotalAmount(),
//                order.getStatus()
//            };
//            tableModel.addRow(rowData);
//        }
//        
//        // Reset selection and filters
//        orderTable.clearSelection();
//        editButton.setEnabled(false);
//        deleteButton.setEnabled(false);
//        viewButton.setEnabled(false);
//        
//        // Update summary statistics
//        updateSummaryStatistics();
//    }
//    
//    private void updateSummaryStatistics() {
//        // In a real application, this would calculate actual statistics
//        // For this demo, we just use fixed values
//    }
//    
//    private void applyDateRangeFilter(String rangeOption) {
//        // Set appropriate dates based on selection
//        LocalDate fromDate = null;
//        LocalDate toDate = null;
//        
//        Map<String, LocalDate[]> datePeriods = getDatePeriods();
//        LocalDate[] period = datePeriods.get(rangeOption);
//        
//        if (period != null) {
//            fromDate = period[0];
//            toDate = period[1];
//            
//            // Update date fields if visible
//            if (fromDateField.isVisible()) {
//                fromDateField.setText(fromDate != null ? fromDate.format(dateFormatter) : "");
//                toDateField.setText(toDate != null ? toDate.format(dateFormatter) : "");
//            }
//        }
//        
//        // Apply filters
//        applyFilters();
//    }
//    
//    private Map<String, LocalDate[]> getDatePeriods() {
//        Map<String, LocalDate[]> periods = new HashMap<>();
//        LocalDate today = LocalDate.now();
//        
//        // All Time
//        periods.put("All Time", new LocalDate[] { null, null });
//        
//        // Today
//        periods.put("Today", new LocalDate[] { today, today });
//        
//        // Last 7 Days
//        periods.put("Last 7 Days", new LocalDate[] { today.minusDays(6), today });
//        
//        // Last 30 Days
//        periods.put("Last 30 Days", new LocalDate[] { today.minusDays(29), today });
//        
//        // This Month
//        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
//        periods.put("This Month", new LocalDate[] { firstDayOfMonth, today });
//        
//        // Last Month
//        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
//        LocalDate lastDayOfLastMonth = firstDayOfMonth.minusDays(1);
//        periods.put("Last Month", new LocalDate[] { firstDayOfLastMonth, lastDayOfLastMonth });
//        
//        // Custom Range - handled separately
//        
//        return periods;
//    }
//    
//    /**
//     * Gets the Order object corresponding to a specific table row
//     * 
//     * @param modelRow Row index in the table model
//     * @return The Order object or null if not found
//     */
//    private Order getOrderAtRow(int modelRow) {
//        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
//            int orderId = (int) tableModel.getValueAt(modelRow, 0);
//            for (Order order : orderList) {
//                if (order.getId() == orderId) {
//                    return order;
//                }
//            }
//        }
//        return null;
//    }
//    
//    /**
//     * Updates the order list and refreshes the table
//     * 
//     * @param orders New list of orders
//     */
//    public void updateOrders(List<Order> orders) {
//        this.orderList = orders != null ? orders : new ArrayList<>();
//        refreshTableData();
//    }
//    
//    /**
//     * Adds an order to the list and refreshes the table
//     * 
//     * @param order Order to add
//     */
//    public void addOrder(Order order) {
//        if (order != null) {
//            this.orderList.add(order);
//            refreshTableData();
//        }
//    }
//    
//    /**
//     * Updates an order in the list and refreshes the table
//     * 
//     * @param order Order to update
//     */
//    public void updateOrder(Order order) {
//        if (order != null) {
//            for (int i = 0; i < orderList.size(); i++) {
//                if (orderList.get(i).getId() == order.getId()) {
//                    orderList.set(i, order);
//                    break;
//                }
//            }
//            refreshTableData();
//        }
//    }
//    
//    /**
//     * Removes an order from the list and refreshes the table
//     * 
//     * @param order Order to remove
//     */
//    public void removeOrder(Order order) {
//        if (order != null) {
//            orderList.removeIf(o -> o.getId() == order.getId());
//            refreshTableData();
//        }
//    }
//}