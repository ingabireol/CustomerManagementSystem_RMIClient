package ui.supplier;

import controller.SupplierController;
import util.LogUtil;
import util.RMIConnectionManager;

import javax.swing.*;
import java.awt.*;
import ui.UIFactory;

/**
 * RMI-based main view for the Supplier module.
 * Serves as a container for the supplier management interface using remote services.
 */
public class SupplierView extends JPanel {
    private SupplierController controller;
    
    /**
     * Constructor
     */
    public SupplierView() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Initialize RMI connection first
        initializeRMIConnection();
        
        // Initialize controller
        controller = new SupplierController(this);
        
        // Add the list view to this panel
        add(controller.getListView(), BorderLayout.CENTER);
        
        LogUtil.info("SupplierView initialized with RMI support");
    }
    
    /**
     * Initializes RMI connection
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Initializing RMI connection for SupplierView");
            
            // Test the connection
            if (!RMIConnectionManager.testConnection()) {
                LogUtil.warn("RMI connection test failed, attempting to initialize");
                if (!RMIConnectionManager.initializeConnection()) {
                    showConnectionWarning();
                }
            }
            
            // Validate that supplier service is available
            if (RMIConnectionManager.getSupplierService() == null) {
                LogUtil.error("SupplierService not available from RMI registry");
                showServiceUnavailableWarning();
            }
            
        } catch (Exception e) {
            LogUtil.error("Error initializing RMI connection for SupplierView", e);
            showConnectionError(e);
        }
    }
    
    /**
     * Shows connection warning dialog
     */
    private void showConnectionWarning() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                "Warning: Could not establish connection to the server.\n" +
                "Some features may not work properly.\n" +
                "Please check your network connection and server status.",
                "Connection Warning",
                JOptionPane.WARNING_MESSAGE
            );
        });
    }
    
    /**
     * Shows service unavailable warning
     */
    private void showServiceUnavailableWarning() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                "Warning: Supplier service is not available on the server.\n" +
                "Please contact your system administrator.",
                "Service Unavailable",
                JOptionPane.WARNING_MESSAGE
            );
        });
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                "Error connecting to server: " + e.getMessage() + "\n" +
                "Please contact your system administrator.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * Gets the supplier controller
     * 
     * @return The SupplierController instance
     */
    public SupplierController getController() {
        return controller;
    }
    
    /**
     * Refreshes the supplier view data
     */
    public void refreshData() {
        if (controller != null) {
            controller.refreshSupplierList();
        }
    }
    
    /**
     * Checks if the RMI connection is active
     * 
     * @return true if connected to server
     */
    public boolean isConnected() {
        return RMIConnectionManager.isConnected() && 
               RMIConnectionManager.getSupplierService() != null;
    }
    
    /**
     * Attempts to reconnect to the RMI server
     */
    public void reconnect() {
        LogUtil.info("Attempting to reconnect SupplierView to server");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return RMIConnectionManager.reconnect();
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        LogUtil.info("SupplierView reconnected successfully");
                        
                        // Refresh the controller's connection
                        if (controller != null) {
                            controller.reconnect();
                        }
                        
                        JOptionPane.showMessageDialog(
                            SupplierView.this,
                            "Successfully reconnected to server!",
                            "Connection Restored",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        LogUtil.error("Failed to reconnect SupplierView");
                        JOptionPane.showMessageDialog(
                            SupplierView.this,
                            "Failed to reconnect to server.\nPlease try again later.",
                            "Reconnection Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    LogUtil.error("Error during SupplierView reconnection", ex);
                    JOptionPane.showMessageDialog(
                        SupplierView.this,
                        "Error during reconnection: " + ex.getMessage(),
                        "Reconnection Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Static method to create and show the supplier view in a frame
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
        JFrame frame = new JFrame("Supplier Management - RMI Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        
        // Add connection status to title
        SwingUtilities.invokeLater(() -> {
            boolean connected = RMIConnectionManager.testConnection();
            String status = connected ? "Connected" : "Disconnected";
            frame.setTitle("Supplier Management - RMI Client (" + status + ")");
        });
        
        // Create and add the supplier view
        SupplierView supplierView = new SupplierView();
        frame.add(supplierView);
        
        // Add menu bar for testing
        JMenuBar menuBar = new JMenuBar();
        
        JMenu connectionMenu = new JMenu("Connection");
        
        JMenuItem testConnectionItem = new JMenuItem("Test Connection");
        testConnectionItem.addActionListener(e -> {
            boolean connected = RMIConnectionManager.testConnection();
            String message = connected ? 
                "Successfully connected to server at " + RMIConnectionManager.getConnectionInfo() :
                "Failed to connect to server";
            int messageType = connected ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
            
            JOptionPane.showMessageDialog(frame, message, "Connection Test", messageType);
        });
        connectionMenu.add(testConnectionItem);
        
        JMenuItem reconnectItem = new JMenuItem("Reconnect");
        reconnectItem.addActionListener(e -> supplierView.reconnect());
        connectionMenu.add(reconnectItem);
        
        connectionMenu.addSeparator();
        
        JMenuItem refreshItem = new JMenuItem("Refresh Data");
        refreshItem.addActionListener(e -> supplierView.refreshData());
        connectionMenu.add(refreshItem);
        
        menuBar.add(connectionMenu);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            String aboutText = "Supplier Management System\n" +
                             "RMI-based Client Application\n" +
                             "Version 1.0\n\n" +
                             "Server: " + RMIConnectionManager.getHost() + ":" + RMIConnectionManager.getPort() + "\n" +
                             "Status: " + (RMIConnectionManager.isConnected() ? "Connected" : "Disconnected");
            
            JOptionPane.showMessageDialog(frame, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);
        
        // Show the frame
        frame.setVisible(true);
        
        LogUtil.info("SupplierView test frame displayed");
    }
    
    /**
     * Main method for testing the supplier view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Initialize logging
        LogUtil.info("Starting SupplierView test application");
        
        SwingUtilities.invokeLater(() -> {
            showInFrame();
        });
    }
}