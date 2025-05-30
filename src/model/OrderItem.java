package model;

import java.math.BigDecimal;

/**
 * Represents an item in an order in the business management system.
 * Contains the relationship between orders and products.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private Order order;
    private int productId;
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
        this.productId = product.getId();
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param orderId ID of the order
     * @param productId ID of the product
     * @param quantity Quantity ordered
     * @param unitPrice Price per unit at time of order
     */
    public OrderItem(int id, int orderId, int productId, int quantity, BigDecimal unitPrice) {
        this();
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
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

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            this.orderId = order.getId();
        }
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getId();
        }
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
        return "OrderItem [id=" + id + ", productId=" + productId + ", quantity=" + quantity + 
               ", unitPrice=" + unitPrice + ", subtotal=" + getSubtotal() + "]";
    }
}