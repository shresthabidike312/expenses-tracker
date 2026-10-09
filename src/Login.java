import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {

    JTextField txtUsername;
    JPasswordField txtPassword;

    Color BLUE = new Color(52, 120, 246);
    Color BG   = new Color(240, 245, 255);

    public Login() {
        setTitle("Login");
        setSize(420, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Welcome", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(BLUE);
        title.setPreferredSize(new Dimension(0, 60));
        add(title, BorderLayout.NORTH);

        // Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 99, 235)),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        g.weightx = 1;
        g.gridx = 0;

        // Username
        g.gridy = 0;
        card.add(makeLabel("Username"), g);
        g.gridy = 1;
        txtUsername = new JTextField();
        styleComp(txtUsername);
        card.add(txtUsername, g);

        // Password
        g.gridy = 2;
        card.add(makeLabel("Password"), g);
        g.gridy = 3;
        txtPassword = new JPasswordField();
        styleComp(txtPassword);
        card.add(txtPassword, g);

        // Center wrapper
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        center.add(card, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));

        JButton btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setBackground(BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Times New Roman", Font.BOLD, 15));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(new Color(52, 255, 246)); }
            public void mouseExited(MouseEvent e)  { btnLogin.setBackground(BLUE); }
        });

        JLabel lblRegister = new JLabel("Don't have an account? Register", SwingConstants.CENTER);
        lblRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRegister.setForeground(BLUE);
        lblRegister.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        lblRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        bottom.add(btnLogin);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(lblRegister);
        add(bottom, BorderLayout.SOUTH);

        // Actions
        btnLogin.addActionListener(e -> loginUser());
        lblRegister.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); new Register(); }
        });

        setVisible(true);
    }

    private JLabel makeLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private void styleComp(JComponent f) {
        f.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void loginUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            msg("Please enter username and password."); return;
        }

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement pst = con.prepareStatement(
                    "SELECT * FROM users WHERE username=? AND password=?");
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));

                JOptionPane.showMessageDialog(this, "Welcome, " + user.getUsername() + "!");
                dispose();
                new ExpensesTracker(user);
            } else {
                msg("Invalid username or password.");
                txtPassword.setText("");
            }

        } catch (Exception ex) {
            msg("Database Error: " + ex.getMessage());
        }
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }

    public static void main(String[] args) { SwingUtilities.invokeLater(Login::new); }
}