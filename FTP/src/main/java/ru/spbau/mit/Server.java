package ru.spbau.mit;

import ru.spbau.mit.exceptions.CommunicationException;

import java.util.Scanner;

public class Server {
    private static final String EXIT_COMMAND_NAME = "exit";
    private static final int EXIT_COMMAND_LEN = 1;
    private static final String START_COMMAND_NAME = "start";
    private static final int START_COMMAND_LEN = 1;
    private static final String STOP_COMMAND_NAME = "stop";
    private static final int STOP_COMMAND_LEN = 1;
    private static final String UNKNOWN_COMMAND_NAME = "";

    private static void printUsage() {
        System.out.println("USAGE:");
        System.out.println(EXIT_COMMAND_NAME + " - exit from the server");
        System.out.println(START_COMMAND_NAME + " - start the server");
        System.out.println(STOP_COMMAND_NAME + "stop - stop the server");
    }

    private static String parse(String[] cmd) {
        if (cmd.length == EXIT_COMMAND_LEN && cmd[0].equals(EXIT_COMMAND_NAME)) {
            return EXIT_COMMAND_NAME;
        } else if (cmd.length == START_COMMAND_LEN && cmd[0].equals(START_COMMAND_NAME)) {
            return START_COMMAND_NAME;
        } else if (cmd.length == STOP_COMMAND_LEN && cmd[0].equals(STOP_COMMAND_NAME)) {
            return STOP_COMMAND_NAME;
        }
        return UNKNOWN_COMMAND_NAME;
    }

    private static void exitCmd(ServerImpl server) {
        try {
            if (server.isStarted()) {
                server.stop();
            }
        } catch (CommunicationException e) {
            ;
        }
    }

    private static void startCmd(ServerImpl server) throws CommunicationException {
        if (server.isStarted()) {
            System.out.println("Server is already started");
            return;
        }
        server.start();
    }

    private static void stopCmd(ServerImpl server) throws CommunicationException {
        if (!server.isStarted()) {
            System.out.println("Server is not started");
            return;
        }
        server.stop();
    }

    private static void unknownCmd() {
        System.out.println("Unknown command");
        printUsage();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: Server <ip> <port>");
            return;
        }

        ServerImpl server;
        try {
            String ip = args[0];
            int port = Integer.valueOf(args[1]);

            server = new ServerImpl(ip, port);
            server.start();
        } catch (CommunicationException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String cmdLine = scanner.nextLine();
                String cmd = parse(cmdLine.split("\\s+"));

                try {
                    switch (cmd) {
                        case EXIT_COMMAND_NAME:
                            exitCmd(server);
                            return;
                        case START_COMMAND_NAME:
                            startCmd(server);
                            break;
                        case STOP_COMMAND_NAME:
                            stopCmd(server);
                            break;
                        case UNKNOWN_COMMAND_NAME:
                            unknownCmd();
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
    }
}
