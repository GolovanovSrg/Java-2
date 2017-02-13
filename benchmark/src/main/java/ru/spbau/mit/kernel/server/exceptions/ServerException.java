package ru.spbau.mit.kernel.server.exceptions;

public class ServerException extends Exception{
    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable e) {
        super(message);
        addSuppressed(e);
    }
}
