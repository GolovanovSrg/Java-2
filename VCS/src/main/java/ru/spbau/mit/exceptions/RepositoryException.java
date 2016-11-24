package ru.spbau.mit.exceptions;

public class RepositoryException extends Exception {
    public RepositoryException() {
        super();
    }

    public RepositoryException(String message) {
        super(message);
    }
}
