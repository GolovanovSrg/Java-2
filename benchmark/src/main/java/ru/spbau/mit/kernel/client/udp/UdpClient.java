package ru.spbau.mit.kernel.client.udp;

import ru.spbau.mit.kernel.client.Client;
import ru.spbau.mit.kernel.client.Configuration;
import ru.spbau.mit.kernel.client.ProcessingTime;
import ru.spbau.mit.kernel.client.exceptions.ClientException;
import ru.spbau.mit.kernel.client.tcp.TcpClientNotFixedConnection;
import ru.spbau.mit.kernel.ProtoMessage;
import ru.spbau.mit.common.TimeStatistics;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpClient extends Client {
    private static final int TIMEOUT_MS = 5000;
    private static final Logger logger = Logger.getLogger(TcpClientNotFixedConnection.class.getName());
    private DatagramSocket socket = null;

    public UdpClient(String serverIp, Configuration config) {
        super(serverIp, config);
    }

    private void connect() throws IOException {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_MS);
    }

    private void disconnect() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void send(ProtoMessage.Message message) throws IOException {
        byte[] messageBytes = message.toByteArray();
        ByteBuffer packetBuffer = ByteBuffer.allocate(Integer.BYTES + messageBytes.length);
        packetBuffer.putInt(messageBytes.length);
        packetBuffer.put(messageBytes);

        DatagramPacket packet = new DatagramPacket(packetBuffer.array(), packetBuffer.capacity(),
                InetAddress.getByName(serverIp), SERVER_PORT);
        socket.send(packet);
    }

    private ProcessingTime receive() throws IOException {
        int bufferSize = socket.getReceiveBufferSize();
        DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(packet);

        ByteBuffer packetBuffer = ByteBuffer.wrap(packet.getData());
        int messageSize = packetBuffer.getInt();
        byte[] messageBytes = new byte[messageSize];
        packetBuffer.get(messageBytes);
        ProtoMessage.Message ignored = ProtoMessage.Message.parseFrom(messageBytes);

        long arrayProcessingTime = packetBuffer.getLong();
        long requestProcessingTime = packetBuffer.getLong();

        return new ProcessingTime(arrayProcessingTime, requestProcessingTime);
    }

    @Override
    public TimeStatistics runBenchmark() throws ClientException {
        long arrayProcessingTimeSum = 0;
        long requestProcessingTimeSum = 0;
        long startTime = System.currentTimeMillis();

        try {
            connect();

            for (int i = 1; i <= configuration.getNRequests(); i++) {
                ProtoMessage.Message msg = createMessage();
                send(msg);
                ProcessingTime time = receive();

                arrayProcessingTimeSum += time.getArrayProcessingTime();
                requestProcessingTimeSum += time.getRequestProcessingTime();

                Thread.sleep(configuration.getTimeBetweenRequests());
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.WARNING, "Client error", e);
            throw new ClientException("Client error", e);
        } finally {
            disconnect();
        }

        long arrayProcessingTime = arrayProcessingTimeSum / configuration.getNRequests();
        long requestProcessingTime = requestProcessingTimeSum / configuration.getNRequests();
        long clientWorkTime = System.currentTimeMillis() - startTime;

        return new TimeStatistics(arrayProcessingTime, requestProcessingTime, clientWorkTime);
    }
}
