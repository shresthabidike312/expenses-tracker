import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ViewSummary extends JFrame {

    private final User currentUser;
    final Color BLUE  = new Color(52, 120, 246);
    final Color GREEN = new Color(39, 174, 96);
    final Color RED   = new Color(192, 57, 43);
    final Color BG    = new Color(240, 245, 255);

    public ViewSummary(User user) {
        this.currentUser = user;
        setTitle("Summary");
        setSize(860, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(buildNav(),    BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── Nav ───────────────────────────────────────────────────────────────────
    private JPanel buildNav() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(BLUE);
        nav.setPreferredSize(new Dimension(0, 55));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel t = new JLabel("View Summary — " + currentUser.getUsername());
        t.setFont(new Font("Times New Roman", Font.BOLD, 20));
        t.setForeground(Color.WHITE);
        nav.add(t, BorderLayout.WEST);
        return nav;
    }


    // ── Body ──────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        double inc = Income.getTotalIncome(currentUser.getUserId());
        double exp = Expenses.getTotalExpenses(currentUser.getUserId());
        double bal = inc - exp;

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(BG);
        body.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        body.add(buildCards(inc, exp, bal), BorderLayout.NORTH);
        body.add(buildTables(),             BorderLayout.CENTER);
        return body;
    }

    // ── Cards ─────────────────────────────────────────────────────────────────
    private JPanel buildCards(double inc, double exp, double bal) {
        JPanel p = new JPanel(new GridLayout(1, 3, 12, 0));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(0, 80));
        card(p, "Total Income",   String.format("Rs. %.2f", inc), new Color(220,255,235), GREEN);
        card(p, "Total Expense",  String.format("Rs. %.2f", exp), new Color(255,225,220), RED);
        card(p, "Balance",        String.format("Rs. %.2f", bal), new Color(220,235,255), bal >= 0 ? BLUE : RED);
        return p;
    }

    private void card(JPanel parent, String title, String value, Color bg, Color fg) {
        JPanel c = new JPanel(new GridLayout(2, 1));
        c.setBackground(bg);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        JLabel t = new JLabel(title); t.setFont(new Font("Times New Roman", Font.PLAIN, 12)); t.setForeground(fg);
        JLabel v = new JLabel(value); v.setFont(new Font("Times New Roman", Font.BOLD, 20)); v.setForeground(fg);
        c.add(t); c.add(v); parent.add(c);
    }

    // ── Tables ────────────────────────────────────────────────────────────────
    private JPanel buildTables() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 12));
        p.setBackground(BG);
        p.add(tableSection("Income Details",  getIncomeModel()));
        p.add(tableSection("Expense Details", getExpenseModel()));
        return p;
    }

    private JPanel tableSection(String title, DefaultTableModel model) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(BLUE);
        p.add(lbl, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(BG);
        table.getTableHeader().setReorderingAllowed(false);
        table.setGridColor(new Color(220, 225, 235));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));
        wrap.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(wrap, BorderLayout.CENTER);
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));

        JButton btn = new JButton("Close");
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setBackground(BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> dispose());
        p.add(btn);
        return p;
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    private DefaultTableModel getIncomeModel() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"Source","Amount","Date","Description"}, 0);
        List<Income> list = Income.getIncomesByUser(currentUser.getUserId());
        if (list == null || list.isEmpty()) { m.addRow(new Object[]{"No Data","-","-","-"}); return m; }
        for (Income i : list)
            m.addRow(new Object[]{ i.getSource(), String.format("%.2f", i.getAmount()), i.getIncomeDate(), i.getDescription() });
        return m;
    }

    private DefaultTableModel getExpenseModel() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"Category","Amount","Date","Description"}, 0);
        List<Expenses> list = Expenses.getExpensesByUser(currentUser.getUserId());
        if (list == null || list.isEmpty()) { m.addRow(new Object[]{"No Data","-","-","-"}); return m; }
        for (Expenses e : list)
            m.addRow(new Object[]{ e.getCategory(), String.format("%.2f", e.getAmount()), e.getExpenseDate(), e.getDescription() });
        return m;
    }
}