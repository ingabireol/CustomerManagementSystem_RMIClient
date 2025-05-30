package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for validating various types of input data.
 */
public class ValidationUtil {
    // Regular expressions for validation
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^\\+?[0-9]{10,15}$");
    private static final Pattern ALPHANUMERIC_PATTERN = 
            Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern NUMBER_PATTERN = 
            Pattern.compile("^[0-9]+$");
    
    /**
     * Checks if a string is null or empty
     * 
     * @param str The string to check
     * @return true if the string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Validates if a string has a minimum length
     * 
     * @param str The string to check
     * @param minLength The minimum length required
     * @return true if the string is valid
     */
    public static boolean validateMinLength(String str, int minLength) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        return str.length() >= minLength;
    }
    
    /**
     * Validates if a string doesn't exceed a maximum length
     * 
     * @param str The string to check
     * @param maxLength The maximum length allowed
     * @return true if the string is valid
     */
    public static boolean validateMaxLength(String str, int maxLength) {
        if (isNullOrEmpty(str)) {
            return true; // Empty string is within any max length
        }
        return str.length() <= maxLength;
    }
    
    /**
     * Validates if a string is a valid email address
     * 
     * @param email The email address to validate
     * @return true if the email is valid
     */
    public static boolean validateEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates if a string is a valid phone number
     * 
     * @param phone The phone number to validate
     * @return true if the phone number is valid
     */
    public static boolean validatePhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validates if a string contains only alphanumeric characters
     * 
     * @param str The string to validate
     * @return true if the string is alphanumeric
     */
    public static boolean validateAlphanumeric(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(str).matches();
    }
    
    /**
     * Validates if a string contains only numbers
     * 
     * @param str The string to validate
     * @return true if the string contains only numbers
     */
    public static boolean validateNumeric(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        return NUMBER_PATTERN.matcher(str).matches();
    }
    
    /**
     * Validates if a BigDecimal is positive
     * 
     * @param value The value to check
     * @return true if the value is positive
     */
    public static boolean validatePositive(BigDecimal value) {
        if (value == null) {
            return false;
        }
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Validates if a BigDecimal is non-negative
     * 
     * @param value The value to check
     * @return true if the value is zero or positive
     */
    public static boolean validateNonNegative(BigDecimal value) {
        if (value == null) {
            return false;
        }
        return value.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    /**
     * Validates if an integer is positive
     * 
     * @param value The value to check
     * @return true if the value is positive
     */
    public static boolean validatePositive(int value) {
        return value > 0;
    }
    
    /**
     * Validates if an integer is non-negative
     * 
     * @param value The value to check
     * @return true if the value is zero or positive
     */
    public static boolean validateNonNegative(int value) {
        return value >= 0;
    }
    
    /**
     * Validates if a date is not in the future
     * 
     * @param date The date to check
     * @return true if the date is today or in the past
     */
    public static boolean validateNotFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isAfter(LocalDate.now());
    }
    
    /**
     * Validates if a date is not in the past
     * 
     * @param date The date to check
     * @return true if the date is today or in the future
     */
    public static boolean validateNotPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(LocalDate.now());
    }
    
    /**
     * Validates if a date is within a specified range
     * 
     * @param date The date to check
     * @param startDate The start of the valid range
     * @param endDate The end of the valid range
     * @return true if the date is within the range
     */
    public static boolean validateDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null) {
            return false;
        }
        
        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);
        
        return afterStart && beforeEnd;
    }
}