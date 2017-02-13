package ru.spbau.mit.kernel.server.tcp;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.kernel.ProtoMessage;
import ru.spbau.mit.kernel.server.Server;
import ru.spbau.mit.kernel.server.exceptions.ServerException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpServerAsync extends Server {
    private static final Logger logger = Logger.getLogger(TcpServerAsync.class.getName());
    private volatile AsynchronousServerSocketChannel channel = null;

    private class ClientHandler implements CompletionHandler<Integer, ByteBuffer> {
        boolean isMessageBody;
        AsynchronousSocketChannel channel;

        ClientHandler(AsynchronousSocketChannel channel, boolean isMessageBody) {
            this.channel = channel;
            this.isMessageBody = isMessageBody;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if (!isMessageBody) {
                if (buffer.hasRemaining()) {
                    channel.read(buffer, buffer, this);
                    return;
                }

                buffer.flip();
                int messageSize = buffer.getInt();
                ByteBuffer messageBuffer = ByteBuffer.allocate(messageSize);
                channel.read(messageBuffer, messageBuffer, new ClientHandler(channel, true));
            } else {
                if (buffer.hasRemaining()) {
                    channel.read(buffer, buffer, this);
                    return;
                }
                try {
                    long requestProcessingTimeStart = System.currentTimeMillis();

                    buffer.flip();
                    byte[] messageBytes = buffer.array();
                    ProtoMessage.Message message = ProtoMessage.Message.parseFrom(messageBytes);
                    ArrayList<Integer> array = new ArrayList<>(message.getItemList());
                    long arrayProcessingTime = sortArray(array);
                    ProtoMessage.Message resultMessage = ProtoMessage.Message.newBuilder()
                                                                            .setSize(array.size())
                                                                            .addAllItem(array)
                                                                            .build();
                    byte[] resultMessageBytes = resultMessage.toByteArray();
                    ByteBuffer resultMessageBuffer = ByteBuffer.allocate(2 * Long.BYTES + Integer.BYTES + resultMessageBytes.length);

                    long requestProcessingTime = System.currentTimeMillis() - requestProcessingTimeStart;

                    resultMessageBuffer.putInt(resultMessageBytes.length);
                    resultMessageBuffer.put(resultMessageBytes);
                    resultMessageBuffer.putLong(arrayProcessingTime);
                    resultMessageBuffer.putLong(requestProcessingTime);
                    resultMessageBuffer.flip();

                    channel.write(resultMessageBuffer, resultMessageBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer messageBuffer) {
                            if (messageBuffer.hasRemaining()) {
                                channel.write(messageBuffer, messageBuffer, this);
                                return;
                            }

                            ByteBuffer messageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
                            channel.read(messageSizeBuffer, messageSizeBuffer, new ClientHandler(channel, false));
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            logger.log(Level.INFO, "Client error", exc);
                        }
                    });

                } catch (InvalidProtocolBufferException e) {
                    logger.log(Level.INFO, "Invalid protobuff message", e);
                }
            }
        }

        @Override
        public void failed(Throwable e, ByteBuffer ignored) {
            logger.log(Level.INFO, "Client error", e);
        }
    }

    @Override
    public void start() throws ServerException {
        logger.log(Level.INFO, "Start server");
        try {
            if (channel == null) {
                channel = AsynchronousServerSocketChannel.open();
                channel.bind(new InetSocketAddress(PORT));

                channel.accept(channel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                    @Override
                    public void completed(AsynchronousSocketChannel clientChannel, AsynchronousServerSocketChannel serverChannel) {
                        serverChannel.accept(serverChannel, this);

                        ByteBuffer messageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
                        clientChannel.read(messageSizeBuffer, messageSizeBuffer, new ClientHandler(clientChannel, false));
                    }

                    @Override
                    public void failed(Throwable e, AsynchronousServerSocketChannel ignored) {
                        if (channel != null) {
                            logger.log(Level.INFO, "Can not accept client", e);
                        }
                    }
                });
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Can not create accepting thread", e);
            throw new ServerException("Can not create accepting thread", e);
        }
    }

    @Override
    public void stop() throws ServerException {
        logger.log(Level.INFO, "Stop server");
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Can not close server channel", e);
            throw new ServerException("Can not close server channel", e);
        }
    }
}
