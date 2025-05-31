package service;

import model.OTP;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote service interface for OTP operations.
 * Defines the contract for OTP-related business operations.
 */
public interface OTPService extends Remote {
    
    /**
     * Generates and sends an OTP to the specified email address
     * 
     * @param email The email address to send OTP to
     * @param otpType The type of OTP (LOGIN, PASSWORD_RESET, etc.)
     * @param userAgent The user agent string (optional)
     * @param ipAddress The IP address (optional)
     * @return The created OTP object if successful, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    OTP generateAndSendOTP(String email, String otpType, String userAgent, String ipAddress) throws RemoteException;
    
    /**
     * Verifies an OTP code for the given email
     * 
     * @param email The email address
     * @param otpCode The OTP code to verify
     * @param otpType The type of OTP
     * @return The verified OTP object if valid, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    OTP verifyOTP(String email, String otpCode, String otpType) throws RemoteException;
    
    /**
     * Generates and sends a login OTP (convenience method)
     * 
     * @param email The email address to send OTP to
     * @return The created OTP object if successful, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    OTP generateLoginOTP(String email) throws RemoteException;
    
    /**
     * Verifies a login OTP (convenience method)
     * 
     * @param email The email address
     * @param otpCode The OTP code to verify
     * @return The verified OTP object if valid, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    OTP verifyLoginOTP(String email, String otpCode) throws RemoteException;
    
    /**
     * Checks if an email is rate limited (too many OTPs sent recently)
     * 
     * @param email The email address to check
     * @param otpType The type of OTP
     * @return true if rate limited, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean isRateLimited(String email, String otpType) throws RemoteException;
    
    /**
     * Gets the latest OTP for an email (for checking status)
     * 
     * @param email The email address
     * @param otpType The type of OTP
     * @return The latest OTP if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    OTP getLatestOTP(String email, String otpType) throws RemoteException;
    
    /**
     * Resends an OTP if the previous one is close to expiring
     * 
     * @param email The email address
     * @param otpType The type of OTP
     * @return The new OTP object if successful, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    OTP resendOTP(String email, String otpType) throws RemoteException;
    
    /**
     * Cleans up expired OTPs (maintenance method)
     * 
     * @return Number of cleaned up OTPs
     * @throws RemoteException If RMI communication fails
     */
    int cleanupExpiredOTPs() throws RemoteException;
    
    /**
     * Validates email format
     * 
     * @param email The email to validate
     * @return true if email format is valid
     * @throws RemoteException If RMI communication fails
     */
    boolean isValidEmail(String email) throws RemoteException;
}