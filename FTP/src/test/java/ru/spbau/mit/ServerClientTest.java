package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ServerClientTest {
    private final String ip = "127.0.0.1";
    private final int port = 60000;
    private final ServerImpl server = new ServerImpl(ip, port);
    private final ClientImpl client1 = new ClientImpl(ip, port);
    private final ClientImpl client2 = new ClientImpl(ip, port);


    @org.junit.Before
    public void serverClientStart() throws IOException {
        server.start();
        assertTrue(server.isStarted());

        client1.connect();
        assertTrue(client1.isConnected());

        client2.connect();
        assertTrue(client2.isConnected());
    }

    @org.junit.After
    public void serverClientStop() throws IOException {
        client1.disconnect();
        assertFalse(client1.isConnected());

        client2.disconnect();
        assertFalse(client2.isConnected());

        server.stop();
        assertFalse(server.isStarted());
    }

    @org.junit.Test
    public void serverClientListTest() throws IOException {
        File serverDir = Paths.get(System.getProperty("user.dir"), "serverDir").toFile();
        serverDir.mkdir();

        File tempFile = File.createTempFile("tempFile", ".txt", serverDir);
        List<Pair<String, Boolean>> list = client1.executeList(serverDir.toString());
        assertTrue(list.contains(Pair.of(serverDir.toString(), true)));
        assertTrue(list.contains(Pair.of(tempFile.toString(), false)));
        assertEquals(2, list.size());

        FileUtils.deleteQuietly(serverDir);
    }

    @org.junit.Test
    public void serverClientGetTest() throws IOException {
        File clientDir = Paths.get(System.getProperty("user.dir"), "clientDir").toFile();
        clientDir.mkdir();
        File serverDir = Paths.get(System.getProperty("user.dir"), "serverDir").toFile();
        serverDir.mkdir();
        System.setProperty( "user.dir", clientDir.toString());

        File tempFile = File.createTempFile("testFile", ".txt", serverDir);
        PrintStream fileStream = new PrintStream(tempFile);
        fileStream.println("4 8 15 16 23 42");
        fileStream.close();


        Path clientTempFilePath = Paths.get(clientDir.toString(), tempFile.getName());
        FileUtils.deleteQuietly(clientTempFilePath.toFile());

        client2.executeGet(tempFile.toString());

        List<Path> filePaths = Files.walk(clientDir.toPath())
                                    .collect(Collectors.toList());

        assertTrue(filePaths.contains(clientTempFilePath));

        FileUtils.deleteQuietly(clientDir);
        FileUtils.deleteQuietly(serverDir);
    }
}