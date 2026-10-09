import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // 1. Set System Look and Feel to make it look like a modern Windows app
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not set System Look and Feel.");
        }


        if (DatabaseConnection.testConnection()) {


            SwingUtilities.invokeLater(() -> {
                try {
                    Login loginFrame = new Login();


                    loginFrame.setVisible(true);


                    loginFrame.setLocationRelativeTo(null);

                    System.out.println("Login window launched successfully.");
                } catch (Exception e) {
                    System.err.println("Error launching Login window: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } else {
            // 4. Show a professional error dialog if the database is offline
            showDatabaseError();
        }
    }

    private static void showDatabaseError() {
        String message = "<html><b style='color:red;'>Database Connection Failed!</b><br><br>" +
                "The Expense Tracker cannot start. Please verify:<br>" +
                "1. <b>XAMPP Control Panel</b> is open and MySQL is started.<br>" +
                "2. The database <b>'expense_tracker_db'</b> exists in phpMyAdmin.<br>" +
                "3. Your network or local firewall isn't blocking Port 3306.</html>";

        JOptionPane.showMessageDialog(null,
                message,
                "System Error - Connection Failed",
                JOptionPane.ERROR_MESSAGE);

        // Exit the program since we can't do anything without the database
        System.exit(0);
    }
}