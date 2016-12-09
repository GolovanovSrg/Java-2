package ru.spbau.mit.client;

import org.apache.commons.lang3.tuple.Pair;
import ru.spbau.mit.exceptions.CommandException;
import ru.spbau.mit.exceptions.CommunicationException;
import ru.spbau.mit.protocol.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.List;

public class ClientImpl {
    private final String ip;
    private final int port;
    private Socket socket = null;

    public ClientImpl(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void connect() throws CommunicationException {
        if (socket != null) {
            System.err.println("Connection is already enabled");
            return;
        }

        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            CommunicationException exception = new CommunicationException("Can not create the new socket when connecting to the server");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public void disconnect() throws CommunicationException {
        if (socket == null) {
            System.err.println("Connection is already disabled");
            return;
        }

        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            DisconnectRequest req = new DisconnectRequest();
            output.writeObject(req);
            output.flush();

            socket.shutdownOutput();
            socket.close();
            socket = null;
        } catch (IOException e) {
            socket = null;
            CommunicationException exception = new CommunicationException("Can not close the socket when disconnecting from the server");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public boolean isConnected() {
        return socket != null;
    }

    public List<Pair<String, Boolean>> executeList(String path) throws CommandException {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ListRequest req = new ListRequest(path);
            output.writeObject(req);
            output.flush();
        } catch (IOException e) {
            CommandException exception = new CommandException("Can not send the request to the server");
            exception.addSuppressed(e);
            throw exception;
        }

        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Message resp = (Message) input.readObject();
            if (resp.getType() != MessageType.LIST_RESPONSE) {
                throw new CommandException("Invalid response from the server");
            }

            if (!((ListResponse) resp).isExists()) {
                throw new CommandException("Directory is not found on the server");
            }

            return ((ListResponse) resp).getFiles();
        } catch (IOException | ClassNotFoundException e) {
            CommandException exception = new CommandException("Can not get the response from the server");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public long executeGet(String path) throws CommandException {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            GetRequest req = new GetRequest(path);
            output.writeObject(req);
            output.flush();
        } catch (IOException e) {
            CommandException exception = new CommandException("Can not send the request to the server");
            exception.addSuppressed(e);
            throw exception;
        }

        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Message resp = (Message) input.readObject();
            if (resp.getType() != MessageType.GET_RESPONSE) {
                throw new CommandException("Invalid response from the server");
            }

            if (!((GetResponse) resp).isExists()) {
                throw new CommandException("File is not found on the server");
            }

            long size = ((GetResponse) resp).getSize();

            String fileName = Paths.get(path).getFileName().toString();
            File file = Paths.get(System.getProperty("user.dir"), fileName).toFile();
            file.createNewFile();

            long allBytes = 0;
            if (size > 0) {
                byte[] buffer = new byte[4096];
                try (OutputStream fileOutput = new FileOutputStream(file)) {
                    int readBytes = 0;
                    while (allBytes < size && (readBytes = input.read(buffer)) != -1) {
                        allBytes += readBytes;
                        fileOutput.write(buffer, 0, readBytes);
                    }
                }
            }

            return size;
        } catch (IOException | ClassNotFoundException e) {
            CommandException exception = new CommandException("Can not get the file from the server");
            exception.addSuppressed(e);
            throw exception;
        }
    }
}
