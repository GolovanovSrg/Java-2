package mit.spbau.ru.server;

import mit.spbau.ru.common.Index;
import mit.spbau.ru.common.exceptions.CommunicationException;
import mit.spbau.ru.common.exceptions.IndexIOException;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    private final static String INDEX_NAME = ".index";
    private final static int PORT = 8081;

    private final Index index;
    private final Swarm swarm = new Swarm();

    private final ServerSocket socket ;

    public Server(int port) throws IndexIOException, CommunicationException {
        Path indexPath = Paths.get(System.getProperty("user.dir")).resolve(INDEX_NAME);
        if (indexPath.toFile().exists()) {
            index = Index.load(indexPath.toString());
        } else {
            index = new Index();
        }

        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            CommunicationException exception = new CommunicationException("Can not create the server socket");
            exception.addSuppressed(e);
            throw exception;
        }
    }
}
