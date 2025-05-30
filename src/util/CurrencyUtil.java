package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility class for currency operations including formatting and calculations.
 */
public class CurrencyUtil {
    // Default currency symbol and locale
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");
    private static final Locale DEFAULT_LOCALE = Locale.US;
    
    /**
     * Formats a BigDecimal as currency using the default locale and currency
     * 
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(BigDecimal amount) {
        return formatCurrency(amount, DEFAULT_LOCALE, DEFAULT_CURRENCY);
    }
    
    /**
     * Formats a BigDecimal as currency using the specified locale and currency
     * 
     * @param amount The amount to format
     * @param locale The locale to use for formatting
     * @param currency The currency to use
     * @return Formatted currency string
     */
    public static String formatCurrency(BigDecimal amount, Locale locale, Currency currency) {
        if (amount == null) {
            return "";
        }
        
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        currencyFormatter.setCurrency(currency);
        return currencyFormatter.format(amount);
    }
    
    /**
     * Rounds a BigDecimal to the specified number of decimal places
     * 
     * @param amount The amount to round
     * @param decimalPlaces Number of decimal places to round to
     * @return Rounded BigDecimal
     */
    public static BigDecimal round(BigDecimal amount, int decimalPlaces) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(decimalPlaces, RoundingMode.HALF_UP);
    }
    
    /**
     * Rounds a BigDecimal to 2 decimal places (common for currency)
     * 
     * @param amount The amount to round
     * @return Rounded BigDecimal
     */
    public static BigDecimal roundCurrency(BigDecimal amount) {
        return round(amount, 2);
    }
    
    /**
     * Calculates the tax amount for a given price and tax rate
     * 
     * @param price The base price
     * @param taxRate The tax rate as a percentage (e.g., 8.5 for 8.5%)
     * @return The calculated tax amount
     */
    public static BigDecimal calculateTax(BigDecimal price, BigDecimal taxRate) {
        if (price == null || taxRate == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal taxMultiplier = taxRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        return roundCurrency(price.multiply(taxMultiplier));
    }
    
    /**
     * Calculates the price with tax included
     * 
     * @param price The base price
     * @param taxRate The tax rate as a percentage (e.g., 8.5 for 8.5%)
     * @return The price with tax included
     */
    public static BigDecimal calculatePriceWithTax(BigDecimal price, BigDecimal taxRate) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal taxAmount = calculateTax(price, taxRate);
        return roundCurrency(price.add(taxAmount));
    }
    
    /**
     * Calculates the discount amount for a given price and discount percentage
     * 
     * @param price The base price
     * @param discountPercent The discount as a percentage (e.g., 10 for 10%)
     * @return The calculated discount amount
     */
    public static BigDecimal calculateDiscount(BigDecimal price, BigDecimal discountPercent) {
        if (price == null || discountPercent == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountMultiplier = discountPercent.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        return roundCurrency(price.multiply(discountMultiplier));
    }
    
    /**
     * Calculates the price after applying a discount
     * 
     * @param price The base price
     * @param discountPercent The discount as a percentage (e.g., 10 for 10%)
     * @return The price after discount
     */
    public static BigDecimal calculatePriceAfterDiscount(BigDecimal price, BigDecimal discountPercent) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountAmount = calculateDiscount(price, discountPercent);
        return roundCurrency(price.subtract(discountAmount));
    }
}