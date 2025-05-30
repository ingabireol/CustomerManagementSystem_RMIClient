package ui;

import controller.AuthController;
import ui.supplier.SupplierView;
import ui.product.ProductView;
import ui.customer.CustomerView;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import model.User;
import ui.auth.Session;
import ui.order.OrderView;

/**
 * Main application frame that provides the foundation for the Business Management System UI.
 * Implements the modern UI design with top bar, side navigation, content area, and status bar.
 */
public class MainView extends JFrame {
    // UI Components
    private JPanel sideNavPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JLabel statusLabel;
    
    // Module panels
    private JPanel dashboardPanel;
    private CustomerView customersPanel;
    private SupplierView suppliersPanel;
    private ProductView productsPanel;
    private JPanel ordersPanel;
    private JPanel invoicesPanel;
    
    // Navigation buttons
    private Map<String, JButton> navButtons = new HashMap<>();
    
    // Constants
    private static final Color PRIMARY_COLOR = new Color(0x1976D2);
    private static final Color SECONDARY_COLOR = new Color(0xFF5722);
    private static final Color BACKGROUND_COLOR = new Color(0xF5F5F5);
    private static final Color DARK_GRAY = new Color(0x424242);
    private static final Color MEDIUM_GRAY = new Color(0x9E9E9E);
    private static final Color LIGHT_GRAY = new Color(0xF5F5F5);
    private static final Color SUCCESS_COLOR = new Color(0x4CAF50);
    private static final Color WARNING_COLOR = new Color(0xFFC107);
    private static final Color ERROR_COLOR = new Color(0xF44336);
    
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    private boolean isMenuExpanded = true;
    
    public MainView() {
        initializeUI();
    }
    
    private void initializeUI() {
        // Configure the main frame
        setTitle("Customer Management System");
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        
        // Set up look and feel
        setupLookAndFeel();
        
        // Create main components
        JPanel topBar = createTopBar();
        sideNavPanel = createSideNav();
        JPanel contentWrapper = createContentPanel(); // This creates contentPanel with CardLayout
        JPanel statusBar = createStatusBar();
        
        // Add components to the frame
        add(topBar, BorderLayout.NORTH);
        add(sideNavPanel, BorderLayout.WEST);
        add(contentWrapper, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        
        // Set up responsive behavior
        setupResponsiveBehavior();
        
        // Set initial content - show dashboard
        selectModule("Dashboard");
        
        // Display status message
        setStatusMessage("System ready");
    }
    
    private void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            
            // Set global font settings
            UIManager.put("Label.font", BODY_FONT);
            UIManager.put("Button.font", BODY_FONT);
            UIManager.put("TextField.font", BODY_FONT);
            UIManager.put("TextArea.font", BODY_FONT);
            UIManager.put("ComboBox.font", BODY_FONT);
            UIManager.put("Table.font", BODY_FONT);
            UIManager.put("TableHeader.font", HEADER_FONT);
            UIManager.put("TabbedPane.font", BODY_FONT);
            
        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }
    }
    
    // Update the top bar section in MainView.java to include user information and logout

// Replace the existing createTopBar method with this updated version:

private JPanel createTopBar() {
    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setBackground(PRIMARY_COLOR);
    topBar.setPreferredSize(new Dimension(getWidth(), 50));
    topBar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    
    // Application title
    JLabel titleLabel = new JLabel("Customer Management System");
    titleLabel.setFont(TITLE_FONT);
    titleLabel.setForeground(Color.WHITE);
    topBar.add(titleLabel, BorderLayout.WEST);
    
    // Global actions (right side)
    JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actionsPanel.setOpaque(false);
    
    // Get the current user from session
    User currentUser = Session.getInstance().getCurrentUser();
    String userDisplayName = "Guest";
    if (currentUser != null) {
        userDisplayName = currentUser.getFullName();
    }
    
    // User info
    JLabel userLabel = new JLabel(userDisplayName);
    userLabel.setForeground(Color.WHITE);
    
    // Settings button
    JButton settingsButton = createIconButton("Settings");
    settingsButton.setForeground(Color.WHITE);
    settingsButton.addActionListener(e -> showSettingsMenu(settingsButton));
    
    // Logout button
    JButton logoutButton = createIconButton("Logout");
    logoutButton.setForeground(Color.WHITE);
    logoutButton.addActionListener(e -> logout());
    
    actionsPanel.add(userLabel);
    actionsPanel.add(settingsButton);
    actionsPanel.add(logoutButton);
    
    topBar.add(actionsPanel, BorderLayout.EAST);
    
    return topBar;
}
    
    private JPanel createSideNav() {
        JPanel sideNav = new JPanel();
        sideNav.setBackground(DARK_GRAY);
        sideNav.setPreferredSize(new Dimension(200, getHeight()));
        sideNav.setLayout(new BoxLayout(sideNav, BoxLayout.Y_AXIS));
        sideNav.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Create navigation buttons
        JButton dashboardBtn = createNavButton("Dashboard", true);
        JButton customersBtn = createNavButton("Customers", false);
        JButton productsBtn = createNavButton("Products", false);
        JButton ordersBtn = createNavButton("Orders", false);
        JButton invoicesBtn = createNavButton("Invoices", false);
        JButton suppliersBtn = createNavButton("Suppliers", false);
        
        // Store buttons in map for easy access
        navButtons.put("Dashboard", dashboardBtn);
        navButtons.put("Customers", customersBtn);
        navButtons.put("Products", productsBtn);
        navButtons.put("Orders", ordersBtn);
        navButtons.put("Invoices", invoicesBtn);
        navButtons.put("Suppliers", suppliersBtn);
        
        // Add buttons to panel
        sideNav.add(dashboardBtn);
        sideNav.add(Box.createRigidArea(new Dimension(0, 10)));
        sideNav.add(customersBtn);
        sideNav.add(Box.createRigidArea(new Dimension(0, 10)));
        sideNav.add(productsBtn);
        sideNav.add(Box.createRigidArea(new Dimension(0, 10)));
        sideNav.add(ordersBtn);
        sideNav.add(Box.createRigidArea(new Dimension(0, 10)));
        sideNav.add(invoicesBtn);
        sideNav.add(Box.createRigidArea(new Dimension(0, 10)));
        sideNav.add(suppliersBtn);
        
        // Add glue to push everything to the top
        sideNav.add(Box.createVerticalGlue());
        
        // Toggle theme button at bottom
        JButton themeToggleButton = createNavButton("Toggle Theme", false);
        themeToggleButton.addActionListener(e -> toggleTheme());
        sideNav.add(themeToggleButton);
        
        return sideNav;
    }
    
    private JButton createNavButton(String text, boolean isSelected) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        
        if (isSelected) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(DARK_GRAY);
            button.setForeground(Color.WHITE);
            
            // Add hover effect
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x555555));
                }
                
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (!button.equals(getSelectedNavButton())) {
                        button.setBackground(DARK_GRAY);
                    }
                }
            });
        }
        
        // Add action to switch panels
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectModule(text);
            }
        });
        
        return button;
    }
    
    private JPanel createContentPanel() {
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BACKGROUND_COLOR);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create card layout for switching between panels
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // Create module panels
        dashboardPanel = createDashboardPanel();
        ordersPanel = createModulePanel("Orders");
        invoicesPanel = createModulePanel("Invoices");
        
        // Initialize the custom module views
        suppliersPanel = new SupplierView();
        productsPanel = new ProductView();
        customersPanel = new CustomerView();
        ordersPanel = new OrderView();
        
        // Add panels to card layout
        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(customersPanel, "Customers");
        contentPanel.add(productsPanel, "Products");
        contentPanel.add(ordersPanel, "Orders");
        contentPanel.add(invoicesPanel, "Invoices");
        contentPanel.add(suppliersPanel, "Suppliers");
        
        contentWrapper.add(contentPanel, BorderLayout.CENTER);
        
        return contentWrapper;
    }
    
    private JPanel createDashboardPanel() {
        return new DashboardPanel();
    }
    
    private JPanel createModulePanel(String moduleName) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Module header with title and actions
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(moduleName);
        titleLabel.setFont(TITLE_FONT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search and action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search " + moduleName.toLowerCase() + "...");
        
        JButton addButton = new JButton("Add New");
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        
        actionPanel.add(searchField);
        actionPanel.add(addButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        
        // Main content - placeholder for now
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel placeholderLabel = new JLabel(moduleName + " content will be implemented here");
        placeholderLabel.setFont(BODY_FONT);
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(placeholderLabel, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(LIGHT_GRAY);
        statusBar.setPreferredSize(new Dimension(getWidth(), 25));
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private JButton createIconButton(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        
        return button;
    }
    
    private void setupResponsiveBehavior() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                if (width < 1000 && isMenuExpanded) {
                    collapseMenu();
                } else if (width >= 1000 && !isMenuExpanded) {
                    expandMenu();
                }
            }
        });
    }
    
    private void collapseMenu() {
        sideNavPanel.setPreferredSize(new Dimension(60, getHeight()));
        isMenuExpanded = false;
        
        // Update nav buttons to show only icons
        for (JButton button : navButtons.values()) {
            button.setText("");
            button.setPreferredSize(new Dimension(40, 40));
        }
        
        revalidate();
    }
    
    private void expandMenu() {
        sideNavPanel.setPreferredSize(new Dimension(200, getHeight()));
        isMenuExpanded = true;
        
        // Restore button text
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            entry.getValue().setText(entry.getKey());
            entry.getValue().setPreferredSize(null); // Reset to default
        }
        
        revalidate();
    }
    
    private void toggleTheme() {
        // This would be replaced with actual theme switching when FlatLaf is implemented
        JOptionPane.showMessageDialog(this, "Theme toggle functionality would be implemented with FlatLaf library", 
                                     "Theme Toggle", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Selects a module and shows its panel
     * 
     * @param moduleName The name of the module to select
     */
    public void selectModule(String moduleName) {
        // Update navigation button states
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(moduleName)) {
                entry.getValue().setBackground(PRIMARY_COLOR);
            } else {
                entry.getValue().setBackground(DARK_GRAY);
            }
        }
        
        // Show the selected panel
        if (contentPanel != null && contentCardLayout != null) {
            contentCardLayout.show(contentPanel, moduleName);
            setStatusMessage("Viewing " + moduleName);
        }
    }
    
    /**
     * Gets the currently selected navigation button
     * 
     * @return The selected button or null if none
     */
    private JButton getSelectedNavButton() {
        for (JButton button : navButtons.values()) {
            if (button.getBackground().equals(PRIMARY_COLOR)) {
                return button;
            }
        }
        return null;
    }
    
    /**
     * Sets a status message
     * 
     * @param message The message to display
     */
    private void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
    /**
 * Shows the settings popup menu
 * 
 * @param component The component to show the menu next to
 */
private void showSettingsMenu(Component component) {
    JPopupMenu menu = new JPopupMenu();
    
    JMenuItem profileItem = new JMenuItem("User Profile");
    JMenuItem changePasswordItem = new JMenuItem("Change Password");
    JMenuItem preferencesItem = new JMenuItem("Preferences");
    
    // Add menu items
    menu.add(profileItem);
    menu.add(changePasswordItem);
    menu.addSeparator();
    menu.add(preferencesItem);
    
    // Add actions
    profileItem.addActionListener(e -> showUserProfile());
    changePasswordItem.addActionListener(e -> changePassword());
    preferencesItem.addActionListener(e -> showPreferences());
    
    // Show the menu
    menu.show(component, 0, component.getHeight());
}

/**
 * Shows the user profile dialog
 */
private void showUserProfile() {
    User currentUser = Session.getInstance().getCurrentUser();
    if (currentUser == null) {
        return;
    }
    
    // Create a panel to display user information
    JPanel panel = new JPanel(new BorderLayout(0, 10));
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Title
    JLabel titleLabel = new JLabel("User Profile");
    titleLabel.setFont(TITLE_FONT);
    panel.add(titleLabel, BorderLayout.NORTH);
    
    // User details panel
    JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    detailsPanel.setBackground(Color.WHITE);
    
    // Add user details
    detailsPanel.add(new JLabel("Username:"));
    detailsPanel.add(new JLabel(currentUser.getUsername()));
    
    detailsPanel.add(new JLabel("Full Name:"));
    detailsPanel.add(new JLabel(currentUser.getFullName()));
    
    detailsPanel.add(new JLabel("Email:"));
    detailsPanel.add(new JLabel(currentUser.getEmail()));
    
    detailsPanel.add(new JLabel("Role:"));
    detailsPanel.add(new JLabel(currentUser.getRole()));
    
    if (currentUser.getLastLogin() != null) {
        detailsPanel.add(new JLabel("Last Login:"));
        detailsPanel.add(new JLabel(currentUser.getLastLogin().toString()));
    }
    
    panel.add(detailsPanel, BorderLayout.CENTER);
    
    // Show the dialog
    JOptionPane.showMessageDialog(
        this,
        panel,
        "User Profile",
        JOptionPane.PLAIN_MESSAGE
    );
}

/**
 * Shows the change password dialog
 */
private void changePassword() {
    // Create an instance of AuthController and call its method
    AuthController authController = new AuthController(this);
    authController.showChangePasswordDialog();
}

/**
 * Shows the preferences dialog
 */
private void showPreferences() {
    // Simple preferences dialog with theme toggle
    JPanel panel = new JPanel(new BorderLayout(0, 10));
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    JLabel titleLabel = new JLabel("Preferences");
    titleLabel.setFont(TITLE_FONT);
    panel.add(titleLabel, BorderLayout.NORTH);
    
    JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 0, 10));
    optionsPanel.setBackground(Color.WHITE);
    
    JCheckBox darkModeCheckbox = new JCheckBox("Enable Dark Mode");
    darkModeCheckbox.setSelected(false); // Would be set from preferences
    
    optionsPanel.add(darkModeCheckbox);
    panel.add(optionsPanel, BorderLayout.CENTER);
    
    // Show the dialog
    int result = JOptionPane.showConfirmDialog(
        this,
        panel,
        "Preferences",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );
    
    if (result == JOptionPane.OK_OPTION) {
        // Save preferences and apply changes
        boolean darkMode = darkModeCheckbox.isSelected();
        // Apply dark mode
        if (darkMode) {
            // Toggle theme implementation would go here
            toggleTheme();
        }
    }
}

/**
 * Logs out the current user and returns to the login screen
 */
private void logout() {
    // Confirm logout
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to log out?",
        "Confirm Logout",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );
    
    if (confirm == JOptionPane.YES_OPTION) {
        // Close the main view
        dispose();
        
        // Create an instance of AuthController and call its logout method
        AuthController authController = new AuthController(null);
        authController.logout();
    }
}
    
    /**
     * Main method to launch the application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainView mainView = new MainView();
            mainView.setLocationRelativeTo(null);
            mainView.setVisible(true);
        });
    }
}