package ru.spbau.mit;

import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Client {
    private final String ip;
    private final int port;
    private Socket socket = null;

    public Client(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(ip, port);
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Can not close client socket (" + e.getMessage() + ")");
        }
    }

    public List<Pair<String, Boolean>> executeList(String path) {
        List<Pair<String, Boolean>> fileList = new LinkedList<>();

        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            Request req = new Request(Request.Type.LIST, path);
            output.writeObject(req);

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            int nFiles = input.readInt();

            for (int i = 0; i < nFiles; ++i) {
                String name = input.readUTF();
                Boolean isDir = input.readBoolean();
                fileList.add(Pair.of(name, isDir));
            }

            return fileList;

        } catch (IOException e) {
            System.out.println("Can not make request (" + e.getMessage() + ")");
        }

        return fileList;
    }

    public void executeGet(String path) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            Request req = new Request(Request.Type.GET, path);
            output.writeObject(req);

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            long fileSize = input.readLong();

            byte[] buffer = new byte[4096];
            String fileName = Paths.get(path).getFileName().toString();

            File file = Paths.get(System.getProperty("user.dir"), fileName).toFile();
            if (!file.createNewFile()) {
                System.out.println("Can not create new file " + file.toString());
                return;
            }

            try (OutputStream fileOutput = new FileOutputStream(file)) {
                int readBytes = 0;
                while ((readBytes = input.read(buffer)) != -1) {
                    fileOutput.write(buffer, 0, readBytes);
                }
            }
        } catch (IOException e) {
            System.out.println("Can not make request (" + e.getMessage() + ")");
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: Client <ip> <port>");
        }

        String ip = args[0];
        int port = Integer.valueOf(args[1]);

        try {
            Client client = new Client(ip, port);

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String cmdLine = scanner.nextLine();
                String[] cmd = cmdLine.split("\\s+");

                if (cmd.length == 1 && cmd[0].equals("exit")) {
                    client.disconnect();
                    System.exit(0);

                } else if (cmd.length == 2 && cmd[0].equals("get")) {
                    client.executeGet(cmd[1]);

                } else if (cmd.length == 2 && cmd[0].equals("list")) {
                    List<Pair<String, Boolean>> pathList = client.executeList(cmd[1]);

                    for(Pair<String, Boolean> path : pathList) {
                        String filePath = path.getLeft();
                        if (path.getRight()) {
                            filePath += " (directory)";
                        }
                        System.out.println(filePath);
                    }

                } else {
                    System.out.println("Unknown command");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
