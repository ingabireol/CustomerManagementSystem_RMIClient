package ui.customer;

import controller.CustomerController;
import util.LogUtil;
import javax.swing.*;
import java.awt.*;
import ui.UIFactory;

/**
 * Main view for the Customer module using RMI-based controller.
 * Serves as a container for the customer management interface with remote connectivity.
 */
public class CustomerView extends JPanel {
    private CustomerController controller;
    private JLabel connectionStatusLabel;
    private JButton reconnectButton;
    
    /**
     * Constructor
     */
    public CustomerView() {
        initializeUI();
        initializeController();
    }
    
    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Create header with connection status
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // The controller will add the list view to the center
    }
    
    /**
     * Creates the header panel with connection status
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIFactory.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UIFactory.PRIMARY_COLOR);
        
        // Connection status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        
        JLabel statusLabelText = new JLabel("Server Status: ");
        statusLabelText.setFont(UIFactory.SMALL_FONT);
        
        connectionStatusLabel = new JLabel("Connecting...");
        connectionStatusLabel.setFont(UIFactory.SMALL_FONT);
        connectionStatusLabel.setForeground(UIFactory.WARNING_COLOR);
        
        reconnectButton = UIFactory.createSecondaryButton("Reconnect");
        reconnectButton.addActionListener(e -> reconnectToServer());
        reconnectButton.setVisible(false); // Initially hidden
        
        statusPanel.add(statusLabelText);
        statusPanel.add(connectionStatusLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(reconnectButton);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statusPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Initializes the controller and sets up the list view
     */
    private void initializeController() {
        try {
            // Initialize controller with this view as parent
            controller = new CustomerController(this);
            
            // Add the list view to this panel
            add(controller.getListView(), BorderLayout.CENTER);
            
            // Update connection status
            updateConnectionStatus();
            
            LogUtil.info("CustomerView initialized successfully");
            
        } catch (Exception e) {
            LogUtil.error("Failed to initialize CustomerView", e);
            showInitializationError(e);
        }
    }
    
    /**
     * Shows initialization error
     */
    private void showInitializationError(Exception e) {
        // Remove any existing components
        if (getComponentCount() > 1) {
            remove(1); // Remove center component if exists
        }
        
        // Create error panel
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel errorLabel = new JLabel("Failed to Initialize Customer Module");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        errorLabel.setForeground(UIFactory.ERROR_COLOR);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel messageLabel = new JLabel("<html><center>Error: " + e.getMessage() + 
                                        "<br><br>Please ensure the server is running and try reconnecting.</center></html>");
        messageLabel.setFont(UIFactory.BODY_FONT);
        messageLabel.setForeground(UIFactory.MEDIUM_GRAY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton retryButton = UIFactory.createPrimaryButton("Retry Initialization");
        retryButton.addActionListener(e2 -> retryInitialization());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(retryButton);
        
        errorPanel.add(errorLabel, BorderLayout.NORTH);
        errorPanel.add(messageLabel, BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(errorPanel, BorderLayout.CENTER);
        
        // Update connection status
        connectionStatusLabel.setText("Failed");
        connectionStatusLabel.setForeground(UIFactory.ERROR_COLOR);
        reconnectButton.setVisible(true);
        
        revalidate();
        repaint();
    }
    
    /**
     * Retries initialization
     */
    private void retryInitialization() {
        // Remove error panel
        if (getComponentCount() > 1) {
            remove(1);
        }
        
        // Reset status
        connectionStatusLabel.setText("Connecting...");
        connectionStatusLabel.setForeground(UIFactory.WARNING_COLOR);
        reconnectButton.setVisible(false);
        
        // Retry controller initialization
        initializeController();
    }
    
    /**
     * Reconnects to the server
     */
    private void reconnectToServer() {
        if (controller != null) {
            connectionStatusLabel.setText("Reconnecting...");
            connectionStatusLabel.setForeground(UIFactory.WARNING_COLOR);
            reconnectButton.setEnabled(false);
            
            // Use SwingWorker to reconnect in background
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    controller.reconnect();
                    return controller.isConnected();
                }
                
                @Override
                protected void done() {
                    try {
                        boolean connected = get();
                        updateConnectionStatus();
                        
                        if (connected) {
                            // Refresh the customer list
                            controller.refreshCustomerList();
                            LogUtil.info("Successfully reconnected to server");
                        } else {
                            JOptionPane.showMessageDialog(CustomerView.this,
                                "Failed to reconnect to server.\nPlease check if the server is running.",
                                "Reconnection Failed",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        LogUtil.error("Error during reconnection", e);
                        updateConnectionStatus();
                    } finally {
                        reconnectButton.setEnabled(true);
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Updates the connection status display
     */
    private void updateConnectionStatus() {
        if (controller != null && controller.isConnected()) {
            connectionStatusLabel.setText("Connected");
            connectionStatusLabel.setForeground(UIFactory.SUCCESS_COLOR);
            reconnectButton.setVisible(false);
        } else {
            connectionStatusLabel.setText("Disconnected");
            connectionStatusLabel.setForeground(UIFactory.ERROR_COLOR);
            reconnectButton.setVisible(true);
        }
    }
    
    /**
     * Gets the customer controller
     * 
     * @return The CustomerController instance
     */
    public CustomerController getController() {
        return controller;
    }
    
    /**
     * Refreshes the customer data
     */
    public void refreshData() {
        if (controller != null) {
            if (controller.isConnected()) {
                controller.refreshCustomerList();
            } else {
                reconnectToServer();
            }
        }
    }
    
    /**
     * Checks if the view is connected to the server
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return controller != null && controller.isConnected();
    }
    
    /**
     * Static method to create and show the customer view in a frame
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
            e.printStackTrace();
        }
        
        // Create the frame
        JFrame frame = new JFrame("Customer Management - RMI Version");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        
        // Create and add the customer view
        CustomerView customerView = new CustomerView();
        frame.add(customerView);
        
        // Add window listener to handle cleanup
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                LogUtil.info("Customer Management application closing");
                System.exit(0);
            }
        });
        
        // Show the frame
        frame.setVisible(true);
        
        LogUtil.info("Customer Management frame displayed");
    }
    
    /**
     * Main method for testing the customer view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Initialize logging
        LogUtil.info("Starting Customer Management Application");
        
        SwingUtilities.invokeLater(() -> {
            try {
                showInFrame();
            } catch (Exception e) {
                LogUtil.error("Failed to start Customer Management Application", e);
                JOptionPane.showMessageDialog(null,
                    "Failed to start application: " + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    /**
     * Forces a refresh of the connection status
     */
    public void forceUpdateConnectionStatus() {
        updateConnectionStatus();
    }
    
    /**
     * Sets up periodic connection monitoring
     */
    public void startConnectionMonitoring() {
        // Create a timer to periodically check connection status
        Timer connectionTimer = new Timer(30000, e -> { // Check every 30 seconds
            if (controller != null) {
                boolean wasConnected = connectionStatusLabel.getForeground() == UIFactory.SUCCESS_COLOR;
                boolean isNowConnected = controller.isConnected();
                
                if (wasConnected != isNowConnected) {
                    updateConnectionStatus();
                    
                    if (!isNowConnected) {
                        LogUtil.warn("Lost connection to server");
                        // Optionally show notification to user
                        JOptionPane.showMessageDialog(this,
                            "Connection to server has been lost.\nPlease check your network connection.",
                            "Connection Lost",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        LogUtil.info("Connection to server restored");
                        // Refresh data when connection is restored
                        controller.refreshCustomerList();
                    }
                }
            }
        });
        
        connectionTimer.start();
        LogUtil.info("Connection monitoring started");
    }
    
    /**
     * Gets connection status for external monitoring
     * 
     * @return Connection status string
     */
    public String getConnectionStatus() {
        if (controller != null && controller.isConnected()) {
            return "Connected";
        } else {
            return "Disconnected";
        }
    }
}