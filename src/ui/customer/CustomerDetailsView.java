package ui.customer;

import model.Customer;
import model.Order;
import dao.CustomerDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ui.UIFactory;

/**
 * Detail view for showing customer information and order history.
 * Displays complete customer details and their related orders.
 */
public class CustomerDetailsView extends JPanel {
    // UI Components
    private JLabel nameValueLabel;
    private JLabel customerIdValueLabel;
    private JLabel emailValueLabel;
    private JLabel phoneValueLabel;
    private JLabel registrationDateValueLabel;
    private JTextArea addressValueArea;
    
    // Orders table
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Customer data
    private Customer customer;
    private CustomerDao customerDao;
    
    // Callback for view actions
    private DetailsViewCallback callback;
    
    /**
     * Interface for view actions callback
     */
    public interface DetailsViewCallback {
        void onEditCustomer(Customer customer);
        void onViewOrders(Customer customer);
        void onClose();
    }
    
    /**
     * Constructor
     * 
     * @param customer The customer to display
     * @param callback Callback for view actions
     */
    public CustomerDetailsView(Customer customer, DetailsViewCallback callback) {
        this.customer = customer;
        this.callback = callback;
        this.customerDao = new CustomerDao();
        
        // Load customer with orders if needed
        if (customer != null && (customer.getOrders() == null || customer.getOrders().isEmpty())) {
            this.customer = customerDao.getCustomerWithOrders(customer.getId());
        }
        
        initializeUI();
        populateData();
    }
    
    private void initializeUI() {
        // Set up the panel
        setLayout(new BorderLayout(0, 15));
        setBackground(UIFactory.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create a split panel for details and orders
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.4); // 40% to details, 60% to orders
        
        // Create the details panel
        JPanel detailsPanel = createDetailsPanel();
        splitPane.setTopComponent(detailsPanel);
        
        // Create the orders panel
        JPanel ordersPanel = createOrdersPanel();
        splitPane.setBottomComponent(ordersPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Create actions panel
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        // Create a header with customer name
        JPanel panel = UIFactory.createModuleHeaderPanel("Customer Details");
        
        if (customer != null) {
            // Add customer name to header
            JLabel customerNameLabel = new JLabel(customer.getFullName());
            customerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            customerNameLabel.setForeground(UIFactory.PRIMARY_COLOR);
            panel.add(customerNameLabel, BorderLayout.EAST);
        }
        
        return panel;
    }
    
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title
        JLabel sectionTitle = new JLabel("Customer Information");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        panel.add(sectionTitle, BorderLayout.NORTH);
        
        // Create details grid
        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Customer ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Customer ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        customerIdValueLabel = createValueLabel("");
        detailsGrid.add(customerIdValueLabel, gbc);
        
        // Name
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Name:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        nameValueLabel = createValueLabel("");
        detailsGrid.add(nameValueLabel, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        emailValueLabel = createValueLabel("");
        detailsGrid.add(emailValueLabel, gbc);
        
        // Phone
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Phone:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        phoneValueLabel = createValueLabel("");
        detailsGrid.add(phoneValueLabel, gbc);
        
        // Registration Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        detailsGrid.add(createLabel("Registration Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        registrationDateValueLabel = createValueLabel("");
        detailsGrid.add(registrationDateValueLabel, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        detailsGrid.add(createLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.9;
        gbc.fill = GridBagConstraints.BOTH;
        addressValueArea = new JTextArea(4, 20);
        addressValueArea.setEditable(false);
        addressValueArea.setLineWrap(true);
        addressValueArea.setWrapStyleWord(true);
        addressValueArea.setBackground(new Color(0xF8F8F8));
        addressValueArea.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        JScrollPane addressScrollPane = new JScrollPane(addressValueArea);
        addressScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailsGrid.add(addressScrollPane, gbc);
        
        panel.add(detailsGrid, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create section title with count
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel sectionTitle = new JLabel("Order History");
        sectionTitle.setFont(UIFactory.HEADER_FONT);
        headerPanel.add(sectionTitle, BorderLayout.WEST);
        
        // Add view all orders button
        JButton viewOrdersButton = UIFactory.createSecondaryButton("View All Orders");
        viewOrdersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null && customer != null) {
                    callback.onViewOrders(customer);
                }
            }
        });
        
        headerPanel.add(viewOrdersButton, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create orders table
        String[] columnNames = {"Order ID", "Date", "Total", "Status"};
        ordersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        ordersTable = UIFactory.createStyledTable(ordersTableModel);
        
        JScrollPane scrollPane = UIFactory.createScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        
        JButton closeButton = UIFactory.createSecondaryButton("Close");
        JButton editButton = UIFactory.createWarningButton("Edit Customer");
        
        panel.add(closeButton);
        panel.add(editButton);
        
        // Add button actions
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null) {
                    callback.onClose();
                }
            }
        });
        
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (callback != null && customer != null) {
                    callback.onEditCustomer(customer);
                }
            }
        });
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFactory.BODY_FONT);
        label.setForeground(UIFactory.MEDIUM_GRAY);
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFactory.BODY_FONT);
        return label;
    }
    
    private void populateData() {
        if (customer == null) {
            return;
        }
        
        // Set customer details
        customerIdValueLabel.setText(customer.getCustomerId());
        nameValueLabel.setText(customer.getFullName());
        emailValueLabel.setText(customer.getEmail());
        phoneValueLabel.setText(customer.getPhone() != null ? customer.getPhone() : "");
        registrationDateValueLabel.setText(customer.getRegistrationDate() != null ? 
                                         customer.getRegistrationDate().format(dateFormatter) : "");
        addressValueArea.setText(customer.getAddress() != null ? customer.getAddress() : "");
        
        // Populate orders table
        ordersTableModel.setRowCount(0);
        
        List<Order> orders = customer.getOrders();
        if (orders != null) {
            for (Order order : orders) {
                Object[] rowData = {
                    order.getOrderId(),
                    order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "",
                    order.getTotalAmount(),
                    order.getStatus()
                };
                ordersTableModel.addRow(rowData);
            }
        }
    }
}