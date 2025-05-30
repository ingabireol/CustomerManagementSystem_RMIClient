package service;

import model.Supplier;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface for Supplier operations.
 * Defines the contract for supplier-related business operations.
 */
public interface SupplierService extends Remote {
    
    /**
     * Creates a new supplier
     * 
     * @param supplier The supplier to create
     * @return The created supplier with generated ID
     * @throws RemoteException If RMI communication fails
     */
    Supplier createSupplier(Supplier supplier) throws RemoteException;
    
    /**
     * Updates an existing supplier
     * 
     * @param supplier The supplier to update
     * @return The updated supplier
     * @throws RemoteException If RMI communication fails
     */
    Supplier updateSupplier(Supplier supplier) throws RemoteException;
    
    /**
     * Deletes a supplier
     * 
     * @param supplier The supplier to delete
     * @return The deleted supplier
     * @throws RemoteException If RMI communication fails
     */
    Supplier deleteSupplier(Supplier supplier) throws RemoteException;
    
    /**
     * Finds a supplier by ID
     * 
     * @param id The supplier ID to search for
     * @return The supplier if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Supplier findSupplierById(int id) throws RemoteException;
    
    /**
     * Finds a supplier by supplier code
     * 
     * @param supplierCode The supplier code to search for
     * @return The supplier if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Supplier findSupplierByCode(String supplierCode) throws RemoteException;
    
    /**
     * Finds suppliers by name (supports partial matching)
     * 
     * @param name The name to search for
     * @return List of matching suppliers
     * @throws RemoteException If RMI communication fails
     */
    List<Supplier> findSuppliersByName(String name) throws RemoteException;
    
    /**
     * Finds a supplier by email
     * 
     * @param email The email to search for
     * @return The supplier if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Supplier findSupplierByEmail(String email) throws RemoteException;
    
    /**
     * Gets all suppliers
     * 
     * @return List of all suppliers
     * @throws RemoteException If RMI communication fails
     */
    List<Supplier> findAllSuppliers() throws RemoteException;
    
    /**
     * Gets a supplier with all their products loaded
     * 
     * @param supplierId The supplier ID
     * @return The supplier with products, null if not found
     * @throws RemoteException If RMI communication fails
     */
    Supplier getSupplierWithProducts(int supplierId) throws RemoteException;
    
    /**
     * Checks if a supplier code already exists
     * 
     * @param supplierCode The supplier code to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean supplierCodeExists(String supplierCode) throws RemoteException;
    
    /**
     * Checks if an email already exists
     * 
     * @param email The email to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean emailExists(String email) throws RemoteException;
    
    /**
     * Finds suppliers by contact person
     * 
     * @param contactPerson The contact person to search for
     * @return List of matching suppliers
     * @throws RemoteException If RMI communication fails
     */
    List<Supplier> findSuppliersByContactPerson(String contactPerson) throws RemoteException;
}