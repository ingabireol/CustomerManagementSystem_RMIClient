package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a payment in the business management system.
 * Contains payment details and relationship to invoices.
 */
public class Payment {
    private int id;
    private String paymentId;
    private int invoiceId;
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
        this.invoiceId = invoice.getId();
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param paymentId Unique payment identifier
     * @param invoiceId ID of the invoice being paid
     * @param amount Payment amount
     * @param paymentDate Date the payment was made
     * @param paymentMethod Method of payment
     */
    public Payment(int id, String paymentId, int invoiceId, BigDecimal amount, 
                   LocalDate paymentDate, String paymentMethod) {
        this();
        this.id = id;
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
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

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        if (invoice != null) {
            this.invoiceId = invoice.getId();
            // Update the invoice status
            invoice.updateStatus();
        }
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
        return "Payment [id=" + id + ", paymentId=" + paymentId + ", invoiceId=" + invoiceId + 
               ", amount=" + amount + ", date=" + paymentDate + ", method=" + paymentMethod + "]";
    }
}