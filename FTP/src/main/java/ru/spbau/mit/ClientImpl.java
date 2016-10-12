package ru.spbau.mit;

import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ClientImpl {
    private final String ip;
    private final int port;
    private Socket socket = null;

    public ClientImpl(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void connect() throws IOException {
        if (socket != null) {
            System.err.println("Connection is already enabled");
            return;
        }

        socket = new Socket(ip, port);
    }

    public void disconnect() throws IOException {
        if (socket == null) {
            System.err.println("Connection is already disabled");
            return;
        }

        socket.close();
        socket = null;
    }

    public boolean isConnected() {
        return socket != null;
    }

    public List<Pair<String, Boolean>> executeList(String path) throws IOException, ClassNotFoundException {
        List<Pair<String, Boolean>> fileList = new LinkedList<>();

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        Request req = new Request(Request.Type.LIST, path);
        output.writeObject(req);
        output.flush();

        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

        int nFiles = input.readInt();
        for (int i = 0; i < nFiles; ++i) {
            String name = (String) input.readObject();
            Boolean isDir = input.readBoolean();
            fileList.add(Pair.of(name, isDir));
        }

        return fileList;
    }

    public Pair<Long, Long> executeGet(String path) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        Request req = new Request(Request.Type.GET, path);
        output.writeObject(req);
        output.flush();

        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

        long fileSize = input.readLong();
        long allBytes = 0;

        if (fileSize > 0) {
            String fileName = Paths.get(path).getFileName().toString();
            File file = Paths.get(System.getProperty("user.dir"), fileName).toFile();
            file.createNewFile();

            byte[] buffer = new byte[4096];
            try (OutputStream fileOutput = new FileOutputStream(file)) {
                int readBytes = 0;
                while (allBytes < fileSize && (readBytes = input.read(buffer)) != -1) {
                    allBytes += readBytes;
                    fileOutput.write(buffer, 0, readBytes);
                }
            }
        }

        return Pair.of(allBytes, fileSize);
    }
}
