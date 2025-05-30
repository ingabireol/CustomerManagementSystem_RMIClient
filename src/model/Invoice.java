package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an invoice in the business management system.
 * Contains invoice details and relationships to orders and payments.
 */
public class Invoice {
    private int id;
    private String invoiceNumber;
    private int orderId;
    private Order order;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private String status;
    private List<Payment> payments;
    
    // Invoice status constants
    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_ISSUED = "Issued";
    public static final String STATUS_PAID = "Paid";
    public static final String STATUS_OVERDUE = "Overdue";
    public static final String STATUS_CANCELLED = "Cancelled";
    
    /**
     * Default constructor
     */
    public Invoice() {
        this.payments = new ArrayList<>();
        this.issueDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(30); // Default due date: 30 days from issue
        this.amount = BigDecimal.ZERO;
        this.status = STATUS_DRAFT;
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param invoiceNumber Unique invoice identifier
     * @param order The order this invoice is for
     * @param amount Invoice amount
     */
    public Invoice(String invoiceNumber, Order order, BigDecimal amount) {
        this();
        this.invoiceNumber = invoiceNumber;
        this.order = order;
        this.orderId = order.getId();
        this.amount = amount;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param invoiceNumber Unique invoice identifier
     * @param orderId ID of the related order
     * @param issueDate Date the invoice was issued
     * @param dueDate Date the invoice is due
     * @param amount Invoice amount
     * @param status Invoice status
     */
    public Invoice(int id, String invoiceNumber, int orderId, LocalDate issueDate, 
                   LocalDate dueDate, BigDecimal amount, String status) {
        this();
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.orderId = orderId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.amount = amount;
        this.status = status;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
    
    /**
     * Adds a payment to this invoice
     * 
     * @param payment The payment to add
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setInvoice(this);
        updateStatus();
    }
    
    /**
     * Calculate the total amount paid on this invoice
     * 
     * @return Sum of all payment amounts
     */
    public BigDecimal getPaidAmount() {
        BigDecimal paidAmount = BigDecimal.ZERO;
        for (Payment payment : payments) {
            paidAmount = paidAmount.add(payment.getAmount());
        }
        return paidAmount;
    }
    
    /**
     * Calculate the remaining balance on this invoice
     * 
     * @return Invoice amount minus paid amount
     */
    public BigDecimal getRemainingBalance() {
        return amount.subtract(getPaidAmount());
    }
    
    /**
     * Check if the invoice is fully paid
     * 
     * @return true if paid amount equals or exceeds invoice amount
     */
    public boolean isFullyPaid() {
        return getPaidAmount().compareTo(amount) >= 0;
    }
    
    /**
     * Check if the invoice is overdue
     * 
     * @return true if due date is in the past and invoice is not fully paid
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && !isFullyPaid();
    }
    
    /**
     * Updates the invoice status based on payments and due date
     */
    public void updateStatus() {
        if (isFullyPaid()) {
            this.status = STATUS_PAID;
        } else if (isOverdue()) {
            this.status = STATUS_OVERDUE;
        } else if (!STATUS_DRAFT.equals(this.status) && !STATUS_CANCELLED.equals(this.status)) {
            this.status = STATUS_ISSUED;
        }
    }
    
    @Override
    public String toString() {
        return "Invoice [id=" + id + ", invoiceNumber=" + invoiceNumber + ", orderId=" + orderId + 
               ", issueDate=" + issueDate + ", amount=" + amount + ", status=" + status + "]";
    }
}