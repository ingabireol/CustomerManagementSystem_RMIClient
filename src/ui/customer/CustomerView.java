package ui.customer;

import controller.CustomerController;
import javax.swing.*;
import java.awt.*;
import ui.UIFactory;

/**
 * Main view for the Customer module.
 * Serves as a container for the customer management interface.
 */
public class CustomerView extends JPanel {
    private CustomerController controller;
    
    /**
     * Constructor
     */
    public CustomerView() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Initialize controller
        controller = new CustomerController(this);
        
        // Add the list view to this panel
        add(controller.getListView(), BorderLayout.CENTER);
    }
    
    /**
     * Static method to create and show the customer view in a frame
     * for testing purposes
     */
    public static void showInFrame() {
        // Set up UI look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Set global font settings
            UIManager.put("Label.font", UIFactory.BODY_FONT);
            UIManager.put("Button.font", UIFactory.BODY_FONT);
            UIManager.put("TextField.font", UIFactory.BODY_FONT);
            UIManager.put("Table.font", UIFactory.BODY_FONT);
            UIManager.put("TableHeader.font", UIFactory.HEADER_FONT);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create the frame
        JFrame frame = new JFrame("Customer Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        
        // Create and add the customer view
        CustomerView customerView = new CustomerView();
        frame.add(customerView);
        
        // Show the frame
        frame.setVisible(true);
    }
    
    /**
     * Main method for testing the customer view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showInFrame();
        });
    }
}