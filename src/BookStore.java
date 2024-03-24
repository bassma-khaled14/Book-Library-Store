import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BookStore {
    private static final String DB_URL = "jdbc:mysql://bnzgiyejcl2e7zap38wu-mysql.services.clever-cloud.com:3306/bnzgiyejcl2e7zap38wu";
    private static final String DB_USER = "ujmhifq03ajr9s0q";
    private static final String DB_PASSWORD = "v3rBwc2foBRJNZejWBq7";

    // Signup method to insert user data into the database
    public static boolean signup(String name, String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO Users (Name, Username, Password) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, username);
                pstmt.setString(3, password);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        // Example usage of the signup method
        String name = "John Doe";
        String username = "johndoe";
        String password = "password123";

        if (signup(name, username, password)) {
            System.out.println("Signup successful!");
        } else {
            System.out.println("Signup failed! Please try again.");
        }
    }
}
