package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for security operations including password hashing.
 */
public class SecurityUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Generates a random salt
     * 
     * @return A random salt as a byte array
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }
    
    /**
     * Generates a random salt and returns it as a Base64 encoded string
     * 
     * @return Base64 encoded salt
     */
    public static String generateSaltString() {
        return Base64.getEncoder().encodeToString(generateSalt());
    }
    
    /**
     * Hashes a password with a provided salt
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password
     */
    public static byte[] hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            return md.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            LogUtil.error("Hashing algorithm not available", e);
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }
    
    /**
     * Hashes a password with a provided salt and returns it as a Base64 encoded string
     * 
     * @param password The password to hash
     * @param saltString Base64 encoded salt
     * @return Base64 encoded hashed password
     */
    public static String hashPasswordString(String password, String saltString) {
        byte[] salt = Base64.getDecoder().decode(saltString);
        byte[] hash = hashPassword(password, salt);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * Verifies a password against a stored hash and salt
     * 
     * @param password The password to verify
     * @param storedHash The stored hash to compare against
     * @param storedSalt The salt used to create the stored hash
     * @return true if the password matches
     */
    public static boolean verifyPassword(String password, byte[] storedHash, byte[] storedSalt) {
        byte[] newHash = hashPassword(password, storedSalt);
        
        // Compare the hashes using a constant-time comparison
        if (newHash.length != storedHash.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < newHash.length; i++) {
            result |= newHash[i] ^ storedHash[i];
        }
        return result == 0;
    }
    
    /**
     * Verifies a password against a stored hash and salt (both as Base64 encoded strings)
     * 
     * @param password The password to verify
     * @param storedHashString Base64 encoded stored hash
     * @param storedSaltString Base64 encoded stored salt
     * @return true if the password matches
     */
    public static boolean verifyPasswordString(String password, String storedHashString, String storedSaltString) {
        byte[] storedHash = Base64.getDecoder().decode(storedHashString);
        byte[] storedSalt = Base64.getDecoder().decode(storedSaltString);
        return verifyPassword(password, storedHash, storedSalt);
    }
    
    /**
     * Generates a secure random token
     * 
     * @param length The length of the token in bytes
     * @return Base64 encoded token
     */
    public static String generateToken(int length) {
        byte[] tokenBytes = new byte[length];
        RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Sanitizes input to prevent SQL injection and XSS attacks
     * 
     * @param input The input to sanitize
     * @return Sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Replace potentially dangerous characters
        String sanitized = input.replaceAll("<", "&lt;")
                               .replaceAll(">", "&gt;")
                               .replaceAll("\"", "&quot;")
                               .replaceAll("'", "&#39;")
                               .replaceAll(";", "&#59;");
        
        return sanitized;
    }
}