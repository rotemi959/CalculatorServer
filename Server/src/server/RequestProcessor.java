package server;

import java.security.InvalidParameterException;

public interface RequestProcessor {
    /**
     * @param input - the received data to process
     * @return the processed data
     * @throws InvalidParameterException - when the given input can't be processed by the server.
     */
    String process(String input) throws InvalidParameterException;
}
