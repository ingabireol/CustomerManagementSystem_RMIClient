package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a payment in the business management system.
 * Contains payment details and relationship to invoices.
 */
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    
    private String paymentId;
    
    private Invoice invoice;
    
    private BigDecimal amount;
    
    private LocalDate paymentDate;
    
    private String paymentMethod;
    
    /**
     * Default constructor
     */
    public Payment() {
        this.amount = BigDecimal.ZERO;
        this.paymentDate = LocalDate.now();
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param paymentId Unique payment identifier
     * @param invoice The invoice being paid
     * @param amount Payment amount
     * @param paymentMethod Method of payment
     */
    public Payment(String paymentId, Invoice invoice, BigDecimal amount, String paymentMethod) {
        this();
        this.paymentId = paymentId;
        this.invoice = invoice;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param paymentId Unique payment identifier
     * @param invoice The invoice being paid
     * @param amount Payment amount
     * @param paymentDate Date the payment was made
     * @param paymentMethod Method of payment
     */
    public Payment(int id, String paymentId, Invoice invoice, BigDecimal amount, 
                   LocalDate paymentDate, String paymentMethod) {
        this();
        this.id = id;
        this.paymentId = paymentId;
        this.invoice = invoice;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        if (invoice != null) {
            // Update the invoice status
            invoice.updateStatus();
        }
    }
    
    /**
     * Gets the invoice ID for compatibility
     * 
     * @return Invoice ID or 0 if no invoice
     */
    public int getInvoiceId() {
        return invoice != null ? invoice.getId() : 0;
    }
    
    /**
     * Sets invoice by ID (for compatibility)
     * Note: This doesn't actually set the invoice, just for interface compatibility
     * 
     * @param invoiceId The invoice ID
     */
    public void setInvoiceId(int invoiceId) {
        // This method is kept for compatibility but doesn't do anything
        // Invoice should be set using setInvoice(Invoice invoice)
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        // Update invoice status if invoice is set
        if (invoice != null) {
            invoice.updateStatus();
        }
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    @Override
    public String toString() {
        return "Payment [id=" + id + ", paymentId=" + paymentId + ", invoiceId=" + getInvoiceId() + 
               ", amount=" + amount + ", date=" + paymentDate + ", method=" + paymentMethod + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Payment payment = (Payment) obj;
        return id == payment.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}