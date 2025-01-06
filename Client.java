import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable{
    private static Socket clientSocket = null;
    private static ObjectOutputStream outptStream = null; // output stream to write data to server
    private static ObjectInputStream inptStream = null; // input stream to read data from server
    private static BufferedReader inputLine = null; // read input from user
    private static volatile boolean closed = false; // one thread can change the value of closed and other threads can see the change, to keep track of the connection status

    public static void main(String[] args) {
        int portNumber = 6942;
        String host = "localhost";
        // no args provided, use default server and port
        if(args.length < 2) {
            System.out.println("Default server: " + host + "\nDefault port: " + portNumber);
            // provided args, java Client.java <server> <port>
        } else {
            host = args[0];
            portNumber = Integer.valueOf(args[1]).intValue();
            System.out.println("Server: " + host + "\nPort: " + portNumber);
        }

        try{
            clientSocket = new Socket(host, portNumber); // create socket to connect to server, the server must be running first, otherwise it will catch IOException
            inputLine = new BufferedReader(new InputStreamReader(System.in)); // read input from user
            outptStream = new ObjectOutputStream(clientSocket.getOutputStream()); // output stream to write data to server
            inptStream = new ObjectInputStream(clientSocket.getInputStream()); // input stream to read data from server
        } catch (UnknownHostException err) {
            System.out.println("Unknown host: " + host);
        } catch (IOException err) {
            System.out.println("Cannot connect to server");
        }

        if(clientSocket != null && outptStream != null && inptStream != null) {
            try{
                new Thread(new Client()).start(); // start a new thread to read data from server, implementing the run method
                while(!closed){
                    String message = inputLine.readLine(); // read input from user
                    if (message == null) { // if user closes the connection, close the client connection
                        break;
                    }
                    
                    message = message.trim(); // remove leading and trailing whitespaces
                    
                    // Check for quit command first
                    if (message.startsWith("/quit")) {
                        outptStream.writeObject(message); // write quit to the server
                        outptStream.flush();
                        closed = true; // close the connection and exit the program
                        break;
                    }

                    // Write message to the server
                    outptStream.writeObject(message);
                    outptStream.flush(); 
                }
            } catch (IOException err) {
                System.out.println("IOException:" + err);
            } finally {
                cleanup(); // close all streams and socket
            }
        }
    }
    // cleanup method to close all streams and socket
    public static void cleanup() {
        try{
            closed = true;
            if(inputLine != null) inputLine.close();
            if(outptStream != null)outptStream.close();
            if(inptStream != null)inptStream.close();
            if(clientSocket != null)clientSocket.close();
        } catch (IOException err) {
            System.out.println("IOException: " + err);
        }
    }
    // overriding the runnable interface run method
    public void run(){
        String responseLine;
        try{
            while(!closed && (responseLine = (String) inptStream.readObject()) != null) {
                System.out.println(responseLine); // print the message from server to the user console
                if(responseLine.indexOf("Bye") != -1) {
                    closed = true;
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException err) {
            if(!closed){
                System.out.println("Server has closed the connection: " + err);
                System.exit(1);
            }
        } finally {
            cleanup();
            System.exit(0);
        }
    }
}