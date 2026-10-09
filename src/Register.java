import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Calendar;

public class Register extends JFrame {

    JTextField txtUsername, txtContact, txtEmail;
    JPasswordField txtPassword, txtConfirmPassword;
    JComboBox<String> cmbDay, cmbMonth, cmbYear;

    Color BLUE = new Color(52, 120, 246);
    Color BG   = new Color(240, 245, 255);


    public Register() {
        setTitle("Register");
        setSize(420, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
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
                BorderFactory.createLineBorder(new Color(220,225,235)),
                BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        g.weightx = 1;
        g.gridx = 0;

        txtUsername        = addField(card, g, "Username");
        txtContact         = addField(card, g, "Contact");
        txtEmail           = addField(card, g, "Email");
        addDOB(card, g);
        txtPassword        = addPassField(card, g, "Password");
        txtConfirmPassword = addPassField(card, g, "Confirm Password");

        // Center card with padding
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        center.add(card, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));

        JButton btnSignup = new JButton("Sign Up");
        btnSignup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSignup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSignup.setBackground(BLUE);
        btnSignup.setForeground(Color.WHITE);
        btnSignup.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSignup.setFocusPainted(false);
        btnSignup.setBorderPainted(false);
        btnSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSignup.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnSignup.setBackground(new Color(30,90,210)); }
            public void mouseExited(MouseEvent e)  { btnSignup.setBackground(BLUE); }
        });

        JLabel lblLogin = new JLabel("Already have an account? Login", SwingConstants.CENTER);
        lblLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogin.setForeground(BLUE);
        lblLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        bottom.add(btnSignup);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(lblLogin);
        add(bottom, BorderLayout.SOUTH);

        btnSignup.addActionListener(e -> registerUser());
        lblLogin.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); new Login().setVisible(true); }
        });

        setVisible(true);
    }

    private JTextField addField(JPanel p, GridBagConstraints g, String label) {
        g.gridy++;
        p.add(makeLabel(label), g);
        g.gridy++;
        JTextField f = new JTextField();
        styleComp(f);
        p.add(f, g);
        return f;
    }

    private JPasswordField addPassField(JPanel p, GridBagConstraints g, String label) {
        g.gridy++;
        p.add(makeLabel(label), g);
        g.gridy++;
        JPasswordField f = new JPasswordField();
        styleComp(f);
        p.add(f, g);
        return f;
    }

    private void addDOB(JPanel p, GridBagConstraints g) {
        String[] days = new String[32]; days[0] = "Day";
        for (int i = 1; i <= 31; i++) days[i] = String.valueOf(i);
        String[] months = {"Month","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        int yr = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[101]; years[0] = "Year";
        for (int i = 1; i <= 100; i++) years[i] = String.valueOf(yr - i);

        cmbDay = new JComboBox<>(days);
        cmbMonth = new JComboBox<>(months);
        cmbYear = new JComboBox<>(years);
        for (JComboBox<String> c : new JComboBox[]{cmbDay, cmbMonth, cmbYear}) {
            c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            c.setBackground(Color.WHITE);
        }

        g.gridy++;
        p.add(makeLabel("Date of Birth"), g);

        JPanel dob = new JPanel(new GridLayout(1, 3, 8, 0));
        dob.setBackground(Color.WHITE);
        dob.add(cmbDay); dob.add(cmbMonth); dob.add(cmbYear);
        g.gridy++;
        p.add(dob, g);
    }

    private JLabel makeLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private void styleComp(JComponent f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,210,230)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void registerUser() {
        String username = txtUsername.getText().trim();
        String contact  = txtContact.getText().trim();
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm  = new String(txtConfirmPassword.getPassword());

        if (username.isEmpty() || contact.isEmpty() || email.isEmpty() || password.isEmpty()) {
            msg("Please fill in all fields."); return; }
        if (!email.contains("@") || !email.contains(".")) {
            msg("Enter a valid email."); return; }
        if (!contact.matches("\\d{10}")) {
            msg("Contact must be 10 digits."); return; }
        if (password.length() < 6) {
            msg("Password must be at least 6 characters."); return; }
        if (!password.equals(confirm)) {
            msg("Passwords do not match."); return; }
        if (cmbDay.getSelectedIndex()==0 || cmbMonth.getSelectedIndex()==0 || cmbYear.getSelectedIndex()==0) {
            msg("Select your date of birth."); return; }

        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE email=?");
            ps.setString(1, email);
            if (ps.executeQuery().next()) { msg("Email already registered!"); return; }

            int id = 1;
            ResultSet rs = con.createStatement().executeQuery("SELECT MAX(user_id) FROM users");
            if (rs.next() && rs.getObject(1) != null) id = rs.getInt(1) + 1;

            String dob = cmbYear.getSelectedItem() + "-" +
                    String.format("%02d", cmbMonth.getSelectedIndex()) + "-" +
                    String.format("%02d", Integer.parseInt(cmbDay.getSelectedItem().toString()));

            PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO users (user_id,username,contact,email,password,dob) VALUES (?,?,?,?,?,?)");
            pst.setInt(1, id); pst.setString(2, username); pst.setString(3, contact);
            pst.setString(4, email); pst.setString(5, password); pst.setString(6, dob);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration Successful!");
            dispose();
            new Login().setVisible(true);

        } catch (Exception ex) { msg("Error: " + ex.getMessage()); }
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }

    public static void main(String[] args) { SwingUtilities.invokeLater(Register::new); }
}