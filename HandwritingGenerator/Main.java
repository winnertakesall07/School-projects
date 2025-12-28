import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main class for the Handwriting Generator application.
 * Creates a simple GUI where users can type text and see it rendered
 * in a simulated handwriting style with unique variations each time.
 * 
 * @author School Project
 * @version 1.0
 */
public class Main extends JFrame {
    private JTextField textField;
    private JButton writeButton;
    private HandwritingPanel handwritingPanel;
    
    /**
     * Constructor for the Main class.
     * Sets up the GUI components and layout.
     */
    public Main() {
        setTitle("Handwriting Generator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create top panel for input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        
        JLabel label = new JLabel("Enter text:");
        textField = new JTextField(30);
        writeButton = new JButton("Write");
        
        inputPanel.add(label);
        inputPanel.add(textField);
        inputPanel.add(writeButton);
        
        // Create the handwriting panel
        handwritingPanel = new HandwritingPanel();
        handwritingPanel.setBackground(Color.WHITE);
        
        // Add components to frame
        add(inputPanel, BorderLayout.NORTH);
        add(handwritingPanel, BorderLayout.CENTER);
        
        // Add button listener
        writeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText();
                if (!text.isEmpty()) {
                    handwritingPanel.setText(text);
                }
            }
        });
        
        // Also allow Enter key to trigger write
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeButton.doClick();
            }
        });
        
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }
    
    /**
     * Main method to launch the application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }
}
