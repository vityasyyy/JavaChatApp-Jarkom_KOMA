import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable{
    private static Socket clientSocket = null;
    private static ObjectOutputStream outptStream = null;
    private static ObjectInputStream inptStream = null;
    private static BufferedReader inputLine = null;
    private static boolean closed = false;

    public static void main(String[] args) {
        int portNumber = 6942;
        String host = "localhost";

        if(args.length < 2) {
            System.out.println("Default server: " + host + "\nDefault port: " + portNumber);
        } else {
            host = args[0];
            portNumber = Integer.valueOf(args[1]).intValue();
            System.out.println("Server: " + host + "\nPort: " + portNumber);
        }

        try{
            clientSocket = new Socket(host, portNumber); // create socket to connect to server
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
                new Thread(new Client()).start();
                while(!closed){
                    String message = (String) inputLine.readLine().trim();

                    if(message.split(":").length > 1) {
                        outptStream.writeObject(message);
                        outptStream.flush();
                    } else {
                        outptStream.writeObject(message);
                        outptStream.flush();
                    } 
                }
                outptStream.close();
                inptStream.close();
                clientSocket.close();
            } catch (IOException err) {
                System.out.println("IOException:" + err);
            }
        }
    }
    
    // overriding the runnable interface run method
    public void run(){
        String responseLine;
        try{
            while((responseLine = (String) inptStream.readObject()) != null) {
                System.out.println(responseLine);
                if(responseLine.indexOf("Bye") != -1) {
                    break;
                }
            }
            closed = true;
            System.exit(0);
        } catch (IOException | ClassNotFoundException err) {
            System.out.println("IOException: " + err);
        }
    }
}