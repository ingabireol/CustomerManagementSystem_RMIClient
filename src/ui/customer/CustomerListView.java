package ui.customer;

import model.Customer;
import service.CustomerService;
import util.LogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import ui.UIFactory;

/**
 * List view for displaying and managing customers using RMI services.
 * Provides functionality for searching, filtering, and performing CRUD operations with remote data access.
 */
public class CustomerListView extends JPanel {
    
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote service
    private CustomerService customerService;
    
    // Table components
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    
    // Search and filter components
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    
    // Action buttons
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton viewOrdersButton;
    
    // Customer data
    private List<Customer> customerList;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Callback for list actions
    private CustomerListCallback callback;
    
    // Connection status
    private boolean isConnected = false;
    
    // Status components
    private JLabel connectionStatusLabel;
    private JLabel recordCountLabel;
    
    /**
     * Interface for customer list actions
     */
    public interface CustomerListCallback {
        void onAddCustomer();
        void onEditCustomer(Customer customer);
        void onDeleteCustomer(Customer customer);
        void onViewCustomerDetails(Customer customer);
        void onViewCustomerOrders(Customer customer);
    }
    
    /**
     * Constructor
     */
    public CustomerListView(CustomerListCallback callback) {
        this.callback = callback;
        this.customerList = new ArrayList<>();
        
        // Initialize RMI connection
        initializeRMIConnection();
        
        // Initialize UI
        initializeUI();
        
        // Load data if connected
        if (isConnected) {
            loadData();
        } else {
            showConnectionError();
        }
    }
    
    /**
     * Initializes RMI connection to CustomerService
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to CustomerService at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            customerService = (CustomerService) registry.lookup("customerService");
            isConnected = true;
            LogUtil.info("Successfully connected to CustomerService");
            
        } catch (Exception e) {
            LogUtil.error("Failed to connect to CustomerService", e);
            isConnected = false;
        }
    }
    
    /**
     * Shows connection error in the list view
     */
    private void showConnectionError() {
        // Clear table and show error message
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"Connection Error", "Unable to connect to server", "", "", "", ""});
        
        // Disable action buttons
        setButtonsEnabled(false);
        
        // Update status
        updateConnectionStatus();
    }
    
    /**
     * Sets the enabled state of action buttons
     */
    private void setButtonsEnabled(boolean enabled) {
        if (addButton != null) addButton.setEnabled(enabled);
        if (editButton != null) editButton.setEnabled(false); // Always disabled until selection
        if (deleteButton != null) deleteButton.setEnabled(false); // Always disabled until selection
        if (viewOrdersButton != null) viewOrdersButton.setEnabled(false); // Always disabled until selection
    }
    
    /**
     * Updates connection status display
     */
    private void updateConnectionStatus() {
        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText(isConnected ? "Connected" : "Disconnected");
            connectionStatusLabel.setForeground(isConnected ? UIFactory.SUCCESS_COLOR : UIFactory.ERROR_COLOR);
        }
        
        if (recordCountLabel != null) {
            int count = isConnected ? customerList.size() : 0;
            recordCountLabel.setText(count + " customers");
        }
    }
    
    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 10));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create the table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Create the actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
        
        // Update connection status
        updateConnectionStatus();
    }
    
    /**
     * Loads customer data from RMI service
     */
    private void loadData() {
        if (!isConnected) {
            return;
        }
        
        SwingWorker<List<Customer>, Void> worker = new SwingWorker<List<Customer>, Void>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                LogUtil.info("Loading customer data from RMI service...");
                return customerService.findAllCustomers();
            }
            
            @Override
            protected void done() {
                try {
                    List<Customer> customers = get();
                    if (customers != null) {
                        customerList = customers;
                        refreshTableData();
                        LogUtil.info("Loaded " + customers.size() + " customers successfully");
                    } else {
                        LogUtil.warn("Received null customer list from service");
                        customerList = new ArrayList<>();
                        refreshTableData();
                    }
                    updateConnectionStatus();
                    
                } catch (Exception e) {
                    LogUtil.error("Error loading customer data", e);
                    JOptionPane.showMessageDialog(CustomerListView.this,
                        "Error loading customer data: " + e.getMessage(),
                        "Data Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    // Show error in table
                    tableModel.setRowCount(0);
                    tableModel.addRow(new Object[]{"Error", "Failed to load data: " + e.getMessage(), "", "", "", ""});
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Creates the header panel with search and filter options
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = UIFactory.createModuleHeaderPanel("Customers");
        
        // Create search and filter section on the right side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        // Filter combo box
        String[] filterOptions = {"All Customers", "Recent Registrations", "Active Customers"};
        filterComboBox = UIFactory.createComboBox(filterOptions);
        filterComboBox.setPreferredSize(new Dimension(150, 30));
        
        // Search field
        searchField = UIFactory.createSearchField("Search customers...", 200);
        
        // Add search button
        JButton searchButton = UIFactory.createSecondaryButton("Search");
        
        // Add components to search panel
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(filterComboBox);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Add search panel to the header
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        // Add search action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Add Enter key listener to search field
        searchField.addActionListener(e -> performSearch());
        
        // Add filter change action
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
        
        return headerPanel;
    }
    
    /**
     * Performs search using RMI service
     */
    private void performSearch() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this,
                "Cannot search - not connected to server.",
                "Connection Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            // If empty search, reload all data
            loadData();
            return;
        }
        
        SwingWorker<List<Customer>, Void> worker = new SwingWorker<List<Customer>, Void>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                // Search by name using RMI service
                return customerService.findCustomersByName(searchText);
            }
            
            @Override
            protected void done() {
                try {
                    List<Customer> searchResults = get();
                    if (searchResults != null) {
                        customerList = searchResults;
                        refreshTableData();
                        LogUtil.info("Search completed, found " + searchResults.size() + " customers");
                        updateConnectionStatus();
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error performing search", e);
                    JOptionPane.showMessageDialog(CustomerListView.this,
                        "Search error: " + e.getMessage(),
                        "Search Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Applies local filter to the current data
     */
    private void applyFilter() {
        if (tableSorter == null) {
            return;
        }
        
        RowFilter<DefaultTableModel, Object> filter = null;
        String filterSelection = (String) filterComboBox.getSelectedItem();
        
        if (!"All Customers".equals(filterSelection)) {
            filter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    if ("Recent Registrations".equals(filterSelection)) {
                        // Filter for registrations in the last 30 days
                        String dateString = entry.getStringValue(5); // Registration date column
                        if (dateString == null || dateString.isEmpty()) {
                            return false;
                        }
                        try {
                            LocalDate registrationDate = LocalDate.parse(dateString, dateFormatter);
                            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                            return !registrationDate.isBefore(thirtyDaysAgo);
                        } catch (Exception e) {
                            return false;
                        }
                    } else if ("Active Customers".equals(filterSelection)) {
                        // For demo purposes, consider all customers as active
                        // In a real application, this would filter based on actual activity data
                        return true;
                    }
                    return true;
                }
            };
        }
        
        tableSorter.setRowFilter(filter);
    }
    
    /**
     * Creates the table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        
        // Create the table model with column names
        String[] columnNames = {"ID", "Customer ID", "Name", "Email", "Phone", "Registration Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        // Create and set up the table
        customerTable = UIFactory.createStyledTable(tableModel);
        
        // Add row sorting
        tableSorter = new TableRowSorter<>(tableModel);
        customerTable.setRowSorter(tableSorter);
        
        // Set column widths
        customerTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID column
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Customer ID
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Name
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Email
        
        // Add double-click listener for viewing details
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = customerTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        selectedRow = customerTable.convertRowIndexToModel(selectedRow);
                        Customer selectedCustomer = getCustomerAtRow(selectedRow);
                        if (selectedCustomer != null && callback != null) {
                            callback.onViewCustomerDetails(selectedCustomer);
                        }
                    }
                }
            }
        });
        
        // Add selection listener to enable/disable action buttons
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = customerTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection && isConnected);
                deleteButton.setEnabled(hasSelection && isConnected);
                viewOrdersButton.setEnabled(hasSelection && isConnected);
            }
        });
        
        // Add the table to a scroll pane
        JScrollPane scrollPane = UIFactory.createScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the actions panel
     */
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Information and status panel (left side)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setOpaque(false);
        
        JLabel infoLabel = new JLabel("Double-click a row to view customer details");
        infoLabel.setFont(UIFactory.SMALL_FONT);
        infoLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
        connectionStatusLabel = new JLabel("Disconnected");
        connectionStatusLabel.setFont(UIFactory.SMALL_FONT);
        connectionStatusLabel.setForeground(UIFactory.ERROR_COLOR);
        
        recordCountLabel = new JLabel("0 customers");
        recordCountLabel.setFont(UIFactory.SMALL_FONT);
        recordCountLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
        infoPanel.add(infoLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(new JLabel("Status: "));
        infoPanel.add(connectionStatusLabel);
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(recordCountLabel);
        
        // Action buttons (right side)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        
        refreshButton = UIFactory.createSecondaryButton("Refresh");
        
        viewOrdersButton = UIFactory.createSecondaryButton("View Orders");
        viewOrdersButton.setEnabled(false); // Disabled until selection
        
        deleteButton = UIFactory.createDangerButton("Delete");
        deleteButton.setEnabled(false); // Disabled until selection
        
        editButton = UIFactory.createWarningButton("Edit");
        editButton.setEnabled(false); // Disabled until selection
        
        addButton = UIFactory.createPrimaryButton("Add New");
        
        // Add buttons to panel
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(viewOrdersButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(addButton);
        
        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);
        
        // Register button actions
        registerButtonActions();
        
        return panel;
    }
    
    /**
     * Registers button action listeners
     */
    private void registerButtonActions() {
        // Add button action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isConnected && callback != null) {
                    callback.onAddCustomer();
                } else if (!isConnected) {
                    showNotConnectedMessage();
                }
            }
        });
        
        // Edit button action
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0 && isConnected && callback != null) {
                    selectedRow = customerTable.convertRowIndexToModel(selectedRow);
                    Customer selectedCustomer = getCustomerAtRow(selectedRow);
                    if (selectedCustomer != null) {
                        callback.onEditCustomer(selectedCustomer);
                    }
                } else if (!isConnected) {
                    showNotConnectedMessage();
                }
            }
        });
        
        // Delete button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0 && isConnected && callback != null) {
                    selectedRow = customerTable.convertRowIndexToModel(selectedRow);
                    Customer selectedCustomer = getCustomerAtRow(selectedRow);
                    if (selectedCustomer != null) {
                        confirmAndDeleteCustomer(selectedCustomer);
                    }
                } else if (!isConnected) {
                    showNotConnectedMessage();
                }
            }
        });
        
        // View Orders button action
        viewOrdersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0 && isConnected && callback != null) {
                    selectedRow = customerTable.convertRowIndexToModel(selectedRow);
                    Customer selectedCustomer = getCustomerAtRow(selectedRow);
                    if (selectedCustomer != null) {
                        callback.onViewCustomerOrders(selectedCustomer);
                    }
                } else if (!isConnected) {
                    showNotConnectedMessage();
                }
            }
        });
        
        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
    }
    
    /**
     * Shows not connected message
     */
    private void showNotConnectedMessage() {
        JOptionPane.showMessageDialog(this,
            "Cannot perform action - not connected to server.\nPlease check your connection and try refreshing.",
            "Connection Error",
            JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Confirms and deletes a customer
     */
    private void confirmAndDeleteCustomer(Customer customer) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete customer: " + customer.getFullName() + "?\n" +
            "This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            deleteCustomer(customer);
        }
    }
    
    /**
     * Deletes a customer using RMI service
     */
    private void deleteCustomer(Customer customer) {
        SwingWorker<Customer, Void> worker = new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() throws Exception {
                return customerService.deleteCustomer(customer);
            }
            
            @Override
            protected void done() {
                try {
                    Customer deletedCustomer = get();
                    
                    if (deletedCustomer != null) {
                        // Remove from local list
                        customerList.removeIf(c -> c.getId() == customer.getId());
                        refreshTableData();
                        updateConnectionStatus();
                        
                        JOptionPane.showMessageDialog(CustomerListView.this,
                            "Customer deleted successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        if (callback != null) {
                            callback.onDeleteCustomer(customer);
                        }
                        
                        LogUtil.info("Customer deleted successfully: " + customer.getFullName());
                        
                    } else {
                        JOptionPane.showMessageDialog(CustomerListView.this,
                            "Failed to delete customer.\nThe customer may be referenced by other records.",
                            "Delete Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error deleting customer", e);
                    JOptionPane.showMessageDialog(CustomerListView.this,
                        "Error deleting customer: " + e.getMessage(),
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Refreshes data from the server
     */
    private void refreshData() {
        // Try to reconnect if not connected
        if (!isConnected) {
            initializeRMIConnection();
        }
        
        if (isConnected) {
            // Clear search field and reset filter
            searchField.setText("");
            filterComboBox.setSelectedIndex(0);
            
            // Reload data
            loadData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Unable to connect to server.\nPlease check if the server is running and try again.",
                "Connection Failed",
                JOptionPane.ERROR_MESSAGE);
        }
        
        updateConnectionStatus();
    }
    
    /**
     * Refreshes the table data with the current customer list
     */
    private void refreshTableData() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Populate the table with data
        for (Customer customer : customerList) {
            Object[] rowData = {
                customer.getId(),
                customer.getCustomerId() != null ? customer.getCustomerId() : "",
                customer.getFullName() != null ? customer.getFullName() : "",
                customer.getEmail() != null ? customer.getEmail() : "",
                customer.getPhone() != null ? customer.getPhone() : "",
                customer.getRegistrationDate() != null ? customer.getRegistrationDate().format(dateFormatter) : ""
            };
            tableModel.addRow(rowData);
        }
        
        // Reset selection and button states
        customerTable.clearSelection();
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        viewOrdersButton.setEnabled(false);
        
        // Apply any current filter
        applyFilter();
    }
    
    /**
     * Gets the Customer object corresponding to a specific table row
     */
    private Customer getCustomerAtRow(int modelRow) {
        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
            int customerId = (Integer) tableModel.getValueAt(modelRow, 0);
            for (Customer customer : customerList) {
                if (customer.getId() == customerId) {
                    return customer;
                }
            }
        }
        return null;
    }
    
    /**
     * Updates the customer list and refreshes the table
     */
    public void updateCustomers(List<Customer> customers) {
        this.customerList = customers != null ? customers : new ArrayList<>();
        refreshTableData();
        updateConnectionStatus();
    }
    
    /**
     * Adds a customer to the list and refreshes the table
     */
    public void addCustomer(Customer customer) {
        if (customer != null && customer.getId() > 0) {
            this.customerList.add(customer);
            refreshTableData();
            updateConnectionStatus();
        }
    }
    
    /**
     * Updates a customer in the list and refreshes the table
     */
    public void updateCustomer(Customer customer) {
        if (customer != null) {
            for (int i = 0; i < customerList.size(); i++) {
                if (customerList.get(i).getId() == customer.getId()) {
                    customerList.set(i, customer);
                    break;
                }
            }
            refreshTableData();
            updateConnectionStatus();
        }
    }
    
    /**
     * Removes a customer from the list and refreshes the table
     */
    public void removeCustomer(Customer customer) {
        if (customer != null) {
            customerList.removeIf(c -> c.getId() == customer.getId());
            refreshTableData();
            updateConnectionStatus();
        }
    }
    
    /**
     * Checks if connected to RMI service
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Reconnects to RMI service
     */
    public void reconnect() {
        initializeRMIConnection();
        updateConnectionStatus();
        if (isConnected) {
            setButtonsEnabled(true);
            loadData();
        }
    }
    
    /**
     * Gets the current customer list
     */
    public List<Customer> getCustomerList() {
        return new ArrayList<>(customerList);
    }
    
    /**
     * Gets the currently selected customer
     */
    public Customer getSelectedCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedRow = customerTable.convertRowIndexToModel(selectedRow);
            return getCustomerAtRow(selectedRow);
        }
        return null;
    }
}