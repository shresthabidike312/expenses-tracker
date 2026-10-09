import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class ExpensesTracker extends JFrame implements ActionListener {

    private final User currentUser;
    private JLabel lblTotalIncome, lblTotalExpense, lblBalance;
    private JTextField txtIncomeSource, txtIncomeAmount, txtIncomeDate, txtIncomeDesc;
    private JTextField txtExpenseAmount, txtExpenseDate, txtExpenseDesc;
    private JComboBox<CategoryModel> cmbExpenseCategory;
    private JButton btnAddIncome, btnAddExpense, btnRefresh, btnViewSummary, btnLogout;
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public ExpensesTracker(User user) {
        this.currentUser = user;
        setTitle("Expense Tracker - Welcome " + user.getUsername());
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(236, 240, 241));
        main.add(buildNavBar(user), BorderLayout.NORTH);
        main.add(buildContent(),    BorderLayout.CENTER);
        add(main);

        refreshData();
        loadCategories();
        setVisible(true);
    }

    // ── Nav Bar ───────────────────────────────────────────────────────────────
    private JPanel buildNavBar(User user) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(new Color(36, 36, 62));
        nav.setPreferredSize(new Dimension(1100, 60));

        JLabel title = new JLabel("  Dashboard");
        title.setFont(new Font("Times New Roman", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        nav.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        JLabel lbl = new JLabel(" " + user.getUsername());
        lbl.setForeground(Color.WHITE);
        right.add(lbl);

        btnRefresh     = navBtn(" Refresh");
        btnViewSummary = navBtn(" Summary");
        btnLogout      = navBtn(" Logout");
        right.add(btnRefresh); right.add(btnViewSummary); right.add(btnLogout);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.addActionListener(this);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Content layout ────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(null);

        addBounded(p, buildCards(),          20,  15, 1040, 100);
        addBounded(p, buildIncomePanel(),    20, 125,  505, 230);
        addBounded(p, buildExpensePanel(),  535, 125,  505, 230);
        addBounded(p, buildTable(),          20, 365, 1040, 225);

        int y = 600, h = 40, gap = 6, w = (1040 - gap * 3) / 4, x = 20;
        p.add(actionBtn("EDIT INCOME AMOUNT",          new Color(39,130,185), new Color(28,100,155), x,               y, w, h, e -> editAmount("INCOME")));
        p.add(actionBtn("EDIT EXPENSE AMOUNT",         new Color(180,120, 20), new Color(140, 90, 10), x+(w+gap),     y, w, h, e -> editAmount("EXPENSE")));
        p.add(actionBtn("EDIT CATEGORY",               new Color(39,110, 60), new Color(27,  80, 43), x+(w+gap)*2,    y, w, h, e -> editCategory()));
        p.add(actionBtn("DELETE SELECTED TRANSACTION", new Color(192, 57, 43), new Color(152, 40, 27), x+(w+gap)*3,  y, w, h, e -> deleteTransaction()));
        return p;
    }

    private void addBounded(JPanel parent, JComponent child, int x, int y, int w, int h) {
        child.setBounds(x, y, w, h);
        parent.add(child);
    }

    // ── Dashboard cards ───────────────────────────────────────────────────────
    private JPanel buildCards() {
        JPanel p = new JPanel(new GridLayout(1, 3, 20, 0));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblTotalIncome  = cardLabel(new Color(214,248,228), new Color(39,174, 96), " Income: 0.00");
        lblTotalExpense = cardLabel(new Color(252,220,215), new Color(192, 57, 43), " Expense: 0.00");
        lblBalance      = cardLabel(new Color(214,234,248), new Color(41, 128,185), " Balance: 0.00");

        for (JLabel l : new JLabel[]{lblTotalIncome, lblTotalExpense, lblBalance}) p.add(l.getParent());
        return p;
    }

    private JLabel cardLabel(Color bg, Color fg, String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Times New Roman", Font.BOLD, 16));
        lbl.setForeground(fg);
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(BorderFactory.createLineBorder(fg, 1));
        card.add(lbl, BorderLayout.CENTER);
        return lbl;
    }

    // ── Income panel ──────────────────────────────────────────────────────────
    private JPanel buildIncomePanel() {
        JPanel p = new JPanel(null);
        p.setBorder(BorderFactory.createTitledBorder(" Add Income"));
        p.setBackground(Color.WHITE);

        txtIncomeSource = field(p, "Source:", 20, 30);
        txtIncomeAmount = field(p, "Amount:", 20, 70);
        txtIncomeDate   = field(p, "Date:",   20, 110);
        txtIncomeDate.setText(LocalDate.now().toString());
        txtIncomeDesc   = field(p, "Desc:",   20, 150);

        btnAddIncome = new JButton(" Add Income");
        style(btnAddIncome, new Color(39, 174, 96));
        btnAddIncome.setBounds(150, 190, 160, 35);
        btnAddIncome.addActionListener(this);
        p.add(btnAddIncome);
        return p;
    }

    // ── Expense panel ─────────────────────────────────────────────────────────
    private JPanel buildExpensePanel() {
        JPanel p = new JPanel(null);
        p.setBorder(BorderFactory.createTitledBorder("Add Expense"));
        p.setBackground(Color.WHITE);

        lbl(p, "Category:", 20, 30);

        // Dropdown — narrowed to leave room for both buttons
        cmbExpenseCategory = new JComboBox<>();
        cmbExpenseCategory.setBounds(110, 30, 170, 30);
        p.add(cmbExpenseCategory);

        // NEW button — width 65, full text visible
        p.add(catBtn("NEW", new Color(46, 204, 113), new Color(39, 174, 96), 285, e -> addNewCategory()));

        // DEL button — starts immediately after NEW (285 + 65 = 350), no gap
        p.add(catBtn("DEL", new Color(231, 76, 60), new Color(192, 57, 43), 350, e -> deleteSelectedCategory()));

        txtExpenseAmount = field(p, "Amount:", 20, 75);
        txtExpenseAmount.setBounds(110, 75, 200, 30);

        txtExpenseDate = field(p, "Date:", 20, 115);
        txtExpenseDate.setBounds(110, 115, 200, 30);
        txtExpenseDate.setText(LocalDate.now().toString());

        txtExpenseDesc = field(p, "Desc:", 20, 155);
        txtExpenseDesc.setBounds(110, 155, 255, 30);

        btnAddExpense = new JButton("Add Expense");
        style(btnAddExpense, new Color(192, 57, 43));
        btnAddExpense.setForeground(Color.blue);
        btnAddExpense.setFont(new Font("Times New Roman", Font.BOLD, 10));
        btnAddExpense.setBounds(150, 195, 180, 45);
        btnAddExpense.addActionListener(this);
        p.add(btnAddExpense);

        return p;
    }

    // ── Transaction table ─────────────────────────────────────────────────────
    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("All Transactions"));

        tableModel = new DefaultTableModel(
                new String[]{"ID","Type","Category","Amount","Date","Description"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(24);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        transactionTable.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        transactionTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));

        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground("INCOME".equals(tableModel.getValueAt(row, 1))
                        ? new Color(232, 250, 240) : new Color(253, 237, 236));
                return c;
            }
        });
        p.add(new JScrollPane(transactionTable), BorderLayout.CENTER);
        return p;
    }

    // ── Edit amount ───────────────────────────────────────────────────────────
    private void editAmount(String expectedType) {
        int row = transactionTable.getSelectedRow();
        if (row == -1) { warn("Please select a row first.", "No Row Selected"); return; }

        int    id   = (int)    tableModel.getValueAt(row, 0);
        String type = (String) tableModel.getValueAt(row, 1);
        String cur  = (String) tableModel.getValueAt(row, 3);

        if (!type.equals(expectedType)) {
            warn("Selected row is " + type + ".\nUse the correct Edit button.", "Type Mismatch"); return;
        }
        String input = JOptionPane.showInputDialog(this,
                "Current: " + cur + "\nEnter new amount:", "Edit " + expectedType, JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            double amt = Double.parseDouble(input.trim());
            if (amt <= 0) { warn("Amount must be > 0!", "Invalid"); return; }
            boolean ok = "INCOME".equals(type) ? Income.updateIncomeAmount(id, amt)
                    : Expenses.updateExpenseAmount(id, amt);
            if (ok) { refreshData(); info("Amount updated: " + cur + " \u2192 " + String.format("%.2f", amt), "Updated"); }
            else      error("Failed to update amount.");
        } catch (NumberFormatException ex) { error("Invalid number entered."); }
    }

    // ── Edit category ─────────────────────────────────────────────────────────
    private void editCategory() {
        int row = transactionTable.getSelectedRow();
        if (row == -1) { warn("Please select a row first.", "No Row Selected"); return; }

        int    id   = (int)    tableModel.getValueAt(row, 0);
        String type = (String) tableModel.getValueAt(row, 1);
        String cur  = (String) tableModel.getValueAt(row, 2);

        if (!"EXPENSE".equals(type)) {
            warn("Category editing is only for EXPENSE rows.\nSelected: " + type, "Not an Expense"); return;
        }
        List<CategoryModel> cats = Category.getAllCategories(currentUser.getUserId());
        if (cats.isEmpty()) { warn("No categories found. Add one first.", "No Categories"); return; }

        JComboBox<CategoryModel> combo = new JComboBox<>();
        cats.forEach(combo::addItem);
        for (int i = 0; i < combo.getItemCount(); i++)
            if (combo.getItemAt(i).getCategoryName().equalsIgnoreCase(cur)) { combo.setSelectedIndex(i); break; }

        JPanel dlg = new JPanel(new BorderLayout(5, 8));
        dlg.add(new JLabel("Current: " + cur), BorderLayout.NORTH);
        dlg.add(new JLabel("Select new category:"), BorderLayout.CENTER);
        dlg.add(combo, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, dlg, "Edit Category — ID: " + id,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        CategoryModel sel = (CategoryModel) combo.getSelectedItem();
        if (sel == null || sel.getCategoryId() <= 0) { warn("Select a valid category.", "Invalid"); return; }
        if (sel.getCategoryName().equalsIgnoreCase(cur)) { info("No change made.", "No Change"); return; }

        if (Expenses.updateExpenseCategory(id, sel.getCategoryName())) {
            refreshData();
            info("Category: " + cur + " \u2192 " + sel.getCategoryName(), "Updated");
        } else error("Failed to update category.");
    }

    // ── Delete transaction ────────────────────────────────────────────────────
    private void deleteTransaction() {
        int row = transactionTable.getSelectedRow();
        if (row == -1) { warn("Please select a row first.", "No Row Selected"); return; }

        int    id   = (int)    tableModel.getValueAt(row, 0);
        String type = (String) tableModel.getValueAt(row, 1);

        if (JOptionPane.showConfirmDialog(this, "Delete this " + type + " (ID: " + id + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            boolean ok = "INCOME".equals(type) ? Income.deleteIncome(id) : Expenses.deleteExpense(id);
            if (ok) { refreshData(); info(type + " deleted. Balance updated.", "Deleted"); }
            else      error("Could not delete. Please try again.");
        } catch (Exception ex) { error("Unexpected error: " + ex.getMessage()); }
    }

    // ── Category helpers ──────────────────────────────────────────────────────
    private void loadCategories() {
        cmbExpenseCategory.removeAllItems();
        List<CategoryModel> cats = Category.getAllCategories(currentUser.getUserId());
        if (cats.isEmpty()) cmbExpenseCategory.addItem(new CategoryModel(0, 0, "No categories yet"));
        else cats.forEach(cmbExpenseCategory::addItem);
    }

    private void addNewCategory() {
        String input = JOptionPane.showInputDialog(this, "Enter new category name:", "New Category", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        if (Category.addCategory(currentUser.getUserId(), input.trim())) { info("Category added!","Success"); loadCategories(); }
        else error("Failed. Category may already exist.");
    }

    private void deleteSelectedCategory() {
        CategoryModel sel = (CategoryModel) cmbExpenseCategory.getSelectedItem();
        if (sel == null || sel.getCategoryId() <= 0) { warn("Select a valid category.", "None Selected"); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete '" + sel.getCategoryName() + "'?",
                "Delete Category", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (Category.deleteCategory(sel.getCategoryId())) { info("Category deleted!","Success"); loadCategories(); refreshData(); }
            else error("Failed to delete category.");
        }
    }

    // ── ActionListener ────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddIncome) {
            try {
                String src = txtIncomeSource.getText().trim();
                double amt = Double.parseDouble(txtIncomeAmount.getText().trim());
                if (src.isEmpty())  { warn("Source is required!", "Missing"); return; }
                if (amt <= 0)       { warn("Amount must be > 0!", "Invalid"); return; }
                Income.addIncome(currentUser.getUserId(), src, amt, txtIncomeDate.getText().trim(), txtIncomeDesc.getText().trim());
                refreshData();
                txtIncomeSource.setText(""); txtIncomeAmount.setText(""); txtIncomeDesc.setText("");
                info("Income added successfully!", "Success");
            } catch (NumberFormatException ex) { error("Invalid amount entered."); }
            catch (Exception ex)               { error("Error: " + ex.getMessage()); }
        }

        if (e.getSource() == btnAddExpense) {
            try {
                CategoryModel cat = (CategoryModel) cmbExpenseCategory.getSelectedItem();
                if (cat == null || cat.getCategoryId() <= 0) { warn("Select a valid category.", "Invalid"); return; }
                double amt = Double.parseDouble(txtExpenseAmount.getText().trim());
                if (amt <= 0) { warn("Amount must be > 0!", "Invalid"); return; }
                Expenses.addExpense(currentUser.getUserId(), cat.getCategoryName(), amt,
                        txtExpenseDate.getText().trim(), txtExpenseDesc.getText().trim());
                refreshData();
                txtExpenseAmount.setText(""); txtExpenseDesc.setText("");
                info("Expense added successfully!", "Success");
            } catch (NumberFormatException ex) { error("Invalid amount entered."); }
            catch (Exception ex)               { error("Error: " + ex.getMessage()); }
        }

        if (e.getSource() == btnRefresh)     { refreshData(); info("Data refreshed!", "Refresh"); }
        if (e.getSource() == btnViewSummary)   new ViewSummary(currentUser);
        if (e.getSource() == btnLogout &&
                JOptionPane.showConfirmDialog(this, "Sure you want to logout?", "Logout",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { new Login(); dispose(); }
    }

    // ── Refresh data ──────────────────────────────────────────────────────────
    private void refreshData() {
        double inc = Income.getTotalIncome(currentUser.getUserId());
        double exp = Expenses.getTotalExpenses(currentUser.getUserId());
        double bal = inc - exp;

        lblTotalIncome.setText(" Income: "   + String.format("%.2f", inc));
        lblTotalExpense.setText(" Expense: " + String.format("%.2f", exp));
        lblBalance.setText(" Balance: "      + String.format("%.2f", bal));
        lblBalance.setForeground(bal >= 0 ? new Color(39, 174, 96) : new Color(192, 57, 43));

        tableModel.setRowCount(0);
        for (Transaction t : Transaction.getAllTransactions(currentUser.getUserId()))
            tableModel.addRow(new Object[]{ t.getTransactionId(), t.getType(), t.getCategory(),
                    String.format("%.2f", t.getAmount()), t.getTransactionDate(), t.getDescription() });
    }

    // ── UI builder helpers ────────────────────────────────────────────────────
    private JTextField field(JPanel p, String label, int x, int y) {
        lbl(p, label, x, y);
        JTextField tf = new JTextField();
        tf.setBounds(120, y, 200, 30);
        p.add(tf);
        return tf;
    }

    private void lbl(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 85, 30);
        l.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        p.add(l);
    }

    private void style(JButton b, Color bg) {
        b.setBackground(bg); b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Fixed: width 65 so "NEW" / "DEL" text is always fully visible, zero gap between buttons
    private JButton catBtn(String text, Color normal, Color hover, int x, ActionListener al) {
        JButton b = new JButton(text);
        b.setBounds(x, 30, 65, 30);
        b.setBackground(normal); b.setForeground(Color.WHITE);
        b.setFont(new Font("Times New Roman", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { b.setBackground(normal); }
        });
        return b;
    }

    private JButton actionBtn(String text, Color normal, Color hover,
                              int x, int y, int w, int h, ActionListener al) {
        JButton b = new JButton(text);
        b.setBounds(x, y, w, h);
        b.setBackground(normal); b.setForeground(Color.WHITE);
        b.setFont(new Font("Times New Roman", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { b.setBackground(normal); }
        });
        return b;
    }

    // ── Dialog shortcuts ──────────────────────────────────────────────────────
    private void info(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE); }
    private void error(String msg)               { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}