import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;
    private boolean running;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
        running = true;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket

        Scanner scanner = new Scanner(System.in);
        String message;
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        while (scanner.hasNextLine()) {
            message = scanner.nextLine();

            try {
                if (message.equalsIgnoreCase("/logout")) {
                    sOutput.writeObject(new ChatMessage(message, 1));
                    running = false;
                    sInput.close();
                    sOutput.close();
                    socket.close();
                    break;
                }
                if (message.startsWith("/msg")) {
                    //TODO: send to only the username
                    try {
                        ChatMessage cm = (ChatMessage) sInput.readObject();
                        sOutput.writeObject(new ChatMessage(message, 0, cm.getRecipient()));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                sOutput.writeObject(new ChatMessage(message, 0));

                sOutput.flush();
            } catch (IOException e) {
                System.out.println("The connection with the server has been lost.");

                try {
                    sOutput.close();
                } catch (IOException f) {
                    f.printStackTrace();
                } //end try catch

            } //end try catch

//            System.out.print("Enter a message to send to the server: ");
        } //end while

        scanner.close();

        try {
            sOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages

//        Runnable r = new ListenFromServer();
//        Thread t = new Thread(r);
//        t.start();


        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults

        // Create your client and start it
        String server;
        String port;
        String user;
        if (args.length <= 0 || args[0] == null) {
            user = "Anonymous";
        } else {
            user = args[0];
        }

        if (args.length <= 1 || args[1] == null) {
            port = "1500";
        } else {
            port = args[1];
        }

        if (args.length <= 2 || args[2] == null) {
            server = "localHost";
        } else {
            server = args[2];
        }

        ChatClient client = new ChatClient(server, Integer.parseInt(port), user);
        client.start();

        // Send an empty message to the server
    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while (running) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                } catch (IOException | ClassNotFoundException e) {
//                    e.printStackTrace();
                    System.out.println("You have Logged out.");
                }
            }
        }
    }
}
