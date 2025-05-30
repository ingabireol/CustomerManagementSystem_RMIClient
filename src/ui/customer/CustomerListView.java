package ui.customer;

import model.Customer;
import dao.CustomerDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import ui.UIFactory;

/**
 * List view for displaying and managing customers.
 * Provides functionality for searching, filtering, and performing CRUD operations.
 */
public class CustomerListView extends JPanel {
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
    
    // Customer data and DAO
    private List<Customer> customerList;
    private CustomerDao customerDao;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Callback for list actions
    private CustomerListCallback callback;
    
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
        this.customerDao = new CustomerDao();
        this.customerList = new ArrayList<>();
        
        initializeUI();
        loadData();
    }
    
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
    }
    
    /**
     * Loads customer data from the database
     */
    private void loadData() {
        try {
            this.customerList = customerDao.findAllCustomers();
            refreshTableData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading customer data: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = UIFactory.createModuleHeaderPanel("Customers");
        
        // Create search and filter section on the right side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        // Filter combo box
        String[] filterOptions = {"All Customers", "Recent Registrations", "Inactive Customers"};
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
                applyFilter();
            }
        });
        
        // Add filter change action
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
        
        return headerPanel;
    }
    
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
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                viewOrdersButton.setEnabled(hasSelection);
            }
        });
        
        // Add the table to a scroll pane
        JScrollPane scrollPane = UIFactory.createScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Information label (left side)
        JLabel infoLabel = new JLabel("Double-click a row to view customer details");
        infoLabel.setFont(UIFactory.SMALL_FONT);
        infoLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
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
        
        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);
        
        // Register button actions
        registerButtonActions();
        
        return panel;
    }
    
    private void registerButtonActions() {
        // Add button action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null) {
                    callback.onAddCustomer();
                }
            }
        });
        
        // Edit button action
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = customerTable.convertRowIndexToModel(selectedRow);
                    Customer selectedCustomer = getCustomerAtRow(selectedRow);
                    if (selectedCustomer != null) {
                        callback.onEditCustomer(selectedCustomer);
                    }
                }
            }
        });
        
        // Delete button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = customerTable.convertRowIndexToModel(selectedRow);
                    Customer selectedCustomer = getCustomerAtRow(selectedRow);
                    if (selectedCustomer != null) {
                        int confirm = JOptionPane.showConfirmDialog(
                            CustomerListView.this,
                            "Are you sure you want to delete customer: " + selectedCustomer.getFullName() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteCustomer(selectedCustomer);
                        }
                    }
                }
            }
        });
        
        // View Orders button action
        viewOrdersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = customerTable.convertRowIndexToModel(selectedRow);
                    Customer selectedCustomer = getCustomerAtRow(selectedRow);
                    if (selectedCustomer != null) {
                        callback.onViewCustomerOrders(selectedCustomer);
                    }
                }
            }
        });
        
        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
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
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getRegistrationDate() != null ? customer.getRegistrationDate().format(dateFormatter) : ""
            };
            tableModel.addRow(rowData);
        }
        
        // Reset selection and filters
        customerTable.clearSelection();
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        viewOrdersButton.setEnabled(false);
        
        // Apply any current filter
        applyFilter();
    }
    
    /**
     * Applies search and filter criteria to the table
     */
    private void applyFilter() {
        RowFilter<DefaultTableModel, Object> filter = null;
        
        // Get search text
        String searchText = searchField.getText().trim().toLowerCase();
        
        // Get filter selection
        String filterSelection = (String) filterComboBox.getSelectedItem();
        
        // Combined filter for search text and filter selection
        if (!searchText.isEmpty() || !"All Customers".equals(filterSelection)) {
            filter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    boolean matchesSearch = true;
                    boolean matchesFilter = true;
                    
                    // Apply search text filter
                    if (!searchText.isEmpty()) {
                        matchesSearch = false;
                        for (int i = 1; i < entry.getValueCount(); i++) { // Skip ID column
                            Object value = entry.getValue(i);
                            if (value != null && value.toString().toLowerCase().contains(searchText)) {
                                matchesSearch = true;
                                break;
                            }
                        }
                    }
                    
                    // Apply selection filter
                    if (!"All Customers".equals(filterSelection)) {
                        if ("Recent Registrations".equals(filterSelection)) {
                            // Example: Filter for registrations in the last 30 days
                            String dateString = entry.getStringValue(5); // Registration date column
                            if (dateString == null || dateString.isEmpty()) {
                                matchesFilter = false;
                            } else {
                                LocalDate registrationDate = LocalDate.parse(dateString, dateFormatter);
                                LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                                matchesFilter = !registrationDate.isBefore(thirtyDaysAgo);
                            }
                        } else if ("Inactive Customers".equals(filterSelection)) {
                            // This would be implemented according to business logic
                            // For this example, we'll assume there are no inactive customers
                            matchesFilter = false;
                        }
                    }
                    
                    return matchesSearch && matchesFilter;
                }
            };
        }
        
        tableSorter.setRowFilter(filter);
    }
    
    /**
     * Gets the Customer object corresponding to a specific table row
     * 
     * @param modelRow Row index in the table model
     * @return The Customer object or null if not found
     */
    private Customer getCustomerAtRow(int modelRow) {
        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
            int customerId = (int) tableModel.getValueAt(modelRow, 0);
            for (Customer customer : customerList) {
                if (customer.getId() == customerId) {
                    return customer;
                }
            }
        }
        return null;
    }
    
    /**
     * Deletes a customer from the database
     * 
     * @param customer The customer to delete
     */
    private void deleteCustomer(Customer customer) {
        try {
            boolean result = customerDao.deleteCustomer(customer.getId()) > 0;
            if (result) {
                customerList.removeIf(c -> c.getId() == customer.getId());
                refreshTableData();
                JOptionPane.showMessageDialog(this,
                    "Customer deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (callback != null) {
                    callback.onDeleteCustomer(customer);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete customer. It may be referenced by other records.",
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error deleting customer: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Updates the customer list and refreshes the table
     * 
     * @param customers New list of customers
     */
    public void updateCustomers(List<Customer> customers) {
        this.customerList = customers != null ? customers : new ArrayList<>();
        refreshTableData();
    }
    
    /**
     * Adds a customer to the list and refreshes the table
     * 
     * @param customer Customer to add
     */
    public void addCustomer(Customer customer) {
        if (customer != null && customer.getId() > 0) {
            this.customerList.add(customer);
            refreshTableData();
        }
    }
    
    /**
     * Updates a customer in the list and refreshes the table
     * 
     * @param customer Customer to update
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
        }
    }
    
    /**
     * Removes a customer from the list and refreshes the table
     * 
     * @param customer Customer to remove
     */
    public void removeCustomer(Customer customer) {
        if (customer != null) {
            customerList.removeIf(c -> c.getId() == customer.getId());
            refreshTableData();
        }
    }
}