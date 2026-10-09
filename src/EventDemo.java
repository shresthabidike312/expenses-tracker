import javax.swing.*;
import java.awt.event.*;

public class EventDemo extends JFrame implements ActionListener {

    JButton b;

    EventDemo() {

        b = new JButton("Click");

        b.setBounds(100,100,100,40);

        add(b);

        b.addActionListener(this);

        setSize(300,300);
        setLayout(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        System.out.println("Button Clicked");

    }

    public static void main(String[] args) {

        new EventDemo();

    }
}