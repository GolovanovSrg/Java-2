package ru.spbau.mit.kernel.client;

import ru.spbau.mit.kernel.client.exceptions.ClientException;
import ru.spbau.mit.kernel.ProtoMessage;
import ru.spbau.mit.common.TimeStatistics;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Client {
    protected final String serverIp;
    protected static final int SERVER_PORT = 3666;
    protected final Configuration configuration;
    private final Random rand = new Random();

    public Client(String serverIp, Configuration config) {
        this.serverIp = serverIp;
        this.configuration = config;
    }

    private List<Integer> createArray() {
        return Stream.generate(rand::nextInt)
                .limit(configuration.getArraySize())
                .collect(Collectors.toList());
    }

    protected ProtoMessage.Message createMessage() {
        return ProtoMessage.Message.newBuilder()
                .setSize(configuration.getArraySize())
                .addAllItem(createArray()).build();
    }

    public abstract TimeStatistics runBenchmark() throws ClientException;
}
