import java.io.*;
import java.net.*;
import java.sql.*;

public class BookStoreServer {
    private static final String DB_URL = "jdbc:mysql://bnzgiyejcl2e7zap38wu-mysql.services.clever-cloud.com:3306/bnzgiyejcl2e7zap38wu?user=ujmhifq03ajr9s0q&password=v3rBwc2foBRJNZejWBq7";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(2000)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Create a new thread for client communication
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private Connection conn;
        private boolean isLoggedIn = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                conn = DriverManager.getConnection(DB_URL);

                // Read the action from the client
                String action = in.readLine();
                if (action.equals("SIGNIN")) {
                    // Handle sign-in
                    String username = in.readLine();
                    String password = in.readLine();
                    handleSignIn(username, password);
                } else if (action.equals("SIGNUP")) {
                    // Handle sign-up
                    String name = in.readLine();
                    String username = in.readLine();
                    String password = in.readLine();
                    handleSignUp(name, username, password);
                } else if (action.equals("BROWSE")) {
                    // Check if user is logged in
                    if (isLoggedIn) {
                        browseBooks();
                    } else {
                        out.println("Error: You need to sign in or sign up first.");
                    }
                } else if (action.equals("SEARCH")) {
                    // Check if user is logged in
                    if (isLoggedIn) {
                        String query = in.readLine();
                        searchBooks(query);
                    } else {
                        out.println("Error: You need to sign in or sign up first.");
                    }
                } else if (action.equals("BORROW")) {
                    // Check if user is logged in
                    if (isLoggedIn) {
                        String bookTitle = in.readLine();
                        String lenderUsername = in.readLine();
                        sendBorrowRequest(bookTitle, lenderUsername);
                    } else {
                        out.println("Error: You need to sign in or sign up first.");
                    }
                } else if (action.equals("CHAT")) {
                    // Check if user is logged in
                    if (isLoggedIn) {
                        String recipientUsername = in.readLine();
                        startChat(recipientUsername);
                    } else {
                        out.println("Error: You need to sign in or sign up first.");
                    }
                } else {
                    out.println("Invalid action!");
                }

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) conn.close();
                    socket.close();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleSignIn(String username, String password) {
            try {
                String sql = "SELECT * FROM Users WHERE Username = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // User found, check password
                            String storedPassword = rs.getString("Password");
                            if (password.equals(storedPassword)) {
                                out.println("Signin successful!");
                                isLoggedIn = true;
                            } else {
                                out.println("401: Incorrect password!");
                            }
                        } else {
                            out.println("404: Username not found!");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }

        private void handleSignUp(String name, String username, String password) {
            try {
                String checkSql = "SELECT * FROM Users WHERE Username = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, username);
                    try (ResultSet checkRs = checkStmt.executeQuery()) {
                        if (checkRs.next()) {
                            // Username already exists
                            out.println("Error: Username already exists!");
                        } else {
                            // Username is available, proceed with registration
                            String insertSql = "INSERT INTO Users (Name, Username, Password) VALUES (?, ?, ?)";
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                insertStmt.setString(1, name);
                                insertStmt.setString(2, username);
                                insertStmt.setString(3, password);
                                int rowsAffected = insertStmt.executeUpdate();
                                if (rowsAffected > 0) {
                                    out.println("Signup successful!");
                                    isLoggedIn = true;
                                } else {
                                    out.println("Signup failed!");
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }

        private void browseBooks() {
            try {
                String sql = "SELECT * FROM Books";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        StringBuilder response = new StringBuilder();
                        response.append("Books:\n");
                        while (rs.next()) {
                            String title = rs.getString("Title");
                            String author = rs.getString("Author");
                            String genre = rs.getString("Genre");
                            response.append("Title: ").append(title).append(", Author: ").append(author)
                                    .append(", Genre: ").append(genre).append("\n");
                        }
                        out.println(response.toString());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }

        private void searchBooks(String query) {
            try {
                String sql = "SELECT * FROM Books WHERE Title LIKE ? OR Author LIKE ? OR Genre LIKE ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    String searchTerm = "%" + query + "%";
                    pstmt.setString(1, searchTerm);
                    pstmt.setString(2, searchTerm);
                    pstmt.setString(3, searchTerm);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        StringBuilder response = new StringBuilder();
                        response.append("Search results for '").append(query).append("':\n");
                        while (rs.next()) {
                            String title = rs.getString("Title");
                            String author = rs.getString("Author");
                            String genre = rs.getString("Genre");
                            response.append("Title: ").append(title).append(", Author: ").append(author)
                                    .append(", Genre: ").append(genre).append("\n");
                        }
                        out.println(response.toString());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }
        private void sendBorrowRequest(String bookTitle, String lenderUsername) {
            try {
                // Check if the lender exists
                String checkLenderSql = "SELECT * FROM Users WHERE Username = ?";
                try (PreparedStatement checkLenderStmt = conn.prepareStatement(checkLenderSql)) {
                    checkLenderStmt.setString(1, lenderUsername);
                    try (ResultSet checkLenderRs = checkLenderStmt.executeQuery()) {
                        if (!checkLenderRs.next()) {
                            out.println("Error: Lender with username '" + lenderUsername + "' not found!");
                            return;
                        }
                    }
                }

                // Check if the book exists and if it's available for borrowing
                String checkBookSql = "SELECT * FROM Books WHERE Title = ? AND IsAvailable = 1";
                try (PreparedStatement checkBookStmt = conn.prepareStatement(checkBookSql)) {
                    checkBookStmt.setString(1, bookTitle);
                    try (ResultSet checkBookRs = checkBookStmt.executeQuery()) {
                        if (checkBookRs.next()) {
                            // Book found and available, proceed with sending the request
                            String borrowerUsername = in.readLine();
                            String insertRequestSql = "INSERT INTO BorrowRequests (BookTitle, BorrowerUsername, LenderUsername) VALUES (?, ?, ?)";
                            try (PreparedStatement insertRequestStmt = conn.prepareStatement(insertRequestSql)) {
                                insertRequestStmt.setString(1, bookTitle);
                                insertRequestStmt.setString(2, borrowerUsername);
                                insertRequestStmt.setString(3, lenderUsername);
                                int rowsAffected = insertRequestStmt.executeUpdate();
                                if (rowsAffected > 0) {
                                    out.println("Borrow request sent successfully!");
                                } else {
                                    out.println("Failed to send borrow request!");
                                }
                            }
                        } else {
                            out.println("Error: Book '" + bookTitle + "' either not found or not available for borrowing!");
                        }
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }

        private void startChat(String recipientUsername) {
            try {
                // Check if the recipient exists
                String checkRecipientSql = "SELECT * FROM Users WHERE Username = ?";
                try (PreparedStatement checkRecipientStmt = conn.prepareStatement(checkRecipientSql)) {
                    checkRecipientStmt.setString(1, recipientUsername);
                    try (ResultSet checkRecipientRs = checkRecipientStmt.executeQuery()) {
                        if (checkRecipientRs.next()) {
                            // Recipient found, proceed with starting the chat
                            String senderUsername = in.readLine();
                            String message;
                            while ((message = in.readLine()) != null && !message.equals("END")) {
                                // Forward the message to the recipient
                                sendMessageToRecipient(senderUsername, recipientUsername, message);
                            }
                            out.println("Chat ended.");
                        } else {
                            out.println("Error: Recipient with username '" + recipientUsername + "' not found!");
                        }
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }

        private void sendMessageToRecipient(String senderUsername, String recipientUsername, String message) {
            try {
                String insertMessageSql = "INSERT INTO Messages (SenderUsername, RecipientUsername, Message) VALUES (?, ?, ?)";
                try (PreparedStatement insertMessageStmt = conn.prepareStatement(insertMessageSql)) {
                    insertMessageStmt.setString(1, senderUsername);
                    insertMessageStmt.setString(2, recipientUsername);
                    insertMessageStmt.setString(3, message);
                    int rowsAffected = insertMessageStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        out.println("Message sent to " + recipientUsername + ": " + message);
                    } else {
                        out.println("Failed to send message!");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }
    }
}


