package service;

import model.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Enhanced Remote service interface for User operations.
 * Defines the contract for user management and authentication operations including OTP login.
 */
public interface UserService extends Remote {
    
    /**
     * Creates a new user
     * 
     * @param user The user to create
     * @return The created user with generated ID
     * @throws RemoteException If RMI communication fails
     */
    User createUser(User user) throws RemoteException;
    
    /**
     * Updates an existing user
     * 
     * @param user The user to update
     * @return The updated user
     * @throws RemoteException If RMI communication fails
     */
    User updateUser(User user) throws RemoteException;
    
    /**
     * Updates a user's password
     * 
     * @param userId The ID of the user
     * @param newPassword The new password (plaintext)
     * @return Number of rows affected
     * @throws RemoteException If RMI communication fails
     */
    int updatePassword(int userId, String newPassword) throws RemoteException;
    
    /**
     * Deletes a user
     * 
     * @param user The user to delete
     * @return The deleted user
     * @throws RemoteException If RMI communication fails
     */
    User deleteUser(User user) throws RemoteException;
    
    /**
     * Finds a user by their database ID
     * 
     * @param id The database ID to search for
     * @return The user if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    User findUserById(int id) throws RemoteException;
    
    /**
     * Finds a user by their username
     * 
     * @param username The username to search for
     * @return The user if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    User findUserByUsername(String username) throws RemoteException;
    
    /**
     * Finds a user by email address
     * 
     * @param email The email to search for
     * @return The user if found, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    User findUserByEmail(String email) throws RemoteException;
    
    /**
     * Gets all users
     * 
     * @return List of all users
     * @throws RemoteException If RMI communication fails
     */
    List<User> findAllUsers() throws RemoteException;
    
    /**
     * Finds users by role
     * 
     * @param role The role to search for
     * @return List of matching users
     * @throws RemoteException If RMI communication fails
     */
    List<User> findUsersByRole(String role) throws RemoteException;
    
    /**
     * Authenticates a user with username and password (traditional login)
     * 
     * @param username The username
     * @param password The password (plaintext)
     * @return The authenticated user if successful, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    User authenticateUser(String username, String password) throws RemoteException;
    
    /**
     * Initiates OTP-based login by sending OTP to user's email
     * 
     * @param email The email address of the user
     * @return true if OTP was sent successfully, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean initiateOTPLogin(String email) throws RemoteException;
    
    /**
     * Completes OTP-based login by verifying the OTP code
     * 
     * @param email The email address of the user
     * @param otpCode The OTP code provided by the user
     * @return The authenticated user if OTP is valid, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    User completeOTPLogin(String email, String otpCode) throws RemoteException;
    
    /**
     * Initiates password reset by sending OTP to user's email
     * 
     * @param email The email address of the user
     * @return true if OTP was sent successfully, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean initiatePasswordReset(String email) throws RemoteException;
    
    /**
     * Verifies password reset OTP
     * 
     * @param email The email address of the user
     * @param otpCode The OTP code provided by the user
     * @return true if OTP is valid, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean verifyPasswordResetOTP(String email, String otpCode) throws RemoteException;
    
    /**
     * Completes password reset with new password (after OTP verification)
     * 
     * @param email The email address of the user
     * @param otpCode The verified OTP code
     * @param newPassword The new password
     * @return true if password was reset successfully, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean completePasswordReset(String email, String otpCode, String newPassword) throws RemoteException;
    
    /**
     * Checks if a username already exists
     * 
     * @param username The username to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean usernameExists(String username) throws RemoteException;
    
    /**
     * Checks if an email already exists
     * 
     * @param email The email to check
     * @return true if exists, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean emailExists(String email) throws RemoteException;
    
    /**
     * Creates a default admin user if no users exist
     * 
     * @return The created admin user, or null if failed or already exists
     * @throws RemoteException If RMI communication fails
     */
    User createDefaultAdmin() throws RemoteException;
    
    /**
     * Validates email format
     * 
     * @param email The email to validate
     * @return true if email format is valid
     * @throws RemoteException If RMI communication fails
     */
    boolean isValidEmail(String email) throws RemoteException;
    
    /**
     * Checks if OTP login is rate limited for an email
     * 
     * @param email The email to check
     * @return true if rate limited, false otherwise
     * @throws RemoteException If RMI communication fails
     */
    boolean isOTPRateLimited(String email) throws RemoteException;
    
    /**
     * Gets the remaining time until next OTP can be sent (in minutes)
     * 
     * @param email The email to check
     * @return Minutes until next OTP can be sent, 0 if can send now
     * @throws RemoteException If RMI communication fails
     */
    int getOTPCooldownMinutes(String email) throws RemoteException;
}