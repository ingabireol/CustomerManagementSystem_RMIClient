package util;

import service.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced utility class for managing RMI connections to the Business Management Server.
 * Provides centralized connection management and service lookup with OTP support.
 * 
 * Compatible with existing RMIConnectionManager while adding enhanced features.
 */
public class RMIConnectionManager {
    
    // RMI Configuration
    private static final String RMI_HOST = "127.0.0.1";
    private static final int RMI_PORT = 4444;
    
    // Service cache
    private static final Map<String, Object> serviceCache = new HashMap<>();
    private static Registry registry = null;
    private static boolean connected = false;
    
    // Service names (keeping existing constants for compatibility)
    public static final String USER_SERVICE = "userService";
    public static final String CUSTOMER_SERVICE = "customerService";
    public static final String PRODUCT_SERVICE = "productService";
    public static final String SUPPLIER_SERVICE = "supplierService";
    public static final String ORDER_SERVICE = "orderService";
    public static final String INVOICE_SERVICE = "invoiceService";
    public static final String PAYMENT_SERVICE = "paymentService";
    public static final String OTP_SERVICE = "otpService"; // NEW: OTP Service
    
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
            
            // Set RMI system properties
            System.setProperty("java.rmi.server.hostname", RMI_HOST);
            System.setProperty("java.rmi.server.useCodebaseOnly", "false");
            
            registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            
            // Test the connection by listing services
            String[] services = registry.list();
            LogUtil.info("Successfully connected to RMI registry. Available services: " + String.join(", ", services));
            
            connected = true;
            return true;
        } catch (ConnectException e) {
            LogUtil.error("Cannot connect to RMI server at " + RMI_HOST + ":" + RMI_PORT + 
                         ". Please ensure the server is running.", e);
            connected = false;
            registry = null;
            return false;
        } catch (Exception e) {
            LogUtil.error("Failed to initialize RMI connection", e);
            connected = false;
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
            connected = true;
            return true;
        } catch (Exception e) {
            LogUtil.warn("RMI connection test failed", e);
            connected = false;
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
            
        } catch (NotBoundException e) {
            LogUtil.error("Service " + serviceName + " is not bound in the registry", e);
            return null;
        } catch (ConnectException e) {
            LogUtil.error("Connection lost while retrieving " + serviceName, e);
            connected = false;
            return null;
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
     * Gets the OTPService instance (NEW)
     * 
     * @return OTPService instance or null
     */
    public static OTPService getOTPService() {
        return getService(OTP_SERVICE, OTPService.class);
    }
    
    /**
     * Clears the service cache and forces reconnection
     */
    public static synchronized void clearCache() {
        LogUtil.info("Clearing RMI service cache");
        serviceCache.clear();
        registry = null;
        connected = false;
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
        return connected && registry != null && testConnection();
    }
    
    /**
     * Gets connection information
     * 
     * @return Connection info string
     */
    public static String getConnectionInfo() {
        return "RMI Server: " + RMI_HOST + ":" + RMI_PORT + 
               " (Connected: " + connected + ")";
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
            PAYMENT_SERVICE,
            OTP_SERVICE // Include OTP service in validation
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
     * Validates that all required services are available (enhanced version)
     * 
     * @return true if all services are available, false otherwise
     */
    public static boolean validateAllServices() {
        return validateServices();
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
     * Gets the RMI host (alternative method name for compatibility)
     * 
     * @return RMI host address
     */
    public static String getRMIHost() {
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
    
    /**
     * Gets the RMI port (alternative method name for compatibility)
     * 
     * @return RMI port number
     */
    public static int getRMIPort() {
        return RMI_PORT;
    }
    
    /**
     * Closes the RMI connection and cleans up resources
     */
    public static void close() {
        LogUtil.info("Closing RMI connection");
        registry = null;
        connected = false;
        clearCache();
    }
}