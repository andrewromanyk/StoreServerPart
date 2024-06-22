package ua.edu.ukma;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class StoreServerTCP {
    private ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        StoreServerTCP server = new StoreServerTCP();
        server.start(5454);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new EchoClientHandler(serverSocket.accept()).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            //Connection:
            try {
                System.out.println("Trying to establish connection...");
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException ioe) {
                System.out.println("Can not connect. Trying again...");
                new EchoClientHandler(clientSocket).start();
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.out.println("Could not close input or output stream.");
                }
                return;
            }

            //Reading
            String inputLine;
            byte[] message = new byte[1024];
            int i = 0;
            try {
                message = new byte[1024];
                i = 0;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received: " + inputLine);
                    if (inputLine.equals("ping")) {
                        System.out.println("ping-pong");
                        System.out.println(Arrays.toString("pong".getBytes()));
                        out.println("pong");
                    }
                    else message[i++] = Byte.parseByte(inputLine);
                }
                System.out.println("Message ended.");
            } catch (IOException ioe) {
                System.out.println("Message stream stopped.");
                try {
                    packetHandler(message);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            //Termination connection
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.out.println("Can not close input stream or stop client.");
            }
        }
        void packetHandler(byte[] message) throws Exception {
            byte[] resultPacket = Reciever.recievePacket(message);
            out.println(Arrays.toString(resultPacket));
        }
    }
}