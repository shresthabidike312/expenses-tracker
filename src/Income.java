import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Income {

    private int incomeId;
    private int userId;
    private String source;
    private double amount;
    private Date incomeDate;
    private String description;

    public Income() {}

    public Income(int userId, String source, double amount, Date incomeDate, String description) {
        this.userId = userId;
        this.source = source;
        this.amount = amount;
        this.incomeDate = incomeDate;
        this.description = description;
    }

    // Getters and Setters
    public int getIncomeId() { return incomeId; }
    public void setIncomeId(int incomeId) { this.incomeId = incomeId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Date getIncomeDate() { return incomeDate; }
    public void setIncomeDate(Date incomeDate) { this.incomeDate = incomeDate; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }

    // ================= ADD INCOME =================
    public static boolean addIncome(int userId, String source, double amount,
                                    String date, String description) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            // Insert into income table
            String sql1 = "INSERT INTO income (user_id, source, amount, income_date, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, source);
                ps.setDouble(3, amount);
                ps.setDate(4, Date.valueOf(date));
                ps.setString(5, description);
                ps.executeUpdate();
            }

            // Insert into transactions table
            String sql2 = "INSERT INTO transactions (user_id, type, category_name, amount, transaction_date, description) " +
                    "VALUES (?, 'INCOME', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, userId);
                ps.setString(2, source);
                ps.setDouble(3, amount);
                ps.setDate(4, Date.valueOf(date));
                ps.setString(5, description);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            closeQuietly(conn);
        }
    }

    // ================= TOTAL INCOME =================
    public static double getTotalIncome(int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return 0;

            String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM income WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(conn);
        }
        return 0;
    }

    // ================= UPDATE INCOME AMOUNT ONLY =================
    // Called by ExpensesTracker when user edits amount via the Edit Income button.
    // transactionId  = the ID from the transactions table (shown in the table as "ID")
    // newAmount      = the new amount entered by the user
    public static boolean updateIncomeAmount(int transactionId, double newAmount) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            // Step 1: fetch matching info from transactions so we can find the income row
            String fetchSql = "SELECT user_id, category_name, amount, transaction_date " +
                    "FROM transactions WHERE transaction_id = ? AND type = 'INCOME'";
            int    userId  = 0;
            String source  = null;
            double oldAmt  = 0;
            Date   date    = null;

            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { conn.rollback(); return false; }
                userId = rs.getInt("user_id");
                source = rs.getString("category_name");
                oldAmt = rs.getDouble("amount");
                date   = rs.getDate("transaction_date");
            }

            // Step 2: update amount in transactions table
            String updateTrans = "UPDATE transactions SET amount = ? WHERE transaction_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateTrans)) {
                ps.setDouble(1, newAmount);
                ps.setInt(2, transactionId);
                ps.executeUpdate();
            }

            // Step 3: update amount in income table — match on user_id, source, old amount, date
            String updateIncome = "UPDATE income SET amount = ? " +
                    "WHERE user_id = ? AND source = ? AND amount = ? AND income_date = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(updateIncome)) {
                ps.setDouble(1, newAmount);
                ps.setInt(2, userId);
                ps.setString(3, source);
                ps.setDouble(4, oldAmt);
                ps.setDate(5, date);
                int rows = ps.executeUpdate();

                // Fallback: match only on user_id + old amount (less precise but safe)
                if (rows == 0) {
                    String fallback = "UPDATE income SET amount = ? WHERE user_id = ? AND amount = ? LIMIT 1";
                    try (PreparedStatement ps2 = conn.prepareStatement(fallback)) {
                        ps2.setDouble(1, newAmount);
                        ps2.setInt(2, userId);
                        ps2.setDouble(3, oldAmt);
                        ps2.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            closeQuietly(conn);
        }
    }

    // ================= FULL UPDATE INCOME =================
    public static boolean updateIncome(int transactionId, String newSource, double newAmount,
                                       String newDate, String newDescription) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);


            String fetchSql = "SELECT user_id FROM transactions WHERE transaction_id = ? AND type = 'INCOME'";
            int userId = 0;

            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                userId = rs.getInt("user_id");
            }


            String updateTrans = "UPDATE transactions SET category_name = ?, amount = ?, transaction_date = ?, description = ? " +
                    "WHERE transaction_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateTrans)) {
                ps.setString(1, newSource);
                ps.setDouble(2, newAmount);
                ps.setDate(3, Date.valueOf(newDate));
                ps.setString(4, newDescription);
                ps.setInt(5, transactionId);
                ps.executeUpdate();
            }

            // Update income table
            String updateIncome = "UPDATE income SET source = ?, amount = ?, income_date = ?, description = ? " +
                    "WHERE user_id = ? AND source = ? AND income_date = ? LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(updateIncome)) {
                ps.setString(1, newSource);
                ps.setDouble(2, newAmount);
                ps.setDate(3, Date.valueOf(newDate));
                ps.setString(4, newDescription);
                ps.setInt(5, userId);
                ps.setString(6, newSource);
                ps.setDate(7, Date.valueOf(newDate));
                int rows = ps.executeUpdate();

                if (rows == 0) {
                    String fallback = "UPDATE income SET source = ?, amount = ?, income_date = ?, description = ? " +
                            "WHERE user_id = ? AND amount = ? LIMIT 1";
                    try (PreparedStatement ps2 = conn.prepareStatement(fallback)) {
                        ps2.setString(1, newSource);
                        ps2.setDouble(2, newAmount);
                        ps2.setDate(3, Date.valueOf(newDate));
                        ps2.setString(4, newDescription);
                        ps2.setInt(5, userId);
                        ps2.setDouble(6, newAmount);
                        ps2.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            closeQuietly(conn);
        }
    }

    // ================= DELETE INCOME =================
    public static boolean deleteIncome(int transactionId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            String fetchSql = "SELECT user_id, category_name, amount, transaction_date FROM transactions " +
                    "WHERE transaction_id = ? AND type='INCOME'";
            int userId = 0;
            String source = null;
            double amount = 0;
            Date date = null;

            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                userId = rs.getInt("user_id");
                source = rs.getString("category_name");
                amount = rs.getDouble("amount");
                date = rs.getDate("transaction_date");
            }

            // Delete from transactions
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE transaction_id = ?")) {
                ps.setInt(1, transactionId);
                ps.executeUpdate();
            }

            // Delete from income
            String delIncome = "DELETE FROM income WHERE user_id = ? AND source = ? AND amount = ? AND income_date = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(delIncome)) {
                ps.setInt(1, userId);
                ps.setString(2, source);
                ps.setDouble(3, amount);
                ps.setDate(4, date);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            closeQuietly(conn);
        }
    }

    // ================= GET ALL INCOMES =================
    public static List<Income> getIncomesByUser(int userId) {
        List<Income> list = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return list;

            String sql = "SELECT * FROM income WHERE user_id = ? ORDER BY income_date DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Income inc = new Income();
                    inc.setIncomeId(rs.getInt("income_id"));
                    inc.setUserId(rs.getInt("user_id"));
                    inc.setSource(rs.getString("source"));
                    inc.setAmount(rs.getDouble("amount"));
                    inc.setIncomeDate(rs.getDate("income_date"));
                    inc.setDescription(rs.getString("description"));
                    list.add(inc);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(conn);
        }
        return list;
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}