package ru.spbau.mit.benchmark.server.exceptions;

public class BenchmarkServerException extends Exception {
    public BenchmarkServerException(String message) {
        super(message);
    }

    public BenchmarkServerException(String message, Throwable e) {
        super(message);
        addSuppressed(e);
    }
}
