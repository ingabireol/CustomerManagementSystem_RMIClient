package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Represents an OTP (One Time Password) in the business management system.
 * Used for email-based authentication and login verification.
 * Fixed for PostgreSQL compatibility.
 */
public class OTP implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    
    private String email;
    
    private String otpCode;
    
    private Date createdAt;
    
    private Date expiresAt;
    
    private boolean used;
    
    private int verificationAttempts;
    
    private String userAgent;
    
    private String ipAddress;
    
    // OTP types
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String TYPE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    
    private String otpType;
    
    // Constants
    public static final int OTP_LENGTH = 6;
    public static final int EXPIRY_MINUTES = 10;
    public static final int MAX_VERIFICATION_ATTEMPTS = 3;
    
    /**
     * Default constructor
     */
    public OTP() {
        this.createdAt = new Date();
        this.expiresAt = new Date(System.currentTimeMillis() + (EXPIRY_MINUTES * 60 * 1000));
        this.used = false;
        this.verificationAttempts = 0;
        this.otpType = TYPE_LOGIN;
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param email The email address for this OTP
     * @param otpCode The generated OTP code
     * @param otpType The type of OTP (LOGIN, PASSWORD_RESET, etc.)
     */
    public OTP(String email, String otpCode, String otpType) {
        this();
        this.email = email;
        this.otpCode = otpCode;
        this.otpType = otpType;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param email Email address
     * @param otpCode OTP code
     * @param createdAt Creation timestamp
     * @param expiresAt Expiration timestamp
     * @param used Whether OTP has been used
     * @param verificationAttempts Number of verification attempts
     * @param otpType Type of OTP
     * @param userAgent User agent string
     * @param ipAddress IP address
     */
    public OTP(int id, String email, String otpCode, Date createdAt, 
               Date expiresAt, boolean used, int verificationAttempts, 
               String otpType, String userAgent, String ipAddress) {
        this.id = id;
        this.email = email;
        this.otpCode = otpCode;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = used;
        this.verificationAttempts = verificationAttempts;
        this.otpType = otpType;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getVerificationAttempts() {
        return verificationAttempts;
    }

    public void setVerificationAttempts(int verificationAttempts) {
        this.verificationAttempts = verificationAttempts;
    }

    public String getOtpType() {
        return otpType;
    }

    public void setOtpType(String otpType) {
        this.otpType = otpType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    // Convenience methods for LocalDateTime compatibility
    
    /**
     * Gets created date as LocalDateTime
     * 
     * @return LocalDateTime representation of creation time
     */
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * Sets created date from LocalDateTime
     * 
     * @param createdAt LocalDateTime to convert and set
     */
    public void setCreatedAtFromLocalDateTime(LocalDateTime createdAt) {
        this.createdAt = Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Gets expiry date as LocalDateTime
     * 
     * @return LocalDateTime representation of expiry time
     */
    public LocalDateTime getExpiresAtAsLocalDateTime() {
        return expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * Sets expiry date from LocalDateTime
     * 
     * @param expiresAt LocalDateTime to convert and set
     */
    public void setExpiresAtFromLocalDateTime(LocalDateTime expiresAt) {
        this.expiresAt = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Checks if the OTP is expired
     * 
     * @return true if the OTP is expired
     */
    public boolean isExpired() {
        return new Date().after(expiresAt);
    }
    
    /**
     * Checks if the OTP is valid (not used, not expired, and within attempt limits)
     * 
     * @return true if the OTP is valid
     */
    public boolean isValid() {
        return !used && !isExpired() && verificationAttempts < MAX_VERIFICATION_ATTEMPTS;
    }
    
    /**
     * Increments the verification attempts counter
     */
    public void incrementVerificationAttempts() {
        this.verificationAttempts++;
    }
    
    /**
     * Marks the OTP as used
     */
    public void markAsUsed() {
        this.used = true;
    }
    
    /**
     * Gets the remaining time until expiration in minutes
     * 
     * @return Minutes until expiration, or 0 if already expired
     */
    public long getRemainingMinutes() {
        Date now = new Date();
        if (now.after(expiresAt)) {
            return 0;
        }
        long diffInMillis = expiresAt.getTime() - now.getTime();
        return diffInMillis / (60 * 1000);
    }
    
    @Override
    public String toString() {
        return "OTP [id=" + id + ", email=" + email + ", type=" + otpType + 
               ", created=" + createdAt + ", expires=" + expiresAt + 
               ", used=" + used + ", attempts=" + verificationAttempts + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OTP otp = (OTP) obj;
        return id == otp.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}