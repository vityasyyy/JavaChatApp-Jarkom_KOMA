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
}