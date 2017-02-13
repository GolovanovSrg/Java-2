package ru.spbau.mit.benchmark.client;

import ru.spbau.mit.common.TimeStatistics;

public class BenchmarkStatistics {
    private final TimeStatistics time;
    private final int arraySize;
    private final int nClients;
    private final int timeBetweenRequests;
    private final int nRequests;

    public BenchmarkStatistics(TimeStatistics time, int arraySize,
                               int nClients, int timeBetweenRequests, int nRequests) {
        this.time = time;
        this.arraySize = arraySize;
        this.nClients = nClients;
        this.timeBetweenRequests = timeBetweenRequests;
        this.nRequests = nRequests;

    }

    public TimeStatistics getTime() {
        return time;
    }

    public int getArraySize() {
        return arraySize;
    }

    public int getnClients() {
        return nClients;
    }

    public int getTimeBetweenRequests() {
        return timeBetweenRequests;
    }

    public int getnRequests() {
        return nRequests;
    }
}
