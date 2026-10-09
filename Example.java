import javax.swing.*;
import java.awt.event.*;

public class FactorialUI extends JFrame implements ActionListener {

    JTextField t1, t2;
    JButton btn;

    FactorialUI() {
        setTitle("Factorial Calculator");
        setLayout(null);

        JLabel l1 = new JLabel("Enter Number");
        l1.setBounds(50, 50, 120, 25);
        add(l1);

        t1 = new JTextField();
        t1.setBounds(180, 50, 150, 25);
        add(t1);

        JLabel l2 = new JLabel("Factorial");
        l2.setBounds(50, 100, 120, 25);
        add(l2);

        t2 = new JTextField();
        t2.setBounds(180, 100, 150, 25);
        t2.setEditable(false);
        add(t2);

        btn = new JButton("OK");
        btn.setBounds(120, 150, 100, 30);
        add(btn);

        btn.addActionListener(this);

        setSize(400, 300);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
        int num = Integer.parseInt(t1.getText());
        int fact = 1;

        for(int i = 1; i <= num; i++) {
            fact *= i;
        }

        t2.setText(String.valueOf(fact));
    }

    public static void main(String[] args) {
        new FactorialUI();
    }
}