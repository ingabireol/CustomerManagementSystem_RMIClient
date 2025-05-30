package ui.auth;

import model.User;
import dao.UserDao;
import ui.MainView;
import ui.UIFactory;

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
 * Login screen for the Business Management System.
 * Handles user authentication and validation.
 */
public class LoginView extends JFrame {
    // Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JCheckBox rememberMeCheckbox;
    private JLabel forgotPasswordLabel;
    
    // Styling constants
    private static final Color FOCUS_INDICATOR_COLOR = UIFactory.PRIMARY_COLOR;
    private static final int FIELD_HEIGHT = 40;
    
    // Data access
    private UserDao userDao;
    
    // Authentication callback
    private AuthenticationCallback callback;
    
    /**
     * Interface for authentication callback
     */
    public interface AuthenticationCallback {
        /**
         * Called when a user successfully logs in
         * 
         * @param user The authenticated user
         */
        void onLoginSuccess(User user);
        
        /**
         * Called when login fails
         * 
         * @param reason The reason for failure
         */
        void onLoginFailure(String reason);
        
        /**
         * Called when the user cancels login
         */
        void onCancel();
        
        /**
         * Called when the user clicks the forgot password link
         */
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
     * 
     * @param callback Authentication callback
     */
    public LoginView(AuthenticationCallback callback) {
        this.callback = callback;
        this.userDao = new UserDao();
        
        // Create the users table and default admin if needed
        userDao.createUsersTable();
        userDao.createDefaultAdmin();
        
        initializeUI();
    }
    
    private void initializeUI() {
        // Set up the frame
        setTitle("Customer Management System - Login");
        setSize(450, 550);
        setMinimumSize(new Dimension(400, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set up the main panel with a gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Create gradient background
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
        
        // Create the logo/header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create the form panel in a card shape
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        formWrapper.setOpaque(false);
        
        JPanel formCardPanel = createFormCardPanel();
        formWrapper.add(formCardPanel);
        
        mainPanel.add(formWrapper, BorderLayout.CENTER);
        
        // Register actions
        registerActions();
        
        // Set initial focus
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(getWidth(), 150));
        // Logo placeholder
        JLabel logoLabel = new JLabel("CUSTOMER MANAGEMENT SYSTEM");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Add subtle shadow effect to text
        logoLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        panel.add(logoLabel, BorderLayout.CENTER);
        
        // Add version info
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setForeground(new Color(255, 255, 255, 180));
        versionLabel.setFont(UIFactory.SMALL_FONT);
        versionLabel.setBorder(new EmptyBorder(0, 0, 5, 10));
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(versionLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFormCardPanel() {
        // Create a card-shaped panel with shadow effect
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
        
        cardPanel.setPreferredSize(new Dimension(350, 350));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the form panel
        JPanel formPanel = createFormPanel();
        cardPanel.add(formPanel, BorderLayout.CENTER);
        
        return cardPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Login to Your Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Enter your credentials to access the system");
        subtitleLabel.setFont(UIFactory.BODY_FONT);
        subtitleLabel.setForeground(UIFactory.MEDIUM_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitleLabel);
        
        // Add some space
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(UIFactory.BODY_FONT);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField = createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(usernameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UIFactory.BODY_FONT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordField = createStyledPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(passwordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Additional options panel (remember me and forgot password)
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        
        // Remember me checkbox
        rememberMeCheckbox = new JCheckBox("Remember me");
        rememberMeCheckbox.setFont(UIFactory.SMALL_FONT);
        rememberMeCheckbox.setOpaque(false);
        optionsPanel.add(rememberMeCheckbox, BorderLayout.WEST);
        
        // Forgot password link
        forgotPasswordLabel = new JLabel("Forgot password?");
        forgotPasswordLabel.setFont(UIFactory.SMALL_FONT);
        forgotPasswordLabel.setForeground(UIFactory.PRIMARY_COLOR);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        optionsPanel.add(forgotPasswordLabel, BorderLayout.EAST);
        
        panel.add(optionsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Login button
        loginButton = createStyledLoginButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(UIFactory.BODY_FONT);
        cancelButton.setForeground(UIFactory.MEDIUM_GRAY);
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(cancelButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Status label for errors
        statusLabel = new JLabel("");
        statusLabel.setFont(UIFactory.SMALL_FONT);
        statusLabel.setForeground(UIFactory.ERROR_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(statusLabel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(UIFactory.BODY_FONT);
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, FIELD_HEIGHT));
        field.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        
        // Create custom border
        field.setBorder(new CompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Add focus listener for border change
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
                    new LineBorder(UIFactory.LIGHT_GRAY, 1, true),
                    new EmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(UIFactory.BODY_FONT);
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, FIELD_HEIGHT));
        field.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        
        // Create custom border
        field.setBorder(new CompoundBorder(
            new LineBorder(UIFactory.LIGHT_GRAY, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Add focus listener for border change
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
                    new LineBorder(UIFactory.LIGHT_GRAY, 1, true),
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
                
                // Draw gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, UIFactory.PRIMARY_COLOR,
                    0, getHeight(), new Color(0x0D47A1)
                );
                g2.setPaint(gp);
                
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                
                // Draw text
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
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setFont(new Font("Segoe UI", Font.BOLD, 15));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        });
        
        return button;
    }
    
    private void registerActions() {
        // Login button action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });
        
        // Cancel button action
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null) {
                    callback.onCancel();
                } else {
                    System.exit(0);
                }
            }
        });
        
        // Enter key in password field triggers login
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginButton.doClick();
            }
        });
        
        // Tab behavior to move between fields
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    passwordField.requestFocusInWindow();
                    e.consume();
                }
            }
        });
        
        // Forgot password link action
        forgotPasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (callback != null) {
                    callback.onForgotPassword();
                } else {
                    JOptionPane.showMessageDialog(
                        LoginView.this,
                        "Password reset functionality would be implemented here.",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE
                    );
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
    
    /**
     * Attempts to log in with the provided credentials
     */
    private void attemptLogin() {
        // Validate input
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        
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
        
        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false);
        setStatusMessage("Authenticating...");
        
        // Perform authentication in a separate thread to avoid UI freeze
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // Authenticate user
                return userDao.authenticateUser(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    
                    // Reset cursor and re-enable button
                    setCursor(Cursor.getDefaultCursor());
                    loginButton.setEnabled(true);
                    
                    if (user != null) {
                        // Authentication successful
                        setStatusMessage("");
                        
                        if (callback != null) {
                            callback.onLoginSuccess(user);
                        } else {
                            // If no callback provided, just open the main view
                            dispose();
                            MainView mainView = new MainView();
                            mainView.setVisible(true);
                        }
                    } else {
                        // Authentication failed
                        setStatusMessage("Invalid username or password");
                        passwordField.setText("");
                        
                        if (callback != null) {
                            callback.onLoginFailure("Invalid username or password");
                        }
                        
                        // Shake effect for failed login
                        shakeLoginButton();
                    }
                } catch (Exception ex) {
                    setCursor(Cursor.getDefaultCursor());
                    loginButton.setEnabled(true);
                    setStatusMessage("Authentication error: " + ex.getMessage());
                    
                    if (callback != null) {
                        callback.onLoginFailure("Authentication error: " + ex.getMessage());
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Sets the status message
     * 
     * @param message Message to display
     */
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Performs a shake animation on the login button for failed login attempts
     */
    private void shakeLoginButton() {
        final int amplitude = 10;
        final int cycles = 4;
        final int speed = 50;
        
        Thread shakeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Point originalLocation = loginButton.getLocation();
                
                try {
                    for (int i = 0; i < cycles; i++) {
                        Thread.sleep(speed);
                        loginButton.setLocation(new Point(originalLocation.x + amplitude, originalLocation.y));
                        
                        Thread.sleep(speed);
                        loginButton.setLocation(new Point(originalLocation.x - amplitude, originalLocation.y));
                    }
                    
                    // Return to original position
                    loginButton.setLocation(originalLocation);
                } catch (InterruptedException e) {
                    // Restore original position if interrupted
                    loginButton.setLocation(originalLocation);
                }
            }
        });
        
        shakeThread.start();
    }
    
    /**
     * Main method to launch the login view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set up look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Show the login view
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}