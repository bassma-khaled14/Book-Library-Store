import java.io.*;
import java.net.*;

public class BookStoreClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 2000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            boolean isLoggedIn = false;

            while (!isLoggedIn) {
                // Choose action: Sign Up, Sign In
                System.out.println("Choose an action:");
                System.out.println("1. Sign Up");
                System.out.println("2. Sign In");
                int actionChoice = Integer.parseInt(userInput.readLine());
                String action;
                if (actionChoice == 1) {
                    action = "SIGNUP";
                    // For sign-up, prompt for name, username, and password
                    System.out.print("Enter your name: ");
                    String name = userInput.readLine();
                    System.out.print("Enter your username: ");
                    String username = userInput.readLine();
                    System.out.print("Enter your password: ");
                    String password = userInput.readLine();
                    out.println(action);
                    out.println(name);
                    out.println(username);
                    out.println(password);
                } else if (actionChoice == 2) {
                    action = "SIGNIN";
                    // For sign-in, prompt for username and password
                    System.out.print("Enter your username: ");
                    String username = userInput.readLine();
                    System.out.print("Enter your password: ");
                    String password = userInput.readLine();
                    out.println(action);
                    out.println(username);
                    out.println(password);
                } else {
                    System.out.println("Invalid choice. Exiting...");
                    return;
                }

                // Receive and display server response
                String response = in.readLine();
                System.out.println(response);

                // Check if sign-in or sign-up was successful
                isLoggedIn = response.startsWith("Signin") || response.startsWith("Signup");
            }

            // Once signed in or signed up, allow browsing, searching, sending borrow requests, or starting chats
            boolean exit = false;
            while (!exit) {
                // Choose action: Browse Books, Search Books, Send Borrow Request, or Start Chat
                System.out.println("Choose an action:");
                System.out.println("1. Browse Books");
                System.out.println("2. Search Books");
                System.out.println("3. Send Borrow Request");
                System.out.println("4. Start Chat");
                int actionChoice = Integer.parseInt(userInput.readLine());
                String action;
                if (actionChoice == 1) {
                    action = "BROWSE";
                    out.println(action);
                } else if (actionChoice == 2) {
                    action = "SEARCH";
                    System.out.print("Enter search query: ");
                    String query = userInput.readLine();
                    out.println(action);
                    out.println(query);
                } else if (actionChoice == 3) {
                    action = "BORROW_REQUEST";
                    System.out.print("Enter book title: ");
                    String bookTitle = userInput.readLine();
                    System.out.print("Enter lender's username: ");
                    String lenderUsername = userInput.readLine();
                    out.println(action);
                    out.println(bookTitle);
                    out.println(lenderUsername);
                } else if (actionChoice == 4) {
                    action = "START_CHAT";
                    System.out.print("Enter recipient's username: ");
                    String recipientUsername = userInput.readLine();
                    out.println(action);
                    out.println(recipientUsername);
                } else {
                    System.out.println("Invalid choice. Exiting...");
                    exit = true;
                }

                // Receive and display server response
                String response = in.readLine();
                System.out.println(response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
