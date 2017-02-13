package ru.spbau.mit.kernel.client;

public class ProcessingTime {
    private final long arrayProcessingTime;
    private final long requestProcessingTime;

    public ProcessingTime(long arrayProcessingTime, long requestProcessingTime) {
        this.arrayProcessingTime = arrayProcessingTime;
        this.requestProcessingTime = requestProcessingTime;
    }

    public long getArrayProcessingTime() {
        return arrayProcessingTime;
    }

    public long getRequestProcessingTime() {
        return requestProcessingTime;
    }
}
