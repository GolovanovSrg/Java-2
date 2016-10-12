package ru.spbau.mit;

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

    private void executeList(String path, ObjectOutputStream output) throws IOException {
        List<Path> filePaths = Files.walk(Paths.get(path))
                                .collect(Collectors.toList());

        output.writeInt(filePaths.size());
        output.flush();

        for (int i = 0; i < filePaths.size() && !Thread.interrupted(); ++i) {
            Path filePath = filePaths.get(i);
            output.writeObject(filePath.toString());
            output.writeBoolean(Files.isDirectory(filePath));
            output.flush();
        }
    }

    private void executeGet(String path, ObjectOutputStream output) throws IOException {
        File file = new File(path);
        if (Files.isDirectory(file.toPath()) || !file.exists()) {
            output.writeLong(0);
            output.flush();
        } else {
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
    }

    private void processClient(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().toString();

        try {
            while (!Thread.interrupted()) {
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                Request req = (Request) input.readObject();

                if (req.getType().equals(Request.Type.LIST)) {
                    executeList(req.getPath(), output);
                } else {
                    executeGet(req.getPath(), output);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (clientSocket.isClosed()) {
                System.err.println("Client " + clientAddress + " error: " + e.getMessage());
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Can not close socket for " + clientAddress + " (" + e.getMessage() + ")");
            }
        }
    }

    private void accept() {
        try {
            while (!Thread.interrupted()) {
                Socket clientSocket = socket.accept();
                Thread clientHandler = new Thread(() -> processClient(clientSocket));
                threadList.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            // exit from function;
        }
    }

    public void stop() throws IOException {
        if (socket == null) {
            System.err.println("Server is already stopping");
            return;
        }

        socket.close();
        socket = null;
        threadList.forEach(Thread::interrupt);
        threadList.clear();
    }

    public void start() throws IOException {
        if (socket != null) {
            System.err.println("Server is already starting");
            return;
        }

        try {
            socket = new ServerSocket();
            socket.bind(address);

            Thread connectHandler = new Thread(this::accept);
            connectHandler.setDaemon(true);
            threadList.add(connectHandler);
            connectHandler.start();
        } catch (IOException e){
            IOException exception = new IOException(e);
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
