package ru.spbau.mit.benchmark.client;

import ru.spbau.mit.common.ServerArchitecture;

public class BenchmarkClientConfiguration {
    private final ServerArchitecture architecture;
    private final int nRequest;
    private final BenchmarkIntArray arraySize;
    private final BenchmarkIntArray nClients;
    private final BenchmarkIntArray timeBetweenRequests;

    public BenchmarkClientConfiguration(ServerArchitecture architecture, int nRequest, BenchmarkIntArray arraySize,
                                        BenchmarkIntArray nClients, BenchmarkIntArray timeBetweenRequests) {
        this.architecture = architecture;
        this.nRequest = nRequest;
        this.arraySize = arraySize;
        this.nClients = nClients;
        this.timeBetweenRequests = timeBetweenRequests;
    }

    public int getNRequest() {
        return nRequest;
    }

    public BenchmarkIntArray getArraySize() {
        return arraySize;
    }

    public BenchmarkIntArray getNClients() {
        return nClients;
    }

    public BenchmarkIntArray getTimeBetweenRequests() {
        return timeBetweenRequests;
    }

    public ServerArchitecture getArchitecture() {
        return architecture;
    }
}
