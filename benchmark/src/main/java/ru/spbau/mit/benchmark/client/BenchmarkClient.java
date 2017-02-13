package ru.spbau.mit.benchmark.client;

import ru.spbau.mit.benchmark.client.exceptions.BenchmarkClientException;
import ru.spbau.mit.common.ServerArchitecture;
import ru.spbau.mit.common.TimeStatistics;
import ru.spbau.mit.kernel.client.Client;
import ru.spbau.mit.kernel.client.Configuration;
import ru.spbau.mit.kernel.client.exceptions.ClientException;
import ru.spbau.mit.kernel.client.tcp.TcpClientFixedConnection;
import ru.spbau.mit.kernel.client.tcp.TcpClientNotFixedConnection;
import ru.spbau.mit.kernel.client.udp.UdpClient;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BenchmarkClient {
    private static final Logger logger = Logger.getLogger(BenchmarkClient.class.getName());
    private static final String BENCHMARK_RESULT_FILE_NAME = "benchmark.csv";
    private static final int SERVER_PORT = 1488;
    private final String serverIp;
    private Socket socket = null;
    private final BenchmarkClientConfiguration config;

    public BenchmarkClient(String serverIp, BenchmarkClientConfiguration config) {
        this.serverIp = serverIp;
        this.config = config;
    }

    private void connect() throws IOException {
        if (socket == null) {
            socket = new Socket(serverIp, SERVER_PORT);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(config.getArchitecture());

            new DataInputStream(socket.getInputStream()).readBoolean();
        }
    }

    private void disconnect() throws IOException {
        if (socket != null) {
            new DataOutputStream(socket.getOutputStream()).writeBoolean(true);
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            socket = null;
        }
    }

    private Client createClient(ServerArchitecture architecture, Configuration config) {
        Client client;
        if (architecture == ServerArchitecture.UDP_SERVER_THREAD_ON_REQUEST ||
                architecture == ServerArchitecture.UDP_SERVER_THREAD_POOL) {
            client = new UdpClient(serverIp, config);
        } else if (architecture == ServerArchitecture.TCP_SERVER_SINGLE_THREAD) {
            client = new TcpClientNotFixedConnection(serverIp, config);
        } else {
            client = new TcpClientFixedConnection(serverIp, config);
        }

        return client;
    }

    private List<BenchmarkStatistics> runBenchmark() throws BenchmarkClientException {
        ServerArchitecture architecture = config.getArchitecture();

        List<BenchmarkStatistics> result = new ArrayList<>();

        while (config.getArraySize().hasNext()) {
            config.getTimeBetweenRequests().reset();
            config.getNClients().reset();
            int arraySize = config.getArraySize().next();
            while (config.getTimeBetweenRequests().hasNext()) {
                config.getNClients().reset();
                int timeBetweenRequests = config.getTimeBetweenRequests().next();
                while (config.getNClients().hasNext()) {
                    int nClients = config.getNClients().next();
                    int nRequets = config.getNRequest();

                    Configuration clientConfig = new Configuration(arraySize, timeBetweenRequests, nRequets);

                    List<Thread> clientThreads = new ArrayList<>();
                    List<TimeStatistics> statisticses = new CopyOnWriteArrayList<>();
                    for (int i = 0; i < nClients; i++) {
                        Client client = createClient(architecture, clientConfig);
                        Thread thread = new Thread(() -> {
                            try {
                                statisticses.add(client.runBenchmark());
                            } catch (ClientException e) {
                                logger.log(Level.INFO, "Error in worker", e);
                            }
                        });
                        thread.setDaemon(true);
                        clientThreads.add(thread);
                        thread.start();
                    }

                    for (Thread thread : clientThreads) {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            logger.log(Level.INFO, "Benchmark error", e);
                            throw new BenchmarkClientException("Benchmark error", e);
                        }
                    }

                    TimeStatistics resultTime = TimeStatistics.mean(statisticses);

                    result.add(new BenchmarkStatistics(resultTime, arraySize, nClients, timeBetweenRequests, nRequets));
                }
            }
        }

        return result;
    }

    private void save(List<BenchmarkStatistics> statisticses) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String fileName = dtf.format(now) + "-" + BENCHMARK_RESULT_FILE_NAME;
        File file = new File(fileName);
        file.createNewFile();
        PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());

        printWriter.println("architecture, n_clients, array_size, n_requests, " +
                "time_between_requests, array_processing_time, request_processing_time, client_time");

        for (BenchmarkStatistics s : statisticses) {
            printWriter.println(config.getArchitecture() + ", " +
                    s.getnClients() + ", " + s.getArraySize() + ", " +
                    s.getnRequests() + ", " + s.getTimeBetweenRequests() + ", " +
                    s.getTime().getArrayProcessingTime() + ", " +
                    s.getTime().getRequestProcessingTime() + ", " +
                    s.getTime().getClientWorkTime());
        }

        printWriter.close();
    }

    public List<TimeStatistics> benchmark() throws BenchmarkClientException {
        try {
            connect();
            List<BenchmarkStatistics> result = runBenchmark();
            save(result);
            return result.stream().map(bs -> bs.getTime()).collect(Collectors.toList());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Benchmark error", e);
            throw new BenchmarkClientException("Benchmark error", e);
        } finally {
            try {
                disconnect();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Can not disconnect benchmark", e);
                throw new BenchmarkClientException("Can not disconnect benchmark", e);
            }
        }
    }
}
