package ru.spbau.mit;

import java.io.IOException;
import java.util.Scanner;

public class Server {

    private static void processCmd(String cmdLine, ServerImpl server) throws IOException {
        if (cmdLine.equals("exit")) {
            if (server.isStarted()) {
                server.stop();
            }
            System.exit(0);

        } else if (cmdLine.equals("stop")) {
            if (!server.isStarted()) {
                System.out.println("Server is not started");
                return;
            }
            server.stop();

        } else if(cmdLine.equals("start")) {
            if (server.isStarted()) {
                System.out.println("Server is already started");
                return;
            }
            server.start();

        } else {
            System.out.println("Unknown command");
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: Server <ip> <port>");
            return;
        }

        String ip = args[0];
        int port = Integer.valueOf(args[1]);

        try {
            ServerImpl server = new ServerImpl(ip, port);
            server.start();

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String cmdLine = scanner.nextLine();
                processCmd(cmdLine, server);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
