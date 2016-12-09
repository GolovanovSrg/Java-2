package ru.spbau.mit.server;

import org.apache.commons.lang3.tuple.Pair;
import ru.spbau.mit.exceptions.CommandException;
import ru.spbau.mit.exceptions.CommunicationException;
import ru.spbau.mit.protocol.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerImpl {
    private final SocketAddress address;
    private final List<Thread> threadList = new LinkedList<>();
    private ServerSocket socket = null;

    public ServerImpl(String ip, int port){
        address = new InetSocketAddress(ip, port);
    }

    private void executeList(String path, ObjectOutputStream output) throws CommandException {
        try {
            ListResponse resp;
            Path directoryPath = Paths.get(path).toAbsolutePath().normalize();

            if (!directoryPath.toFile().exists()) {
                resp = new ListResponse(false, null);
            } else {
                List<Pair<String, Boolean>> filePaths = Files.walk(directoryPath)
                                                             .map(p -> Pair.of(p.toString(), Files.isDirectory(p)))
                                                             .collect(Collectors.toList());
                resp = new ListResponse(true, filePaths);
            }

            output.writeObject(resp);
            output.flush();
        } catch (IOException e) {
            CommandException exception = new CommandException("Can not send the response to the client");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    private void executeGet(String path, ObjectOutputStream output) throws CommandException {
        try {
            GetResponse resp;

            File file = Paths.get(path).toAbsolutePath().normalize().toFile();
            boolean fileExists = !Files.isDirectory(file.toPath()) && file.exists();

            if (!fileExists) {
                resp = new GetResponse(false, 0);
            } else {
                resp = new GetResponse(true, file.length());
            }

            output.writeObject(resp);
            output.flush();

            if (fileExists && file.length() > 0) {
                byte[] buffer = new byte[4096];
                try (InputStream fileInput = new FileInputStream(file)) {
                    long size = file.length();
                    output.writeLong(size);

                    int readBytes = 0;
                    while ((readBytes = fileInput.read(buffer)) != -1 && !Thread.interrupted()) {
                        output.write(buffer, 0, readBytes);
                        output.flush();
                    }
                }
            }
        } catch (IOException e) {
            CommandException exception = new CommandException("Can not send the response to the client");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    private void processClient(Socket clientSocket) throws CommandException, CommunicationException {
        String clientAddress = clientSocket.getInetAddress().toString();

        try {
            while (!Thread.interrupted()) {
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                Message req = (Message) input.readObject();

                if (req.getType() == MessageType.DISCONNECT_REQUEST) {
                    return;
                } else if (req.getType() == MessageType.LIST_REQUEST) {
                    executeList(((ListRequest) req).getPath(), output);
                } else if (req.getType() == MessageType.GET_REQUEST) {
                    executeGet(((GetRequest) req).getPath(), output);
                } else {
                    throw new CommandException("Unknown request from the client " + clientAddress);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            CommandException exception = new CommandException("Can not process the request from the client " + clientAddress);
            exception.addSuppressed(e);
            throw exception;
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                CommandException exception = new CommandException("Can not close the socket for client " + clientAddress);
                exception.addSuppressed(e);
                throw exception;
            }
        }
    }

    private void accept() throws CommunicationException {
        try {
            while (!Thread.interrupted()) {
                Socket clientSocket = socket.accept();
                Thread clientHandler = new Thread(() -> {
                    try {
                        processClient(clientSocket);
                    } catch (CommandException | CommunicationException e) {
                        System.err.println("Error: " + e.getMessage());
                        for (Throwable t : e.getSuppressed()) {
                            System.err.println("Suppressed: " + t.getMessage());
                        }
                    }
                });

                threadList.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                CommunicationException exception = new CommunicationException("Socket error");
                exception.addSuppressed(e);
                throw exception;
            }
        }
    }

    public void stop() throws CommunicationException {
        if (socket == null) {
            System.err.println("Server is already stopping");
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            CommunicationException exception = new CommunicationException("Can not close the socket when stopping the server");
            exception.addSuppressed(e);
            throw exception;
        }

        socket = null;
        threadList.forEach(Thread::interrupt);
        threadList.clear();
    }

    public void start() throws CommunicationException {
        if (socket != null) {
            System.err.println("Server is already starting");
            return;
        }

        try {
            socket = new ServerSocket();
            socket.bind(address);

            Thread connectHandler = new Thread(() -> {
                try {
                    this.accept();
                } catch (CommunicationException e) {
                    System.err.println("Error: " + e.getMessage());
                    for (Throwable t : e.getSuppressed()) {
                        System.err.println("Suppressed: " + t.getMessage());
                    }
                }
            });

            connectHandler.setDaemon(true);
            threadList.add(connectHandler);
            connectHandler.start();
        } catch (IOException e){
            CommunicationException exception = new CommunicationException("Socket error");
            exception.addSuppressed(e);
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e2) {
                    exception.addSuppressed(e2);
                }
                socket = null;
            }

            throw exception;
        }
    }

    public boolean isStarted() {
        return socket != null;
    }
}
