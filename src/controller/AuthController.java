package controller;

import model.User;
import service.UserService;
import ui.MainView;
import ui.auth.LoginView;
import ui.auth.Session;
import util.LogUtil;
import util.RMIConnectionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Authentication Controller compatible with existing code structure
 * Manages the login process using both traditional and OTP-based authentication
 */
public class AuthController {
    
    // Views
    private LoginView loginView;
    private Component parentComponent;
    
    // Session
    private Session session;
    
    // Services
    private UserService userService;
    
    /**
     * Constructor
     * 
     * @param parentComponent Parent component for dialogs
     */
    public AuthController(Component parentComponent) {
        this.parentComponent = parentComponent;
        this.session = Session.getInstance();
        
        // Initialize RMI connection
        initializeRMIConnection();
    }
    
    /**
     * Initializes the RMI connection
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Initializing RMI connection for authentication...");
            
            if (!RMIConnectionManager.initializeConnection()) {
                showConnectionError();
                return;
            }
            
            // Validate that services are available
            if (!RMIConnectionManager.validateServices()) {
                LogUtil.warn("Not all required services are available");
            }
            
            // Get the UserService
            userService = RMIConnectionManager.getUserService();
            if (userService != null) {
                LogUtil.info("Successfully connected to UserService");
            } else {
                LogUtil.error("Failed to get UserService from RMI registry");
                showConnectionError();
            }
            
        } catch (Exception e) {
            LogUtil.error("Failed to initialize RMI connection", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError() {
        SwingUtilities.invokeLater(() -> {
            String message = "Unable to connect to the Business Management Server.\n\n" +
                           "Please ensure that:\n" +
                           "• The server is running\n" +
                           "• The server is accessible at " + RMIConnectionManager.getHost() + 
                           ":" + RMIConnectionManager.getPort() + "\n" +
                           "• No firewall is blocking the connection\n\n" +
                           "Contact your system administrator if the problem persists.";
            
            int result = JOptionPane.showConfirmDialog(
                parentComponent,
                message,
                "Server Connection Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                initializeRMIConnection();
            } else {
                System.exit(1);
            }
        });
    }
    
    /**
     * Shows the enhanced login view with OTP support
     */
    public void showLoginView() {
        loginView = new LoginView(new LoginView.AuthenticationCallback() {
            @Override
            public void onLoginSuccess(User user) {
                handleLoginSuccess(user);
            }
            
            @Override
            public void onLoginFailure(String reason) {
                handleLoginFailure(reason);
            }
            
            @Override
            public void onCancel() {
                handleCancel();
            }
            
            @Override
            public void onForgotPassword() {
                handleForgotPassword();
            }
        });
        
        // Show the login view
        loginView.setVisible(true);
    }
    
    /**
     * Handles successful login
     * 
     * @param user The authenticated user
     */
    private void handleLoginSuccess(User user) {
        try {
            LogUtil.info("Login successful for user: " + user.getUsername() + 
                        " (" + user.getFullName() + ")");
            
            // Log the authentication for audit trail
            LogUtil.logAuthentication(user.getUsername(), true);
            
            // Set the current user in the session
            session.setCurrentUser(user);
            
            // Close the login view
            if (loginView != null) {
                loginView.dispose();
            }
            
            // Show the main application view
            SwingUtilities.invokeLater(() -> {
                try {
                    MainView mainView = new MainView();
                    mainView.setVisible(true);
                    LogUtil.info("Main application view displayed");
                } catch (Exception e) {
                    LogUtil.error("Failed to show main application view", e);
                    showErrorMessage("Failed to load main application", e.getMessage());
                }
            });
            
        } catch (Exception e) {
            LogUtil.error("Error handling login success", e);
            showErrorMessage("Login Error", "An error occurred after successful authentication: " + e.getMessage());
        }
    }
    
    /**
     * Handles login failure
     * 
     * @param reason The reason for failure
     */
    private void handleLoginFailure(String reason) {
        LogUtil.warn("Login failed: " + reason);
        // The login view will handle displaying the error message
        // No additional action needed here
    }
    
    /**
     * Handles cancel/exit action
     */
    private void handleCancel() {
        LogUtil.info("User cancelled login");
        
        int result = JOptionPane.showConfirmDialog(
            loginView,
            "Are you sure you want to exit the application?",
            "Exit Application",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            LogUtil.info("Application exit confirmed");
            System.exit(0);
        }
    }
    
    /**
     * Handles forgot password action
     */
    private void handleForgotPassword() {
        LogUtil.info("Forgot password action triggered");
        
        // Create forgot password dialog
        showForgotPasswordDialog();
    }
    
    /**
     * Shows the forgot password dialog
     */
    private void showForgotPasswordDialog() {
        String email = JOptionPane.showInputDialog(
            loginView,
            "Enter your email address to reset your password:",
            "Password Reset",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (email != null && !email.trim().isEmpty()) {
            try {
                email = email.trim().toLowerCase();
                
                if (!isValidEmail(email)) {
                    showErrorMessage("Invalid Email", "Please enter a valid email address.");
                    return;
                }
                
                if (userService != null) {
                    boolean success = userService.initiatePasswordReset(email);
                    
                    if (success) {
                        showInfoMessage("Password Reset", 
                            "Password reset instructions have been sent to: " + email + "\n\n" +
                            "Please check your email and follow the instructions to reset your password.");
                        LogUtil.info("Password reset initiated for email: " + email);
                    } else {
                        showErrorMessage("Reset Failed", 
                            "Failed to send password reset instructions. Please verify your email address and try again.");
                    }
                } else {
                    showErrorMessage("Service Error", "Service unavailable. Please try again later.");
                }
            } catch (Exception e) {
                LogUtil.error("Error processing password reset request", e);
                showErrorMessage("Error", "Error processing password reset request: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shows the change password dialog for logged-in users
     */
    public void showChangePasswordDialog() {
        if (!session.isLoggedIn()) {
            showErrorMessage("Authentication Required", "Please log in to change your password.");
            return;
        }
        
        // Create change password dialog
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel currentLabel = new JLabel("Current Password:");
        JPasswordField currentField = new JPasswordField(20);
        
        JLabel newLabel = new JLabel("New Password:");
        JPasswordField newField = new JPasswordField(20);
        
        JLabel confirmLabel = new JLabel("Confirm New Password:");
        JPasswordField confirmField = new JPasswordField(20);
        
        panel.add(currentLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(currentField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        panel.add(newLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(newField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        panel.add(confirmLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(confirmField);
        
        // Show the dialog
        int result = JOptionPane.showConfirmDialog(
            parentComponent,
            panel,
            "Change Password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String currentPassword = new String(currentField.getPassword());
            String newPassword = new String(newField.getPassword());
            String confirmPassword = new String(confirmField.getPassword());
            
            // Validate input
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showErrorMessage("Validation Error", "All fields are required.");
                return;
            }
            
            if (newPassword.length() < 6) {
                showErrorMessage("Validation Error", "New password must be at least 6 characters long.");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showErrorMessage("Validation Error", "New password and confirmation do not match.");
                return;
            }
            
            try {
                // Verify current password
                User currentUser = session.getCurrentUser();
                User authenticatedUser = userService.authenticateUser(currentUser.getUsername(), currentPassword);
                
                if (authenticatedUser == null) {
                    showErrorMessage("Authentication Error", "Current password is incorrect.");
                    return;
                }
                
                // Update password
                int rowsAffected = userService.updatePassword(currentUser.getId(), newPassword);
                
                if (rowsAffected > 0) {
                    showInfoMessage("Success", "Password updated successfully.");
                    LogUtil.info("Password updated for user: " + currentUser.getUsername());
                } else {
                    showErrorMessage("Error", "Failed to update password. Please try again.");
                }
                
            } catch (Exception e) {
                LogUtil.error("Error updating password for user: " + session.getCurrentUser().getUsername(), e);
                showErrorMessage("Error", "Error updating password: " + e.getMessage());
            }
            
            // Clear password fields for security
            currentField.setText("");
            newField.setText("");
            confirmField.setText("");
        }
    }
    
    /**
     * Logs out the current user and shows login view
     */
    public void logout() {
        try {
            LogUtil.info("User logout initiated");
            
            User currentUser = session.getCurrentUser();
            if (currentUser != null) {
                LogUtil.info("User " + currentUser.getUsername() + " logged out");
            }
            
            // Clear the session
            session.logout();
            
            // Close any open windows except login
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window.isDisplayable() && !(window instanceof LoginView)) {
                    window.dispose();
                }
            }
            
            // Show the login view again
            showLoginView();
            
            LogUtil.info("User logout completed");
        } catch (Exception e) {
            LogUtil.error("Error during logout", e);
        }
    }
    
    /**
     * Gets the current user session
     * 
     * @return The user session
     */
    public Session getSession() {
        return session;
    }
    
    /**
     * Gets the UserService instance
     * 
     * @return The UserService
     */
    public UserService getUserService() {
        return userService;
    }
    
    /**
     * Checks if the connection to the server is available
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return userService != null && RMIConnectionManager.isConnected();
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
    }
    
    /**
     * Validates email format
     * 
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Shows an error message dialog
     * 
     * @param title The dialog title
     * @param message The error message
     */
    private void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(
            parentComponent,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Shows an info message dialog
     * 
     * @param title The dialog title
     * @param message The info message
     */
    private void showInfoMessage(String title, String message) {
        JOptionPane.showMessageDialog(
            parentComponent,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}