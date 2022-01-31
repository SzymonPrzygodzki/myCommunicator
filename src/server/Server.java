package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

    static Vector<ClientHandler> clients  = new Vector<ClientHandler>();

    public static void main(String[] args) {
        try (ServerSocket s = new ServerSocket(2200)) {
            int count = 0;
            System.out.println("Server is waiting for users...");
            while (true) {
                Socket connection = s.accept();
                String clientName = "Client" + count;
                BufferedReader inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                PrintWriter outStream = new PrintWriter(connection.getOutputStream(), true);
                ClientHandler clientHandler = new ClientHandler(clientName, connection, inStream, outStream);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                ++count;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {

    String clientName;
    Socket connection;
    BufferedReader inStream;
    PrintWriter outStream;

    public ClientHandler(String clientName, Socket connection, BufferedReader inStream, PrintWriter outStream) {
        this.clientName = clientName;
        this.connection = connection;
        this.inStream = inStream;
        this.outStream = outStream;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = receiveMsg();
                if (message.endsWith("User wants to disconnect")) {
                    message = "Server:" + message.substring(0, message.indexOf(">")) + " has left the server.";
                    for (ClientHandler ch : Server.clients) {
                        if (!ch.getClientName().equals(clientName)) {
                            ch.sendMsg(message);
                        }
                    }
                    closeClientStreams();
                    break;
                }
                for (ClientHandler ch : Server.clients) {
                    if (!ch.getClientName().equals(clientName)) {
                        ch.sendMsg(message);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public String receiveMsg() throws IOException {
        return inStream.readLine();
    }

    public void sendMsg(String msg) {
        outStream.println(msg);
    }

    public void closeClientStreams() throws IOException {
        inStream.close();
        outStream.close();
        connection.close();
        Server.clients.remove(this);
    }

    public String getClientName() {
        return clientName;
    }
}
