package ru.spbau.mit.kernel.client.exceptions;

public class ClientException extends Exception {
    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable e) {
        super(message);
        addSuppressed(e);
    }
}
