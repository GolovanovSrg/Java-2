package ru.spbau.mit.kernel.server.tcp;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.kernel.ProtoMessage;
import ru.spbau.mit.kernel.server.Server;
import ru.spbau.mit.kernel.server.exceptions.AcceptException;
import ru.spbau.mit.kernel.server.exceptions.ServerException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpServerCachedThreadPool extends Server {
    private static final Logger logger = Logger.getLogger(TcpServerCachedThreadPool.class.getName());
    private volatile ExecutorService threadPool = Executors.newCachedThreadPool();
    private Thread acceptThread = null;
    private volatile ServerSocket socket = null;

    private void processClient(Socket clientSocket) {
        try {
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            while (!Thread.interrupted()) {
                int messageSize = input.readInt();
                byte[] messageBytes = new byte[messageSize];
                for (int i = 0; i < messageSize; ++i) {
                    messageBytes[i] = input.readByte();
                }

                long requestProcessingTimeStart = System.currentTimeMillis();

                ProtoMessage.Message message = ProtoMessage.Message.parseFrom(messageBytes);
                ArrayList<Integer> array = new ArrayList<>(message.getItemList());
                long arrayProcessingTime = sortArray(array);
                ProtoMessage.Message resultMessage = ProtoMessage.Message.newBuilder()
                                                                .setSize(messageSize)
                                                                .addAllItem(array)
                                                                .build();
                byte[] resultMessageBytes = resultMessage.toByteArray();

                long requestProcessingTime = System.currentTimeMillis() - requestProcessingTimeStart;
                output.writeInt(resultMessageBytes.length);
                output.write(resultMessageBytes);
                output.writeLong(arrayProcessingTime);
                output.writeLong(requestProcessingTime);
            }
        } catch (InvalidProtocolBufferException e ) {
            logger.log(Level.INFO, "Invalid protobuff message", e);
        }  catch (IOException ignored) {
            //logger.log(Level.INFO, "Client from " + clientSocket.getInetAddress() + " leave");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.INFO, "Can not close client socket");
            }
        }
    }

    private void accept() {
        try {
            while (!Thread.interrupted()) {
                Socket clientSocket = socket.accept();
                threadPool.execute(() -> processClient(clientSocket));
            }
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e2) {
                    logger.log(Level.WARNING, "Can not close accept socket", e2);
                }
                socket = null;
                logger.log(Level.WARNING, "Accept error. Server not accept connections", e);
                throw new AcceptException("Accept error. Server not accept connections", e);
            }
        }
    }

    @Override
    public void start() throws ServerException {
        logger.log(Level.INFO, "Start server");
        try {
            if (socket == null) {
                socket = new ServerSocket(PORT);
                acceptThread = new Thread(this::accept);
                acceptThread.setDaemon(true);
                acceptThread.start();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Can not create accepting thread", e);
            throw new ServerException("Can not create accepting thread", e);
        }
    }

    @Override
    public void stop() throws ServerException {
        try {
            if (socket != null) {
                acceptThread.interrupt();
                socket.close();
                socket = null;
                threadPool.shutdownNow();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Can not close server socket", e);
            throw new ServerException("Can not close server socket", e);
        }
    }
}

