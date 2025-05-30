package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order in the business management system.
 * Contains order details and relationships to customers, order items, and invoices.
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    
    private String orderId;
    
    private Customer customer;
    
    private LocalDate orderDate;
    
    private BigDecimal totalAmount;
    
    private String status;
    
    private String paymentMethod;
    
    private List<OrderItem> orderItems = new ArrayList<>();
    
    private List<Invoice> invoices = new ArrayList<>();
    
    // Order status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_PROCESSING = "Processing";
    public static final String STATUS_SHIPPED = "Shipped";
    public static final String STATUS_DELIVERED = "Delivered";
    public static final String STATUS_CANCELLED = "Cancelled";
    
    /**
     * Default constructor
     */
    public Order() {
        this.orderDate = LocalDate.now();
        this.totalAmount = BigDecimal.ZERO;
        this.status = STATUS_PENDING;
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param orderId Unique order identifier
     * @param customer Customer who placed the order
     */
    public Order(String orderId, Customer customer) {
        this();
        this.orderId = orderId;
        this.customer = customer;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param orderId Unique order identifier
     * @param customer Customer who placed the order
     * @param orderDate Date the order was placed
     * @param totalAmount Total order amount
     * @param status Order status
     * @param paymentMethod Payment method used
     */
    public Order(int id, String orderId, Customer customer, LocalDate orderDate, 
                 BigDecimal totalAmount, String status, String paymentMethod) {
        this();
        this.id = id;
        this.orderId = orderId;
        this.customer = customer;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    /**
     * Gets the customer ID for compatibility
     * 
     * @return Customer ID or 0 if no customer
     */
    public int getCustomerId() {
        return customer != null ? customer.getId() : 0;
    }
    
    /**
     * Sets customer by ID (for compatibility)
     * Note: This doesn't actually set the customer, just for interface compatibility
     * 
     * @param customerId The customer ID
     */
    public void setCustomerId(int customerId) {
        // This method is kept for compatibility but doesn't do anything
        // Customer should be set using setCustomer(Customer customer)
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }
    
    /**
     * Adds an item to the order and updates the total amount
     * 
     * @param item The order item to add
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
        recalculateTotal();
    }
    
    /**
     * Removes an item from the order and updates the total amount
     * 
     * @param item The order item to remove
     * @return true if the item was removed
     */
    public boolean removeOrderItem(OrderItem item) {
        boolean removed = orderItems.remove(item);
        if (removed) {
            recalculateTotal();
        }
        return removed;
    }
    
    /**
     * Recalculates the total amount of the order based on the order items
     */
    public void recalculateTotal() {
        this.totalAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            this.totalAmount = this.totalAmount.add(itemTotal);
        }
    }
    
    /**
     * Adds an invoice to this order
     * 
     * @param invoice The invoice to add
     */
    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoice.setOrder(this);
    }
    
    /**
     * Checks if the order has been fully invoiced
     * 
     * @return true if the sum of invoice amounts equals the order total
     */
    public boolean isFullyInvoiced() {
        if (invoices.isEmpty()) {
            return false;
        }
        
        BigDecimal invoicedAmount = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            invoicedAmount = invoicedAmount.add(invoice.getAmount());
        }
        
        return invoicedAmount.compareTo(totalAmount) >= 0;
    }
    
    @Override
    public String toString() {
        return "Order [id=" + id + ", orderId=" + orderId + ", customerId=" + getCustomerId() + 
               ", date=" + orderDate + ", total=" + totalAmount + ", status=" + status + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Order order = (Order) obj;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}