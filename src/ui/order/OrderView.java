package ui.order;

import controller.OrderController;
import util.LogUtil;
import javax.swing.*;
import java.awt.*;
import ui.UIFactory;

/**
 * RMI-based Main view for the Order module.
 * Serves as a container for the order management interface using RMI services.
 */
public class OrderView extends JPanel {
    private OrderController controller;
    
    /**
     * Constructor
     */
    public OrderView() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Initialize controller with RMI support
        try {
            controller = new OrderController(this);
            
            // Add the list view to this panel
            if (controller.isConnected()) {
                add(controller.getListView(), BorderLayout.CENTER);
                LogUtil.info("OrderView initialized successfully with RMI connection");
            } else {
                // Show connection error panel
                add(createConnectionErrorPanel(), BorderLayout.CENTER);
                LogUtil.warn("OrderView initialized but RMI connection failed");
            }
        } catch (Exception e) {
            LogUtil.error("Failed to initialize OrderView", e);
            add(createErrorPanel(e.getMessage()), BorderLayout.CENTER);
        }
    }
    
    /**
     * Creates an error panel to display when RMI connection fails
     */
    private JPanel createConnectionErrorPanel() {
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(UIFactory.BACKGROUND_COLOR);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Create error message panel
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIFactory.ERROR_COLOR, 2, true),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Error icon and title
        JLabel titleLabel = new JLabel("Connection Error");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIFactory.ERROR_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Error message
        JLabel messageLabel = new JLabel("<html><center>" +
            "Unable to connect to the Order Management Service.<br><br>" +
            "Please ensure that:<br>" +
            "• The server is running<br>" +
            "• The server is accessible at 127.0.0.1:4444<br>" +
            "• No firewall is blocking the connection<br><br>" +
            "Contact your system administrator if the problem persists." +
            "</center></html>");
        messageLabel.setFont(UIFactory.BODY_FONT);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Retry button
        JButton retryButton = UIFactory.createPrimaryButton("Retry Connection");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(e -> retryConnection());
        
        messagePanel.add(titleLabel);
        messagePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        messagePanel.add(messageLabel);
        messagePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        messagePanel.add(retryButton);
        
        errorPanel.add(messagePanel, BorderLayout.CENTER);
        
        return errorPanel;
    }
    
    /**
     * Creates a general error panel
     */
    private JPanel createErrorPanel(String errorMessage) {
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(UIFactory.BACKGROUND_COLOR);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIFactory.ERROR_COLOR, 2, true),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        JLabel titleLabel = new JLabel("Initialization Error");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIFactory.ERROR_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel messageLabel = new JLabel("<html><center>" +
            "An error occurred while initializing the Order module:<br><br>" +
            errorMessage +
            "</center></html>");
        messageLabel.setFont(UIFactory.BODY_FONT);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton retryButton = UIFactory.createPrimaryButton("Retry");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(e -> reinitialize());
        
        messagePanel.add(titleLabel);
        messagePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        messagePanel.add(messageLabel);
        messagePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        messagePanel.add(retryButton);
        
        errorPanel.add(messagePanel, BorderLayout.CENTER);
        
        return errorPanel;
    }
    
    /**
     * Retries the RMI connection
     */
    private void retryConnection() {
        try {
            // Show loading indicator
            removeAll();
            add(createLoadingPanel(), BorderLayout.CENTER);
            revalidate();
            repaint();
            
            // Attempt to reconnect in a background thread
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (controller != null) {
                        controller.reconnect();
                        return controller.isConnected();
                    }
                    return false;
                }
                
                @Override
                protected void done() {
                    try {
                        boolean connected = get();
                        removeAll();
                        
                        if (connected) {
                            add(controller.getListView(), BorderLayout.CENTER);
                            LogUtil.info("RMI connection restored successfully");
                        } else {
                            add(createConnectionErrorPanel(), BorderLayout.CENTER);
                            LogUtil.warn("RMI connection retry failed");
                        }
                        
                        revalidate();
                        repaint();
                        
                    } catch (Exception e) {
                        LogUtil.error("Error during connection retry", e);
                        removeAll();
                        add(createErrorPanel(e.getMessage()), BorderLayout.CENTER);
                        revalidate();
                        repaint();
                    }
                }
            };
            
            worker.execute();
            
        } catch (Exception e) {
            LogUtil.error("Error retrying connection", e);
            removeAll();
            add(createErrorPanel(e.getMessage()), BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }
    
    /**
     * Reinitializes the entire OrderView
     */
    private void reinitialize() {
        try {
            // Show loading indicator
            removeAll();
            add(createLoadingPanel(), BorderLayout.CENTER);
            revalidate();
            repaint();
            
            // Reinitialize in background thread
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Reinitialize controller
                    controller = new OrderController(OrderView.this);
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        removeAll();
                        
                        if (controller != null && controller.isConnected()) {
                            add(controller.getListView(), BorderLayout.CENTER);
                            LogUtil.info("OrderView reinitialized successfully");
                        } else {
                            add(createConnectionErrorPanel(), BorderLayout.CENTER);
                            LogUtil.warn("OrderView reinitialization failed - no connection");
                        }
                        
                        revalidate();
                        repaint();
                        
                    } catch (Exception e) {
                        LogUtil.error("Error during reinitialization", e);
                        removeAll();
                        add(createErrorPanel(e.getMessage()), BorderLayout.CENTER);
                        revalidate();
                        repaint();
                    }
                }
            };
            
            worker.execute();
            
        } catch (Exception e) {
            LogUtil.error("Error reinitializing OrderView", e);
            removeAll();
            add(createErrorPanel(e.getMessage()), BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }
    
    /**
     * Creates a loading panel
     */
    private JPanel createLoadingPanel() {
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(UIFactory.BACKGROUND_COLOR);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // Loading message
        JLabel loadingLabel = new JLabel("Connecting to Order Service...");
        loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setMaximumSize(new Dimension(200, 20));
        
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(loadingLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(progressBar);
        centerPanel.add(Box.createVerticalGlue());
        
        loadingPanel.add(centerPanel, BorderLayout.CENTER);
        
        return loadingPanel;
    }
    
    /**
     * Gets the controller instance
     * 
     * @return The OrderController
     */
    public OrderController getController() {
        return controller;
    }
    
    /**
     * Checks if the view is properly connected to RMI services
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return controller != null && controller.isConnected();
    }
    
    /**
     * Refreshes the order data
     */
    public void refreshData() {
        if (controller != null && controller.isConnected()) {
            controller.refreshOrderList();
        }
    }
    
    /**
     * Static method to create and show the order view in a frame
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
        JFrame frame = new JFrame("Order Management - RMI Version");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.setLocationRelativeTo(null);
        
        // Create and add the order view
        OrderView orderView = new OrderView();
        frame.add(orderView);
        
        // Add window listener for cleanup
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                LogUtil.info("Order Management application closing");
                System.exit(0);
            }
        });
        
        // Show the frame
        frame.setVisible(true);
        LogUtil.info("Order Management window displayed");
    }
    
    /**
     * Main method for testing the order view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Initialize logging
        LogUtil.info("Starting Order Management application");
        
        SwingUtilities.invokeLater(() -> {
            showInFrame();
        });
    }
}