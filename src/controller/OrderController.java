package controller;

import model.Customer;
import model.Invoice;
import model.Order;
import service.CustomerService;
import service.InvoiceService;
import service.OrderService;
import ui.DialogFactory;
import ui.order.OrderDetailsView;
import ui.order.OrderFormView;
import ui.order.OrderListView;
import util.LogUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.List;

/**
 * RMI-based Controller for Order module operations.
 * Manages interactions between the order views and the remote order service.
 */
public class OrderController {
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Remote services
    private OrderService orderService;
    private CustomerService customerService;
    private InvoiceService invoiceService;
    
    // Views
    private OrderListView listView;
    private Component parentComponent;
    
    /**
     * Constructor
     * 
     * @param parentComponent Parent component for dialogs
     */
    public OrderController(Component parentComponent) {
        this.parentComponent = parentComponent;
        
        // Initialize RMI connections
        initializeRMIConnection();
        
        // Initialize the list view
        initializeListView();
    }
    
    /**
     * Initializes the RMI connections to the server
     */
    private void initializeRMIConnection() {
        try {
            LogUtil.info("Connecting to Order services at " + RMI_HOST + ":" + RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            orderService = (OrderService) registry.lookup("orderService");
            customerService = (CustomerService) registry.lookup("customerService");
            invoiceService = (InvoiceService) registry.lookup("invoiceService");
            LogUtil.info("Successfully connected to Order, Customer, and Invoice services");
        } catch (Exception e) {
            LogUtil.error("Failed to connect to Order services", e);
            showConnectionError();
        }
    }
    
    /**
     * Shows connection error dialog
     */
    private void showConnectionError() {
        JOptionPane.showMessageDialog(
            parentComponent,
            "Failed to connect to the Order Service.\nPlease ensure the server is running and try again.",
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Gets the order list view
     * 
     * @return The order list view
     */
    public OrderListView getListView() {
        return listView;
    }
    
    /**
     * Initializes the order list view with callbacks
     */
    private void initializeListView() {
        listView = new OrderListView(new OrderListView.OrderListCallback() {
            @Override
            public void onAddOrder() {
                showAddOrderDialog();
            }
            
            @Override
            public void onEditOrder(Order order) {
                showEditOrderDialog(order);
            }
            
            @Override
            public void onDeleteOrder(Order order) {
                deleteOrder(order);
            }
            
            @Override
            public void onViewOrderDetails(Order order) {
                showOrderDetailsDialog(order);
            }
            
            @Override
            public void onCreateInvoice(Order order) {
                createInvoiceForOrder(order);
            }
        });
    }
    
    /**
     * Shows the add order dialog
     */
    private void showAddOrderDialog() {
        final OrderFormView[] formView = new OrderFormView[1];
        formView[0] = new OrderFormView(new OrderFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Order order) {
                try {
                    // Validate order data
                    if (!validateOrder(order)) {
                        return;
                    }
                    
                    // Check if order ID already exists
                    if (orderService.orderIdExists(order.getOrderId())) {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Order ID already exists. Please use a different ID.",
                            "Duplicate Order ID",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    
                    // Create the order using RMI service
                    Order createdOrder = orderService.createOrder(order);
                    
                    if (createdOrder != null) {
                        // Update the list view with the new order
                        listView.addOrder(createdOrder);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Order created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to create order. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error creating order", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error creating order: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            
            @Override
            public void onCancel() {
                // Close the dialog
                Window window = SwingUtilities.getWindowAncestor(formView[0]);
                if (window instanceof JDialog) {
                    ((JDialog) window).dispose();
                }
            }
        });
        
        // Create and show the dialog
        JDialog dialog = DialogFactory.createFormDialog(
            parentComponent,
            "Add New Order",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            800,
            600
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Shows the edit order dialog
     * 
     * @param order The order to edit
     */
    private void showEditOrderDialog(Order order) {
        OrderFormView[] formView = new OrderFormView[1];
        formView[0] = new OrderFormView(order, new OrderFormView.FormSubmissionCallback() {
            @Override
            public void onSave(Order updatedOrder) {
                try {
                    // Validate order data
                    if (!validateOrder(updatedOrder)) {
                        return;
                    }
                    
                    // Update the order using RMI service
                    Order savedOrder = orderService.updateOrder(updatedOrder);
                    
                    if (savedOrder != null) {
                        // Update the list view with the modified order
                        listView.updateOrder(savedOrder);
                        
                        // Close the dialog
                        Window window = SwingUtilities.getWindowAncestor(formView[0]);
                        if (window instanceof JDialog) {
                            ((JDialog) window).dispose();
                        }
                        
                        JOptionPane.showMessageDialog(
                            parentComponent,
                            "Order updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            formView[0],
                            "Failed to update order. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    LogUtil.error("Error updating order", e);
                    JOptionPane.showMessageDialog(
                        formView[0],
                        "Error updating order: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            
            @Override
            public void onCancel() {
                // Close the dialog
                Window window = SwingUtilities.getWindowAncestor(formView[0]);
                if (window instanceof JDialog) {
                    ((JDialog) window).dispose();
                }
            }
        });
        
        // Create and show the dialog
        JDialog dialog = DialogFactory.createFormDialog(
            parentComponent,
            "Edit Order",
            formView[0],
            null, // onSave handled in callback
            null, // onCancel handled in callback
            800,
            600
        );
        
        dialog.setVisible(true);
    }
    
    /**
     * Deletes an order
     * 
     * @param order The order to delete
     */
    private void deleteOrder(Order order) {
        int result = JOptionPane.showConfirmDialog(
            parentComponent,
            "Are you sure you want to delete order: " + order.getOrderId() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                Order deletedOrder = orderService.deleteOrder(order);
                
                if (deletedOrder != null) {
                    // Remove from list view
                    listView.remove(order.getId());
                    
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Order deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Failed to delete order. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                
            } catch (Exception e) {
                LogUtil.error("Error deleting order", e);
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Error deleting order: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Shows the order details dialog
     * 
     * @param order The order to view
     */
    private void showOrderDetailsDialog(Order order) {
        try {
            // First, load the order with all details
            Order orderWithDetails = orderService.getOrderWithDetails(order.getId());
            if (orderWithDetails == null) {
                orderWithDetails = order;
            }
            
            // Create the details view with the order that has complete information
            final OrderDetailsView[] detailsView = new OrderDetailsView[1];
            detailsView[0] = new OrderDetailsView(orderWithDetails, 
                    new OrderDetailsView.DetailsViewCallback() {
                        @Override
                        public void onEditOrder(Order orderToEdit) {
                            // Close the details dialog
                            Window window = SwingUtilities.getWindowAncestor(detailsView[0]);
                            if (window instanceof JDialog) {
                                ((JDialog) window).dispose();
                            }
                            
                            // Show the edit dialog
                            showEditOrderDialog(orderToEdit);
                        }
                        
                        @Override
                        public void onViewCustomer(Customer customer) {
                            // Redirect to customer details view
                            JOptionPane.showMessageDialog(
                                    parentComponent,
                                    "Would navigate to Customer Details view for: " + customer.getFullName(),
                                    "View Customer",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                        
                        @Override
                        public void onCreateInvoice(Order order) {
                            // Close the details dialog
                            Window window = SwingUtilities.getWindowAncestor(detailsView[0]);
                            if (window instanceof JDialog) {
                                ((JDialog) window).dispose();
                            }
                            
                            // Create invoice for the order
                            createInvoiceForOrder(order);
                        }
                        
                        @Override
                        public void onClose() {
                            // Close the dialog
                            Window window = SwingUtilities.getWindowAncestor(detailsView[0]);
                            if (window instanceof JDialog) {
                                ((JDialog) window).dispose();
                            }
                        }
                    });
            
            // Create and show the dialog
            JDialog dialog = DialogFactory.createDetailsDialog(
                parentComponent,
                "Order Details",
                detailsView[0],
                null, // onEdit handled in callback
                800,
                650
            );
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            LogUtil.error("Error loading order details", e);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading order details: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Creates an invoice for the selected order
     * 
     * @param order The order to create an invoice for
     */
    private void createInvoiceForOrder(Order order) {
        // Only allow invoices for delivered orders
        if (!Order.STATUS_DELIVERED.equals(order.getStatus())) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Invoices can only be created for delivered orders.",
                "Cannot Create Invoice",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            // Check if order already has invoices
            List<Invoice> existingInvoices = invoiceService.findInvoicesByOrder(order);
            if (existingInvoices != null && !existingInvoices.isEmpty()) {
                // Ask if user wants to create another invoice
                int response = JOptionPane.showConfirmDialog(
                    parentComponent,
                    "This order already has " + existingInvoices.size() + " invoice(s).\n" +
                    "Would you like to create another invoice?",
                    "Invoice Exists",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (response != JOptionPane.YES_OPTION) {
                    // Show the existing invoice instead
                    JOptionPane.showMessageDialog(
                        parentComponent,
                        "Would display existing invoice details here.",
                        "Existing Invoice",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }
            }
            
            // Create a new invoice
            Invoice invoice = new Invoice();
            
            // Generate invoice number (could be more sophisticated in real app)
            String invoiceNumber = "INV-" + LocalDate.now().getYear() + "-" + 
                                 String.format("%04d", (int)(Math.random() * 10000));
            
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setOrder(order);
            invoice.setAmount(order.getTotalAmount());
            invoice.setIssueDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(30)); // Due in 30 days
            invoice.setStatus(Invoice.STATUS_ISSUED);
            
            Invoice createdInvoice = invoiceService.createInvoice(invoice);
            
            if (createdInvoice != null) {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Invoice created successfully.\nInvoice Number: " + invoice.getInvoiceNumber(),
                    "Invoice Created",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                // Refresh the order details
                showOrderDetailsDialog(order);
            } else {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Failed to create invoice. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            
        } catch (Exception ex) {
            LogUtil.error("Error creating invoice", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error creating invoice: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Validates order data
     * 
     * @param order The order to validate
     * @return true if valid, false otherwise
     */
    private boolean validateOrder(Order order) {
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Order ID is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (order.getCustomer() == null) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Customer is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Order must have at least one item.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        if (order.getTotalAmount() == null || order.getTotalAmount().signum() <= 0) {
            JOptionPane.showMessageDialog(
                parentComponent,
                "Order total must be greater than zero.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        return true;
    }
    
    /**
     * Refreshes the order list with data from the server
     */
    public void refreshOrderList() {
        try {
            List<Order> orders = orderService.findAllOrders();
            if (orders != null) {
                listView.updateOrders(orders);
                LogUtil.info("Order list refreshed with " + orders.size() + " orders");
            } else {
                LogUtil.warn("Received null order list from server");
            }
        } catch (Exception ex) {
            LogUtil.error("Error loading orders", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error loading orders: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for orders by customer
     * 
     * @param customer The customer to search for
     */
    public void searchOrdersByCustomer(Customer customer) {
        try {
            List<Order> orders = orderService.findOrdersByCustomer(customer);
            if (orders != null) {
                listView.updateOrders(orders);
                LogUtil.info("Customer search completed, found " + orders.size() + " orders");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching orders by customer", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching orders by customer: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for orders by status
     * 
     * @param status The status to search for
     */
    public void searchOrdersByStatus(String status) {
        try {
            List<Order> orders = orderService.findOrdersByStatus(status);
            if (orders != null) {
                listView.updateOrders(orders);
                LogUtil.info("Status search completed, found " + orders.size() + " orders");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching orders by status", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching orders by status: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Searches for orders by date range
     * 
     * @param startDate Start date
     * @param endDate End date
     */
    public void searchOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            List<Order> orders = orderService.findOrdersByDateRange(startDate, endDate);
            if (orders != null) {
                listView.updateOrders(orders);
                LogUtil.info("Date range search completed, found " + orders.size() + " orders");
            }
        } catch (Exception ex) {
            LogUtil.error("Error searching orders by date range", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error searching orders by date range: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Updates order status
     * 
     * @param order The order to update
     * @param newStatus The new status
     */
    public void updateOrderStatus(Order order, String newStatus) {
        try {
            int rowsAffected = orderService.updateOrderStatus(order.getId(), newStatus);
            if (rowsAffected > 0) {
                order.setStatus(newStatus);
                listView.updateOrder(order);
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Order status updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Failed to update order status.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            LogUtil.error("Error updating order status", ex);
            JOptionPane.showMessageDialog(
                parentComponent,
                "Error updating order status: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Gets all customers for order forms
     * 
     * @return List of customers
     */
    public List<Customer> getAllCustomers() {
        try {
            return customerService.findAllCustomers();
        } catch (Exception ex) {
            LogUtil.error("Error getting customers", ex);
            return null;
        }
    }
    
    /**
     * Gets the order service instance
     * 
     * @return The OrderService
     */
    public OrderService getOrderService() {
        return orderService;
    }
    
    /**
     * Gets the customer service instance
     * 
     * @return The CustomerService
     */
    public CustomerService getCustomerService() {
        return customerService;
    }
    
    /**
     * Gets the invoice service instance
     * 
     * @return The InvoiceService
     */
    public InvoiceService getInvoiceService() {
        return invoiceService;
    }
    
    /**
     * Checks if the connection to the server is available
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return orderService != null && customerService != null && invoiceService != null;
    }
    
    /**
     * Reconnects to the RMI server
     */
    public void reconnect() {
        initializeRMIConnection();
    }
}