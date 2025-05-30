package controller;

import model.User;
import service.UserService;
import ui.MainView;
import ui.auth.LoginView;
import ui.auth.Session;
import util.LogUtil;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI-based Controller for authentication operations.
 * Manages the login process and user session using remote services.
 */
public class AuthController {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private UserService userService;
    
    // Views
    private LoginView loginView;
    private Component parentComponent;
    
    // Session
    private Session session;
    
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
     * Initializes the RMI connection to the server
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to RMI server at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            userService = (UserService) registry.lookup("userService");
            LogUtil.info("Successfully connected to UserService");
        } catch (Exception e) {
            LogUtil.error("Failed to connect to RMI server", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            parentComponent,
            "Failed to connect to the server.\nPlease ensure the server is running and try again.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
        System.exit(1);
    }
    
    /**
     * Shows the login view
     */
    public void showLoginView() {
        loginView = new LoginView(new LoginView.AuthenticationCallback() {
            @Override
            public void onLoginSuccess(User user) {
                // Set the current user in the session
                session.setCurrentUser(user);
                
                // Close the login view
                loginView.dispose();
                
                // Show the main view
                SwingUtilities.invokeLater(() -> {
                    MainView mainView = new MainView();
                    mainView.setVisible(true);
                });
            }
            
            @Override
            public void onLoginFailure(String reason) {
                // Already handled in LoginView
            }
            
            @Override
            public void onCancel() {
                // Exit the application
                System.exit(0);
            }
            
            @Override
            public void onForgotPassword() {
                showForgotPasswordDialog();
            }
        });
        
        // Show the login view
        loginView.setVisible(true);
    }
    
    /**
     * Authenticates a user using RMI service
     * 
     * @param username The username
     * @param password The password
     * @return The authenticated user if successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        try {
            if (userService == null) {
                LogUtil.error("UserService is not available");
                return null;
            }
            
            User user = userService.authenticateUser(username, password);
            if (user != null) {
                LogUtil.info("User authenticated successfully: " + username);
            } else {
                LogUtil.debug("Authentication failed for user: " + username);
            }
            return user;
        } catch (Exception e) {
            LogUtil.error("Error during authentication for user: " + username, e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Authentication error: " + e.getMessage(),
                "Authentication Error",
                JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }
    
    /**
     * Shows the forgot password dialog
     */
    private void showForgotPasswordDialog() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel messageLabel = new JLabel("Enter your username to reset your password:");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JTextField usernameField = new JTextField(20);
        
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.setOpaque(false);
        fieldPanel.add(new JLabel("Username:"));
        fieldPanel.add(usernameField);
        
        panel.add(messageLabel, BorderLayout.NORTH);
        panel.add(fieldPanel, BorderLayout.CENTER);
        
        // Show the dialog
        int result = JOptionPane.showConfirmDialog(
            loginView,
            panel,
            "Forgot Password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                try {
                    // Check if user exists
                    User user = userService.findUserByUsername(username);
                    if (user != null) {
                        // In a real application, this would send a password reset email
                        JOptionPane.showMessageDialog(
                            loginView,
                            "A password reset link has been sent to the email associated with this account.",
                            "Password Reset",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            loginView,
                            "Username not found.",
                            "User Not Found",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    LogUtil.error("Error during password reset for user: " + username, e);
                    JOptionPane.showMessageDialog(
                        loginView,
                        "Error processing password reset: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        // Clear the session
        session.logout();
        
        // Show the login view
        showLoginView();
    }
    
    /**
     * Shows the change password dialog
     */
    public void showChangePasswordDialog() {
        if (!session.isLoggedIn()) {
            return;
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
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
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "All fields are required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "New password must be at least 6 characters long.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "New password and confirmation do not match.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            try {
                // Verify current password
                User currentUser = session.getCurrentUser();
                User authenticatedUser = userService.authenticateUser(currentUser.getUsername(), currentPassword);
                
                if (authenticatedUser == null) {
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Current password is incorrect.",
                        "Authentication Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                // Update password
                int rowsAffected = userService.updatePassword(currentUser.getId(), newPassword);
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Password updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Failed to update password. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                
            } catch (Exception e) {
                LogUtil.error("Error updating password for user: " + session.getCurrentUser().getUsername(), e);
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Error updating password: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
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
        return userService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
    }
}