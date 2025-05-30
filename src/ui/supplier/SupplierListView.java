package ui.supplier;

import model.Supplier;
import util.LogUtil;
import util.RMIConnectionManager;
import service.SupplierService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import ui.UIFactory;

/**
 * RMI-based list view for displaying and managing suppliers.
 * Provides functionality for searching, filtering, and performing CRUD operations using remote services.
 */
public class SupplierListView extends JPanel {
    // Table components
    private JTable supplierTable;
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
    private JButton viewProductsButton;
    
    // Supplier data
    private List<Supplier> supplierList;
    
    // RMI Services
    private SupplierService supplierService;
    
    // Callback for list actions
    private SupplierListCallback callback;
    
    /**
     * Interface for supplier list actions
     */
    public interface SupplierListCallback {
        void onAddSupplier();
        void onEditSupplier(Supplier supplier);
        void onDeleteSupplier(Supplier supplier);
        void onViewSupplierDetails(Supplier supplier);
        void onViewSupplierProducts(Supplier supplier);
    }
    
    /**
     * Constructor
     */
    public SupplierListView(SupplierListCallback callback) {
        this.callback = callback;
        this.supplierList = new ArrayList<>();
        
        // Initialize RMI service
        initializeRMIServices();
        initializeUI();
        loadData();
    }
    
    /**
     * Initializes RMI service connections
     */
    private void initializeRMIServices() {
        try {
            LogUtil.info("Initializing SupplierService for list view");
            supplierService = RMIConnectionManager.getSupplierService();
            
            if (supplierService == null) {
                LogUtil.error("Failed to get SupplierService from RMI");
                showConnectionError();
            } else {
                LogUtil.info("Successfully connected to SupplierService");
            }
        } catch (Exception e) {
            LogUtil.error("Error connecting to SupplierService", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error message
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            this,
            "Failed to connect to the server.\nSome features may not work properly.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
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
     * Loads supplier data from the server using RMI
     */
    private void loadData() {
        SwingWorker<List<Supplier>, Void> worker = new SwingWorker<List<Supplier>, Void>() {
            @Override
            protected List<Supplier> doInBackground() throws Exception {
                if (supplierService != null) {
                    LogUtil.info("Loading suppliers from server via RMI");
                    return supplierService.findAllSuppliers();
                }
                return new ArrayList<>();
            }
            
            @Override
            protected void done() {
                try {
                    List<Supplier> suppliers = get();
                    if (suppliers != null) {
                        supplierList = suppliers;
                        refreshTableData();
                        LogUtil.info("Loaded " + suppliers.size() + " suppliers from server");
                    } else {
                        LogUtil.warn("Received null supplier list from server");
                        supplierList = new ArrayList<>();
                        refreshTableData();
                    }
                } catch (Exception ex) {
                    LogUtil.error("Error loading supplier data from server", ex);
                    JOptionPane.showMessageDialog(SupplierListView.this,
                        "Error loading supplier data: " + ex.getMessage(),
                        "Server Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    // Initialize with empty list
                    supplierList = new ArrayList<>();
                    refreshTableData();
                }
            }
        };
        
        worker.execute();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = UIFactory.createModuleHeaderPanel("Suppliers");
        
        // Create search and filter section on the right side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        // Filter combo box
        String[] filterOptions = {"All Suppliers", "Active Suppliers", "Inactive Suppliers"};
        filterComboBox = UIFactory.createComboBox(filterOptions);
        filterComboBox.setPreferredSize(new Dimension(150, 30));
        
        // Search field
        searchField = UIFactory.createSearchField("Search suppliers...", 200);
        
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
        
        // Add filter change action
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
        
        // Add enter key listener to search field
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
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
        String[] columnNames = {"ID", "Supplier Code", "Name", "Contact Person", "Email", "Phone"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        // Create and set up the table
        supplierTable = UIFactory.createStyledTable(tableModel);
        
        // Add row sorting
        tableSorter = new TableRowSorter<>(tableModel);
        supplierTable.setRowSorter(tableSorter);
        
        // Set column widths
        supplierTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID column
        supplierTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Supplier Code
        supplierTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Name
        supplierTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Contact Person
        
        // Add double-click listener for viewing details
        supplierTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = supplierTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        selectedRow = supplierTable.convertRowIndexToModel(selectedRow);
                        Supplier selectedSupplier = getSupplierAtRow(selectedRow);
                        if (selectedSupplier != null && callback != null) {
                            callback.onViewSupplierDetails(selectedSupplier);
                        }
                    }
                }
            }
        });
        
        // Add selection listener to enable/disable action buttons
        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = supplierTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                viewProductsButton.setEnabled(hasSelection);
            }
        });
        
        // Add the table to a scroll pane
        JScrollPane scrollPane = UIFactory.createScrollPane(supplierTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Information label (left side)
        JLabel infoLabel = new JLabel("Double-click a row to view supplier details");
        infoLabel.setFont(UIFactory.SMALL_FONT);
        infoLabel.setForeground(UIFactory.MEDIUM_GRAY);
        
        // Action buttons (right side)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        
        refreshButton = UIFactory.createSecondaryButton("Refresh");
        
        viewProductsButton = UIFactory.createSecondaryButton("View Products");
        viewProductsButton.setEnabled(false); // Disabled until selection
        
        deleteButton = UIFactory.createDangerButton("Delete");
        deleteButton.setEnabled(false); // Disabled until selection
        
        editButton = UIFactory.createWarningButton("Edit");
        editButton.setEnabled(false); // Disabled until selection
        
        addButton = UIFactory.createPrimaryButton("Add New");
        
        // Add buttons to panel
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(viewProductsButton);
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
                    callback.onAddSupplier();
                }
            }
        });
        
        // Edit button action
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = supplierTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = supplierTable.convertRowIndexToModel(selectedRow);
                    Supplier selectedSupplier = getSupplierAtRow(selectedRow);
                    if (selectedSupplier != null) {
                        callback.onEditSupplier(selectedSupplier);
                    }
                }
            }
        });
        
        // Delete button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = supplierTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = supplierTable.convertRowIndexToModel(selectedRow);
                    Supplier selectedSupplier = getSupplierAtRow(selectedRow);
                    if (selectedSupplier != null) {
                        int confirm = JOptionPane.showConfirmDialog(
                            SupplierListView.this,
                            "Are you sure you want to delete supplier: " + selectedSupplier.getName() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteSupplier(selectedSupplier);
                        }
                    }
                }
            }
        });
        
        // View Products button action
        viewProductsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = supplierTable.getSelectedRow();
                if (selectedRow >= 0 && callback != null) {
                    selectedRow = supplierTable.convertRowIndexToModel(selectedRow);
                    Supplier selectedSupplier = getSupplierAtRow(selectedRow);
                    if (selectedSupplier != null) {
                        callback.onViewSupplierProducts(selectedSupplier);
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
     * Performs search using RMI service
     */
    private void performSearch() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            // If search is empty, reload all data
            loadData();
            return;
        }
        
        SwingWorker<List<Supplier>, Void> worker = new SwingWorker<List<Supplier>, Void>() {
            @Override
            protected List<Supplier> doInBackground() throws Exception {
                if (supplierService != null) {
                    LogUtil.info("Searching suppliers by name: " + searchText);
                    return supplierService.findSuppliersByName(searchText);
                }
                return new ArrayList<>();
            }
            
            @Override
            protected void done() {
                try {
                    List<Supplier> searchResults = get();
                    if (searchResults != null) {
                        supplierList = searchResults;
                        refreshTableData();
                        LogUtil.info("Search completed, found " + searchResults.size() + " suppliers");
                    } else {
                        LogUtil.warn("Search returned null results");
                        supplierList = new ArrayList<>();
                        refreshTableData();
                    }
                } catch (Exception ex) {
                    LogUtil.error("Error searching suppliers", ex);
                    JOptionPane.showMessageDialog(SupplierListView.this,
                        "Error searching suppliers: " + ex.getMessage(),
                        "Search Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Refreshes the table data with the current supplier list
     */
    private void refreshTableData() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Populate the table with data
        for (Supplier supplier : supplierList) {
            Object[] rowData = {
                supplier.getId(),
                supplier.getSupplierCode() != null ? supplier.getSupplierCode() : "N/A",
                supplier.getName() != null ? supplier.getName() : "N/A",
                supplier.getContactPerson() != null ? supplier.getContactPerson() : "N/A",
                supplier.getEmail() != null ? supplier.getEmail() : "N/A",
                supplier.getPhone() != null ? supplier.getPhone() : "N/A"
            };
            tableModel.addRow(rowData);
        }
        
        // Reset selection and filters
        supplierTable.clearSelection();
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        viewProductsButton.setEnabled(false);
        
        // Apply any current filter
        applyFilter();
    }
    
    /**
     * Applies search and filter criteria to the table
     */
    private void applyFilter() {
        RowFilter<DefaultTableModel, Object> filter = null;
        
        // Get filter selection
        String filterSelection = (String) filterComboBox.getSelectedItem();
        
        // Apply filter based on selection
        if (!"All Suppliers".equals(filterSelection)) {
            filter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    // In a real application, this would filter based on an active/inactive field
                    // For this example, we'll assume all suppliers are active
                    if ("Inactive Suppliers".equals(filterSelection)) {
                        return false; // No inactive suppliers for now
                    }
                    return true; // Show all suppliers for "Active Suppliers"
                }
            };
        }
        
        tableSorter.setRowFilter(filter);
    }
    
    /**
     * Gets the Supplier object corresponding to a specific table row
     * 
     * @param modelRow Row index in the table model
     * @return The Supplier object or null if not found
     */
    private Supplier getSupplierAtRow(int modelRow) {
        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
            int supplierId = (int) tableModel.getValueAt(modelRow, 0);
            for (Supplier supplier : supplierList) {
                if (supplier.getId() == supplierId) {
                    return supplier;
                }
            }
        }
        return null;
    }
    
    /**
     * Deletes a supplier using RMI service
     * 
     * @param supplier The supplier to delete
     */
    private void deleteSupplier(Supplier supplier) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (supplierService != null) {
                    LogUtil.info("Deleting supplier via RMI: " + supplier.getName());
                    Supplier deletedSupplier = supplierService.deleteSupplier(supplier);
                    return deletedSupplier != null;
                }
                return false;
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        supplierList.removeIf(s -> s.getId() == supplier.getId());
                        refreshTableData();
                        
                        JOptionPane.showMessageDialog(SupplierListView.this,
                            "Supplier deleted successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        LogUtil.info("Supplier deleted successfully: " + supplier.getName());
                        
                        if (callback != null) {
                            callback.onDeleteSupplier(supplier);
                        }
                    } else {
                        JOptionPane.showMessageDialog(SupplierListView.this,
                            "Failed to delete supplier. It may be referenced by other records.",
                            "Delete Failed",
                            JOptionPane.ERROR_MESSAGE);
                        LogUtil.warn("Failed to delete supplier: " + supplier.getName());
                    }
                } catch (Exception ex) {
                    LogUtil.error("Error deleting supplier via RMI", ex);
                    JOptionPane.showMessageDialog(SupplierListView.this,
                        "Error deleting supplier: " + ex.getMessage(),
                        "Server Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Updates the supplier list and refreshes the table
     * 
     * @param suppliers New list of suppliers
     */
    public void updateSuppliers(List<Supplier> suppliers) {
        this.supplierList = suppliers != null ? suppliers : new ArrayList<>();
        refreshTableData();
    }
    
    /**
     * Adds a supplier to the list and refreshes the table
     * 
     * @param supplier Supplier to add
     */
    public void addSupplier(Supplier supplier) {
        if (supplier != null && supplier.getId() > 0) {
            this.supplierList.add(supplier);
            refreshTableData();
        }
    }
    
    /**
     * Updates a supplier in the list and refreshes the table
     * 
     * @param supplier Supplier to update
     */
    public void updateSupplier(Supplier supplier) {
        if (supplier != null) {
            for (int i = 0; i < supplierList.size(); i++) {
                if (supplierList.get(i).getId() == supplier.getId()) {
                    supplierList.set(i, supplier);
                    break;
                }
            }
            refreshTableData();
        }
    }
    
    /**
     * Removes a supplier from the list and refreshes the table
     * 
     * @param supplier Supplier to remove
     */
    public void removeSupplier(Supplier supplier) {
        if (supplier != null) {
            supplierList.removeIf(s -> s.getId() == supplier.getId());
            refreshTableData();
        }
    }
    
    /**
     * Gets the current supplier list
     * 
     * @return Current list of suppliers
     */
    public List<Supplier> getSupplierList() {
        return new ArrayList<>(supplierList);
    }
    
    /**
     * Refreshes RMI connection if needed
     */
    public void refreshConnection() {
        initializeRMIServices();
        loadData();
    }
}