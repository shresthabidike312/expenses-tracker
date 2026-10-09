import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// ---------- MODEL ----------
class CategoryModel {
    private final int categoryId;
    private final int userId;
    private final String categoryName;

    public CategoryModel(int categoryId, int userId, String categoryName) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.categoryName = categoryName == null ? "" : categoryName.trim();
    }

    public int getCategoryId() { return categoryId; }
    public int getUserId() { return userId; }
    public String getCategoryName() { return categoryName; }

    @Override
    public String toString() {
        return categoryName.isEmpty() ? "No categories yet" : categoryName;
    }
}


public class Category {

    public static boolean addCategory(int userId, String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) return false;
        String name = categoryName.trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String check = "SELECT 1 FROM category WHERE user_id=? AND category_name=?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, userId);
                ps.setString(2, name);
                if (ps.executeQuery().next()) return false;
            }

            String insert = "INSERT INTO category (user_id, category_name) VALUES (?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, userId);
                ps.setString(2, name);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCategory(int categoryId, String newName) {
        if (categoryId <= 0 || newName == null || newName.trim().isEmpty()) return false;
        String name = newName.trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            int userId = -1;
            String oldName = "";

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id, category_name FROM category WHERE category_id=?")) {
                ps.setInt(1, categoryId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) { userId = rs.getInt("user_id"); oldName = rs.getString("category_name"); }
            }
            if (userId == -1) return false;

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM category WHERE user_id=? AND category_name=? AND category_id!=?")) {
                ps.setInt(1, userId); ps.setString(2, name); ps.setInt(3, categoryId);
                if (ps.executeQuery().next()) return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE category SET category_name=? WHERE category_id=?")) {
                ps.setString(1, name); ps.setInt(2, categoryId);
                if (ps.executeUpdate() == 0) return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE expenses SET category_name=? WHERE category_name=?")) {
                ps.setString(1, name); ps.setString(2, oldName);
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- DELETE (FIXED) ----------
    public static boolean deleteCategory(int categoryId) {
        if (categoryId <= 0) return false;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Step 1: Fetch category name first (avoids MySQL subquery restriction)
            String categoryName = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT category_name FROM category WHERE category_id=?")) {
                ps.setInt(1, categoryId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) categoryName = rs.getString("category_name");
            }
            if (categoryName == null) return false;

            // Step 2: Update expenses using the fetched name
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE expenses SET category_name='Uncategorized' WHERE category_name=?")) {
                ps.setString(1, categoryName);
                ps.executeUpdate();
            }

            // Step 3: Delete the category
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM category WHERE category_id=?")) {
                ps.setInt(1, categoryId);
                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- GET ALL ----------
    public static List<CategoryModel> getAllCategories(int userId) {
        List<CategoryModel> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM category WHERE user_id=? ORDER BY category_name")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new CategoryModel(
                            rs.getInt("category_id"),
                            rs.getInt("user_id"),
                            rs.getString("category_name")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---------- GET ID ----------
    public static int getCategoryId(int userId, String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) return -1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT category_id FROM category WHERE user_id=? AND category_name=?")) {
                ps.setInt(1, userId);
                ps.setString(2, categoryName.trim());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}