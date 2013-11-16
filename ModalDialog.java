import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ModalDialog extends JDialog implements ActionListener {
  public ModalDialog(JFrame parent, String title, String message) {
    super(parent, title, true);
    
    if (parent != null) {
      Dimension parentSize = parent.getSize(); 
      Point p = parent.getLocation(); 
      setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
    }
    
    JPanel messagePane = new JPanel();
    messagePane.add(new JLabel(message));
    getContentPane().add(messagePane);
    JPanel buttonPane = new JPanel();
    JButton button = new JButton("OK"); 
    buttonPane.add(button); 
    button.addActionListener(this);
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack(); 
    setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e) {
    setVisible(false); 
    dispose(); 
  }
}