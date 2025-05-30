package ui.product;

import controller.ProductController;
import util.LogUtil;
import util.RMIConnectionManager;
import ui.UIFactory;

import javax.swing.*;
import java.awt.*;

/**
 * RMI-based main view for the Product module.
 * Serves as a container for the product management interface using RMI services.
 */
public class ProductView extends JPanel {
    private ProductController controller;
    private JLabel connectionStatusLabel;
    private JButton reconnectButton;
    
    /**
     * Constructor
     */
    public ProductView() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Initialize UI first
        initializeUI();
        
        // Initialize controller with RMI connection
        initializeController();
    }
    
    /**
     * Constructor with parent component for better dialog positioning
     * 
     * @param parentComponent Parent component for dialogs
     */
    public ProductView(Component parentComponent) {
        this();
        // Update controller with parent component if needed
        if (controller != null) {
            // The controller already has the parent component reference
        }
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Create header panel with connection status
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // The center will be populated with the list view once controller is initialized
        // For now, show a loading panel
        JPanel loadingPanel = createLoadingPanel();
        add(loadingPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create header panel with connection status and controls
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIFactory.BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Product Management");
        titleLabel.setFont(UIFactory.HEADER_FONT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Connection status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        
        connectionStatusLabel = new JLabel("Connecting to RMI server...");
        connectionStatusLabel.setFont(UIFactory.SMALL_FONT);
        connectionStatusLabel.setForeground(UIFactory.WARNING_COLOR);
        
        reconnectButton = UIFactory.createSecondaryButton("Reconnect");
        reconnectButton.setVisible(false);
        reconnectButton.addActionListener(e -> reconnectToServer());
        
        statusPanel.add(connectionStatusLabel);
        statusPanel.add(reconnectButton);
        
        headerPanel.add(statusPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create loading panel shown while initializing
     */
    private JPanel createLoadingPanel() {
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // Loading icon/spinner (you can replace with an actual spinner)
        JLabel loadingIcon = new JLabel("⟳");
        loadingIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        loadingIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingIcon.setForeground(UIFactory.PRIMARY_COLOR);
        
        JLabel loadingText = new JLabel("Initializing Product Management...");
        loadingText.setFont(UIFactory.BODY_FONT);
        loadingText.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingText.setForeground(UIFactory.MEDIUM_GRAY);
        
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(loadingIcon);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(loadingText);
        centerPanel.add(Box.createVerticalGlue());
        
        loadingPanel.add(centerPanel, BorderLayout.CENTER);
        
        return loadingPanel;
    }
    
    /**
     * Initialize the RMI-based controller
     */
    private void initializeController() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    LogUtil.info("Initializing ProductController with RMI connection...");
                    
                    // Test RMI connection first
                    if (!RMIConnectionManager.testConnection()) {
                        LogUtil.error("RMI connection test failed");
                        return false;
                    }
                    
                    // Initialize controller
                    controller = new ProductController(ProductView.this);
                    
                    // Verify controller is properly connected
                    if (!controller.isConnected()) {
                        LogUtil.error("ProductController failed to connect to RMI services");
                        return false;
                    }
                    
                    LogUtil.info("ProductController initialized successfully");
                    return true;
                    
                } catch (Exception ex) {
                    LogUtil.error("Error initializing ProductController", ex);
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success && controller != null) {
                        // Remove loading panel and add the list view
                        removeAll();
                        
                        // Re-add header
                        JPanel headerPanel = createHeaderPanel();
                        add(headerPanel, BorderLayout.NORTH);
                        
                        // Add the list view from controller
                        add(controller.getListView(), BorderLayout.CENTER);
                        
                        // Update connection status
                        updateConnectionStatus(true, "Connected to RMI server");
                        
                        LogUtil.info("Product view initialized successfully with RMI connection");
                        
                    } else {
                        // Show error state
                        showErrorState("Failed to connect to RMI server");
                        updateConnectionStatus(false, "RMI connection failed");
                    }
                    
                    // Refresh the UI
                    revalidate();
                    repaint();
                    
                } catch (Exception ex) {
                    LogUtil.error("Error in ProductView initialization completion", ex);
                    showErrorState("Initialization error: " + ex.getMessage());
                    updateConnectionStatus(false, "Initialization failed");
                    revalidate();
                    repaint();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Update connection status display
     */
    private void updateConnectionStatus(boolean connected, String message) {
        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText(message);
            if (connected) {
                connectionStatusLabel.setForeground(UIFactory.SUCCESS_COLOR);
                reconnectButton.setVisible(false);
            } else {
                connectionStatusLabel.setForeground(UIFactory.ERROR_COLOR);
                reconnectButton.setVisible(true);
            }
        }
    }
    
    /**
     * Show error state when RMI connection fails
     */
    private void showErrorState(String errorMessage) {
        removeAll();
        
        // Re-add header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create error panel
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // Error icon
        JLabel errorIcon = new JLabel("⚠");
        errorIcon.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        errorIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorIcon.setForeground(UIFactory.ERROR_COLOR);
        
        // Error title
        JLabel errorTitle = new JLabel("Connection Error");
        errorTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        errorTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorTitle.setForeground(UIFactory.ERROR_COLOR);
        
        // Error message
        JLabel errorText = new JLabel("<html><center>" + errorMessage + "<br><br>" +
            "Please ensure the RMI server is running and accessible.</center></html>");
        errorText.setFont(UIFactory.BODY_FONT);
        errorText.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorText.setForeground(UIFactory.MEDIUM_GRAY);
        
        // Retry button
        JButton retryButton = UIFactory.createPrimaryButton("Retry Connection");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(e -> reconnectToServer());
        
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(errorIcon);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(errorTitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(errorText);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(retryButton);
        centerPanel.add(Box.createVerticalGlue());
        
        errorPanel.add(centerPanel, BorderLayout.CENTER);
        add(errorPanel, BorderLayout.CENTER);
    }
    
    /**
     * Attempt to reconnect to the RMI server
     */
    private void reconnectToServer() {
        updateConnectionStatus(false, "Reconnecting...");
        reconnectButton.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Test connection
                    if (!RMIConnectionManager.testConnection()) {
                        return false;
                    }
                    
                    // Reinitialize controller
                    if (controller != null) {
                        controller.reconnect();
                    } else {
                        controller = new ProductController(ProductView.this);
                    }
                    
                    return controller.isConnected();
                    
                } catch (Exception ex) {
                    LogUtil.error("Error during reconnection", ex);
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        // Rebuild the entire UI
                        removeAll();
                        initializeUI();
                        
                        // Re-add the list view
                        if (controller != null && controller.getListView() != null) {
                            JPanel headerPanel = createHeaderPanel();
                            add(headerPanel, BorderLayout.NORTH);
                            add(controller.getListView(), BorderLayout.CENTER);
                        }
                        
                        updateConnectionStatus(true, "Reconnected to RMI server");
                        LogUtil.info("Successfully reconnected to RMI server");
                        
                    } else {
                        updateConnectionStatus(false, "Reconnection failed");
                        LogUtil.error("Failed to reconnect to RMI server");
                    }
                    
                } catch (Exception ex) {
                    LogUtil.error("Error handling reconnection result", ex);
                    updateConnectionStatus(false, "Reconnection error");
                }
                
                reconnectButton.setEnabled(true);
                revalidate();
                repaint();
            }
        };
        
        worker.execute();
    }
    
    /**
     * Gets the product controller
     * 
     * @return The ProductController instance
     */
    public ProductController getController() {
        return controller;
    }
    
    /**
     * Checks if the view is properly initialized and connected
     * 
     * @return true if connected and ready
     */
    public boolean isInitialized() {
        return controller != null && controller.isConnected();
    }
    
    /**
     * Refresh the product data
     */
    public void refreshData() {
        if (controller != null && controller.isConnected()) {
            controller.refreshProductList();
        } else {
            JOptionPane.showMessageDialog(this,
                "Cannot refresh data - not connected to RMI server",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Static method to create and show the product view in a frame
     * for testing purposes
     */
    public static void showInFrame() {
        // Set up UI look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Set global font settings
            UIManager.put("Label.font", UIFactory.BODY_FONT);
            UIManager.put("Button.font", UIFactory.BODY_FONT);
            UIManager.put("TextField.font", UIFactory.BODY_FONT);
            UIManager.put("Table.font", UIFactory.BODY_FONT);
            UIManager.put("TableHeader.font", UIFactory.HEADER_FONT);
            
        } catch (Exception e) {
            LogUtil.error("Failed to set look and feel", e);
        }
        
        // Create the frame
        JFrame frame = new JFrame("Product Management - RMI Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 600));
        frame.setLocationRelativeTo(null);
        
        // Create and add the product view
        ProductView productView = new ProductView();
        frame.add(productView);
        
        // Add window closing listener for cleanup
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                LogUtil.info("Product Management window closing");
                System.exit(0);
            }
        });
        
        // Show the frame
        frame.setVisible(true);
        LogUtil.info("Product Management frame displayed");
    }
    
    /**
     * Main method for testing the product view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Initialize logging
        LogUtil.initialize();
        LogUtil.info("Starting Product Management application...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                showInFrame();
            } catch (Exception ex) {
                LogUtil.error("Error starting Product Management application", ex);
                JOptionPane.showMessageDialog(null,
                    "Error starting application: " + ex.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}