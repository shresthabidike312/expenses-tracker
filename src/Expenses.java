import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Expenses {
    private int expenseId;
    private int userId;
    private String category_name;
    private double amount;
    private Date expenseDate;
    private String description;

    public Expenses() {}

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCategory() { return category_name; }
    public void setCategory(String category) { this.category_name = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Date getExpenseDate() { return expenseDate; }
    public void setExpenseDate(Date expenseDate) { this.expenseDate = expenseDate; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }

    // ================= CHECK IF ref_id COLUMN EXISTS =================
    private static boolean hasRefIdColumn(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "transactions", "ref_id");
            return rs.next();
        } catch (SQLException e) { return false; }
    }

    // ================= ADD EXPENSE =================
    public static boolean addExpense(int userId, String category, double amount, String date, String description) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            int newExpenseId = -1;
            String sql1 = "INSERT INTO expenses (user_id, category_name, amount, expense_date, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId); ps.setString(2, category); ps.setDouble(3, amount);
                ps.setDate(4, Date.valueOf(date)); ps.setString(5, description);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) newExpenseId = keys.getInt(1);
            }

            boolean useRefId = hasRefIdColumn(conn);
            String sql2 = useRefId
                    ? "INSERT INTO transactions (user_id, type, category_name, amount, transaction_date, description, ref_id) VALUES (?, 'EXPENSE', ?, ?, ?, ?, ?)"
                    : "INSERT INTO transactions (user_id, type, category_name, amount, transaction_date, description) VALUES (?, 'EXPENSE', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, userId); ps.setString(2, category); ps.setDouble(3, amount);
                ps.setDate(4, Date.valueOf(date)); ps.setString(5, description);
                if (useRefId) ps.setInt(6, newExpenseId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally { closeQuietly(conn); }
    }

    // ================= DELETE EXPENSE =================
    // Tries ref_id (new rows) first; falls back to safe subquery match for old rows
    public static boolean deleteExpense(int transactionId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            int refId = -1; int uId = 0; String cat = ""; double amt = 0; Date dt = null;
            boolean useRefId = hasRefIdColumn(conn);

            String fetchSql = useRefId
                    ? "SELECT user_id, category_name, amount, transaction_date, ref_id FROM transactions WHERE transaction_id = ? AND type = 'EXPENSE'"
                    : "SELECT user_id, category_name, amount, transaction_date FROM transactions WHERE transaction_id = ? AND type = 'EXPENSE'";

            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    uId = rs.getInt("user_id"); cat = rs.getString("category_name");
                    amt = rs.getDouble("amount"); dt = rs.getDate("transaction_date");
                    if (useRefId) refId = rs.getInt("ref_id");
                } else {
                    conn.rollback(); return false;
                }
            }

            // Delete transaction row first
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE transaction_id = ?")) {
                ps.setInt(1, transactionId); ps.executeUpdate();
            }

            // Delete expense row — precise if ref_id available, safe fallback otherwise
            if (useRefId && refId > 0) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE expense_id = ?")) {
                    ps.setInt(1, refId); ps.executeUpdate();
                }
            } else {
                // Subquery trick: MySQL won't let you DELETE...WHERE id IN (SELECT from same table) directly,
                // so we wrap in an extra subquery alias to force a temp copy — this is safe and portable.
                String fallback =
                        "DELETE FROM expenses WHERE expense_id = (" +
                                "  SELECT expense_id FROM (" +
                                "    SELECT expense_id FROM expenses" +
                                "    WHERE user_id=? AND category_name=? AND amount=? AND expense_date=?" +
                                "    ORDER BY expense_id ASC LIMIT 1" +
                                "  ) AS tmp)";
                try (PreparedStatement ps = conn.prepareStatement(fallback)) {
                    ps.setInt(1, uId); ps.setString(2, cat); ps.setDouble(3, amt); ps.setDate(4, dt);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally { closeQuietly(conn); }
    }

    // ================= GET TOTAL =================
    public static double getTotalExpenses(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException ex) { ex.printStackTrace(); }
        return 0;
    }

    // ================= UPDATE CATEGORY =================
    public static boolean updateExpenseCategory(int transactionId, String newCategory) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            int refId = -1; int uId = 0; String oldCat = ""; double amt = 0; Date dt = null;
            boolean useRefId = hasRefIdColumn(conn);
            String fetchSql = useRefId
                    ? "SELECT user_id, category_name, amount, transaction_date, ref_id FROM transactions WHERE transaction_id = ? AND type = 'EXPENSE'"
                    : "SELECT user_id, category_name, amount, transaction_date FROM transactions WHERE transaction_id = ? AND type = 'EXPENSE'";
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, transactionId); ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    uId = rs.getInt("user_id"); oldCat = rs.getString("category_name");
                    amt = rs.getDouble("amount"); dt = rs.getDate("transaction_date");
                    if (useRefId) refId = rs.getInt("ref_id");
                } else { conn.rollback(); return false; }
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE transactions SET category_name = ? WHERE transaction_id = ?")) {
                ps.setString(1, newCategory); ps.setInt(2, transactionId); ps.executeUpdate();
            }
            if (useRefId && refId > 0) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE expenses SET category_name = ? WHERE expense_id = ?")) {
                    ps.setString(1, newCategory); ps.setInt(2, refId); ps.executeUpdate();
                }
            } else {
                String fallback = "UPDATE expenses SET category_name = ? WHERE expense_id = (SELECT expense_id FROM (SELECT expense_id FROM expenses WHERE user_id=? AND category_name=? AND amount=? AND expense_date=? ORDER BY expense_id ASC LIMIT 1) AS tmp)";
                try (PreparedStatement ps = conn.prepareStatement(fallback)) {
                    ps.setString(1, newCategory); ps.setInt(2, uId); ps.setString(3, oldCat); ps.setDouble(4, amt); ps.setDate(5, dt); ps.executeUpdate();
                }
            }
            conn.commit(); return true;
        } catch (SQLException ex) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally { closeQuietly(conn); }
    }

    // ================= UPDATE AMOUNT =================
    public static boolean updateExpenseAmount(int transactionId, double newAmount) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            int refId = -1; int uId = 0; String cat = ""; double oldAmt = 0; Date dt = null;
            boolean useRefId = hasRefIdColumn(conn);
            String fetchSql = useRefId
                    ? "SELECT user_id, category_name, amount, transaction_date, ref_id FROM transactions WHERE transaction_id = ? AND type = 'EXPENSE'"
                    : "SELECT user_id, category_name, amount, transaction_date FROM transactions WHERE transaction_id = ? AND type = 'EXPENSE'";
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, transactionId); ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    uId = rs.getInt("user_id"); cat = rs.getString("category_name");
                    oldAmt = rs.getDouble("amount"); dt = rs.getDate("transaction_date");
                    if (useRefId) refId = rs.getInt("ref_id");
                } else { conn.rollback(); return false; }
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE transactions SET amount = ? WHERE transaction_id = ?")) {
                ps.setDouble(1, newAmount); ps.setInt(2, transactionId); ps.executeUpdate();
            }
            if (useRefId && refId > 0) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE expenses SET amount = ? WHERE expense_id = ?")) {
                    ps.setDouble(1, newAmount); ps.setInt(2, refId); ps.executeUpdate();
                }
            } else {
                String fallback = "UPDATE expenses SET amount = ? WHERE expense_id = (SELECT expense_id FROM (SELECT expense_id FROM expenses WHERE user_id=? AND category_name=? AND amount=? AND expense_date=? ORDER BY expense_id ASC LIMIT 1) AS tmp)";
                try (PreparedStatement ps = conn.prepareStatement(fallback)) {
                    ps.setDouble(1, newAmount); ps.setInt(2, uId); ps.setString(3, cat); ps.setDouble(4, oldAmt); ps.setDate(5, dt); ps.executeUpdate();
                }
            }
            conn.commit(); return true;
        } catch (SQLException ex) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally { closeQuietly(conn); }
    }

    // ================= GET BY USER =================
    public static List<Expenses> getExpensesByUser(int userId) {
        List<Expenses> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Expenses exp = new Expenses();
                exp.setExpenseId(rs.getInt("expense_id")); exp.setUserId(rs.getInt("user_id"));
                exp.setCategory(rs.getString("category_name")); exp.setAmount(rs.getDouble("amount"));
                exp.setExpenseDate(rs.getDate("expense_date")); exp.setDescription(rs.getString("description"));
                list.add(exp);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    private static void closeQuietly(Connection conn) {
        try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
    }
}