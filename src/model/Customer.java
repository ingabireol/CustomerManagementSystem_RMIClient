package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer in the business management system.
 * Contains customer details and relationships to orders.
 * FIXED: Removed Hibernate annotations for RMI compatibility
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate registrationDate;
    private List<Order> orders = new ArrayList<>();
    
    /**
     * Default constructor
     */
    public Customer() {
        this.registrationDate = LocalDate.now();
    }
    
    /**
     * Constructor with essential fields
     */
    public Customer(String customerId, String firstName, String lastName, String email) {
        this();
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    /**
     * Full constructor
     */
    public Customer(int id, String customerId, String firstName, String lastName, String email, 
                    String phone, String address, LocalDate registrationDate) {
        this();
        this.id = id;
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registrationDate = registrationDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getFullName() { return firstName + " " + lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    
    public void addOrder(Order order) {
        this.orders.add(order);
        order.setCustomer(this);
    }
    
    @Override
    public String toString() {
        return "Customer [id=" + id + ", customerId=" + customerId + ", name=" + getFullName() + 
               ", email=" + email + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer customer = (Customer) obj;
        return id == customer.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
