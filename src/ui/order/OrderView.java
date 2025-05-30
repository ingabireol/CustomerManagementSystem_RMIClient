package ui.order;

import controller.OrderController;
import javax.swing.*;
import java.awt.*;
import ui.UIFactory;

/**
 * Main view for the Order module.
 * Serves as a container for the order management interface.
 */
public class OrderView extends JPanel {
    private OrderController controller;
    
    /**
     * Constructor
     */
    public OrderView() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Initialize controller
        controller = new OrderController(this);
        
        // Add the list view to this panel
        add(controller.getListView(), BorderLayout.CENTER);
    }
    
    /**
     * Static method to create and show the order view in a frame
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
        JFrame frame = new JFrame("Order Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        
        // Create and add the order view
        OrderView orderView = new OrderView();
        frame.add(orderView);
        
        // Show the frame
        frame.setVisible(true);
    }
    
    /**
     * Main method for testing the order view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showInFrame();
        });
    }
}