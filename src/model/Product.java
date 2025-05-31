package model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents a product in the business management system.
 * FIXED: Removed Hibernate annotations for RMI compatibility
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String productCode;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String category;
    private Supplier supplier;
    
    public Product() {
        this.price = BigDecimal.ZERO;
        this.stockQuantity = 0;
    }
    
    public Product(String productCode, String name, BigDecimal price, int stockQuantity) {
        this();
        this.productCode = productCode;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    public Product(int id, String productCode, String name, String description, 
                   BigDecimal price, int stockQuantity, String category, Supplier supplier) {
        this();
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.supplier = supplier;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    
    public int getSupplierId() { return supplier != null ? supplier.getId() : 0; }
    public void setSupplierId(int supplierId) {
        // For compatibility - actual supplier should be set using setSupplier
    }
    
    public int updateStock(int quantity) {
        this.stockQuantity += quantity;
        return this.stockQuantity;
    }
    
    public boolean isInStock() { return stockQuantity > 0; }
    public boolean isLowStock(int threshold) { return stockQuantity < threshold; }
    
    @Override
    public String toString() {
        return "Product [id=" + id + ", code=" + productCode + ", name=" + name + 
               ", price=" + price + ", stock=" + stockQuantity + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}