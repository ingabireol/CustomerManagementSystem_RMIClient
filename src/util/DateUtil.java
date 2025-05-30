package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date operations including formatting, parsing, and calculations.
 */
public class DateUtil {
    // Common date formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    
    /**
     * Formats a LocalDate object to a string using the default formatter
     * 
     * @param date The date to format
     * @return Formatted date string or empty string if date is null
     */
    public static String formatDate(LocalDate date) {
        return formatDate(date, DATE_FORMATTER);
    }
    
    /**
     * Formats a LocalDate object to a string using the specified formatter
     * 
     * @param date The date to format
     * @param formatter The formatter to use
     * @return Formatted date string or empty string if date is null
     */
    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return "";
        }
        return date.format(formatter);
    }
    
    /**
     * Formats a LocalDateTime object to a string using the default formatter
     * 
     * @param dateTime The datetime to format
     * @return Formatted datetime string or empty string if datetime is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DATE_TIME_FORMATTER);
    }
    
    /**
     * Formats a LocalDateTime object to a string using the specified formatter
     * 
     * @param dateTime The datetime to format
     * @param formatter The formatter to use
     * @return Formatted datetime string or empty string if datetime is null
     */
    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(formatter);
    }
    
    /**
     * Parses a date string to a LocalDate object using the default formatter
     * 
     * @param dateStr The date string to parse
     * @return Parsed LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, DATE_FORMATTER);
    }
    
    /**
     * Parses a date string to a LocalDate object using the specified formatter
     * 
     * @param dateStr The date string to parse
     * @param formatter The formatter to use
     * @return Parsed LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            LogUtil.error("Failed to parse date: " + dateStr, e);
            return null;
        }
    }
    
    /**
     * Parses a datetime string to a LocalDateTime object using the default formatter
     * 
     * @param dateTimeStr The datetime string to parse
     * @return Parsed LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DATE_TIME_FORMATTER);
    }
    
    /**
     * Parses a datetime string to a LocalDateTime object using the specified formatter
     * 
     * @param dateTimeStr The datetime string to parse
     * @param formatter The formatter to use
     * @return Parsed LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, DateTimeFormatter formatter) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            LogUtil.error("Failed to parse datetime: " + dateTimeStr, e);
            return null;
        }
    }
    
    /**
     * Calculates the number of days between two dates
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days between the dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * Checks if a date is in the past
     * 
     * @param date The date to check
     * @return true if the date is before today
     */
    public static boolean isPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Checks if a date is in the future
     * 
     * @param date The date to check
     * @return true if the date is after today
     */
    public static boolean isFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * Gets the first day of the month for a given date
     * 
     * @param date The date
     * @return The first day of the month
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }
    
    /**
     * Gets the last day of the month for a given date
     * 
     * @param date The date
     * @return The last day of the month
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }
}