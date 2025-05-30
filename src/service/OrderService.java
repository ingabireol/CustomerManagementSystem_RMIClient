package service;

import model.Customer;
import model.Order;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

/**
 * Remote service interface for Order operations.
 * Defines the contract for order-related business operations.
 */
public interface OrderService extends Remote {
    
    /**
     * Creates a new order
     * 
     * @param order The order to create
     * @return The created order with generated ID
     * @throws RemoteException If RMI communication fails
     */
    Order createOrder(Order order) throws RemoteException;
    
    /**
     * Updates an existing order
     * 
     * @param order The order to update
     * @return The updated order
     * @throws RemoteException If RMI communication fails
     */
    Order updateOrder(Order order) throws RemoteException;
    
    /**
     * Updates the status of an order
     * 
     * @param orderId The ID of the order
     * @param status The new status
     * @return Number of rows affected
     * @throws RemoteException If RMI communication fails
     */
    int updateOrderStatus(int orderId, String status) throws RemoteException;
    
    /**
     * Deletes an order
     * 
     * @param order The order to delete
     * @return The deleted order
     * @throws RemoteException If RMI communication fails
     */
    Order deleteOrder(Order order) throws RemoteException;
    
    /**
     * Finds an order by ID
     * 
     * @param id The order ID to search for
     * @return The order if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Order findOrderById(int id) throws RemoteException;
    
    /**
     * Finds an order by order ID
     * 
     * @param orderId The order ID to search for
     * @return The order if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Order findOrderByOrderId(String orderId) throws RemoteException;
    
    /**
     * Finds orders by customer
     * 
     * @param customer The customer to search for
     * @return List of matching orders
     * @throws RemoteException If RMI communication fails
     */
    List<Order> findOrdersByCustomer(Customer customer) throws RemoteException;
    
    /**
     * Finds orders by status
     * 
     * @param status The status to search for
     * @return List of matching orders
     * @throws RemoteException If RMI communication fails
     */
    List<Order> findOrdersByStatus(String status) throws RemoteException;
    
    /**
     * Finds orders by date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of matching orders
     * @throws RemoteException If RMI communication fails
     */
    List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException;
    
    /**
     * Gets all orders
     * 
     * @return List of all orders
     * @throws RemoteException If RMI communication fails
     */
    List<Order> findAllOrders() throws RemoteException;
    
    /**
     * Gets an order with all its items and customer information loaded
     * 
     * @param orderId The order ID
     * @return The order with details, null if not found
     * @throws RemoteException If RMI communication fails
     */
    Order getOrderWithDetails(int orderId) throws RemoteException;
    
    /**
     * Checks if an order ID already exists
     * 
     * @param orderId The order ID to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean orderIdExists(String orderId) throws RemoteException;
}