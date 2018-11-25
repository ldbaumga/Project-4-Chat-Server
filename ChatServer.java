

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private SimpleDateFormat d = new SimpleDateFormat("HH:mm:ss");
    private String file;
    private int x;


    private ChatServer(int port, String badwords) {
        this.file = badwords;
        this.port = port;
    }

    private ChatServer() {
        this.port = 1500;
        this.file = "C:\\Users\\ldbau\\IdeaProjects\\Project4\\src\\badwords.txt";
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println(d.format(new Date()) + " Server waiting for clients on port " + this.port + ".");

            while (true) {
                //System.out.println(d.format(new Date()) + " Server waiting for clients on port " + this.port + ".");
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
//                broadcast("just connected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void  broadcast(String message) {
        x = 0;
        ChatFilter cf = new ChatFilter(this.file);
        String newmessage = "";
        newmessage = cf.filter(message);
//        System.out.println(d.format(new Date()) + " " + message);
        for (int x = 0; x < clients.size(); x++) {
            this.clients.get(x).writeMessage(newmessage);
        }
    }

    private synchronized void directMessage(String message, String username, int id) {
        x = 0;
        int u = -1;
        ChatFilter cf = new ChatFilter(this.file);
        for (int x = 0; x < clients.size(); x++) {
            if (clients.get(x).username.equals(username)) {
                u = x;
                break;
            }
        }
        if (u == -1) {
            clients.get(id).writeMessage("This user does not exist.");
        } else {
            String newMsg = cf.filter(message);
//        System.out.println(message);
            clients.get(u).writeMessage(newMsg);
        }
    }

    private synchronized void remove(int d) {
        clients.remove(d);
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer(1500, "C:\\Users\\ldbau\\IdeaProjects\\Project4\\src\\badwords.txt");
        server.start();
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            System.out.println(d.format(new Date()) + " " + username + " just connected.");
            // Read the username sent to you by client
            while (true) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("There was an error receiving messages.");
                    e.printStackTrace();
                    break;
                }
                if (cm.getRecipient() != null) {
                    if (cm.getRecipient().equals(username)) {
                        try {
                            sOutput.writeObject("You cannot direct message yourself.");
                        } catch (IOException e) {
                            System.out.println("There was an error messaging yourself.");
//                            e.printStackTrace();
                            continue;
                        }
                    }
                    String dirMsg = username + " -> " + cm.getRecipient() + ": " + cm.getMessage();
                    directMessage(dirMsg, cm.getRecipient(), this.id);
                    continue;
                }
                if (cm.getType() == 1) {
                    this.close();
                    remove(this.id);
                    continue;
                }
                if (cm.getisListMessage()) {
                    String list = "";
                    if (clients.size() > 1) {
                        for (int y = 0; y < clients.size(); y++) {
                            list += clients.get(y).username + "\n";
                        }
                        list = list.replaceAll(username + "\n", "");
                        try {
                            sOutput.writeObject(list);
                        } catch (IOException e) {
                            System.out.println("There was an error listing clients.");
//                            e.printStackTrace();
                        }
                    } else {
                        try {
                            sOutput.writeObject("Your the only one connected.\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                } else if (cm.getType() == 0) {
                    broadcast(username + ": " + cm.getMessage());
                }
//            broadcast("Hello!");
//            // Send message back to the client
//            try {
//                sOutput.writeObject("Poing!");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            }
        }


        //Synchronized might not be needed here
        private boolean writeMessage(String msg) {
            String date = d.format(new Date());
            if (!socket.isConnected()) {
                return false;
            }
            String newmsg = "";
//            newmsg = date + " " + username + ": " + msg;
            newmsg = date + " " + msg;
            if (x == 0) {
                System.out.println(newmsg);
                x++;
            }
            try {
                sOutput.writeObject(newmsg + "\n");
            } catch (IOException e) {
                System.out.println("There was an error writing a message.");
                e.printStackTrace();
            }
            return true;
        }


        private void close() {
            try {
                broadcast(username + " disconnected with a LOGOUT message.");
                socket.close();
                sInput.close();
                sOutput.close();
            } catch (IOException e) {
                System.out.println("There was an error closing clients.");
                e.printStackTrace();
            }
        }
    }
}
