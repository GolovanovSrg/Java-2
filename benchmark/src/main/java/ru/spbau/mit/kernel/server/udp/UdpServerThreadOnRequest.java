package ru.spbau.mit.kernel.server.udp;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.kernel.ProtoMessage;
import ru.spbau.mit.kernel.server.Server;
import ru.spbau.mit.kernel.server.exceptions.AcceptException;
import ru.spbau.mit.kernel.server.exceptions.ServerException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpServerThreadOnRequest extends Server {
    private static final Logger logger = Logger.getLogger(UdpServerThreadOnRequest.class.getName());
    private volatile DatagramSocket socket = null;
    private Thread acceptThread = null;

    private void processClient(DatagramPacket packet) {
        try {
            long requestProcessingTimeStart = System.currentTimeMillis();

            ByteBuffer packetBuffer = ByteBuffer.wrap(packet.getData());
            int messageSize = packetBuffer.getInt();
            byte[] messageBytes = new byte[messageSize];
            packetBuffer.get(messageBytes);
            ProtoMessage.Message message = ProtoMessage.Message.parseFrom(messageBytes);

            ArrayList<Integer> array = new ArrayList<>(message.getItemList());
            long arrayProcessingTime = sortArray(array);
            ProtoMessage.Message resultMessage = ProtoMessage.Message.newBuilder()
                                                            .setSize(messageSize)
                                                            .addAllItem(array)
                                                            .build();
            byte[] resultMessageBytes = resultMessage.toByteArray();
            ByteBuffer resultPacketBuffer = ByteBuffer.allocate(2 * Long.BYTES + Integer.BYTES + resultMessageBytes.length);

            long requestProcessingTime = System.currentTimeMillis() - requestProcessingTimeStart;

            resultPacketBuffer.putInt(resultMessageBytes.length);
            resultPacketBuffer.put(resultMessageBytes);
            resultPacketBuffer.putLong(arrayProcessingTime);
            resultPacketBuffer.putLong(requestProcessingTime);

            DatagramPacket resultPacket = new DatagramPacket(resultPacketBuffer.array(), resultPacketBuffer.capacity(),
                                                            packet.getAddress(), packet.getPort());

            socket.send(resultPacket);
        } catch (InvalidProtocolBufferException e ) {
            logger.log(Level.INFO, "Invalid protobuff message", e);
        }  catch (IOException e) {
            logger.log(Level.INFO, "Request from " + packet.getAddress() + " failed", e);
        }
    }

    private void accept() {
        try {
            while (!Thread.interrupted()) {
                int bufferSize = socket.getReceiveBufferSize();
                DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
                socket.receive(packet);
                Thread handler = new Thread(() -> { processClient(packet); });
                handler.setDaemon(true);
                handler.start();
            }
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
                logger.log(Level.WARNING, "Accept error. Server not accept connections", e);
                throw new AcceptException("Accept error. Server not accept connections", e);
            }
        }
    }


    @Override
    public void start() throws ServerException {
        logger.log(Level.INFO, "Start server");
        try{
            if (socket == null) {
                socket = new DatagramSocket(PORT);
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
    public void stop() {
        logger.log(Level.INFO, "Stop server");
        if (socket != null) {
            acceptThread.interrupt();
            socket.close();
            socket = null;
        }
    }
}
