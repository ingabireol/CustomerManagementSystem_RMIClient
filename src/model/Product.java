package model;

import java.math.BigDecimal;

/**
 * Represents a product in the business management system.
 * Contains product details with inventory tracking.
 */
public class Product {
    private int id;
    private String productCode;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String category;
    private int supplierId;
    private Supplier supplier;
    
    /**
     * Default constructor
     */
    public Product() {
        this.price = BigDecimal.ZERO;
        this.stockQuantity = 0;
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param productCode Unique product code
     * @param name Product name
     * @param price Product price
     * @param stockQuantity Current stock level
     */
    public Product(String productCode, String name, BigDecimal price, int stockQuantity) {
        this();
        this.productCode = productCode;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param productCode Unique product code
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param stockQuantity Current stock level
     * @param category Product category
     * @param supplierId ID of the supplier
     */
    public Product(int id, String productCode, String name, String description, 
                   BigDecimal price, int stockQuantity, String category, int supplierId) {
        this();
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.supplierId = supplierId;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    /**
     * Updates the stock quantity by adding the specified amount
     * 
     * @param quantity Amount to add (can be negative for reduction)
     * @return New stock quantity
     */
    public int updateStock(int quantity) {
        this.stockQuantity += quantity;
        return this.stockQuantity;
    }
    
    /**
     * Check if the product is in stock
     * 
     * @return true if stock quantity > 0
     */
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    /**
     * Check if stock is below a specified threshold
     * 
     * @param threshold Minimum acceptable quantity
     * @return true if stock is below threshold
     */
    public boolean isLowStock(int threshold) {
        return stockQuantity < threshold;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }
    
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        if (supplier != null) {
            this.supplierId = supplier.getId();
        }
    }
    
    @Override
    public String toString() {
        return "Product [id=" + id + ", code=" + productCode + ", name=" + name + 
               ", price=" + price + ", stock=" + stockQuantity + "]";
    }
}