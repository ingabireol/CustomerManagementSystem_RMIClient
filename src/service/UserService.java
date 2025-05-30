package service;

import model.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface for User operations.
 * Defines the contract for user management and authentication operations.
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
     * Authenticates a user with username and password
     * 
     * @param username The username
     * @param password The password (plaintext)
     * @return The authenticated user if successful, null otherwise
     * @throws RemoteException If RMI communication fails
     */
    User authenticateUser(String username, String password) throws RemoteException;
    
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
}