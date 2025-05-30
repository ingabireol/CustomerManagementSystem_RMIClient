package ui.supplier;

import controller.SupplierController;
import javax.swing.*;
import java.awt.*;
import ui.UIFactory;

/**
 * Main view for the Supplier module.
 * Serves as a container for the supplier management interface.
 */
public class SupplierView extends JPanel {
    private SupplierController controller;
    
    /**
     * Constructor
     */
    public SupplierView() {
        setLayout(new BorderLayout());
        setBackground(UIFactory.BACKGROUND_COLOR);
        
        // Initialize controller
        controller = new SupplierController(this);
        
        // Add the list view to this panel
        add(controller.getListView(), BorderLayout.CENTER);
    }
    
    /**
     * Static method to create and show the supplier view in a frame
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
        JFrame frame = new JFrame("Supplier Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        
        // Create and add the supplier view
        SupplierView supplierView = new SupplierView();
        frame.add(supplierView);
        
        // Show the frame
        frame.setVisible(true);
    }
    
    /**
     * Main method for testing the supplier view
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showInFrame();
        });
    }
}