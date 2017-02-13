package ru.spbau.mit.kernel.client.tcp;

import ru.spbau.mit.kernel.client.Client;
import ru.spbau.mit.kernel.client.Configuration;
import ru.spbau.mit.kernel.client.ProcessingTime;
import ru.spbau.mit.kernel.client.exceptions.ClientException;
import ru.spbau.mit.kernel.ProtoMessage;
import ru.spbau.mit.common.TimeStatistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpClientNotFixedConnection extends Client {
    private static final Logger logger = Logger.getLogger(TcpClientNotFixedConnection.class.getName());
    private Socket socket = null;

    public TcpClientNotFixedConnection(String serverIp, Configuration config) {
        super(serverIp, config);
    }

    private void connect() throws IOException {
        socket = new Socket(serverIp, SERVER_PORT);
    }

    private void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        }
    }

    private void send(ProtoMessage.Message message) throws IOException {
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        byte[] messageBytes = message.toByteArray();
        output.writeInt(messageBytes.length);
        output.write(messageBytes);
    }

    private ProcessingTime receive() throws IOException {
        DataInputStream input = new DataInputStream(socket.getInputStream());

        int messageSize = input.readInt();
        byte[] messageBytes = new byte[messageSize];
        for (int i = 0; i < messageSize; ++i) {
            messageBytes[i] = input.readByte();
        }
        ProtoMessage.Message ignored = ProtoMessage.Message.parseFrom(messageBytes);

        long arrayProcessingTime = input.readLong();
        long requestProcessingTime = input.readLong();

        return new ProcessingTime(arrayProcessingTime, requestProcessingTime);
    }

    @Override
    public TimeStatistics runBenchmark() throws ClientException {
        long startTime = System.currentTimeMillis();
        long arrayProcessingTimeSum = 0;
        long requestProcessingTimeSum = 0;

        try {
            for (int i = 0; i < configuration.getNRequests(); i++) {
                connect();

                ProtoMessage.Message msg = createMessage();
                send(msg);
                ProcessingTime time = receive();

                arrayProcessingTimeSum += time.getArrayProcessingTime();
                requestProcessingTimeSum += time.getRequestProcessingTime();

                disconnect();

                Thread.sleep(configuration.getTimeBetweenRequests());
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.WARNING, "Client error", e);
            throw new ClientException("Client error", e);
        } finally {
            try {
                disconnect();
            } catch (IOException e) {
                logger.log(Level.INFO, "Can not close client socket", e);
            }
        }

        long arrayProcessingTime = arrayProcessingTimeSum / configuration.getNRequests();
        long requestProcessingTime = requestProcessingTimeSum / configuration.getNRequests();
        long clientWorkTime = System.currentTimeMillis() - startTime;

        return new TimeStatistics(arrayProcessingTime, requestProcessingTime, clientWorkTime);
    }
}
