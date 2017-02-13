package ru.spbau.mit.kernel.server.exceptions;

public class AcceptException extends RuntimeException {
    public AcceptException(String message) {
        super(message);
    }

    public AcceptException(String message, Throwable e) {
        super(message);
        addSuppressed(e);
    }
}
