package ru.spbau.mit.client;

import org.apache.commons.lang3.tuple.Pair;
import ru.spbau.mit.exceptions.CommandException;
import ru.spbau.mit.exceptions.CommunicationException;

import java.util.List;
import java.util.Scanner;

public class Client {
    private static final String EXIT_COMMAND_NAME = "exit";
    private static final int EXIT_COMMAND_LEN = 1;
    private static final String CONNECT_COMMAND_NAME = "connect";
    private static final int CONNECT_COMMAND_LEN = 1;
    private static final String DISCONNECT_COMMAND_NAME = "disconnect";
    private static final int DISCONNECT_COMMAND_LEN = 1;
    private static final String LIST_COMMAND_NAME = "list";
    private static final int LIST_COMMAND_LEN = 2;
    private static final String GET_COMMAND_NAME = "get";
    private static final int GET_COMMAND_LEN = 2;
    private static final String UNKNOWN_COMMAND_NAME = "";

    private static void printUsage() {
        System.out.println("USAGE:");
        System.out.println(EXIT_COMMAND_NAME + " - exit from the client");
        System.out.println(CONNECT_COMMAND_NAME + " - connect to the server");
        System.out.println(DISCONNECT_COMMAND_NAME + " - disconnect from the server");
        System.out.println(LIST_COMMAND_NAME + " <path> - get list of files on the server in directory");
        System.out.println(GET_COMMAND_NAME + " <path> - get the file on the server");
    }

    private static Pair<String, String> parse(String[] cmd) {

        if (cmd.length == EXIT_COMMAND_LEN && cmd[0].equals(EXIT_COMMAND_NAME)) {
            return Pair.of(EXIT_COMMAND_NAME, null);
        } else if (cmd.length == CONNECT_COMMAND_LEN && cmd[0].equals(CONNECT_COMMAND_NAME)) {
            return Pair.of(CONNECT_COMMAND_NAME, null);
        } else if (cmd.length == DISCONNECT_COMMAND_LEN && cmd[0].equals(DISCONNECT_COMMAND_NAME)) {
            return Pair.of(DISCONNECT_COMMAND_NAME, null);
        } else if (cmd.length == LIST_COMMAND_LEN && cmd[0].equals(LIST_COMMAND_NAME)) {
            return Pair.of(LIST_COMMAND_NAME, cmd[1]);
        } else if (cmd.length == GET_COMMAND_LEN  && cmd[0].equals(GET_COMMAND_NAME)) {
            return Pair.of(GET_COMMAND_NAME, cmd[1]);
        }
        return Pair.of(UNKNOWN_COMMAND_NAME, null);
    }

    private static void exitCmd(ClientImpl client) {
        try {
            if (client.isConnected()) {
                    client.disconnect();
            }
        } catch (CommunicationException e) {
            ;
        }
    }

    private static void connectCmd(ClientImpl client) throws CommunicationException {
        if (client.isConnected()) {
            System.out.println("Client is already connected");
            return;
        }
        client.connect();
    }

    private static void disconnectCmd(ClientImpl client) throws CommunicationException {
        if (!client.isConnected()) {
            System.out.println("Client is not connected");
            return;
        }
        client.disconnect();
    }

    private static void getCmd(ClientImpl client, String path) throws CommandException {
        long size = client.executeGet(path);
        System.out.println("File is received (size " +  size + ")");
    }

    private static void listCmd(ClientImpl client, String path) throws CommandException {
        List<Pair<String, Boolean>> pathsList = client.executeList(path);
        for (Pair<String, Boolean> p : pathsList) {
            String filePath = p.getLeft();
            if (p.getRight()) {
                filePath += " (directory)";
            }
            System.out.println(filePath);
        }
    }

    private static void unknownCmd() {
        System.out.println("Unknown command");
        printUsage();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: Client <server ip> <port>");
            return;
        }

        ClientImpl client;
        try {
            String ip = args[0];
            int port = Integer.valueOf(args[1]);

            client = new ClientImpl(ip, port);
            client.connect();

        } catch (CommunicationException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print(">> ");
        while (scanner.hasNextLine()) {
            String cmdLine = scanner.nextLine();
            Pair<String, String> cmd = parse(cmdLine.split("\\s+"));

            try {
                switch (cmd.getLeft()) {
                    case EXIT_COMMAND_NAME:
                        exitCmd(client);
                        return;
                    case CONNECT_COMMAND_NAME:
                        connectCmd(client);
                        break;
                    case DISCONNECT_COMMAND_NAME:
                        disconnectCmd(client);
                        break;
                    case LIST_COMMAND_NAME:
                        listCmd(client, cmd.getRight());
                        break;
                    case GET_COMMAND_NAME:
                        getCmd(client, cmd.getRight());
                        break;
                    case UNKNOWN_COMMAND_NAME:
                        unknownCmd();
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.print(">> ");
        }
    }

}
