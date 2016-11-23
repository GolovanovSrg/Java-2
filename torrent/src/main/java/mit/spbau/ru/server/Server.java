package mit.spbau.ru.server;

import mit.spbau.ru.common.Index;
import mit.spbau.ru.common.Seed;
import mit.spbau.ru.common.Torrent;
import mit.spbau.ru.common.exceptions.CommunicationException;
import mit.spbau.ru.common.exceptions.IndexIOException;
import mit.spbau.ru.common.protocol.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String INDEX_FILE_NAME = ".index";
    public static final Path INDEX_FILE_PATH = Paths.get(System.getProperty("user.dir")).resolve(INDEX_FILE_NAME);

    public static final int PORT = 8081;
    private static final int ACCEPT_TIMEOUT_MS = 2000;
    private static final int N_THREADS = 10;

    private final ServerSocket socket;
    private final Index index;
    private final Swarm swarm = new Swarm();
    private final ExecutorService threadpool = Executors.newFixedThreadPool(N_THREADS);
    private final Thread connector = new Thread(this::accept);


    public Server(int port) throws IndexIOException, CommunicationException {
        if (INDEX_FILE_PATH.toFile().exists()) {
            index = Index.load(INDEX_FILE_PATH.toString());
        } else {
            index = new Index();
        }

        try {
            socket = new ServerSocket(port);
            socket.setSoTimeout(ACCEPT_TIMEOUT_MS);
        } catch (IOException e) {
            CommunicationException exception = new CommunicationException("Can not create the server socket");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    private Message executeList() {
        return new ListResponse(index.getTorrents());
    }

    private Message executeUpload(Message request) {
        UploadRequest uploadReq = (UploadRequest) request;
        Torrent torrent = new Torrent(uploadReq.getName(), uploadReq.getSize());
        index.addTorrent(torrent);

        return new UploadResponse(torrent.getId());
    }

    private Message executeSources(Message request) {
        SourcesRequest sourcesReq = (SourcesRequest) request;
        List<Seed> activeSeeds = swarm.getSeeds(sourcesReq.getId());

        return new SourcesResponse(activeSeeds);
    }

    private Message executeUpdate(InetAddress address, Message request) {
        UpdateRequest updateReq = (UpdateRequest) request;
        Seed seed = new Seed(address.toString(), updateReq.getPort());
        swarm.updateSeed(seed, updateReq.getFileIds());

        return new UpdateResponse(true);
    }

    // Не может кидать исключений, т. к. выполняется в новом потоке
    private void processClient(Socket clientSocket) {
        try {
            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

            Message request = (Message) input.readObject();
            Message response = null;

            switch (request.getType()) {
                case LIST_REQUEST:
                    response = executeList();
                    break;
                case UPLOAD_REQUEST:
                    response = executeUpload(request);
                    break;
                case SOURCES_REQUEST:
                    response = executeSources(request);
                    break;
                case UPDATE_REQUEST:
                    response = executeUpdate(clientSocket.getInetAddress(), request);
                    break;
                default:
                    System.err.println("Invalid request type from client " + clientSocket.getInetAddress().toString());
            }

            output.writeObject(response);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error (client " + clientSocket.getInetAddress().toString() + "): " + e.getMessage());
        } finally {
            try {
                clientSocket.shutdownOutput();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error (client " + clientSocket.getInetAddress().toString() + "): " + e.getMessage());
            }
        }
    }

    // Не может кидать исключений, т. к. выполняется в новом потоке
    private void accept() {
        try {
            while (!Thread.interrupted()) {
                try {
                    Socket clientSocket = socket.accept();
                    threadpool.execute(() -> processClient(clientSocket));
                } catch (SocketTimeoutException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Connector error: " + e.getMessage());
        }
    }

    public boolean isAlive() {
        return connector.isAlive();
    }

    public void start() {
        connector.start();
    }

    public void saveIndex() throws IndexIOException {
        index.save(INDEX_FILE_PATH.toString());
    }

    public void stop() throws InterruptedException {
        connector.interrupt();
        threadpool.shutdown();
        connector.join();
    }
}
