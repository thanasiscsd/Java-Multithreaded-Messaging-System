import java.io.*;
import java.net.*;

public class MessagingClient {

    public static void main(String[] args) throws Exception {

        // Connect client with server for IP:localhost port:5000
        Socket socket = new Socket("localhost", 5000);

        // Stream to read input from server
        BufferedReader serverIn =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Stream to give output to server
        PrintWriter serverOut =
                new PrintWriter(socket.getOutputStream(), true);

        // Stream to read what user types
        BufferedReader keyboard =
                new BufferedReader(new InputStreamReader(System.in));

        // Thread to respond to server
        Thread receiver = new Thread(() -> {
            try {
                String line;
                while ((line = serverIn.readLine()) != null) {
                    System.out.println("[SERVER] " + line);
                }
            } catch (Exception e) {
                System.out.println("Server closed connection.");
            }
        });

        receiver.start();  // thread that reads from server


        String input;
        while ((input = keyboard.readLine()) != null) { //send user's stream to server
            serverOut.println(input);
        }

        socket.close();
    }
}