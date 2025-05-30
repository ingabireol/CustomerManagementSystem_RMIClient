package service;

import model.Customer;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface for Customer operations.
 * Defines the contract for customer-related business operations.
 */
public interface CustomerService extends Remote {
    
    /**
     * Creates a new customer
     * 
     * @param customer The customer to create
     * @return The created customer with generated ID
     * @throws RemoteException If RMI communication fails
     */
    Customer createCustomer(Customer customer) throws RemoteException;
    
    /**
     * Updates an existing customer
     * 
     * @param customer The customer to update
     * @return The updated customer
     * @throws RemoteException If RMI communication fails
     */
    Customer updateCustomer(Customer customer) throws RemoteException;
    
    /**
     * Deletes a customer
     * 
     * @param customer The customer to delete
     * @return The deleted customer
     * @throws RemoteException If RMI communication fails
     */
    Customer deleteCustomer(Customer customer) throws RemoteException;
    
    /**
     * Finds a customer by their database ID
     * 
     * @param id The database ID to search for
     * @return The customer if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Customer findCustomerById(int id) throws RemoteException;
    
    /**
     * Finds a customer by their customer ID
     * 
     * @param customerId The customer ID to search for
     * @return The customer if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Customer findCustomerByCustomerId(String customerId) throws RemoteException;
    
    /**
     * Finds customers by name (supports partial matching)
     * 
     * @param name The name to search for
     * @return List of matching customers
     * @throws RemoteException If RMI communication fails
     */
    List<Customer> findCustomersByName(String name) throws RemoteException;
    
    /**
     * Finds a customer by email address
     * 
     * @param email The email to search for
     * @return The customer if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Customer findCustomerByEmail(String email) throws RemoteException;
    
    /**
     * Gets all customers
     * 
     * @return List of all customers
     * @throws RemoteException If RMI communication fails
     */
    List<Customer> findAllCustomers() throws RemoteException;
    
    /**
     * Gets a customer with all their orders loaded
     * 
     * @param customerId The customer ID
     * @return The customer with orders, null if not found
     * @throws RemoteException If RMI communication fails
     */
    Customer getCustomerWithOrders(int customerId) throws RemoteException;
    
    /**
     * Checks if a customer ID already exists
     * 
     * @param customerId The customer ID to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean customerIdExists(String customerId) throws RemoteException;
    
    /**
     * Checks if an email already exists
     * 
     * @param email The email to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean emailExists(String email) throws RemoteException;
}