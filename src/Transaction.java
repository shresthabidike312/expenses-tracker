import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private int transactionId;
    private int userId;
    private String type;
    private String category_name;
    private double amount;
    private Date transactionDate;
    private String description;

    public Transaction() {}

    public int getTransactionId()       { return transactionId; }
    public void setTransactionId(int v) { this.transactionId = v; }
    public int getUserId()              { return userId; }
    public void setUserId(int v)        { this.userId = v; }
    public String getType()             { return type; }
    public void setType(String v)       { this.type = v; }
    public String getCategory()         { return category_name; }
    public void setCategory(String v)   { this.category_name = v; }
    public double getAmount()           { return amount; }
    public void setAmount(double v)     { this.amount = v; }
    public Date getTransactionDate()    { return transactionDate; }
    public void setTransactionDate(Date v) { this.transactionDate = v; }
    public String getDescription()      { return description; }
    public void setDescription(String v){ this.description = v; }

    public static List<Transaction> getAllTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setUserId(rs.getInt("user_id"));
                t.setType(rs.getString("type"));
                t.setCategory(rs.getString("category_name"));
                t.setAmount(rs.getDouble("amount"));
                t.setTransactionDate(rs.getDate("transaction_date"));
                t.setDescription(rs.getString("description"));
                list.add(t);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public static List<Transaction> getRecentTransactions(int userId, int limit) {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC LIMIT ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setUserId(rs.getInt("user_id"));
                t.setType(rs.getString("type"));
                t.setCategory(rs.getString("category_name"));
                t.setAmount(rs.getDouble("amount"));
                t.setTransactionDate(rs.getDate("transaction_date"));
                t.setDescription(rs.getString("description"));
                list.add(t);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public static boolean deleteTransaction(int transactionId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM transactions WHERE transaction_id = ?")) {
            ps.setInt(1, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static double getBalance(int userId) {
        return Income.getTotalIncome(userId) - Expenses.getTotalExpenses(userId);
    }
}