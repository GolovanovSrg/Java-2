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
import java.util.Scanner;
import java.util.stream.Collectors;

public class Server {
    private final SocketAddress address;
    private ServerSocket socket = null;
    private List<Thread> threadList = new LinkedList<>();

    public Server(String ip, int port){
        address = new InetSocketAddress(ip, port);
    }

    private void executeList(String path, ObjectOutputStream output) throws IOException {
        List<Path> files = Files.walk(Paths.get(path))
                                .collect(Collectors.toList());

        output.writeInt(files.size());
        for (int i = 0; i < files.size() && !Thread.interrupted(); ++i) {
            output.writeChars(files.get(i).toString());
            output.writeBoolean(Files.isDirectory(files.get(i)));
        }
    }

    private void executeGet(String path, ObjectOutputStream output) throws IOException {
        File file = new File(path);
        if (Files.isDirectory(file.toPath())) {
            output.write(0);
        } else {
            byte[] buffer = new byte[4096];
            try (InputStream fileInput = new FileInputStream(file)) {
                long size = file.length();
                output.write(String.valueOf(size).getBytes());

                int readBytes = 0;
                while ((readBytes = fileInput.read(buffer)) != -1 && !Thread.interrupted()) {
                    output.write(buffer, 0, readBytes);
                }
            }
        }
    }

    private void processClient(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().toString();

        try {
            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

            while (!Thread.interrupted()) {
                Request req = (Request) input.readObject();

                if (req.getType().equals(Request.Type.LIST)) {
                    executeList(req.getPath(), output);
                } else {
                    executeGet(req.getPath(), output);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client " + clientAddress + " error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Can not close socket for " + clientAddress + " (" + e.getMessage() + ")");
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
            System.out.println("Accepting connections is stopped");
            // exit from function;
        }
    }

    public void stop() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Can not close server socket (" + e.getMessage() + ")");
        }

        threadList.forEach(Thread::interrupt);
        threadList.clear();
    }

    public void start() throws IOException {
        try {
            socket = new ServerSocket();
            socket.bind(address);

            Thread connectHandler = new Thread(this::accept);
            connectHandler.setDaemon(true);
            threadList.add(connectHandler);
            connectHandler.start();
        } finally {
            socket.close();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: Server <ip> <port>");
        }

        String ip = args[0];
        int port = Integer.valueOf(args[1]);
        Server server = new Server(ip, port);

        try {
            server.start();

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine();
                if (cmd.equals("exit")) {
                    server.stop();
                    System.exit(0);
                } else {
                    System.out.println("Unknown command");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
