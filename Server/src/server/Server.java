package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * A class representing a server.
 *
 * @property port - the port the server will be listening to.
 * @property logger - logs server's operations.
 * @property serverSocket - the server's socket.
 * @property processor - the processor for handling the requests the server gets.
 * @property aliveFrequencyInMillisecond - frequency of alive message from the server.
 */
public class Server {
    private final int port;
    private final Logger logger;
    private ServerSocket serverSocket;
    private final RequestProcessor processor;
    private final int aliveFrequencyInMillisecond;

    /**
     * @param port                        - the port the server will be listening to.
     * @param logger                      - logs server's operations.
     * @param processor                   - the processor for handling the requests the server gets.
     * @param aliveFrequencyInMillisecond - frequency of alive message from the server.
     */
    public Server(int port, Logger logger, RequestProcessor processor, int aliveFrequencyInMillisecond) {
        this.port = port;
        this.logger = logger;
        this.processor = processor;
        this.aliveFrequencyInMillisecond = aliveFrequencyInMillisecond;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    /**
     * start waiting for clients to connect.
     */
    public void startServer() {
        if (serverSocket == null) {
            logger.warning("no server");
            return;
        }

        logger.info("Server is listening on port:" + port);

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                logger.info("A new client has connected: " + socket);
                ClientHandler clientHandler = new ClientHandler(socket, logger, processor, aliveFrequencyInMillisecond);

                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException e) {
                closeServerSocket();
            }
        }
    }

    /**
     * closed serverSocket resource.
     */
    private void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        Server server = new Server(12000, Logger.getLogger(Server.class.getName()), new SimpleCalculatorProcessor(), 60000); //TODO
        server.startServer();
    }

}
