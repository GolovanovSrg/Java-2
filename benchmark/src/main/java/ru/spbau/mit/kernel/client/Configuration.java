package ru.spbau.mit.kernel.client;

public class Configuration {
    private final int arraySize;
    private final int timeBetweenRequests;
    private final int nRequests;

    public Configuration(int arraySize, int timeBetweenRequests, int nRequests) {
        this.arraySize = arraySize;
        this.timeBetweenRequests = timeBetweenRequests;
        this.nRequests = nRequests;
    }

    public int getArraySize() {
        return arraySize;
    }

    public long getTimeBetweenRequests() {
        return timeBetweenRequests;
    }

    public int getNRequests() {
        return nRequests;
    }
}
