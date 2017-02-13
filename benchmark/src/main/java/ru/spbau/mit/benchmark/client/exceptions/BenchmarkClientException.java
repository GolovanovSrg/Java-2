package ru.spbau.mit.benchmark.client.exceptions;

public class BenchmarkClientException extends Exception {
    public BenchmarkClientException(String message) {
        super(message);
    }

    public BenchmarkClientException(String message, Throwable e) {
        super(message);
        addSuppressed(e);
    }
}
