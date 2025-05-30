package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced logging utility for the client application
 */
public class LogUtil {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static PrintWriter fileWriter;
    private static boolean loggingInitialized = false;
    private static LogLevel currentLogLevel = LogLevel.INFO;
    
    public enum LogLevel {
        DEBUG(0), INFO(1), WARN(2), ERROR(3);
        
        private final int level;
        
        LogLevel(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    /**
     * Initializes logging with file output
     */
    public static void initialize() {
        if (!loggingInitialized) {
            try {
                // Create log file in user's temp directory
                String logFileName = System.getProperty("java.io.tmpdir") + 
                                   "/business_client_" + 
                                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                                   ".log";
                
                fileWriter = new PrintWriter(new FileWriter(logFileName, true));
                loggingInitialized = true;
                
                info("Logging initialized. Log file: " + logFileName);
            } catch (IOException e) {
                System.err.println("Failed to initialize file logging: " + e.getMessage());
                // Continue without file logging
            }
        }
    }
    
    /**
     * Sets the current log level
     * 
     * @param level The minimum log level to output
     */
    public static void setLogLevel(LogLevel level) {
        currentLogLevel = level;
        info("Log level set to: " + level);
    }
    
    /**
     * Log debug message
     * 
     * @param message The message to log
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }
    
    /**
     * Log info message
     * 
     * @param message The message to log
     */
    public static void info(String message) {
        log(LogLevel.INFO, message, null);
    }
    
    /**
     * Log warning message
     * 
     * @param message The warning message to log
     */
    public static void warn(String message) {
        log(LogLevel.WARN, message, null);
    }
    
    /**
     * Log warning message with exception
     * 
     * @param message The warning message to log
     * @param exception The exception to log
     */
    public static void warn(String message, Exception exception) {
        log(LogLevel.WARN, message, exception);
    }
    
    /**
     * Log error message
     * 
     * @param message The error message to log
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message, null);
    }
    
    /**
     * Log error message with exception
     * 
     * @param message The error message to log
     * @param exception The exception to log
     */
    public static void error(String message, Exception exception) {
        log(LogLevel.ERROR, message, exception);
    }
    
    /**
     * Internal logging method
     * 
     * @param level The log level
     * @param message The message to log
     * @param exception The exception to log (can be null)
     */
    private static void log(LogLevel level, String message, Exception exception) {
        // Check if we should log this level
        if (level.getLevel() < currentLogLevel.getLevel()) {
            return;
        }
        
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] %s - %s", level.name(), timestamp, message);
        
        // Output to console
        if (level == LogLevel.ERROR || level == LogLevel.WARN) {
            System.err.println(logEntry);
            if (exception != null) {
                exception.printStackTrace();
            }
        } else {
            System.out.println(logEntry);
        }
        
        // Output to file if available
        if (fileWriter != null) {
            fileWriter.println(logEntry);
            if (exception != null) {
                exception.printStackTrace(fileWriter);
            }
            fileWriter.flush();
        }
    }
    
    /**
     * Logs application startup information
     */
    public static void logStartup() {
        info("========================================================================================");
        info("Business Management Client Application Starting");
        info("Timestamp: " + LocalDateTime.now().format(formatter));
        info("Java Version: " + System.getProperty("java.version"));
        info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        info("User: " + System.getProperty("user.name"));
        info("Working Directory: " + System.getProperty("user.dir"));
        info("========================================================================================");
    }
    
    /**
     * Logs application shutdown information
     */
    public static void logShutdown() {
        info("========================================================================================");
        info("Business Management Client Application Shutting Down");
        info("Timestamp: " + LocalDateTime.now().format(formatter));
        info("========================================================================================");
        
        // Close file writer
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
    }
    
    /**
     * Logs RMI connection information
     * 
     * @param host RMI host
     * @param port RMI port
     * @param success Whether connection was successful
     */
    public static void logRMIConnection(String host, int port, boolean success) {
        if (success) {
            info("RMI Connection established to " + host + ":" + port);
        } else {
            error("RMI Connection failed to " + host + ":" + port);
        }
    }
    
    /**
     * Logs user authentication events
     * 
     * @param username The username
     * @param success Whether authentication was successful
     */
    public static void logAuthentication(String username, boolean success) {
        if (success) {
            info("User authentication successful: " + username);
        } else {
            warn("User authentication failed: " + username);
        }
    }
    
    /**
     * Logs user actions for audit trail
     * 
     * @param username The username performing the action
     * @param action The action being performed
     * @param details Additional details about the action
     */
    public static void logUserAction(String username, String action, String details) {
        info(String.format("USER_ACTION: %s performed %s - %s", username, action, details));
    }
    
    /**
     * Logs service operation calls
     * 
     * @param serviceName The name of the service
     * @param operation The operation being called
     * @param success Whether the operation was successful
     */
    public static void logServiceCall(String serviceName, String operation, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        debug(String.format("SERVICE_CALL: %s.%s - %s", serviceName, operation, status));
    }
    
    /**
     * Logs performance metrics
     * 
     * @param operation The operation being measured
     * @param durationMs The duration in milliseconds
     */
    public static void logPerformance(String operation, long durationMs) {
        debug(String.format("PERFORMANCE: %s took %d ms", operation, durationMs));
    }
    
    /**
     * Logs system information periodically
     */
    public static void logSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        debug(String.format("SYSTEM_INFO: Memory - Used: %d MB, Free: %d MB, Total: %d MB", 
                          usedMemory, freeMemory, totalMemory));
    }
    
    /**
     * Gets the current log file path
     * 
     * @return Log file path or null if file logging not initialized
     */
    public static String getLogFilePath() {
        if (loggingInitialized) {
            return System.getProperty("java.io.tmpdir") + 
                   "/business_client_" + 
                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                   ".log";
        }
        return null;
    }
}