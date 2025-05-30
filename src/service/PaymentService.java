package service;

import model.Invoice;
import model.Payment;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

/**
 * Remote service interface for Payment operations.
 * Defines the contract for payment-related business operations.
 */
public interface PaymentService extends Remote {
    
    /**
     * Creates a new payment
     * 
     * @param payment The payment to create
     * @return The created payment with generated ID
     * @throws RemoteException If RMI communication fails
     */
    Payment createPayment(Payment payment) throws RemoteException;
    
    /**
     * Updates an existing payment
     * 
     * @param payment The payment to update
     * @return The updated payment
     * @throws RemoteException If RMI communication fails
     */
    Payment updatePayment(Payment payment) throws RemoteException;
    
    /**
     * Deletes a payment
     * 
     * @param payment The payment to delete
     * @return The deleted payment
     * @throws RemoteException If RMI communication fails
     */
    Payment deletePayment(Payment payment) throws RemoteException;
    
    /**
     * Finds a payment by ID
     * 
     * @param id The payment ID to search for
     * @return The payment if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Payment findPaymentById(int id) throws RemoteException;
    
    /**
     * Finds a payment by payment ID
     * 
     * @param paymentId The payment ID to search for
     * @return The payment if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Payment findPaymentByPaymentId(String paymentId) throws RemoteException;
    
    /**
     * Finds payments by invoice
     * 
     * @param invoice The invoice to search for
     * @return List of matching payments
     * @throws RemoteException If RMI communication fails
     */
    List<Payment> findPaymentsByInvoice(Invoice invoice) throws RemoteException;
    
    /**
     * Finds payments by date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of matching payments
     * @throws RemoteException If RMI communication fails
     */
    List<Payment> findPaymentsByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException;
    
    /**
     * Finds payments by payment method
     * 
     * @param paymentMethod The payment method to search for
     * @return List of matching payments
     * @throws RemoteException If RMI communication fails
     */
    List<Payment> findPaymentsByMethod(String paymentMethod) throws RemoteException;
    
    /**
     * Gets all payments
     * 
     * @return List of all payments
     * @throws RemoteException If RMI communication fails
     */
    List<Payment> findAllPayments() throws RemoteException;
    
    /**
     * Gets a payment with its invoice information loaded
     * 
     * @param paymentId The payment ID
     * @return The payment with invoice, null if not found
     * @throws RemoteException If RMI communication fails
     */
    Payment getPaymentWithInvoice(int paymentId) throws RemoteException;
    
    /**
     * Checks if a payment ID already exists
     * 
     * @param paymentId The payment ID to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean paymentIdExists(String paymentId) throws RemoteException;
    
    /**
     * Gets all distinct payment methods
     * 
     * @return List of all payment methods
     * @throws RemoteException If RMI communication fails
     */
    List<String> findAllPaymentMethods() throws RemoteException;
}