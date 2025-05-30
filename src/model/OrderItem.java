package model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents an item in an order in the business management system.
 * Contains the relationship between orders and products.
 */
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    
    private Order order;
    
    private Product product;
    
    private int quantity;
    
    private BigDecimal unitPrice;
    
    /**
     * Default constructor
     */
    public OrderItem() {
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param product The product being ordered
     * @param quantity The quantity ordered
     */
    public OrderItem(Product product, int quantity) {
        this();
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param order The order this item belongs to
     * @param product The product being ordered
     * @param quantity Quantity ordered
     * @param unitPrice Price per unit at time of order
     */
    public OrderItem(int id, Order order, Product product, int quantity, BigDecimal unitPrice) {
        this();
        this.id = id;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
    
    /**
     * Gets the order ID for compatibility
     * 
     * @return Order ID or 0 if no order
     */
    public int getOrderId() {
        return order != null ? order.getId() : 0;
    }
    
    /**
     * Sets order by ID (for compatibility)
     * Note: This doesn't actually set the order, just for interface compatibility
     * 
     * @param orderId The order ID
     */
    public void setOrderId(int orderId) {
        // This method is kept for compatibility but doesn't do anything
        // Order should be set using setOrder(Order order)
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
    
    /**
     * Gets the product ID for compatibility
     * 
     * @return Product ID or 0 if no product
     */
    public int getProductId() {
        return product != null ? product.getId() : 0;
    }
    
    /**
     * Sets product by ID (for compatibility)
     * Note: This doesn't actually set the product, just for interface compatibility
     * 
     * @param productId The product ID
     */
    public void setProductId(int productId) {
        // This method is kept for compatibility but doesn't do anything
        // Product should be set using setProduct(Product product)
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        // Update order total if order is set
        if (order != null) {
            order.recalculateTotal();
        }
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        // Update order total if order is set
        if (order != null) {
            order.recalculateTotal();
        }
    }
    
    /**
     * Calculate the subtotal for this order item
     * 
     * @return The quantity multiplied by the unit price
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
    
    @Override
    public String toString() {
        return "OrderItem [id=" + id + ", productId=" + getProductId() + ", quantity=" + quantity + 
               ", unitPrice=" + unitPrice + ", subtotal=" + getSubtotal() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItem orderItem = (OrderItem) obj;
        return id == orderItem.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}