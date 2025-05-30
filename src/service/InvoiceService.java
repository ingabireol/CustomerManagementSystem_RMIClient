package service;

import model.Invoice;
import model.Order;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

/**
 * Remote service interface for Invoice operations.
 * Defines the contract for invoice-related business operations.
 */
public interface InvoiceService extends Remote {
    
    /**
     * Creates a new invoice
     * 
     * @param invoice The invoice to create
     * @return The created invoice with generated ID
     * @throws RemoteException If RMI communication fails
     */
    Invoice createInvoice(Invoice invoice) throws RemoteException;
    
    /**
     * Updates an existing invoice
     * 
     * @param invoice The invoice to update
     * @return The updated invoice
     * @throws RemoteException If RMI communication fails
     */
    Invoice updateInvoice(Invoice invoice) throws RemoteException;
    
    /**
     * Updates the status of an invoice
     * 
     * @param invoiceId The ID of the invoice
     * @param status The new status
     * @return Number of rows affected
     * @throws RemoteException If RMI communication fails
     */
    int updateInvoiceStatus(int invoiceId, String status) throws RemoteException;
    
    /**
     * Deletes an invoice
     * 
     * @param invoice The invoice to delete
     * @return The deleted invoice
     * @throws RemoteException If RMI communication fails
     */
    Invoice deleteInvoice(Invoice invoice) throws RemoteException;
    
    /**
     * Finds an invoice by ID
     * 
     * @param id The invoice ID to search for
     * @return The invoice if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Invoice findInvoiceById(int id) throws RemoteException;
    
    /**
     * Finds an invoice by invoice number
     * 
     * @param invoiceNumber The invoice number to search for
     * @return The invoice if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    Invoice findInvoiceByNumber(String invoiceNumber) throws RemoteException;
    
    /**
     * Finds invoices by order
     * 
     * @param order The order to search for
     * @return List of matching invoices
     * @throws RemoteException If RMI communication fails
     */
    List<Invoice> findInvoicesByOrder(Order order) throws RemoteException;
    
    /**
     * Finds invoices by status
     * 
     * @param status The status to search for
     * @return List of matching invoices
     * @throws RemoteException If RMI communication fails
     */
    List<Invoice> findInvoicesByStatus(String status) throws RemoteException;
    
    /**
     * Finds invoices that are overdue
     * 
     * @return List of overdue invoices
     * @throws RemoteException If RMI communication fails
     */
    List<Invoice> findOverdueInvoices() throws RemoteException;
    
    /**
     * Finds invoices by date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of matching invoices
     * @throws RemoteException If RMI communication fails
     */
    List<Invoice> findInvoicesByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException;
    
    /**
     * Gets all invoices
     * 
     * @return List of all invoices
     * @throws RemoteException If RMI communication fails
     */
    List<Invoice> findAllInvoices() throws RemoteException;
    
    /**
     * Gets an invoice with its order information loaded
     * 
     * @param invoiceId The invoice ID
     * @return The invoice with order, null if not found
     * @throws RemoteException If RMI communication fails
     */
    Invoice getInvoiceWithOrder(int invoiceId) throws RemoteException;
    
    /**
     * Gets an invoice with its payments loaded
     * 
     * @param invoiceId The invoice ID
     * @return The invoice with payments, null if not found
     * @throws RemoteException If RMI communication fails
     */
    Invoice getInvoiceWithPayments(int invoiceId) throws RemoteException;
    
    /**
     * Checks if an invoice number already exists
     * 
     * @param invoiceNumber The invoice number to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean invoiceNumberExists(String invoiceNumber) throws RemoteException;
}