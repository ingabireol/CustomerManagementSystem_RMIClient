package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer in the business management system.
 * Contains customer details and relationships to orders.
 */
public class Customer {
    private int id;
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate registrationDate;
    private List<Order> orders;
    
    /**
     * Default constructor
     */
    public Customer() {
        this.orders = new ArrayList<>();
        this.registrationDate = LocalDate.now();
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param customerId Unique identifier for the customer
     * @param firstName Customer's first name
     * @param lastName Customer's last name
     * @param email Customer's email address
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
     * 
     * @param id Database ID
     * @param customerId Unique identifier for the customer
     * @param firstName Customer's first name
     * @param lastName Customer's last name
     * @param email Customer's email address
     * @param phone Customer's phone number
     * @param address Customer's address
     * @param registrationDate Date the customer was registered
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
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Gets the full name of the customer
     * 
     * @return Full name (first name + last name)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    /**
     * Add an order to this customer
     * 
     * @param order The order to add
     */
    public void addOrder(Order order) {
        this.orders.add(order);
        order.setCustomer(this);
    }
    
    @Override
    public String toString() {
        return "Customer [id=" + id + ", customerId=" + customerId + ", name=" + getFullName() + 
               ", email=" + email + "]";
    }
}