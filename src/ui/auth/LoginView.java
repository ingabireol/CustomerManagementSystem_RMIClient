package ui.auth;

import model.User;
import service.UserService;
import ui.UIFactory;
import util.LogUtil;
import util.RMIConnectionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Enhanced Login View with OTP Support
 * Supports both traditional username/password login and OTP-based email login
 */
public class LoginView extends JFrame {
    
    // Login modes
    private enum LoginMode {
        TRADITIONAL, OTP
    }
    
    // Components for traditional login
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    // Components for OTP login
    private JTextField emailField;
    private JTextField otpField;
    
    // Common components
    private JButton loginButton;
    private JButton cancelButton;
    private JButton switchModeButton;
    private JButton resendOTPButton;
    private JLabel statusLabel;
    private JCheckBox rememberMeCheckbox;
    private JLabel forgotPasswordLabel;
    private JProgressBar progressBar;
    private JPanel loginFormPanel;
    
    // State management
    private LoginMode currentMode = LoginMode.TRADITIONAL;
    private boolean otpSent = false;
    private Timer countdownTimer;
    private int resendCountdown = 0;
    
    // Styling constants
    private static final Color FOCUS_INDICATOR_COLOR = UIFactory.PRIMARY_COLOR;
    private static final int FIELD_HEIGHT = 40;
    
    // RMI service
    private UserService userService;
    
    // Authentication callback
    private AuthenticationCallback callback;
    
    /**
     * Interface for authentication callback
     */
    public interface AuthenticationCallback {
        void onLoginSuccess(User user);
        void onLoginFailure(String reason);
        void onCancel();
        void onForgotPassword();
    }
    
    /**
     * Constructor
     */
    public LoginView() {
        this(null);
    }
    
    /**
     * Constructor with callback
     */
    public LoginView(AuthenticationCallback callback) {
        this.callback = callback;
        initializeRMIConnection();
        initializeUI();
        LogUtil.info("Enhanced LoginView with OTP support initialized");
    }
    
    /**
     * Initializes the RMI connection to the user service
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to UserService for authentication...");
            
            // Initialize RMI connection if not already done
            if (!RMIConnectionManager.isConnected()) {
                if (!RMIConnectionManager.initializeConnection()) {
                    LogUtil.error("Failed to initialize RMI connection");
                    showConnectionError();
                    return;
                }
            }
            
            userService = RMIConnectionManager.getUserService();
            
            if (userService != null) {
                LogUtil.info("Successfully connected to UserService");
            } else {
                LogUtil.error("Failed to get UserService from RMI registry");
                showConnectionError();
            }
        } catch (Exception e) {
            LogUtil.error("Failed to connect to UserService", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error and offers retry option
     */
    private void showConnectionError() {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Unable to connect to the authentication service.\n\n" +
                "Please ensure the server is running and try again.\n\n" +
                "Server: " + RMIConnectionManager.getHost() + ":" + RMIConnectionManager.getPort() + "\n\n" +
                "Would you like to retry the connection?",
                "Connection Error",
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
    
    private void initializeUI() {
        // Set up the frame
        setTitle("Business Management System - Login");
        setSize(450, 650);
        setMinimumSize(new Dimension(400, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set up the main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Top section gradient (blue to lighter blue)
                GradientPaint gp = new GradientPaint(
                    0, 0, UIFactory.PRIMARY_COLOR,
                    0, h/3, new Color(0x2196F3)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h/3);
                
                // Bottom section (white)
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, h/3, w, h*2/3);
                
                g2d.dispose();
            }
        };
        
        setContentPane(mainPanel);
        
        // Create components
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        formWrapper.setOpaque(false);
        JPanel formCardPanel = createFormCardPanel();
        formWrapper.add(formCardPanel);
        mainPanel.add(formWrapper, BorderLayout.CENTER);
        
        registerActions();
        updateUIForCurrentMode();
        SwingUtilities.invokeLater(() -> focusAppropriateField());
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(getWidth(), 120));
        
        // Main title
        JLabel logoLabel = new JLabel("BUSINESS MANAGEMENT SYSTEM");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setBorder(new EmptyBorder(20, 0, 5, 0));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Customer • Product • Order Management");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(logoLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormCardPanel() {
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            public boolean isOpaque() {
                return false;
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                int shadowSize = 5;
                int width = getWidth() - (shadowSize * 2);
                int height = getHeight() - (shadowSize * 2);
                
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(shadowSize, shadowSize, width, height, 15, 15);
                
                // Draw card background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, width, height, 15, 15);
                
                g2.dispose();
            }
        };
        
        cardPanel.setPreferredSize(new Dimension(350, 450));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        loginFormPanel = createLoginFormPanel();
        cardPanel.add(loginFormPanel, BorderLayout.CENTER);
        
        return cardPanel;
    }
    
    private JPanel createLoginFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Title and mode indicator
        JLabel titleLabel = new JLabel("Sign In");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        // Mode switch button
        switchModeButton = new JButton("Use Email & OTP");
        switchModeButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        switchModeButton.setForeground(UIFactory.PRIMARY_COLOR);
        switchModeButton.setBorderPainted(false);
        switchModeButton.setContentAreaFilled(false);
        switchModeButton.setFocusPainted(false);
        switchModeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(switchModeButton);
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Create both login forms but show only one at a time
        createTraditionalLoginForm(panel);
        createOTPLoginForm(panel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(200, 20));
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Login button
        loginButton = createStyledLoginButton("Sign In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Cancel button
        cancelButton = new JButton("Exit Application");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cancelButton.setForeground(new Color(0x757575));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(cancelButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Status label
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(0xD32F2F));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private void createTraditionalLoginForm(JPanel parent) {
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameLabel.setName("usernameLabel");
        
        usernameField = createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setName("usernameField");
        
        parent.add(usernameLabel);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(usernameField);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordLabel.setName("passwordLabel");
        
        passwordField = createStyledPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setName("passwordField");
        
        parent.add(passwordLabel);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(passwordField);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Options panel for traditional login
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        optionsPanel.setName("traditionalOptionsPanel");
        
        rememberMeCheckbox = new JCheckBox("Remember me");
        rememberMeCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rememberMeCheckbox.setOpaque(false);
        optionsPanel.add(rememberMeCheckbox, BorderLayout.WEST);
        
        forgotPasswordLabel = new JLabel("Forgot password?");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        forgotPasswordLabel.setForeground(UIFactory.PRIMARY_COLOR);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        optionsPanel.add(forgotPasswordLabel, BorderLayout.EAST);
        
        parent.add(optionsPanel);
    }
    
    private void createOTPLoginForm(JPanel parent) {
        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailLabel.setName("emailLabel");
        
        emailField = createStyledTextField();
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setName("emailField");
        
        parent.add(emailLabel);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(emailField);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // OTP field
        JLabel otpLabel = new JLabel("Verification Code");
        otpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        otpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        otpLabel.setName("otpLabel");
        
        otpField = createStyledTextField();
        otpField.setAlignmentX(Component.LEFT_ALIGNMENT);
        otpField.setName("otpField");
        
        parent.add(otpLabel);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(otpField);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // OTP options panel
        JPanel otpOptionsPanel = new JPanel(new BorderLayout());
        otpOptionsPanel.setOpaque(false);
        otpOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        otpOptionsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        otpOptionsPanel.setName("otpOptionsPanel");
        
        JLabel otpInfoLabel = new JLabel("Enter the 6-digit code sent to your email");
        otpInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        otpInfoLabel.setForeground(new Color(0x757575));
        otpOptionsPanel.add(otpInfoLabel, BorderLayout.WEST);
        
        resendOTPButton = new JButton("Resend Code");
        resendOTPButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resendOTPButton.setForeground(UIFactory.PRIMARY_COLOR);
        resendOTPButton.setBorderPainted(false);
        resendOTPButton.setContentAreaFilled(false);
        resendOTPButton.setFocusPainted(false);
        resendOTPButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resendOTPButton.setEnabled(false);
        otpOptionsPanel.add(resendOTPButton, BorderLayout.EAST);
        
        parent.add(otpOptionsPanel);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, FIELD_HEIGHT));
        field.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        
        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(FOCUS_INDICATOR_COLOR, 2, true),
                    new EmptyBorder(4, 9, 4, 9)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(new Color(0xE0E0E0), 1, true),
                    new EmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, FIELD_HEIGHT));
        field.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        
        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(FOCUS_INDICATOR_COLOR, 2, true),
                    new EmptyBorder(4, 9, 4, 9)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(new Color(0xE0E0E0), 1, true),
                    new EmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        
        return field;
    }
    
    private JButton createStyledLoginButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0, UIFactory.PRIMARY_COLOR,
                    0, getHeight(), new Color(0x0D47A1)
                );
                g2.setPaint(gp);
                
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                Rectangle textBounds = fm.getStringBounds(text, g2).getBounds();
                int x = (getWidth() - textBounds.width) / 2;
                int y = (getHeight() - textBounds.height) / 2 + fm.getAscent();
                g2.drawString(text, x, y);
                
                g2.dispose();
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(200, 45));
        button.setMaximumSize(new Dimension(200, 45));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void registerActions() {
        // Login button action
        loginButton.addActionListener(e -> {
            if (currentMode == LoginMode.TRADITIONAL) {
                attemptTraditionalLogin();
            } else {
                if (!otpSent) {
                    requestOTP();
                } else {
                    verifyOTP();
                }
            }
        });
        
        // Mode switch button
        switchModeButton.addActionListener(e -> switchLoginMode());
        
        // Cancel button
        cancelButton.addActionListener(e -> {
            if (callback != null) {
                callback.onCancel();
            } else {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit the application?",
                    "Exit Application",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        
        // Resend OTP button
        resendOTPButton.addActionListener(e -> resendOTP());
        
        // Enter key actions
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                    e.consume();
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);
        emailField.addKeyListener(enterKeyAdapter);
        otpField.addKeyListener(enterKeyAdapter);
        
        // Forgot password
        forgotPasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (callback != null) {
                    callback.onForgotPassword();
                } else {
                    handleForgotPassword();
                }
            }
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                forgotPasswordLabel.setText("<html><u>Forgot password?</u></html>");
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                forgotPasswordLabel.setText("Forgot password?");
            }
        });
    }
    
    private void switchLoginMode() {
        currentMode = (currentMode == LoginMode.TRADITIONAL) ? LoginMode.OTP : LoginMode.TRADITIONAL;
        otpSent = false;
        stopCountdownTimer();
        clearFields();
        updateUIForCurrentMode();
        setStatusMessage("");
        focusAppropriateField();
    }
    
    private void updateUIForCurrentMode() {
        // Hide all mode-specific components first
        hideComponent("usernameLabel");
        hideComponent("usernameField");
        hideComponent("passwordLabel");
        hideComponent("passwordField");
        hideComponent("traditionalOptionsPanel");
        hideComponent("emailLabel");
        hideComponent("emailField");
        hideComponent("otpLabel");
        hideComponent("otpField");
        hideComponent("otpOptionsPanel");
        
        if (currentMode == LoginMode.TRADITIONAL) {
            showComponent("usernameLabel");
            showComponent("usernameField");
            showComponent("passwordLabel");
            showComponent("passwordField");
            showComponent("traditionalOptionsPanel");
            
            switchModeButton.setText("Use Email & OTP");
            loginButton.setText("Sign In");
        } else {
            showComponent("emailLabel");
            showComponent("emailField");
            if (otpSent) {
                showComponent("otpLabel");
                showComponent("otpField");
                showComponent("otpOptionsPanel");
                loginButton.setText("Verify Code");
            } else {
                loginButton.setText("Send Code");
            }
            
            switchModeButton.setText("Use Username & Password");
        }
        
        revalidate();
        repaint();
    }
    
    private void hideComponent(String name) {
        Component[] components = getAllComponents(loginFormPanel);
        for (Component comp : components) {
            if (name.equals(comp.getName())) {
                comp.setVisible(false);
            }
        }
    }
    
    private void showComponent(String name) {
        Component[] components = getAllComponents(loginFormPanel);
        for (Component comp : components) {
            if (name.equals(comp.getName())) {
                comp.setVisible(true);
            }
        }
    }
    
    private Component[] getAllComponents(Container container) {
        java.util.List<Component> components = new java.util.ArrayList<>();
        for (Component comp : container.getComponents()) {
            components.add(comp);
            if (comp instanceof Container) {
                for (Component subComp : getAllComponents((Container) comp)) {
                    components.add(subComp);
                }
            }
        }
        return components.toArray(new Component[0]);
    }
    
    private void focusAppropriateField() {
        SwingUtilities.invokeLater(() -> {
            if (currentMode == LoginMode.TRADITIONAL) {
                usernameField.requestFocusInWindow();
            } else {
                if (!otpSent) {
                    emailField.requestFocusInWindow();
                } else {
                    otpField.requestFocusInWindow();
                }
            }
        });
    }
    
    private void clearFields() {
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");
        if (emailField != null) emailField.setText("");
        if (otpField != null) otpField.setText("");
    }
    
    private void attemptTraditionalLogin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        
        java.util.Arrays.fill(passwordChars, ' ');
        
        if (username.isEmpty()) {
            setStatusMessage("Username is required");
            usernameField.requestFocusInWindow();
            return;
        }
        
        if (password.isEmpty()) {
            setStatusMessage("Password is required");
            passwordField.requestFocusInWindow();
            return;
        }
        
        if (userService == null) {
            setStatusMessage("Service unavailable. Please try again.");
            LogUtil.error("UserService is null during login attempt");
            initializeRMIConnection();
            return;
        }
        
        showLoadingState(true);
        setStatusMessage("Authenticating...");
        
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                try {
                    LogUtil.info("Attempting traditional authentication for user: " + username);
                    return userService.authenticateUser(username, password);
                } catch (Exception e) {
                    LogUtil.error("Traditional authentication error", e);
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                showLoadingState(false);
                
                try {
                    User user = get();
                    
                    if (user != null) {
                        setStatusMessage("");
                        LogUtil.info("Traditional authentication successful for user: " + username);
                        
                        if (callback != null) {
                            callback.onLoginSuccess(user);
                        } else {
                            JOptionPane.showMessageDialog(
                                LoginView.this,
                                "Login successful! Welcome, " + user.getFullName() + "!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    } else {
                        setStatusMessage("Invalid username or password");
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                        
                        LogUtil.warn("Traditional authentication failed for user: " + username);
                        
                        if (callback != null) {
                            callback.onLoginFailure("Invalid username or password");
                        }
                        
                        shakeLoginButton();
                    }
                } catch (Exception ex) {
                    setStatusMessage("Authentication error. Please try again.");
                    LogUtil.error("Error during traditional authentication", ex);
                    
                    if (callback != null) {
                        callback.onLoginFailure("Authentication error: " + ex.getMessage());
                    }
                    
                    handleConnectionError(ex);
                }
            }
        };
        
        worker.execute();
    }
    
    private void requestOTP() {
        String email = emailField.getText().trim().toLowerCase();
        
        if (email.isEmpty()) {
            setStatusMessage("Email address is required");
            emailField.requestFocusInWindow();
            return;
        }
        
        if (!isValidEmail(email)) {
            setStatusMessage("Please enter a valid email address");
            emailField.requestFocusInWindow();
            return;
        }
        
        if (userService == null) {
            setStatusMessage("Service unavailable. Please try again.");
            LogUtil.error("UserService is null during OTP request");
            initializeRMIConnection();
            return;
        }
        
        showLoadingState(true);
        setStatusMessage("Sending verification code...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    LogUtil.info("Requesting OTP for email: " + email);
                    return userService.initiateOTPLogin(email);
                } catch (Exception e) {
                    LogUtil.error("OTP request error", e);
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                showLoadingState(false);
                
                try {
                    Boolean success = get();
                    
                    if (success != null && success) {
                        otpSent = true;
                        updateUIForCurrentMode();
                        setStatusMessage("Verification code sent to " + email);
                        startResendCountdown();
                        focusAppropriateField();
                        LogUtil.info("OTP sent successfully to: " + email);
                    } else {
                        setStatusMessage("Failed to send verification code. Please check your email address.");
                        LogUtil.warn("OTP request failed for email: " + email);
                        shakeLoginButton();
                    }
                } catch (Exception ex) {
                    setStatusMessage("Error sending verification code. Please try again.");
                    LogUtil.error("Error during OTP request", ex);
                    handleConnectionError(ex);
                }
            }
        };
        
        worker.execute();
    }
    
    private void verifyOTP() {
        String email = emailField.getText().trim().toLowerCase();
        String otpCode = otpField.getText().trim();
        
        if (email.isEmpty()) {
            setStatusMessage("Email address is required");
            emailField.requestFocusInWindow();
            return;
        }
        
        if (otpCode.isEmpty()) {
            setStatusMessage("Verification code is required");
            otpField.requestFocusInWindow();
            return;
        }
        
        if (otpCode.length() != 6 || !otpCode.matches("\\d{6}")) {
            setStatusMessage("Please enter a valid 6-digit verification code");
            otpField.requestFocusInWindow();
            return;
        }
        
        if (userService == null) {
            setStatusMessage("Service unavailable. Please try again.");
            LogUtil.error("UserService is null during OTP verification");
            initializeRMIConnection();
            return;
        }
        
        showLoadingState(true);
        setStatusMessage("Verifying code...");
        
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                try {
                    LogUtil.info("Verifying OTP for email: " + email);
                    return userService.completeOTPLogin(email, otpCode);
                } catch (Exception e) {
                    LogUtil.error("OTP verification error", e);
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                showLoadingState(false);
                
                try {
                    User user = get();
                    
                    if (user != null) {
                        setStatusMessage("");
                        LogUtil.info("OTP verification successful for email: " + email);
                        
                        if (callback != null) {
                            callback.onLoginSuccess(user);
                        } else {
                            JOptionPane.showMessageDialog(
                                LoginView.this,
                                "Login successful! Welcome, " + user.getFullName() + "!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    } else {
                        setStatusMessage("Invalid or expired verification code");
                        otpField.setText("");
                        otpField.requestFocusInWindow();
                        
                        LogUtil.warn("OTP verification failed for email: " + email);
                        
                        if (callback != null) {
                            callback.onLoginFailure("Invalid or expired verification code");
                        }
                        
                        shakeLoginButton();
                    }
                } catch (Exception ex) {
                    setStatusMessage("Verification error. Please try again.");
                    LogUtil.error("Error during OTP verification", ex);
                    
                    if (callback != null) {
                        callback.onLoginFailure("Verification error: " + ex.getMessage());
                    }
                    
                    handleConnectionError(ex);
                }
            }
        };
        
        worker.execute();
    }
    
    private void resendOTP() {
        String email = emailField.getText().trim().toLowerCase();
        
        if (email.isEmpty()) {
            setStatusMessage("Email address is required");
            emailField.requestFocusInWindow();
            return;
        }
        
        if (userService == null) {
            setStatusMessage("Service unavailable. Please try again.");
            initializeRMIConnection();
            return;
        }
        
        showLoadingState(true);
        setStatusMessage("Resending verification code...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    LogUtil.info("Resending OTP for email: " + email);
                    return userService.initiateOTPLogin(email);
                } catch (Exception e) {
                    LogUtil.error("OTP resend error", e);
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                showLoadingState(false);
                
                try {
                    Boolean success = get();
                    
                    if (success != null && success) {
                        setStatusMessage("New verification code sent to " + email);
                        startResendCountdown();
                        otpField.setText("");
                        otpField.requestFocusInWindow();
                        LogUtil.info("OTP resent successfully to: " + email);
                    } else {
                        setStatusMessage("Failed to resend verification code. Please try again later.");
                        LogUtil.warn("OTP resend failed for email: " + email);
                    }
                } catch (Exception ex) {
                    setStatusMessage("Error resending verification code. Please try again.");
                    LogUtil.error("Error during OTP resend", ex);
                    handleConnectionError(ex);
                }
            }
        };
        
        worker.execute();
    }
    
    private void startResendCountdown() {
        resendCountdown = 120; // 2 minutes
        resendOTPButton.setEnabled(false);
        
        countdownTimer = new Timer(1000, e -> {
            resendCountdown--;
            if (resendCountdown > 0) {
                resendOTPButton.setText("Resend Code (" + resendCountdown + "s)");
            } else {
                resendOTPButton.setText("Resend Code");
                resendOTPButton.setEnabled(true);
                countdownTimer.stop();
            }
        });
        
        countdownTimer.start();
    }
    
    private void stopCountdownTimer() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        resendOTPButton.setText("Resend Code");
        resendOTPButton.setEnabled(false);
    }
    
    private void handleConnectionError(Exception ex) {
        if (ex.getCause() instanceof java.rmi.ConnectException || 
            ex.getCause() instanceof java.net.ConnectException) {
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "Connection to server lost. Would you like to reconnect?",
                "Connection Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                initializeRMIConnection();
            }
        }
    }
    
    private void showLoadingState(boolean loading) {
        loginButton.setEnabled(!loading);
        switchModeButton.setEnabled(!loading);
        
        if (currentMode == LoginMode.TRADITIONAL) {
            usernameField.setEnabled(!loading);
            passwordField.setEnabled(!loading);
        } else {
            emailField.setEnabled(!loading && !otpSent);
            if (otpSent) {
                otpField.setEnabled(!loading);
                if (!loading && resendCountdown <= 0) {
                    resendOTPButton.setEnabled(true);
                }
            }
        }
        
        progressBar.setVisible(loading);
        
        if (loading) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void handleForgotPassword() {
        LogUtil.info("Forgot password requested");
        
        // Create a dialog for password reset
        JDialog resetDialog = new JDialog(this, "Password Reset", true);
        resetDialog.setSize(400, 300);
        resetDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Reset Your Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel instructionLabel = new JLabel("<html><center>Enter your email address and we'll send you<br>a verification code to reset your password.</center></html>");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(instructionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JTextField resetEmailField = createStyledTextField();
        resetEmailField.setMaximumSize(new Dimension(300, FIELD_HEIGHT));
        resetEmailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resetEmailField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendResetButton = UIFactory.createPrimaryButton("Send Reset Code");
        JButton cancelResetButton = UIFactory.createSecondaryButton("Cancel");
        
        sendResetButton.addActionListener(e -> {
            String resetEmail = resetEmailField.getText().trim();
            if (resetEmail.isEmpty() || !isValidEmail(resetEmail)) {
                JOptionPane.showMessageDialog(resetDialog, "Please enter a valid email address.");
                return;
            }
            
            try {
                boolean success = userService.initiatePasswordReset(resetEmail);
                if (success) {
                    JOptionPane.showMessageDialog(resetDialog, 
                        "Password reset code sent to " + resetEmail + "\n\n" +
                        "Please check your email and follow the instructions to reset your password.");
                    resetDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(resetDialog, "Failed to send reset code. Please try again.");
                }
            } catch (Exception ex) {
                LogUtil.error("Error initiating password reset", ex);
                JOptionPane.showMessageDialog(resetDialog, "Error sending reset code: " + ex.getMessage());
            }
        });
        
        cancelResetButton.addActionListener(e -> resetDialog.dispose());
        
        buttonPanel.add(sendResetButton);
        buttonPanel.add(cancelResetButton);
        panel.add(buttonPanel);
        
        resetDialog.add(panel);
        resetDialog.setVisible(true);
    }
    
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
        if (!message.isEmpty()) {
            LogUtil.debug("Login status message: " + message);
        }
    }
    
    private void shakeLoginButton() {
        final int amplitude = 10;
        final int cycles = 4;
        final int speed = 50;
        
        Thread shakeThread = new Thread(() -> {
            Point originalLocation = loginButton.getLocation();
            
            try {
                for (int i = 0; i < cycles; i++) {
                    Thread.sleep(speed);
                    SwingUtilities.invokeLater(() -> 
                        loginButton.setLocation(new Point(originalLocation.x + amplitude, originalLocation.y))
                    );
                    
                    Thread.sleep(speed);
                    SwingUtilities.invokeLater(() -> 
                        loginButton.setLocation(new Point(originalLocation.x - amplitude, originalLocation.y))
                    );
                }
                
                SwingUtilities.invokeLater(() -> loginButton.setLocation(originalLocation));
            } catch (InterruptedException e) {
                SwingUtilities.invokeLater(() -> loginButton.setLocation(originalLocation));
            }
        });
        
        shakeThread.setDaemon(true);
        shakeThread.start();
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    public void setRememberMe(boolean remember) {
        if (rememberMeCheckbox != null) {
            rememberMeCheckbox.setSelected(remember);
        }
    }
    
    public boolean isRememberMe() {
        return rememberMeCheckbox != null && rememberMeCheckbox.isSelected();
    }
    
    public void setUsername(String username) {
        if (usernameField != null) {
            usernameField.setText(username);
            if (username != null && !username.isEmpty()) {
                passwordField.requestFocusInWindow();
            }
        }
    }
    
    public String getUsername() {
        return usernameField != null ? usernameField.getText().trim() : "";
    }
    
    public void setEmail(String email) {
        if (emailField != null) {
            emailField.setText(email);
        }
    }
    
    public String getEmail() {
        return emailField != null ? emailField.getText().trim() : "";
    }
    
    public void clearPassword() {
        if (passwordField != null) {
            passwordField.setText("");
        }
    }
    
    public void clearOTP() {
        if (otpField != null) {
            otpField.setText("");
        }
    }
    
    public boolean isConnected() {
        return userService != null && RMIConnectionManager.isConnected();
    }
    
    public void reconnect() {
        setStatusMessage("Reconnecting...");
        initializeRMIConnection();
        
        if (isConnected()) {
            setStatusMessage("");
        } else {
            setStatusMessage("Connection failed");
        }
    }
    
    public LoginMode getCurrentMode() {
        return currentMode;
    }
    
    public void setLoginMode(LoginMode mode) {
        if (mode != currentMode) {
            currentMode = mode;
            otpSent = false;
            stopCountdownTimer();
            clearFields();
            updateUIForCurrentMode();
            setStatusMessage("");
            focusAppropriateField();
        }
    }
    
    // For testing purposes
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", defaultFont);
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("PasswordField.font", defaultFont);
            
        } catch (Exception e) {
            LogUtil.error("Failed to set look and feel", e);
        }
        
        LogUtil.info("Starting Enhanced LoginView test");
        
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView(new LoginView.AuthenticationCallback() {
                @Override
                public void onLoginSuccess(User user) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Login successful!\nWelcome, " + user.getFullName() + "!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    System.exit(0);
                }
                
                @Override
                public void onLoginFailure(String reason) {
                    LogUtil.warn("Login failed: " + reason);
                }
                
                @Override
                public void onCancel() {
                    System.exit(0);
                }
                
                @Override
                public void onForgotPassword() {
                    LogUtil.info("Forgot password action triggered");
                }
            });
            
            loginView.setVisible(true);
        });
    }
}