import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessagingServer  {

    public static void main(String[] args) throws Exception {

        // Create server socket that responds to port 5000
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server running on port 5000...");
        String port = "5000";
        while (true) {
            // wait for client
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected.");

            // For each client, create thread (to allow multiple clients at once)
            ClientHandler handler = new ClientHandler(clientSocket);
            Thread t = new Thread(handler);
            t.start();
        }
    }
}

// this class runs for each user in a different thread
class ClientHandler implements Runnable {

    private Socket socket;
    private static List<Account> accounts=new ArrayList<>(); //to keep track of all accounts created
    public ClientHandler(Socket socket) {
        this.socket = socket;  // Save client's socket
    }
    @Override
    public void run() {
        try {
            // Input from client
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Output towards client
            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);


            String expectedString1; //should be "java"
            String expectedString2; //should be "client"
            String ip;
            String givenPort;
            String FN_ID;
            String args;
            String line;

            // While client sends line, server reads input
            while ((line = in.readLine()) != null) {
                String[] parts=line.split("\\s+"); //one or more spaces
                if (parts.length<6) {
                    out.println("Invalid input (all operations require more than 6 words)");
                    continue;
                }
                expectedString1 = parts[0];
                expectedString2 = parts[1];
                if (!expectedString1.equals("java") || !expectedString2.equals("client")) { //check for correct input
                    out.println("Invalid input (must start with java client ...)");
                    continue;
                }
                ip=parts[2];
                if (!ip.equals("localhost")) {
                    out.println("Invalid IP given (IP must be localhost)");
                }
                givenPort=parts[3];
                if (!givenPort.equals("5000")) { //chech if port given is correct
                    out.println("Invalid port given (port must be 5000)");
                    continue;
                }
                FN_ID=parts[4];
                args="";
                for (int i=5;i<parts.length;i++) {
                    if (i > 5) args += " "; // add space only after the first word
                    args += parts[i];
                }
                String[] argsParts=args.split("\\s+"); //one or more spaces
                if (FN_ID.equals("1")) {
                        if (argsParts.length!=1) { //check for correct input
                            out.println("Invalid input (for FN_ID = 1 args must be only one word, for account's username)");
                            continue;
                         }
                        //generate unique token
                         Random rand = new Random();
                         String authToken=1000+rand.nextInt(9000)+" ";
                         Account account=new Account(args,authToken);
                         if (account.getUsername()==null) { //check if username uses correct syntax
                             out.println("Invalid Username");
                             continue;
                        }
                        boolean exists=false;
                        for (Account acc : accounts) { //check if username exists
                             if (acc.getUsername().equals(account.getUsername()))
                            {
                                out.println("Sorry, the user already exists");
                                exists=true;
                                break;
                             }
                         }
                         if (exists){
                             continue;
                         }
                         for (Account acc : accounts) {   //check if token already exists
                            if (acc.getAuthToken().equals(account.getAuthToken())) { //if it exists, generate new token so they are unique
                                 rand = new Random();
                                 authToken=1000+rand.nextInt(9000)+" ";
                            }
                        }
                        accounts.add(account); //add new account to accounts list
                        out.println(account.getAuthToken());
                }
                if (FN_ID.equals("2")) {
                         if (argsParts.length!=1) { //check for correct input
                             out.println("Invalid input (for FN_ID = 2 args must be only one word, for account's authToken)");
                             continue;
                         }
                         if (!authTokenExists(argsParts[0])) //check if authtoken exists
                         {
                             out.println("Invalid Auth Token");
                             continue;
                         }
                         String username;
                         int i=1;
                         for (Account acc : accounts) {
                            username=acc.getUsername();
                            out.println(i+". "+username);
                            i++;
                         }
                }
                if  (FN_ID.equals("3")) {
                          if (argsParts.length<3) { //check for correct input
                             out.println("Invalid input (for FN_ID = 3 args must be above three words, for authToken, recipient, message_body)");
                             continue;
                         }
                         if (!authTokenExists(argsParts[0])) //check if authtoken exists
                         {
                             out.println("Invalid Auth Token");
                             continue;
                         }
                         String authToken=argsParts[0];
                         String recipient=argsParts[1];
                         String messageBody="";
                         for (int i=2;i<argsParts.length;i++) {
                               if (i > 2) messageBody += " "; // add space only after the first word
                                 messageBody += argsParts[i];
                            }
                         boolean existsRecipient=false;
                         boolean existsSender=false;
                         for  (Account acc : accounts) {
                             if (acc.getUsername().trim().equals(recipient.trim()))  //check if recipient is an existent account
                             {
                                 existsRecipient=true;
                             }
                         }
                         String sender=null;
                         for  (Account acc : accounts) {
                             if (acc.getAuthToken().trim().equals(authToken.trim())) //get senders account from authToken
                             {
                                 sender=acc.getUsername();
                                 existsSender=true; //check if sender is an existent account
                             }
                         }
                         if (sender.equals(recipient)) { //if user tries to send message to himself
                             out.println("Sorry, can't send message to self");
                             continue;
                         }
                         if (!existsRecipient) {
                             out.println("User does not exist");
                             continue;
                         }
                         if (!existsSender) {
                             out.println("Sender does not exist");
                             continue;
                         }
                         Message message=new Message(false,sender,recipient,messageBody);
                         for (Account acc : accounts) {
                             if (acc.getUsername().equals(recipient)) //get recipients account
                             {
                                 acc.addMessage(message);
                             }
                         }
                         out.println("OK");

                }
                if (FN_ID.equals("4")) {
                         if (argsParts.length!=1) { //check for correct input
                             out.println("Invalid input (for FN_ID = 4 args must be only one word, for account's authToken)");
                             continue;
                         }
                        if (!authTokenExists(argsParts[0])) //check if authtoken exists
                         {
                            out.println("Invalid Auth Token");
                            continue;
                         }
                        String authToken=argsParts[0];
                        List<Message> messageBox=new ArrayList<>();
                        for  (Account acc : accounts) {
                             if (acc.getAuthToken().trim().equals(authToken.trim())) //get account from authToken
                            {
                                 messageBox=acc.getMessageBox(); //get message box
                            }
                        }
                        int id;
                        if (messageBox.isEmpty()) {
                                out.println("No messages found");
                                continue;
                        }
                         for (Message message : messageBox) {
                             id = message.getId();
                             if (message.getIsRead()) {
                                 out.println(id + ". from: " + message.getSender());
                             }
                             if (!message.getIsRead()) {
                                 out.println(id + ". from: " + message.getSender()+'*');
                             }
                        }

                }
                if (FN_ID.equals("5")) {
                    if (argsParts.length!=2) { //check for correct input
                        out.println("Invalid input (for FN_ID = 5 args must be two words, for account's authToken and message_id)");
                        continue;
                    }
                    if (!authTokenExists(argsParts[0])) //check if authtoken exists
                    {
                        out.println("Invalid Auth Token");
                        continue;
                    }
                    String authToken=argsParts[0];
                    int id=Integer.parseInt(argsParts[1]); //get id of the message
                    List<Message> messageBox=new ArrayList<>();
                    for  (Account acc : accounts) {
                        if (acc.getAuthToken().trim().equals(authToken.trim())) //get account from authToken
                        {
                            messageBox=acc.getMessageBox(); //get message box
                        }
                    }
                    boolean existsId=false;
                    Message messageToRead=null;
                    for (Message message : messageBox) { //for each message, check if id matches
                         if  (message.getId()==id)
                         {
                             messageToRead=message;
                             existsId=true;
                         }
                    }
                    if (!existsId)
                    {
                        out.println("Message ID does not exist");
                        continue;
                    }
                    messageToRead.markAsRead();
                    out.println(messageToRead.getBody());



                }
                if  (FN_ID.equals("6")) {
                    if (argsParts.length!=2) { //check for correct input
                        out.println("Invalid input (for FN_ID = 6 args must be two words, for account's authToken and message_id)");
                        continue;
                    }
                    if (!authTokenExists(argsParts[0])) //check if authtoken exists
                    {
                        out.println("Invalid Auth Token");
                        continue;
                    }
                    String authToken=argsParts[0];
                    int id=Integer.parseInt(argsParts[1]); //get id of the message
                    List<Message> messageBox=new ArrayList<>();
                    for  (Account acc : accounts) {
                        if (acc.getAuthToken().trim().equals(authToken.trim())) //get account from authToken
                        {
                            messageBox=acc.getMessageBox(); //get message box
                        }
                    }
                    boolean existsId=false;
                    Message messageToDelete=null;
                    for (Message message : messageBox) { //for each message, check if id matches
                        if  (message.getId()==id)
                        {
                            messageToDelete=message;
                            existsId=true;
                        }
                    }
                    if (!existsId)
                    {
                        out.println("Message ID does not exist");
                        continue;
                    }
                    messageBox.remove(messageToDelete); //remove message from user's message box (delete)
                    out.println("OK");
                }

            }

            // Close connection
            socket.close();
            System.out.println("Client disconnected.");
        }
        catch (Exception e) {
            System.out.println("Error in client thread.");
        }
    }
    public boolean authTokenExists(String authToken) { //helper function to check if authtoken exists
        for (Account acc: accounts)
        {
            if (acc.getAuthToken().trim().equals(authToken.trim()))
            {
               return true;
            }
        }
        return false;
    }
}