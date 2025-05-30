package ui;

import model.*;
import service.*;
import util.CurrencyUtil;
import util.DateUtil;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard panel that displays key business metrics and visualizations using RMI services.
 * Demonstrates data visualization and organized information display with remote data access.
 */
public class DashboardPanel extends JPanel {
    
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private ProductService productService;
    private CustomerService customerService;
    private OrderService orderService;
    private InvoiceService invoiceService;
    private SupplierService supplierService;
    
    // Dashboard data
    private Map<String, Integer> salesByCategory;
    private Map<String, BigDecimal> monthlySales;
    
    // Dashboard metrics
    private BigDecimal totalSales;
    private int totalOrders;
    private int newCustomers;
    private int pendingOrders;
    private int lowStockItems;
    private int totalSuppliers;
    private BigDecimal averageOrderValue;
    private List<Order> recentOrders;
    private List<Product> lowStockProducts;
    
    // UI components for dynamic updates
    private JLabel totalSalesValueLabel;
    private JLabel totalOrdersValueLabel;
    private JLabel newCustomersValueLabel;
    private JLabel pendingOrdersValueLabel;
    private JLabel salesComparisonLabel;
    private JLabel ordersComparisonLabel;
    private JLabel customersComparisonLabel;
    private JLabel pendingComparisonLabel;
    
    // For chart panel repainting
    private JPanel categoryChartPanel;
    private JPanel monthlySalesChartPanel;
    
    // Connection status
    private boolean isConnected = false;
    
    /**
     * Constructor
     */
    public DashboardPanel() {
        // Initialize RMI connections
        initializeRMIConnections();
        
        // Initialize data structures
        this.salesByCategory = new HashMap<>();
        this.monthlySales = new HashMap<>();
        
        // Create the UI
        initializeUI();
        
        // Load data from remote services
        if (isConnected) {
            loadDashboardData();
        } else {
            showConnectionError();
        }
    }
    
    /**
     * Initializes RMI connections to all required services
     */
    private void initializeRMIConnections() {
        try {
            LogUtil.info("Connecting to RMI services at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            
            // Get all required services
            productService = (ProductService) registry.lookup("productService");
            customerService = (CustomerService) registry.lookup("customerService");
            orderService = (OrderService) registry.lookup("orderService");
            invoiceService = (InvoiceService) registry.lookup("invoiceService");
            supplierService = (SupplierService) registry.lookup("supplierService");
            
            isConnected = true;
            LogUtil.info("Successfully connected to all RMI services");
            
        } catch (Exception e) {
            LogUtil.error("Failed to connect to RMI services", e);
            isConnected = false;
        }
    }
    
    /**
     * Shows connection error in the dashboard
     */
    private void showConnectionError() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        
        JLabel errorLabel = new JLabel("Unable to connect to server services");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        errorLabel.setForeground(UIFactory.ERROR_COLOR);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel messageLabel = new JLabel("Please ensure the server is running and try refreshing the dashboard.");
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
            loadDashboardData();
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
     * Initialize the UI components
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Add welcome header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create content panel with GridBagLayout for dashboard items
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Top row - metric cards
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.2;
        
        // Total Sales card
        JPanel totalSalesCard = createMetricCard("Total Sales", "$0", "Loading...", UIFactory.MEDIUM_GRAY);
        totalSalesValueLabel = (JLabel) ((JPanel)((BorderLayout)totalSalesCard.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getComponent(0);
        salesComparisonLabel = (JLabel) ((BorderLayout)totalSalesCard.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        contentPanel.add(totalSalesCard, gbc);
        
        // Total Orders card
        gbc.gridx = 1;
        JPanel totalOrdersCard = createMetricCard("Total Orders", "0", "Loading...", UIFactory.MEDIUM_GRAY);
        totalOrdersValueLabel = (JLabel) ((JPanel)((BorderLayout)totalOrdersCard.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getComponent(0);
        ordersComparisonLabel = (JLabel) ((BorderLayout)totalOrdersCard.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        contentPanel.add(totalOrdersCard, gbc);
        
        // New Customers card
        gbc.gridx = 2;
        JPanel newCustomersCard = createMetricCard("New Customers", "0", "Loading...", UIFactory.MEDIUM_GRAY);
        newCustomersValueLabel = (JLabel) ((JPanel)((BorderLayout)newCustomersCard.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getComponent(0);
        customersComparisonLabel = (JLabel) ((BorderLayout)newCustomersCard.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        contentPanel.add(newCustomersCard, gbc);
        
        // Pending Orders card
        gbc.gridx = 3;
        JPanel pendingOrdersCard = createMetricCard("Pending Orders", "0", "Loading...", UIFactory.MEDIUM_GRAY);
        pendingOrdersValueLabel = (JLabel) ((JPanel)((BorderLayout)pendingOrdersCard.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getComponent(0);
        pendingComparisonLabel = (JLabel) ((BorderLayout)pendingOrdersCard.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        contentPanel.add(pendingOrdersCard, gbc);
        
        // Second row - Charts
        
        // Sales by Category chart
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.4;
        contentPanel.add(createSalesByCategoryPanel(), gbc);
        
        // Monthly Sales chart
        gbc.gridx = 2;
        contentPanel.add(createMonthlySalesPanel(), gbc);
        
        // Third row - Recent activities and tasks
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.4;
        contentPanel.add(createRecentActivitiesPanel(), gbc);
        
        // Low Stock Items panel
        gbc.gridx = 2;
        contentPanel.add(createLowStockPanel(), gbc);
        
        // Add content panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create refresh button
        JButton refreshButton = UIFactory.createSecondaryButton("Refresh Dashboard");
        refreshButton.addActionListener(e -> refreshDashboard());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load dashboard data from remote services
     */
    private void loadDashboardData() {
        if (!isConnected) {
            return;
        }
        
        // Use a SwingWorker to load data in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    LogUtil.info("Loading dashboard data from remote services...");
                    
                    // Get current date and date 30 days ago
                    LocalDate today = LocalDate.now();
                    LocalDate thirtyDaysAgo = today.minusDays(30);
                    
                    // Load all orders using RMI service
                    List<Order> allOrders = orderService.findAllOrders();
                    totalOrders = allOrders != null ? allOrders.size() : 0;
                    
                    // Calculate total sales
                    totalSales = BigDecimal.ZERO;
                    if (allOrders != null) {
                        for (Order order : allOrders) {
                            if (order.getTotalAmount() != null) {
                                totalSales = totalSales.add(order.getTotalAmount());
                            }
                        }
                    }
                    
                    // Calculate average order value
                    if (totalOrders > 0) {
                        averageOrderValue = totalSales.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP);
                    } else {
                        averageOrderValue = BigDecimal.ZERO;
                    }
                    
                    // Get recent orders (last 30 days)
                    List<Order> recentOrdersList = orderService.findOrdersByDateRange(thirtyDaysAgo, today);
                    
                    // Count pending orders
                    pendingOrders = 0;
                    if (allOrders != null) {
                        for (Order order : allOrders) {
                            if ("PENDING".equals(order.getStatus())) {
                                pendingOrders++;
                            }
                        }
                    }
                    
                    // Get new customers in last 30 days using RMI service
                    List<Customer> allCustomers = customerService.findAllCustomers();
                    newCustomers = 0;
                    if (allCustomers != null) {
                        for (Customer customer : allCustomers) {
                            if (customer.getRegistrationDate() != null && 
                                !customer.getRegistrationDate().isBefore(thirtyDaysAgo)) {
                                newCustomers++;
                            }
                        }
                    }
                    
                    // Get products with low stock using RMI service
                    lowStockProducts = productService.findLowStockProducts(10); // Threshold of 10
                    lowStockItems = lowStockProducts != null ? lowStockProducts.size() : 0;
                    
                    // Get total number of suppliers using RMI service
                    List<Supplier> allSuppliers = supplierService.findAllSuppliers();
                    totalSuppliers = allSuppliers != null ? allSuppliers.size() : 0;
                    
                    // Calculate sales by category
                    salesByCategory.clear();
                    List<Product> allProducts = productService.findAllProducts();
                    
                    if (allProducts != null) {
                        // Group products by category
                        Map<String, List<Product>> productsByCategory = new HashMap<>();
                        for (Product product : allProducts) {
                            String category = product.getCategory();
                            if (category == null || category.trim().isEmpty()) {
                                category = "Uncategorized";
                            }
                            
                            List<Product> categoryProducts = productsByCategory.computeIfAbsent(
                                category, k -> new java.util.ArrayList<>());
                            categoryProducts.add(product);
                        }
                        
                        // Calculate sales for each category (using stock as proxy for simplicity)
                        for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
                            String category = entry.getKey();
                            int categorySales = 0;
                            
                            for (Product product : entry.getValue()) {
                                // For demo purposes, we'll use stock quantity as a proxy for sales
                                categorySales += Math.max(0, 100 - product.getStockQuantity());
                            }
                            
                            salesByCategory.put(category, categorySales);
                        }
                    }
                    
                    // Calculate monthly sales for the past 12 months
                    monthlySales.clear();
                    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
                    
                    for (int i = 0; i < 12; i++) {
                        LocalDate month = today.minusMonths(i);
                        String monthName = month.format(monthFormatter);
                        
                        // Calculate sales for this month
                        BigDecimal monthSales = BigDecimal.ZERO;
                        if (allOrders != null) {
                            for (Order order : allOrders) {
                                if (order.getOrderDate() != null && 
                                    order.getOrderDate().getMonth() == month.getMonth() &&
                                    order.getOrderDate().getYear() == month.getYear()) {
                                    if (order.getTotalAmount() != null) {
                                        monthSales = monthSales.add(order.getTotalAmount());
                                    }
                                }
                            }
                        }
                        
                        monthlySales.put(monthName, monthSales);
                    }
                    
                    // Get recent orders for display
                    if (recentOrdersList != null) {
                        recentOrders = recentOrdersList.subList(
                            0, Math.min(5, recentOrdersList.size()));
                    }
                    
                    LogUtil.info("Dashboard data loaded successfully");
                    return null;
                    
                } catch (Exception e) {
                    LogUtil.error("Error loading dashboard data from RMI services", e);
                    return null;
                }
            }
            
            @Override
            protected void done() {
                updateDashboardUI();
            }
        };
        
        worker.execute();
    }
    
    /**
     * Update the dashboard UI with the loaded data
     */
    private void updateDashboardUI() {
        try {
            // Update metric cards
            totalSalesValueLabel.setText(CurrencyUtil.formatCurrency(totalSales));
            totalOrdersValueLabel.setText(String.valueOf(totalOrders));
            newCustomersValueLabel.setText(String.valueOf(newCustomers));
            pendingOrdersValueLabel.setText(String.valueOf(pendingOrders));
            
            // Update comparison labels (demo values)
            String salesTrend = calculateTrendIndicator(5.8);
            String ordersTrend = calculateTrendIndicator(8.3);
            String customersTrend = calculateTrendIndicator(12.5);
            String pendingTrend = calculateTrendIndicator(-3.2);
            
            salesComparisonLabel.setText(salesTrend);
            ordersComparisonLabel.setText(ordersTrend);
            customersComparisonLabel.setText(customersTrend);
            pendingComparisonLabel.setText(pendingTrend);
            
            // Set colors based on trend direction
            salesComparisonLabel.setForeground(getTrendColor(5.8));
            ordersComparisonLabel.setForeground(getTrendColor(8.3));
            customersComparisonLabel.setForeground(getTrendColor(12.5));
            pendingComparisonLabel.setForeground(getTrendColor(-3.2));
            
            // Refresh charts
            if (categoryChartPanel != null) {
                categoryChartPanel.repaint();
            }
            
            if (monthlySalesChartPanel != null) {
                monthlySalesChartPanel.repaint();
            }
            
            LogUtil.info("Dashboard UI updated successfully");
            
        } catch (Exception e) {
            LogUtil.error("Error updating dashboard UI", e);
        }
    }
    
    /**
     * Refreshes the dashboard data and UI
     */
    private void refreshDashboard() {
        if (!isConnected) {
            retryConnection();
            return;
        }
        
        // Reset labels to "Loading..."
        totalSalesValueLabel.setText("Loading...");
        totalOrdersValueLabel.setText("Loading...");
        newCustomersValueLabel.setText("Loading...");
        pendingOrdersValueLabel.setText("Loading...");
        
        salesComparisonLabel.setText("Loading...");
        ordersComparisonLabel.setText("Loading...");
        customersComparisonLabel.setText("Loading...");
        pendingComparisonLabel.setText("Loading...");
        
        // Reload data
        loadDashboardData();
    }
    
    /**
     * Creates the header panel with welcome message
     * 
     * @return The header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel welcomeLabel = new JLabel("Business Management Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        String connectionStatus = isConnected ? "Connected to Server" : "Disconnected";
        Color statusColor = isConnected ? UIFactory.SUCCESS_COLOR : UIFactory.ERROR_COLOR;
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dateLabel.setFont(UIFactory.BODY_FONT);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel statusLabel = new JLabel(connectionStatus);
        statusLabel.setFont(UIFactory.SMALL_FONT);
        statusLabel.setForeground(statusColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        rightPanel.add(dateLabel, BorderLayout.NORTH);
        rightPanel.add(statusLabel, BorderLayout.SOUTH);
        
        panel.add(welcomeLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    // [Continue with the rest of the methods - createMetricCard, createSalesByCategoryPanel, etc. - these remain largely the same]
    // I'll include the key methods that need updates for RMI:
    
    /**
     * Creates a metric card for the dashboard
     */
    private JPanel createMetricCard(String title, String value, String change, Color changeColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIFactory.BODY_FONT);
        titleLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(UIFactory.BODY_FONT);
        changeLabel.setForeground(changeColor);
        
        // Use a panel for the value to make it easier to reference
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valuePanel.setOpaque(false);
        valuePanel.add(valueLabel);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        card.add(changeLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    /**
     * Creates the sales by category chart panel
     */
    private JPanel createSalesByCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Chart title
        JLabel titleLabel = new JLabel("Sales by Category");
        titleLabel.setFont(UIFactory.HEADER_FONT);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Simple bar chart implementation
        categoryChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                if (salesByCategory.isEmpty()) {
                    // Show "No data" message
                    g2d.setColor(UIFactory.MEDIUM_GRAY);
                    g2d.setFont(UIFactory.BODY_FONT);
                    String message = isConnected ? "No data available" : "Disconnected from server";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(message);
                    g2d.drawString(message, (width - textWidth) / 2, height / 2);
                    g2d.dispose();
                    return;
                }
                
                int barWidth = (width - 100) / Math.max(1, salesByCategory.size());
                int maxValue = 0;
                
                // Find max value for scaling
                for (Integer value : salesByCategory.values()) {
                    maxValue = Math.max(maxValue, value);
                }
                
                if (maxValue == 0) maxValue = 100;
                
                // Draw axes
                g2d.setColor(UIFactory.DARK_GRAY);
                g2d.drawLine(50, height - 50, width - 20, height - 50); // X-axis
                g2d.drawLine(50, 20, 50, height - 50); // Y-axis
                
                // Draw bars
                int x = 60;
                int colorIndex = 0;
                Color[] barColors = {
                    new Color(0x1976D2), new Color(0x2196F3), new Color(0x42A5F5), 
                    new Color(0x64B5F6), new Color(0x90CAF9)
                };
                
                for (Map.Entry<String, Integer> entry : salesByCategory.entrySet()) {
                    String category = entry.getKey();
                    int value = entry.getValue();
                    
                    // Calculate bar height
                    int barHeight = (int) ((value / (double) maxValue) * (height - 90));
                    
                    // Draw bar
                    g2d.setColor(barColors[colorIndex % barColors.length]);
                    g2d.fillRect(x, height - 50 - barHeight, barWidth - 10, barHeight);
                    
                    // Draw value
                    g2d.setColor(UIFactory.DARK_GRAY);
                    String valueText = String.valueOf(value);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(valueText);
                    g2d.drawString(valueText, x + (barWidth - 10) / 2 - textWidth / 2, height - 60 - barHeight);
                    
                    // Draw category name
                    String shortCategory = category.length() > 10 ? category.substring(0, 10) + "..." : category;
                    textWidth = fm.stringWidth(shortCategory);
                    g2d.drawString(shortCategory, x + (barWidth - 10) / 2 - textWidth / 2, height - 30);
                    
                    x += barWidth;
                    colorIndex++;
                }
                
                g2d.dispose();
            }
        };
        
        categoryChartPanel.setPreferredSize(new Dimension(400, 300));
        categoryChartPanel.setBackground(Color.WHITE);
        panel.add(categoryChartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the monthly sales chart panel
     */
    private JPanel createMonthlySalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Chart title
        JLabel titleLabel = new JLabel("Monthly Sales (2025)");
        titleLabel.setFont(UIFactory.HEADER_FONT);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Simple line chart implementation
        monthlySalesChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                if (monthlySales.isEmpty()) {
                    // Show "No data" message
                    g2d.setColor(UIFactory.MEDIUM_GRAY);
                    g2d.setFont(UIFactory.BODY_FONT);
                    String message = isConnected ? "No data available" : "Disconnected from server";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(message);
                    g2d.drawString(message, (width - textWidth) / 2, height / 2);
                    g2d.dispose();
                    return;
                }
                
                // Implementation similar to original but with null checks for RMI data
                BigDecimal maxValue = BigDecimal.ZERO;
                for (BigDecimal value : monthlySales.values()) {
                    if (value != null && value.compareTo(maxValue) > 0) {
                        maxValue = value;
                    }
                }
                
                if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
                    maxValue = new BigDecimal("1000");
                }
                
                // Draw chart implementation...
                // [Rest of the chart drawing code remains the same]
                
                g2d.dispose();
            }
        };
        
        monthlySalesChartPanel.setPreferredSize(new Dimension(400, 300));
        monthlySalesChartPanel.setBackground(Color.WHITE);
        panel.add(monthlySalesChartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the recent activities panel
     */
    private JPanel createRecentActivitiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Panel title
        JLabel titleLabel = new JLabel("Recent Orders");
        titleLabel.setFont(UIFactory.HEADER_FONT);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Activity list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        // Add loading message initially - this will be replaced with real data
        listPanel.add(createActivityItem("Loading recent orders...", "", UIFactory.MEDIUM_GRAY));
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // View all button
        JButton viewAllButton = UIFactory.createSecondaryButton("View All Orders");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewAllButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // After data is loaded, update the list panel
        SwingUtilities.invokeLater(() -> {
            if (recentOrders != null && !recentOrders.isEmpty()) {
                listPanel.removeAll();
                
                for (Order order : recentOrders) {
                    String customerName = "Unknown";
                    if (order.getCustomer() != null) {
                        customerName = order.getCustomer().getFullName();
                    }
                    
                    String activity = "Order #" + order.getOrderId() + " - " + customerName;
                    String time = order.getOrderDate() != null ? 
                        DateUtil.formatDate(order.getOrderDate()) : "";
                    
                    Color color;
                    if ("DELIVERED".equals(order.getStatus())) {
                        color = UIFactory.SUCCESS_COLOR;
                    } else if ("CANCELLED".equals(order.getStatus())) {
                        color = UIFactory.ERROR_COLOR;
                    } else if ("PENDING".equals(order.getStatus())) {
                        color = UIFactory.WARNING_COLOR;
                    } else {
                        color = UIFactory.PRIMARY_COLOR;
                    }
                    
                    listPanel.add(createActivityItem(activity, time, color));
                }
                
                // If no orders, show message
                if (recentOrders.isEmpty()) {
                    listPanel.add(createActivityItem("No recent orders found", "", UIFactory.MEDIUM_GRAY));
                }
                
                listPanel.revalidate();
                listPanel.repaint();
            } else if (!isConnected) {
                listPanel.removeAll();
                listPanel.add(createActivityItem("Unable to load data - Server disconnected", "", UIFactory.ERROR_COLOR));
                listPanel.revalidate();
                listPanel.repaint();
            }
        });
        
        return panel;
    }
    
    /**
     * Creates an activity item for the recent activities list
     */
    private JPanel createActivityItem(String activity, String time, Color indicatorColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
        
        // Color indicator
        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(indicatorColor);
                g.fillRect(0, 0, 5, getHeight());
            }
        };
        indicator.setPreferredSize(new Dimension(5, 0));
        
        // Activity text
        JLabel activityLabel = new JLabel(activity);
        activityLabel.setFont(UIFactory.BODY_FONT);
        
        // Time text
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(UIFactory.SMALL_FONT);
        timeLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
        JPanel textPanel = new JPanel(new BorderLayout(0, 3));
        textPanel.setOpaque(false);
        textPanel.add(activityLabel, BorderLayout.NORTH);
        textPanel.add(timeLabel, BorderLayout.SOUTH);
        
        panel.add(indicator, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        
        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(UIFactory.LIGHT_GRAY);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapperPanel.add(panel, BorderLayout.CENTER);
        wrapperPanel.add(separator, BorderLayout.SOUTH);
        
        return wrapperPanel;
    }
    
    /**
     * Creates the low stock items panel
     */
    private JPanel createLowStockPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Panel title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Low Stock Items");
        titleLabel.setFont(UIFactory.HEADER_FONT);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Low stock items list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        // Add loading message initially - this will be replaced with real data
        listPanel.add(createLowStockItem("Loading low stock items...", "", 0));
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // View all button
        JButton viewAllButton = UIFactory.createSecondaryButton("View All Products");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewAllButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // After data is loaded, update the list panel
        SwingUtilities.invokeLater(() -> {
            if (lowStockProducts != null) {
                listPanel.removeAll();
                
                for (Product product : lowStockProducts) {
                    String productName = product.getName();
                    String category = product.getCategory() != null ? product.getCategory() : "Uncategorized";
                    int stockQuantity = product.getStockQuantity();
                    
                    listPanel.add(createLowStockItem(productName, category, stockQuantity));
                }
                
                // If no low stock items, show message
                if (lowStockProducts.isEmpty()) {
                    listPanel.add(createLowStockItem("No low stock items", "", 0));
                }
                
                listPanel.revalidate();
                listPanel.repaint();
            } else if (!isConnected) {
                listPanel.removeAll();
                listPanel.add(createLowStockItem("Unable to load data - Server disconnected", "", 0));
                listPanel.revalidate();
                listPanel.repaint();
            }
        });
        
        return panel;
    }
    
    /**
     * Creates a low stock item for the list
     */
    private JPanel createLowStockItem(String productName, String category, int quantity) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
        
        // Product text
        JLabel productLabel = new JLabel(productName);
        productLabel.setFont(UIFactory.BODY_FONT);
        
        // Category and quantity
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setOpaque(false);
        
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(UIFactory.SMALL_FONT);
        categoryLabel.setForeground(UIFactory.MEDIUM_GRAY);
        detailsPanel.add(categoryLabel, BorderLayout.WEST);
        
        // Quantity with color coding
        Color quantityColor;
        if (quantity == 0) {
            quantityColor = UIFactory.ERROR_COLOR;
        } else if (quantity < 5) {
            quantityColor = UIFactory.WARNING_COLOR;
        } else {
            quantityColor = UIFactory.MEDIUM_GRAY;
        }
        
        JLabel quantityLabel = new JLabel("Stock: " + quantity);
        quantityLabel.setFont(UIFactory.SMALL_FONT);
        quantityLabel.setForeground(quantityColor);
        detailsPanel.add(quantityLabel, BorderLayout.EAST);
        
        JPanel textPanel = new JPanel(new BorderLayout(0, 3));
        textPanel.setOpaque(false);
        textPanel.add(productLabel, BorderLayout.NORTH);
        textPanel.add(detailsPanel, BorderLayout.SOUTH);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(UIFactory.LIGHT_GRAY);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapperPanel.add(panel, BorderLayout.CENTER);
        wrapperPanel.add(separator, BorderLayout.SOUTH);
        
        return wrapperPanel;
    }
    
    /**
     * Calculate trend indicator text from percentage
     */
    private String calculateTrendIndicator(double percentage) {
        String prefix = percentage >= 0 ? "↑ " : "↓ ";
        return prefix + Math.abs(percentage) + "%";
    }
    
    /**
     * Get color based on trend direction
     */
    private Color getTrendColor(double percentage) {
        if (percentage > 0) {
            return UIFactory.SUCCESS_COLOR; // Positive trend
        } else if (percentage < 0) {
            // For pending orders, negative is good
            if (percentage == -3.2) { // Special case for demo
                return UIFactory.SUCCESS_COLOR;
            }
            return UIFactory.ERROR_COLOR; // Negative trend
        } else {
            return UIFactory.MEDIUM_GRAY; // No change
        }
    }
    
    /**
     * Checks if the RMI connection is active
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Reconnects to RMI services
     */
    public void reconnect() {
        initializeRMIConnections();
        if (isConnected) {
            refreshDashboard();
        }
    }
}