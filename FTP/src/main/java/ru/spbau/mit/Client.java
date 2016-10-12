package ru.spbau.mit;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static void processCmd(String cmdLine, ClientImpl client) throws IOException, ClassNotFoundException {
        String[] cmd = cmdLine.split("\\s+");

        if (cmd.length == 1 && cmd[0].equals("exit")) {
            if (client.isConnected()) {
                client.disconnect();
            }
            System.exit(0);

        } else if (cmd.length == 2 && cmd[0].equals("get")) {
            Pair<Long, Long> sizes = client.executeGet(cmd[1]);
            if (sizes.getRight() == 0) {
                System.out.println("File is not found or empty");
            } else {
                System.out.println("File is received (" + sizes.getLeft() + " from " + sizes.getRight() + ")");
            }

        } else if (cmd.length == 2 && cmd[0].equals("list")) {
            List<Pair<String, Boolean>> pathList = client.executeList(cmd[1]);
            for (Pair<String, Boolean> path : pathList) {
                String filePath = path.getLeft();
                if (path.getRight()) {
                    filePath += " (directory)";
                }
                System.out.println(filePath);
            }

        } else if (cmd.length == 1 && cmd[0].equals("connect")) {
            if (client.isConnected()) {
                System.out.println("Client is already connected");
                return;
            }
            client.connect();

        } else if (cmd.length == 1 && cmd[0].equals("disconnect")) {
            if (!client.isConnected()) {
                System.out.println("Client is not connected");
                return;
            }
            client.disconnect();

        } else {
            System.out.println("Unknown command");
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: Client <ip> <port>");
            return;
        }

        String ip = args[0];
        int port = Integer.valueOf(args[1]);

        try {
            ClientImpl client = new ClientImpl(ip, port);
            client.connect();

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String cmdLine = scanner.nextLine();
                processCmd(cmdLine, client);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
