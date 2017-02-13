package ru.spbau.mit.benchmark.server;

import ru.spbau.mit.benchmark.server.exceptions.BenchmarkServerException;
import ru.spbau.mit.common.ServerArchitecture;
import ru.spbau.mit.kernel.server.Server;
import ru.spbau.mit.kernel.server.exceptions.ServerException;
import ru.spbau.mit.kernel.server.tcp.*;
import ru.spbau.mit.kernel.server.udp.UdpServerThreadOnRequest;
import ru.spbau.mit.kernel.server.udp.UdpServerThreadPool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BenchmarkServer {
    private static final Logger logger = Logger.getLogger(BenchmarkServer.class.getName());
    private static final int PORT = 1488;
    private ServerSocket socket = null;

    public static void main(String[] args) {
        BenchmarkServer benchmarkServer = new BenchmarkServer();
        try {
            benchmarkServer.start();
            System.out.println("Benchmark server is running");
            benchmarkServer.accept();
        } catch (BenchmarkServerException e) {
            System.out.println("Can not run benchmark server: " + e.getMessage());
        }
    }

    private Server createServer(ServerArchitecture architecture) {
        Server server;
        if (architecture == ServerArchitecture.TCP_SERVER_ASYNC) {
            server = new TcpServerAsync();
        } else if (architecture == ServerArchitecture.TCP_SERVER_SINGLE_THREAD) {
            server = new TcpServerSingleThread();
        } else if (architecture == ServerArchitecture.TCP_THREAD_ON_CLIENT) {
            server = new TcpServerThreadOnClient();
        } else if (architecture == ServerArchitecture.TCP_SERVER_CACHED_THREAD_POOL) {
            server = new TcpServerCachedThreadPool();
        } else if (architecture == ServerArchitecture.TCP_SERVER_FIXED_THREAD_POOL) {
            server = new TcpServerFixedThreadPool();
        } else if (architecture == ServerArchitecture.UDP_SERVER_THREAD_ON_REQUEST) {
            server = new UdpServerThreadOnRequest();
        } else {
            server = new UdpServerThreadPool();
        }

        return server;
    }

    private void processClient(Socket clientSocket) {
        Server server = null;
        try {
            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

            ServerArchitecture architecture = (ServerArchitecture) input.readObject();
            server = createServer(architecture);
            server.start();

            new DataOutputStream(clientSocket.getOutputStream()).writeBoolean(true);
            new DataInputStream(clientSocket.getInputStream()).readBoolean();
        } catch (ServerException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Can not start server");
        } catch (IOException ignored) {
        } finally {
            if (server != null) {
                try {
                    server.stop();
                } catch (ServerException e) {
                    logger.log(Level.INFO, "Can not stop server");
                }
            }

            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.INFO, "Can not close benchmark client socket");
            }
        }
    }

    private void accept() {
        try {
            while (!Thread.interrupted()) {
                Socket clientSocket = socket.accept();
                processClient(clientSocket);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Accept error. Benchmark server not accept connections", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.INFO, "Can not close benchmark server socket", e);
            }
        }
    }

    private void start() throws BenchmarkServerException {
        try {
            socket = new ServerSocket(PORT);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can not create benchmark server socket", e);
            throw new BenchmarkServerException("Can not create benchmark server socket", e);
        }
    }
}
