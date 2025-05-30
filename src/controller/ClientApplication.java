package controller;

import controller.AuthController;
import util.LogUtil;
import util.RMIConnectionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Main client application class for the Business Management System.
 * Initializes the client application and starts the login screen.
 */
public class ClientApplication {
    
    /**
     * Application entry point
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Initialize logging
            LogUtil.info("Client Application starting up...");
            
            // Set application properties for better UI rendering
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // Set up the UI look and feel
            setupLookAndFeel();
            
            // Test RMI connection to server
            if (!testServerConnection()) {
                showServerConnectionError();
                return;
            }
            
            // Show the login screen
            LogUtil.info("Launching login view...");
            SwingUtilities.invokeLater(() -> {
                try {
                    AuthController authController = new AuthController(null);
                    authController.showLoginView();
                    LogUtil.info("Login view displayed successfully");
                } catch (Exception ex) {
                    LogUtil.error("Failed to show login view", ex);
                    showErrorAndExit("Failed to show login view: " + ex.getMessage());
                }
            });
            
        } catch (Exception ex) {
            LogUtil.error("Failed to initialize client application", ex);
            showErrorAndExit("Error initializing application: " + ex.getMessage());
        }
    }
    
    /**
     * Tests the connection to the RMI server
     * 
     * @return true if connection successful, false otherwise
     */
    private static boolean testServerConnection() {
        try {
            LogUtil.info("Testing connection to RMI server...");
            return RMIConnectionManager.testConnection();
        } catch (Exception e) {
            LogUtil.error("Server connection test failed", e);
            return false;
        }
    }
    
    /**
     * Shows server connection error dialog
     */
    private static void showServerConnectionError() {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("Server Connection Error");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(new Color(0xD32F2F));
            
            JTextArea messageArea = new JTextArea(
                "Unable to connect to the Business Management Server.\n\n" +
                "Please ensure that:\n" +
                "• The server is running\n" +
                "• The server is accessible at 127.0.0.1:4444\n" +
                "• No firewall is blocking the connection\n\n" +
                "Contact your system administrator if the problem persists."
            );
            messageArea.setEditable(false);
            messageArea.setOpaque(false);
            messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            
            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(messageArea, BorderLayout.CENTER);
            
            JOptionPane.showMessageDialog(
                null,
                panel,
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
            
            System.exit(1);
        });
    }
    
    /**
     * Sets up the UI look and feel
     */
    private static void setupLookAndFeel() {
        try {
            LogUtil.info("Setting up Look and Feel...");
            // Set the look and feel to the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Set global font settings
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
            Font boldFont = new Font("Segoe UI", Font.BOLD, 12);
            Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
            
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", defaultFont);
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("TextArea.font", defaultFont);
            UIManager.put("ComboBox.font", defaultFont);
            UIManager.put("CheckBox.font", defaultFont);
            UIManager.put("RadioButton.font", defaultFont);
            UIManager.put("Table.font", defaultFont);
            UIManager.put("TableHeader.font", boldFont);
            UIManager.put("TabbedPane.font", defaultFont);
            UIManager.put("MenuBar.font", defaultFont);
            UIManager.put("Menu.font", defaultFont);
            UIManager.put("MenuItem.font", defaultFont);
            UIManager.put("OptionPane.messageFont", defaultFont);
            UIManager.put("OptionPane.buttonFont", defaultFont);
            
            // Set colors for better visual appearance
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("OptionPane.background", Color.WHITE);
            
            LogUtil.info("UI Look and Feel initialized successfully");
        } catch (Exception ex) {
            LogUtil.error("Failed to set look and feel", ex);
            // Continue with default look and feel
        }
    }
    
    /**
     * Shows an error message and exits the application
     * 
     * @param message The error message to display
     */
    private static void showErrorAndExit(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Application Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (Exception e) {
                // If even showing the dialog fails, print to console
                System.err.println("FATAL ERROR: " + message);
                e.printStackTrace();
            }
            
            // Exit with error code
            System.exit(1);
        });
    }
}