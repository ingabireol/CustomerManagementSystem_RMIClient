//package controller;
//import controller.AuthController;
//import dao.UserDao;
//import ui.UIFactory;
//import util.LogUtil;
//
//import javax.swing.*;
//import java.awt.*;
//import java.sql.Connection;
//import java.sql.DriverManager;
//
///**
// * Main application class for the Business Management System.
// * Initializes the application and starts the login screen.
// */
//public class Application {
//    
//    // Database connection details - should match your UserDao settings
//    private static final String DB_URL = "jdbc:postgresql://localhost:5432/business_db";
//    private static final String DB_USERNAME = "postgres";
//    private static final String DB_PASSWD = "078868";
//    
//    /**
//     * Application entry point
//     * 
//     * @param args Command-line arguments
//     */
//    public static void main(String[] args) {
//        try {
//            // Initialize logging
//            LogUtil.initialize();
//            LogUtil.info("Application starting up...");
//            
//            // Set application properties
//            System.setProperty("awt.useSystemAAFontSettings", "on");
//            System.setProperty("swing.aatext", "true");
//            
//            // Test database connection
//            testDatabaseConnection();
//            
//            // Set up the UI look and feel
//            setupLookAndFeel();
//            
//            // Show the login screen
//            LogUtil.info("Launching login view...");
//            SwingUtilities.invokeLater(() -> {
//                LogUtil.info("Inside SwingUtilities.invokeLater...");
//                try {
//                    AuthController authController = new AuthController(null);
//                    authController.showLoginView();
//                    LogUtil.info("Login view should now be visible");
//                } catch (Exception ex) {
//                    LogUtil.error("Failed to show login view: " + ex.getMessage(), ex);
//                    showErrorAndExit("Failed to show login view: " + ex.getMessage());
//                }
//            });
//            
//            // Keep the main thread alive
//            LogUtil.info("Main thread continuing...");
//            
//        } catch (Exception ex) {
//            // Log error and show message
//            LogUtil.error("Failed to initialize application: " + ex.getMessage(), ex);
//            showErrorAndExit("Error initializing application: " + ex.getMessage());
//        }
//    }
//    
//    /**
//     * Tests the database connection
//     */
//    private static void testDatabaseConnection() {
//        LogUtil.info("Testing database connection...");
//        try {
//            // Load the JDBC driver
//            Class.forName("org.postgresql.Driver");
//            LogUtil.info("PostgreSQL JDBC driver loaded");
//            
//            // Try to connect
//            Connection con = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWD);
//            LogUtil.info("Database connection successful");
//            con.close();
//            
//            // Initialize tables
//            UserDao userDao = new UserDao();
//            boolean tablesCreated = userDao.createUsersTable();
//            LogUtil.info("Users table created or exists: " + tablesCreated);
//            
//            boolean adminCreated = userDao.createDefaultAdmin();
//            LogUtil.info("Default admin user created or exists: " + adminCreated);
//            
//        } catch (Exception ex) {
//            LogUtil.error("Database connection failed: " + ex.getMessage(), ex);
//            showErrorAndExit("Database connection failed: " + ex.getMessage() + 
//                            "\n\nPlease check your database settings. The application requires PostgreSQL.");
//        }
//    }
//    
//    /**
//     * Sets up the UI look and feel
//     */
//    private static void setupLookAndFeel() {
//        try {
//            LogUtil.info("Setting up Look and Feel...");
//            // Set the look and feel to the system look and feel
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            
//            // Set global font settings
//            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
//            Font boldFont = new Font("Segoe UI", Font.BOLD, 12);
//            Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
//            
//            UIManager.put("Label.font", defaultFont);
//            UIManager.put("Button.font", defaultFont);
//            UIManager.put("TextField.font", defaultFont);
//            UIManager.put("TextArea.font", defaultFont);
//            UIManager.put("ComboBox.font", defaultFont);
//            UIManager.put("CheckBox.font", defaultFont);
//            UIManager.put("RadioButton.font", defaultFont);
//            UIManager.put("Table.font", defaultFont);
//            UIManager.put("TableHeader.font", boldFont);
//            UIManager.put("TabbedPane.font", defaultFont);
//            UIManager.put("MenuBar.font", defaultFont);
//            UIManager.put("Menu.font", defaultFont);
//            UIManager.put("MenuItem.font", defaultFont);
//            UIManager.put("OptionPane.messageFont", defaultFont);
//            UIManager.put("OptionPane.buttonFont", defaultFont);
//            
//            LogUtil.info("UI Look and Feel initialized");
//        } catch (Exception ex) {
//            LogUtil.warning("Failed to set look and feel: " + ex.getMessage(), ex);
//            // Continue with default look and feel
//        }
//    }
//    
//    /**
//     * Shows an error message and exits the application
//     * 
//     * @param message The error message to display
//     */
//    private static void showErrorAndExit(String message) {
//        try {
//            JOptionPane.showMessageDialog(
//                null,
//                message,
//                "Application Error",
//                JOptionPane.ERROR_MESSAGE
//            );
//        } catch (Exception e) {
//            // If even showing the dialog fails, print to console
//            System.err.println("FATAL ERROR: " + message);
//            e.printStackTrace();
//        }
//        
//        // Exit with error code
//        System.exit(1);
//    }
//}