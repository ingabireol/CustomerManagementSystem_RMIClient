package controller;

import controller.AuthController;
import util.LogUtil;
import util.RMIConnectionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Main client application class for the Business Management System.
 * Compatible with existing code structure while adding OTP support.
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
            LogUtil.initialize();
            LogUtil.logStartup();
            LogUtil.info("Business Management Client with OTP support starting up...");
            
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
            
            // Show the enhanced login screen with OTP support
            LogUtil.info("Launching enhanced login view with OTP support...");
            SwingUtilities.invokeLater(() -> {
                try {
                    AuthController authController = new AuthController(null);
                    authController.showLoginView();
                    LogUtil.info("Enhanced login view displayed successfully");
                } catch (Exception ex) {
                    LogUtil.error("Failed to show enhanced login view", ex);
                    showErrorAndExit("Failed to show login view: " + ex.getMessage());
                }
            });
            
        } catch (Exception ex) {
            LogUtil.error("Failed to initialize enhanced client application", ex);
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
            boolean connected = RMIConnectionManager.testConnection();
            
            if (connected) {
                LogUtil.logRMIConnection(RMIConnectionManager.getHost(), RMIConnectionManager.getPort(), true);
                
                // Validate that all required services are available
                if (RMIConnectionManager.validateServices()) {
                    LogUtil.info("✓ Server connection test passed - all services available");
                    return true;
                } else {
                    LogUtil.warn("⚠ Server connected but some services may be unavailable");
                    return true; // Still allow connection with warning
                }
            } else {
                LogUtil.logRMIConnection(RMIConnectionManager.getHost(), RMIConnectionManager.getPort(), false);
                LogUtil.error("✗ Server connection test failed");
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Server connection test failed", e);
            return false;
        }
    }
    
    /**
     * Shows server connection error dialog with retry option
     */
    private static void showServerConnectionError() {
        SwingUtilities.invokeLater(() -> {
            String message = "Unable to connect to the Business Management Server.\n\n" +
                           "Server Details:\n" +
                           "• Host: " + RMIConnectionManager.getHost() + "\n" +
                           "• Port: " + RMIConnectionManager.getPort() + "\n\n" +
                           "Please ensure that:\n" +
                           "• The server is running\n" +
                           "• The server is accessible from this machine\n" +
                           "• No firewall is blocking the connection\n" +
                           "• The server port is not in use by another application\n\n" +
                           "Contact your system administrator if the problem persists.";
            
            Object[] options = {"Retry Connection", "Exit Application"};
            int choice = JOptionPane.showOptionDialog(
                null,
                message,
                "Server Connection Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[0]
            );
            
            if (choice == 0) { // Retry
                // Attempt to reconnect
                if (testServerConnection()) {
                    // If successful, continue with application startup
                    SwingUtilities.invokeLater(() -> {
                        try {
                            AuthController authController = new AuthController(null);
                            authController.showLoginView();
                        } catch (Exception ex) {
                            showErrorAndExit("Failed to show login view: " + ex.getMessage());
                        }
                    });
                } else {
                    // Show error again
                    showServerConnectionError();
                }
            } else {
                // Exit application
                LogUtil.logShutdown();
                System.exit(1);
            }
        });
    }
    
    /**
     * Sets up the UI look and feel with fallback to system default
     */
    private static void setupLookAndFeel() {
        try {
            LogUtil.info("Setting up Look and Feel...");
            
            // Try to use a modern look and feel, fall back to system L&F
            try {
                // Attempt to use FlatLaf if available (optional enhancement)
                Class.forName("com.formdev.flatlaf.FlatLightLaf");
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
                LogUtil.info("Using FlatLaf Light theme");
            } catch (ClassNotFoundException e) {
                // FlatLaf not available, use system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                LogUtil.info("Using system Look and Feel");
            }
            
            // Set global font settings for better consistency
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
            Font boldFont = new Font("Segoe UI", Font.BOLD, 12);
            
            // Apply fonts to common components
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", defaultFont);
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("PasswordField.font", defaultFont);
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
            
            LogUtil.info("✓ UI Look and Feel initialized successfully");
            
        } catch (Exception ex) {
            LogUtil.error("Failed to set look and feel", ex);
            LogUtil.info("Continuing with default Look and Feel");
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
            
            // Log shutdown and exit with error code
            LogUtil.logShutdown();
            System.exit(1);
        });
    }
    
    /**
     * Shows application information dialog
     * 
     * @param parent Parent component for the dialog
     */
    public static void showAboutDialog(Component parent) {
        String aboutMessage = "Business Management System\n" +
                            "Enhanced Edition with OTP Support\n\n" +
                            "Version: 2.0.0\n" +
                            "Features:\n" +
                            "• Traditional & OTP Authentication\n" +
                            "• Customer Management\n" +
                            "• Product & Inventory Control\n" +
                            "• Order Processing\n" +
                            "• Invoice & Payment Tracking\n" +
                            "• RMI-based Architecture\n\n" +
                            "© 2024 Business Management Solutions";
        
        JOptionPane.showMessageDialog(
            parent,
            aboutMessage,
            "About Business Management System",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Checks system requirements and logs warnings if needed
     */
    private static void checkSystemRequirements() {
        try {
            // Check Java version
            String javaVersion = System.getProperty("java.version");
            LogUtil.info("Java version: " + javaVersion);
            
            String[] versionParts = javaVersion.split("\\.");
            int majorVersion = Integer.parseInt(versionParts[0]);
            
            if (majorVersion < 8) {
                LogUtil.warn("Java version " + javaVersion + " may not be fully supported. Java 8+ recommended.");
            }
            
            // Check available memory
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            
            LogUtil.info("Max memory available: " + (maxMemory / 1024 / 1024) + "MB");
            
            if (maxMemory < 256 * 1024 * 1024) { // Less than 256MB
                LogUtil.warn("Low memory available. Application may run slowly.");
            }
            
            // Log system information
            LogUtil.logSystemInfo();
            
        } catch (Exception e) {
            LogUtil.warn("Could not check system requirements", e);
        }
    }
    
    /**
     * Alternative main method with command line argument support
     * 
     * @param args Command line arguments
     */
    public static void startApplication(String[] args) {
        // Parse command line arguments
        boolean debugMode = false;
        boolean skipConnectionTest = false;
        
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "--debug":
                case "-d":
                    debugMode = true;
                    LogUtil.setLogLevel(LogUtil.LogLevel.DEBUG);
                    break;
                case "--skip-connection-test":
                case "-s":
                    skipConnectionTest = true;
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    return;
                case "--version":
                case "-v":
                    printVersion();
                    return;
            }
        }
        
        // Set debug mode if requested
        if (debugMode) {
            LogUtil.info("Debug mode enabled");
            System.setProperty("java.rmi.server.logCalls", "true");
        }
        
        // Check system requirements
        checkSystemRequirements();
        
        // Continue with normal startup
        if (skipConnectionTest) {
            LogUtil.info("Skipping connection test as requested");
            SwingUtilities.invokeLater(() -> {
                try {
                    setupLookAndFeel();
                    AuthController authController = new AuthController(null);
                    authController.showLoginView();
                } catch (Exception ex) {
                    showErrorAndExit("Failed to show login view: " + ex.getMessage());
                }
            });
        } else {
            main(new String[0]); // Call normal main method
        }
    }
    
    /**
     * Prints usage information
     */
    private static void printUsage() {
        System.out.println("Business Management System - Enhanced Client");
        System.out.println("\nUsage: java ClientApplication [options]");
        System.out.println("\nOptions:");
        System.out.println("  --help, -h                 Show this help message");
        System.out.println("  --version, -v              Show version information");
        System.out.println("  --debug, -d                Enable debug mode");
        System.out.println("  --skip-connection-test, -s Skip server connection test");
        System.out.println("\nWith no options, starts the application normally.");
    }
    
    /**
     * Prints version information
     */
    private static void printVersion() {
        System.out.println("Business Management System - Enhanced Client");
        System.out.println("Version: 2.0.0 - Enhanced Edition");
        System.out.println("Features: Traditional & OTP Authentication, RMI Architecture");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
    }
}