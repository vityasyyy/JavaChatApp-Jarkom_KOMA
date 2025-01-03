import java.util.ArrayList; // Resizable array package to store clients thread

import java.io.IOException; // to handle error IO exception
import java.io.ObjectInputStream; // input stream to read data from client
import java.io.ObjectOutputStream; // output stream to write data to client

import java.net.Socket; // to create socket for client
import java.net.ServerSocket; // to create server socket to listen to client request

public class Server {
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    public static ArrayList<clientThread> clients = new ArrayList<clientThread>();

    public static void main (String[] args) {
        int portNumber = 6942; // port number to listen for client request

        // check if port number is specified in args, if not then use the default port number
        if(args.length < 1) {
            System.out.println("No port specified. \nUsing default port: " + portNumber);
        } else {
            portNumber = Integer.parseInt(args[0]);
            System.out.println("Using port: " + portNumber);
        }

        // create server socket with the portNumber, wrap in try catch block
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException err) {
            System.out.println("Server socket cannot be created");
        }

        int clientID = 1; // unique id for client
    
        // Loop to listen for client request
        while(true) {
            try{
                clientSocket = serverSocket.accept(); // accept client request to connect to server
                clientThread current_client = new clientThread(clientSocket, clients); // create new client thread
                clients.add(current_client); // push the new client thread to the clients array
                current_client.start(); // start the client thread to begin executing
                System.out.println("Client " + clientID + " connected");
                clientID++; // increment client id
            } catch(IOException err) {
                System.out.println("Client cannot connect to server");
            }
        }
    }
}

class clientThread extends Thread {
    private String clientName = null; // store the client name in clientThread object
    private ObjectInputStream inptStream = null; // input stream to read data from client
    private ObjectOutputStream outptStream = null; // output stream to write data to client
    private Socket clientSocket = null; // socket for client
    private final ArrayList<clientThread> clients; // store all of the clients thread

    // constructor to initialize the client socket and clients array
    public clientThread(Socket clientSocket, ArrayList<clientThread> clients) {
        this.clientSocket = clientSocket;
        this.clients = clients;
    }
    // override thread run methods to run the clientThread as we wish
    public void run() {
        ArrayList<clientThread> clients = this.clients;

        try{
            inptStream = new ObjectInputStream(clientSocket.getInputStream()); // create input stream to read data from client
            outptStream = new ObjectOutputStream(clientSocket.getOutputStream()); // output stream to write data to client
            
            String clientNameInput; // store client name in string

            while(true) {
                // no more than one thread will access this block of code
                synchronized(this) {
                    this.outptStream.writeObject("Enter your name: "); // send message to client to enter their name
                    this.outptStream.flush(); // flush the output stream
                    clientNameInput = (String) this.inptStream.readObject(); // read the client name from input stream

                    // check if the client name is empty
                    if(clientNameInput == null || clientNameInput.isEmpty()) {
                        outptStream.writeObject("Name cannot be empty");
                        outptStream.flush();
                        continue;
                    }

                    // client name cannot contain @ or ! because it will be used for unicast and blockcast
                    if((clientNameInput.contains("@") || clientNameInput.contains("!"))) {
                        outptStream.writeObject("Username should not contain '@' or '!' char");
                        outptStream.flush();
                        continue;
                    }
                    boolean nameExists = false;
                    for(clientThread current_client : clients) {
                        if(current_client != null && current_client.clientName != null && current_client.clientName.equals("@" + clientNameInput)) {
                            nameExists = true;
                            break;
                        }
                    }
                    if(nameExists) {
                        outptStream.writeObject("Username already exists");
                        outptStream.flush();
                        continue;
                    }
                    break; // break the loop if the client name is valid
                }
            }

            System.out.println("Client name is: " + clientNameInput);

            this.outptStream.writeObject("Welcome " + clientNameInput + " to the chat room");
            this.outptStream.flush();

            synchronized(this) {
                for(clientThread current_client : clients) {
                    if(current_client != null && current_client == this) {
                        clientName = "@" + clientNameInput; // add @ prefix to the client name for unicasting
                        break;
                    }
                }

                for(clientThread current_client : clients) {
                    if(current_client != null && current_client != this) {
                        current_client.outptStream.writeObject("New user " + clientNameInput + " entered the chat room");
                        current_client.outptStream.flush();
                    }
                }
            }
            // initial message for the client
            this.outptStream.writeObject("<command><client_name>: <your_message> \n(@ for unicast, ! for blockcast, anything else for broadcast, /quit for quit)");
            this.outptStream.flush();

            while(true) {
                String line = (String) inptStream.readObject();

                if(line.startsWith("/quit")){
                    break;
                }

                if(line.startsWith("@")){
                    unicast(line, clientNameInput);
                } else if(line.startsWith("!")){
                    blockcast(line, clientNameInput);
                } else {
                    broadcast(line, clientNameInput);
                }
            }
        } catch (IOException err) {
            System.out.println("Session terminated");
        } catch (ClassNotFoundException err) {
            System.out.println("Class not found");
        } finally {
            try {
                if (outptStream != null) outptStream.close();
                if (inptStream != null) inptStream.close();
                if (clientSocket != null) clientSocket.close();
                
                // Remove client from list
                synchronized (clients) {
                    clients.remove(this);
                    System.out.println("Client " + clientName + " disconnected");
                    
                    // Notify others about disconnection
                    for (clientThread client : clients) {
                        if (client != null && client.clientName != null) {
                            client.outptStream.writeObject("User " + clientName.substring(1) + " left the chat room");
                            client.outptStream.flush();
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error during cleanup: " + e.getMessage());
            }
        }
    }

    // send messages to a specific client
    void unicast(String line, String name) throws IOException, ClassNotFoundException {
        String[] words = line.split(":", 2); // split the words from commands and message
        if(!(words.length > 1 && words[1] != null)) {
            this.outptStream.writeObject("Specify the client name and your message, e.g. @<client_name>: <your_message>");
            this.outptStream.flush();
            return;
        } else {
            words[1] = words[1].trim(); //remove the whitespace from both ends of the messages
            if(words[1].isEmpty()) {
                this.outptStream.writeObject("Message cannot be empty");
                this.outptStream.flush();
                return;
            }
            for(clientThread current_client : clients) {
                if(current_client == null || current_client == this) continue;
                if(current_client.clientName.equals(words[0])){
                    current_client.outptStream.writeObject("{" + name + "(to you)}" + words[1]);
                    current_client.outptStream.flush();

                    System.out.println(this.clientName.substring(1) + " transferred a private message to client " + current_client.clientName.substring(1));

                    this.outptStream.writeObject("Private message sent to " + current_client.clientName.substring(1));
                    this.outptStream.flush();
                    break;
                }
                this.outptStream.writeObject("Client " + words[0].substring(1) + " not found");
                this.outptStream.flush();
                break;
            }
        }
    }
    
    // send messages to all clients except for the blocked one
    void blockcast(String line, String name) throws IOException, ClassNotFoundException{
        String[] words = line.split(":", 2);
        if(words.length < 1 && words[1] == null) {
            this.outptStream.writeObject("Specify the client name and your message, e.g. !<client_name>: <your_message>");
            this.outptStream.flush();
            return;
        } else {
            if(words[1].isEmpty()) {
                this.outptStream.writeObject("Message cannot be empty");
                this.outptStream.flush();
                return;
            }
            for(clientThread current_client : clients) {
                if(current_client != null && current_client != this && current_client.clientName != null && !current_client.clientName.equals("@"+words[0].substring(1))){
                    current_client.outptStream.writeObject("{" + name + "(blockcast)}" + words[1]);
                    current_client.outptStream.flush();
                }
                System.out.println(this.clientName.substring(1) + " transferred a blockcast message except to client " + current_client.clientName.substring(1));
                this.outptStream.writeObject("Blockcast message sent to everyone except: " + words[0]);
                this.outptStream.flush();
                break;
            }
        }
    }

    void broadcast(String line, String name) throws IOException, ClassNotFoundException{
        if(line.isEmpty()) {
            this.outptStream.writeObject("Broadcast message cannot be empty");
            this.outptStream.flush();
            return;
        }
        for(clientThread current_client : clients) {
            if(current_client != null && current_client.clientName != null && current_client.clientName != this.clientName) {
                current_client.outptStream.writeObject("{" + name + "(broadcast)}" + line);
                current_client.outptStream.flush();
            }
        }
        this.outptStream.writeObject("Broadcast message sent successfully");
        this.outptStream.flush();
        System.out.println("Broadcast message sent by " + this.clientName.substring(1));
    }
}