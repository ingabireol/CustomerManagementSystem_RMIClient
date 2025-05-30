package ui.supplier;

import model.Supplier;
import dao.SupplierDao;

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
 * List view for displaying and managing suppliers.
 * Provides functionality for searching, filtering, and performing CRUD operations.
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
    
    // Supplier data and DAO
    private List<Supplier> supplierList;
    private SupplierDao supplierDao;
    
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
        this.supplierDao = new SupplierDao();
        this.supplierList = new ArrayList<>();
        
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
     * Loads supplier data from the database
     */
    private void loadData() {
        try {
            this.supplierList = supplierDao.findAllSuppliers();
            refreshTableData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading supplier data: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
     * Refreshes the table data with the current supplier list
     */
    private void refreshTableData() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Populate the table with data
        for (Supplier supplier : supplierList) {
            Object[] rowData = {
                supplier.getId(),
                supplier.getSupplierCode(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone()
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
        
        // Get search text
        String searchText = searchField.getText().trim().toLowerCase();
        
        // Get filter selection
        String filterSelection = (String) filterComboBox.getSelectedItem();
        
        // Combined filter for search text and filter selection
        if (!searchText.isEmpty() || !"All Suppliers".equals(filterSelection)) {
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
                    if (!"All Suppliers".equals(filterSelection)) {
                        // In a real application, this would filter based on an active/inactive field
                        // For this example, we'll assume all suppliers are active
                        if ("Inactive Suppliers".equals(filterSelection)) {
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
     * Deletes a supplier from the database
     * 
     * @param supplier The supplier to delete
     */
    private void deleteSupplier(Supplier supplier) {
        try {
            int result = supplierDao.deleteSupplier(supplier.getId());
            if (result > 0) {
                supplierList.removeIf(s -> s.getId() == supplier.getId());
                refreshTableData();
                JOptionPane.showMessageDialog(this,
                    "Supplier deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (callback != null) {
                    callback.onDeleteSupplier(supplier);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete supplier. It may be referenced by other records.",
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error deleting supplier: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
}