package server;


import java.io.*;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * A handler for request in the server side.
 * The ClientHandler process the request and response.
 *
 * @property socket - the socket for the request and the response.
 * @property bufferedReader - the buffer for the request.
 * @property bufferedWriter - the buffer for the response.
 * @property processor - the processor that is required for processing client's request.
 * @property logger - logs server's operations.
 * @property aliveFrequencyInMillisecond - frequency of alive message from the server.
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private RequestProcessor processor;
    private Logger logger;
    private int aliveFrequencyInMillisecond;

    /**
     * [ClientHandler] constructor
     *
     * @param socket                      - the relevant socket for the request
     * @param logger                      - logs server's operations.
     * @param processor                   - the processor that is required for processing client's request.
     * @param aliveFrequencyInMillisecond - frequency of alive message from the server.
     */
    public ClientHandler(Socket socket, Logger logger, RequestProcessor processor, int aliveFrequencyInMillisecond) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.processor = processor;
            this.logger = logger;
            this.aliveFrequencyInMillisecond = aliveFrequencyInMillisecond;
        } catch (IOException e) {
            closeResources();
        }
    }

    /**
     * Handles a request and sends the response to the client
     */
    @Override
    public void run() {
        String clientRequest;
        startTimer(aliveFrequencyInMillisecond);

        while (!socket.isClosed()) {
            try {
                clientRequest = bufferedReader.readLine();

                if (clientRequest.equals("exit")) {
                    logger.info("Client " + socket + "sends exit");
                    logger.info("Closing this connection.");
                    socket.close();
                    logger.info("Connection closed");
                    break;
                }

                String res = "";

                try {
                    res = processor.process(clientRequest);
                    logger.info("<" + socket + ">" + " request: " + clientRequest + ", result: " + res);
                } catch (InvalidParameterException e) {
                    logger.warning("<" + socket + ">" + " request: " + clientRequest + " is invalid");
                    bufferedWriter.write("Invalid Expression");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                bufferedWriter.write(res);
                bufferedWriter.newLine();
                bufferedWriter.flush();

            } catch (IOException e) {
                closeResources();
                break;
            }
        }
    }

    /**
     * starts periodic timer for alive message from the server.
     *
     * @param timeInMilliseconds - frequency of the alive message in milliseconds
     */
    private void startTimer(int timeInMilliseconds) {
        Timer timer = new Timer();
        timer.schedule(new sendAlivePeriodic(), 0, timeInMilliseconds);
    }

    /**
     * closes resources
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


    /**
     * A TimerTask for sending alive message from the server.
     */
    private class sendAlivePeriodic extends TimerTask {
        public void run() {
            try {
                bufferedWriter.write("Server is alive");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                ClientHandler.this.closeResources();
            }

        }
    }

}
