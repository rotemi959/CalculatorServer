package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * A class for server's client.
 *
 * @property socket - a socket for client's requests and their responses from the server.
 * @property bufferedReader - buffer for responses.
 * @property bufferedWriter - buffer for requests
 */
public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeResources();
        }
    }

    /**
     * Sends client's requests to the server from the commandline.
     */
    public void sendRequest() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(System.in);
            while (!socket.isClosed()) {
                String requestToSend = scanner.nextLine();
                bufferedWriter.write(requestToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                if (requestToSend.equals("exit")) {
                    System.out.println("Closing this connection: " + socket);
                    socket.close();
                    System.out.println("Connection closed");
                    break;
                }
            }
        } catch (IOException e) {
            scanner.close();
            e.printStackTrace();
        }
    }

    /**
     * Listens for server's responses.
     */
    public void listenForResponses() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverResponse;

                while (!socket.isClosed()) {
                    try {
                        serverResponse = bufferedReader.readLine();
                        System.out.println(serverResponse);
                    } catch (IOException e) {
                        closeResources();
                    }
                }
            }
        }).start();
    }

    /**
     * closes opened resources.
     */
    private void closeResources() {
        try {
            if (socket != null) {
                socket.close();
            }

            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12000);
        Client client = new Client(socket);
        client.listenForResponses();
        client.sendRequest();

    }
}
