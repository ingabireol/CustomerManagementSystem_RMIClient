package util;

import service.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing RMI connections to the server.
 * Provides centralized connection management and service lookups.
 */
public class RMIConnectionManager {
    
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Service cache
    private static final Map<String, Object> serviceCache = new HashMap<>();
    private static Registry registry = null;
    
    // Service names
    public static final String USER_SERVICE = "userService";
    public static final String CUSTOMER_SERVICE = "customerService";
    public static final String PRODUCT_SERVICE = "productService";
    public static final String SUPPLIER_SERVICE = "supplierService";
    public static final String ORDER_SERVICE = "orderService";
    public static final String INVOICE_SERVICE = "invoiceService";
    public static final String PAYMENT_SERVICE = "paymentService";
    
    /**
     * Private constructor to prevent instantiation
     */
    private RMIConnectionManager() {
    }
    
    /**
     * Initializes the RMI registry connection
     * 
     * @return true if successful, false otherwise
     */
    public static synchronized boolean initializeConnection() {
        try {
            LogUtil.info("Initializing RMI connection to " + RMI_HOST + ":" + RMI_PORT);
            registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            
            // Test the connection by listing services
            String[] services = registry.list();
            LogUtil.info("Successfully connected to RMI registry. Available services: " + String.join(", ", services));
            
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to initialize RMI connection", e);
            registry = null;
            return false;
        }
    }
    
    /**
     * Tests the connection to the RMI server
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            if (registry == null) {
                return initializeConnection();
            }
            
            // Test by listing services
            registry.list();
            return true;
        } catch (Exception e) {
            LogUtil.error("RMI connection test failed", e);
            return false;
        }
    }
    
    /**
     * Gets a service from the RMI registry with caching
     * 
     * @param serviceName The name of the service to lookup
     * @param serviceClass The class type of the service
     * @param <T> The service type
     * @return The service instance or null if not found
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> T getService(String serviceName, Class<T> serviceClass) {
        try {
            // Check cache first
            if (serviceCache.containsKey(serviceName)) {
                return (T) serviceCache.get(serviceName);
            }
            
            // Ensure registry is initialized
            if (registry == null && !initializeConnection()) {
                LogUtil.error("Cannot get service - RMI registry not initialized");
                return null;
            }
            
            // Lookup service
            T service = (T) registry.lookup(serviceName);
            
            // Cache the service
            serviceCache.put(serviceName, service);
            
            LogUtil.debug("Retrieved service: " + serviceName);
            return service;
            
        } catch (Exception e) {
            LogUtil.error("Failed to get service: " + serviceName, e);
            return null;
        }
    }
    
    /**
     * Gets the UserService instance
     * 
     * @return UserService instance or null
     */
    public static UserService getUserService() {
        return getService(USER_SERVICE, UserService.class);
    }
    
    /**
     * Gets the CustomerService instance
     * 
     * @return CustomerService instance or null
     */
    public static CustomerService getCustomerService() {
        return getService(CUSTOMER_SERVICE, CustomerService.class);
    }
    
    /**
     * Gets the ProductService instance
     * 
     * @return ProductService instance or null
     */
    public static ProductService getProductService() {
        return getService(PRODUCT_SERVICE, ProductService.class);
    }
    
    /**
     * Gets the SupplierService instance
     * 
     * @return SupplierService instance or null
     */
    public static SupplierService getSupplierService() {
        return getService(SUPPLIER_SERVICE, SupplierService.class);
    }
    
    /**
     * Gets the OrderService instance
     * 
     * @return OrderService instance or null
     */
    public static OrderService getOrderService() {
        return getService(ORDER_SERVICE, OrderService.class);
    }
    
    /**
     * Gets the InvoiceService instance
     * 
     * @return InvoiceService instance or null
     */
    public static InvoiceService getInvoiceService() {
        return getService(INVOICE_SERVICE, InvoiceService.class);
    }
    
    /**
     * Gets the PaymentService instance
     * 
     * @return PaymentService instance or null
     */
    public static PaymentService getPaymentService() {
        return getService(PAYMENT_SERVICE, PaymentService.class);
    }
    
    /**
     * Clears the service cache and forces reconnection
     */
    public static synchronized void clearCache() {
        LogUtil.info("Clearing RMI service cache");
        serviceCache.clear();
        registry = null;
    }
    
    /**
     * Reconnects to the RMI server
     * 
     * @return true if reconnection successful, false otherwise
     */
    public static synchronized boolean reconnect() {
        LogUtil.info("Reconnecting to RMI server");
        clearCache();
        return initializeConnection();
    }
    
    /**
     * Checks if the connection is active
     * 
     * @return true if connected, false otherwise
     */
    public static boolean isConnected() {
        return registry != null && testConnection();
    }
    
    /**
     * Gets connection information
     * 
     * @return Connection info string
     */
    public static String getConnectionInfo() {
        return "RMI Server: " + RMI_HOST + ":" + RMI_PORT + 
               " (Connected: " + isConnected() + ")";
    }
    
    /**
     * Lists all available services on the server
     * 
     * @return Array of service names or empty array if connection fails
     */
    public static String[] listAvailableServices() {
        try {
            if (registry == null && !initializeConnection()) {
                return new String[0];
            }
            
            return registry.list();
        } catch (Exception e) {
            LogUtil.error("Failed to list available services", e);
            return new String[0];
        }
    }
    
    /**
     * Validates that all required services are available
     * 
     * @return true if all services are available, false otherwise
     */
    public static boolean validateServices() {
        String[] requiredServices = {
            USER_SERVICE,
            CUSTOMER_SERVICE,
            PRODUCT_SERVICE,
            SUPPLIER_SERVICE,
            ORDER_SERVICE,
            INVOICE_SERVICE,
            PAYMENT_SERVICE
        };
        
        try {
            String[] availableServices = listAvailableServices();
            
            for (String required : requiredServices) {
                boolean found = false;
                for (String available : availableServices) {
                    if (required.equals(available)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    LogUtil.error("Required service not available: " + required);
                    return false;
                }
            }
            
            LogUtil.info("All required services are available");
            return true;
            
        } catch (Exception e) {
            LogUtil.error("Failed to validate services", e);
            return false;
        }
    }
    
    /**
     * Gets the RMI host
     * 
     * @return RMI host address
     */
    public static String getHost() {
        return RMI_HOST;
    }
    
    /**
     * Gets the RMI port
     * 
     * @return RMI port number
     */
    public static int getPort() {
        return RMI_PORT;
    }
}