package ui.auth;

import model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the current user session.
 * Provides access to the authenticated user and handles session events.
 */
public class Session {
    // Singleton instance
    private static Session instance;
    
    // Current user
    private User currentUser;
    
    // Session listeners
    private List<SessionListener> listeners = new ArrayList<>();
    
    /**
     * Interface for session events
     */
    public interface SessionListener {
        /**
         * Called when a user logs in
         * 
         * @param user The user who logged in
         */
        void onLogin(User user);
        
        /**
         * Called when a user logs out
         */
        void onLogout();
    }
    
    /**
     * Private constructor to enforce singleton pattern
     */
    private Session() {
        // Nothing to initialize
    }
    
    /**
     * Gets the singleton instance
     * 
     * @return The Session instance
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    /**
     * Gets the current authenticated user
     * 
     * @return The current user or null if not authenticated
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the current user and notifies listeners
     * 
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Notify listeners of login
        if (user != null) {
            for (SessionListener listener : listeners) {
                listener.onLogin(user);
            }
        }
    }
    
    /**
     * Checks if a user is currently logged in
     * 
     * @return true if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Logs out the current user and notifies listeners
     */
    public void logout() {
        this.currentUser = null;
        
        // Notify listeners of logout
        for (SessionListener listener : listeners) {
            listener.onLogout();
        }
    }
    
    /**
     * Checks if the current user has admin role
     * 
     * @return true if the current user is an admin
     */
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }
    
    /**
     * Checks if the current user has manager role
     * 
     * @return true if the current user is a manager
     */
    public boolean isManager() {
        return isLoggedIn() && currentUser.isManager();
    }
    
    /**
     * Adds a session listener
     * 
     * @param listener The listener to add
     */
    public void addSessionListener(SessionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a session listener
     * 
     * @param listener The listener to remove
     */
    public void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }
}