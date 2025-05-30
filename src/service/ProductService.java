package service;

import model.Product;
import model.Supplier;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface for Product operations.
 * Defines the contract for product-related business operations.
 */
public interface ProductService extends Remote {
    
    /**
     * Creates a new product
     * 
     * @param product The product to create
     * @return The created product with generated ID
     * @throws RemoteException If RMI communication fails
     */
    Product createProduct(Product product) throws RemoteException;
    
    /**
     * Updates an existing product
     * 
     * @param product The product to update
     * @return The updated product
     * @throws RemoteException If RMI communication fails
     */
    Product updateProduct(Product product) throws RemoteException;
    
    /**
     * Updates the stock quantity of a product
     * 
     * @param productId The ID of the product
     * @param quantity The quantity to add (negative to remove)
     * @return Number of rows affected
     * @throws RemoteException If RMI communication fails
     */
    int updateProductStock(int productId, int quantity) throws RemoteException;
    
    /**
     * Deletes a product
     * 
     * @param product The product to delete
     * @return The deleted product
     * @throws RemoteException If RMI communication fails
     */
    Product deleteProduct(Product product) throws RemoteException;
    
    /**
     * Finds a product by ID
     * 
     * @param id The product ID to search for
     * @return The product if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Product findProductById(int id) throws RemoteException;
    
    /**
     * Finds a product by product code
     * 
     * @param productCode The product code to search for
     * @return The product if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Product findProductByCode(String productCode) throws RemoteException;
    
    /**
     * Finds products by name (supports partial matching)
     * 
     * @param name The name to search for
     * @return List of matching products
     * @throws RemoteException If RMI communication fails
     */
    List<Product> findProductsByName(String name) throws RemoteException;
    
    /**
     * Finds products by category
     * 
     * @param category The category to search for
     * @return List of matching products
     * @throws RemoteException If RMI communication fails
     */
    List<Product> findProductsByCategory(String category) throws RemoteException;
    
    /**
     * Finds products by supplier
     * 
     * @param supplier The supplier to search for
     * @return List of matching products
     * @throws RemoteException If RMI communication fails
     */
    List<Product> findProductsBySupplier(Supplier supplier) throws RemoteException;
    
    /**
     * Finds products with low stock below a threshold
     * 
     * @param threshold The stock threshold
     * @return List of products with stock below threshold
     * @throws RemoteException If RMI communication fails
     */
    List<Product> findLowStockProducts(int threshold) throws RemoteException;
    
    /**
     * Gets all products
     * 
     * @return List of all products
     * @throws RemoteException If RMI communication fails
     */
    List<Product> findAllProducts() throws RemoteException;
    
    /**
     * Gets a product with its supplier information loaded
     * 
     * @param productId The product ID
     * @return The product with supplier, null if not found
     * @throws RemoteException If RMI communication fails
     */
    Product getProductWithSupplier(int productId) throws RemoteException;
    
    /**
     * Checks if a product code already exists
     * 
     * @param productCode The product code to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean productCodeExists(String productCode) throws RemoteException;
    
    /**
     * Gets all distinct categories
     * 
     * @return List of all categories
     * @throws RemoteException If RMI communication fails
     */
    List<String> findAllCategories() throws RemoteException;
}