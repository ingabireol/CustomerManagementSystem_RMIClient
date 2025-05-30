package ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Manages application theme settings including dark mode support.
 * Provides a centralized place for theme configuration and switching.
 */
public class ThemeManager {
    // Singleton instance
    private static ThemeManager instance;
    
    // Theme constants
    public static final String LIGHT_THEME = "light";
    public static final String DARK_THEME = "dark";
    public static final String BLUE_THEME = "blue";
    public static final String GREEN_THEME = "green";
    
    // Current theme
    private String currentTheme = LIGHT_THEME;
    
    // Preferences for storing theme settings
    private Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    
    // Theme listeners
    private java.util.List<ThemeChangeListener> listeners = new java.util.ArrayList<>();
    
    // Color maps for each theme
    private Map<String, Map<String, Color>> themeColors = new HashMap<>();
    
    /**
     * Interface for theme change listeners
     */
    public interface ThemeChangeListener {
        void onThemeChanged(String newTheme);
    }
    
    /**
     * Private constructor to enforce singleton pattern
     */
    private ThemeManager() {
        initializeThemes();
        loadSavedTheme();
    }
    
    /**
     * Gets the singleton instance
     * 
     * @return ThemeManager instance
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Initializes all theme color schemes
     */
    private void initializeThemes() {
        // Light theme (default)
        Map<String, Color> lightColors = new HashMap<>();
        lightColors.put("primary", new Color(0x1976D2));
        lightColors.put("secondary", new Color(0xFF5722));
        lightColors.put("background", new Color(0xF5F5F5));
        lightColors.put("card", Color.WHITE);
        lightColors.put("text", new Color(0x212121));
        lightColors.put("textSecondary", new Color(0x757575));
        lightColors.put("border", new Color(0xE0E0E0));
        lightColors.put("success", new Color(0x4CAF50));
        lightColors.put("warning", new Color(0xFFC107));
        lightColors.put("error", new Color(0xF44336));
        themeColors.put(LIGHT_THEME, lightColors);
        
        // Dark theme
        Map<String, Color> darkColors = new HashMap<>();
        darkColors.put("primary", new Color(0x2196F3));
        darkColors.put("secondary", new Color(0xFF7043));
        darkColors.put("background", new Color(0x303030));
        darkColors.put("card", new Color(0x424242));
        darkColors.put("text", new Color(0xFFFFFF));
        darkColors.put("textSecondary", new Color(0xBDBDBD));
        darkColors.put("border", new Color(0x616161));
        darkColors.put("success", new Color(0x81C784));
        darkColors.put("warning", new Color(0xFFD54F));
        darkColors.put("error", new Color(0xE57373));
        themeColors.put(DARK_THEME, darkColors);
        
        // Blue theme
        Map<String, Color> blueColors = new HashMap<>();
        blueColors.put("primary", new Color(0x0D47A1));
        blueColors.put("secondary", new Color(0x03A9F4));
        blueColors.put("background", new Color(0xE3F2FD));
        blueColors.put("card", Color.WHITE);
        blueColors.put("text", new Color(0x212121));
        blueColors.put("textSecondary", new Color(0x757575));
        blueColors.put("border", new Color(0xBBDEFB));
        blueColors.put("success", new Color(0x4CAF50));
        blueColors.put("warning", new Color(0xFFC107));
        blueColors.put("error", new Color(0xF44336));
        themeColors.put(BLUE_THEME, blueColors);
        
        // Green theme
        Map<String, Color> greenColors = new HashMap<>();
        greenColors.put("primary", new Color(0x2E7D32));
        greenColors.put("secondary", new Color(0x8BC34A));
        greenColors.put("background", new Color(0xE8F5E9));
        greenColors.put("card", Color.WHITE);
        greenColors.put("text", new Color(0x212121));
        greenColors.put("textSecondary", new Color(0x757575));
        greenColors.put("border", new Color(0xC8E6C9));
        greenColors.put("success", new Color(0x4CAF50));
        greenColors.put("warning", new Color(0xFFC107));
        greenColors.put("error", new Color(0xF44336));
        themeColors.put(GREEN_THEME, greenColors);
    }
    
    /**
     * Loads the saved theme preference
     */
    private void loadSavedTheme() {
        currentTheme = prefs.get("theme", LIGHT_THEME);
    }
    
    /**
     * Gets the current theme name
     * 
     * @return Current theme name
     */
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Checks if dark mode is active
     * 
     * @return true if dark mode is active
     */
    public boolean isDarkMode() {
        return DARK_THEME.equals(currentTheme);
    }
    
    /**
     * Switches the application theme
     * 
     * @param themeName The theme to switch to
     */
    public void setTheme(String themeName) {
        if (!themeColors.containsKey(themeName)) {
            throw new IllegalArgumentException("Unknown theme: " + themeName);
        }
        
        currentTheme = themeName;
        prefs.put("theme", themeName);
        
        // Apply the theme
        applyTheme();
        
        // Notify listeners
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(themeName);
        }
    }
    
    /**
     * Toggle between light and dark themes
     */
    public void toggleDarkMode() {
        if (isDarkMode()) {
            setTheme(LIGHT_THEME);
        } else {
            setTheme(DARK_THEME);
        }
    }
    
    /**
     * Applies the current theme to the UI
     */
    public void applyTheme() {
        try {
            // Get the colors for the current theme
            Map<String, Color> colors = themeColors.get(currentTheme);
            
            // Configure UI Manager colors
            UIManager.put("Panel.background", colors.get("background"));
            UIManager.put("Button.background", colors.get("primary"));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Label.foreground", colors.get("text"));
            UIManager.put("TextField.background", colors.get("card"));
            UIManager.put("TextField.foreground", colors.get("text"));
            UIManager.put("TextField.caretForeground", colors.get("primary"));
            UIManager.put("TextArea.background", colors.get("card"));
            UIManager.put("TextArea.foreground", colors.get("text"));
            UIManager.put("Table.background", colors.get("card"));
            UIManager.put("Table.foreground", colors.get("text"));
            UIManager.put("Table.gridColor", colors.get("border"));
            UIManager.put("TableHeader.background", colors.get("background"));
            UIManager.put("TableHeader.foreground", colors.get("text"));
            UIManager.put("ComboBox.background", colors.get("card"));
            UIManager.put("ComboBox.foreground", colors.get("text"));
            UIManager.put("ScrollPane.background", colors.get("card"));
            UIManager.put("List.background", colors.get("card"));
            UIManager.put("List.foreground", colors.get("text"));
            
            // Update the UI
            SwingUtilities.updateComponentTreeUI(getActiveWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gets a color from the current theme
     * 
     * @param colorKey The color key
     * @return The color from current theme
     */
    public Color getColor(String colorKey) {
        Map<String, Color> colors = themeColors.get(currentTheme);
        if (colors.containsKey(colorKey)) {
            return colors.get(colorKey);
        }
        return Color.BLACK; // Default fallback
    }
    
    /**
     * Adds a theme change listener
     * 
     * @param listener The listener to add
     */
    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a theme change listener
     * 
     * @param listener The listener to remove
     */
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Gets the active window for updating UI
     * 
     * @return The active window or null if none
     */
    private Window getActiveWindow() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window.isVisible()) {
                return window;
            }
        }
        return null;
    }
}