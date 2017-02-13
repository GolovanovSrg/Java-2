package ru.spbau.mit.kernel.server.tcp;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.kernel.ProtoMessage;
import ru.spbau.mit.kernel.server.Server;
import ru.spbau.mit.kernel.server.exceptions.AcceptException;
import ru.spbau.mit.kernel.server.exceptions.ServerException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpServerFixedThreadPool extends Server {
    private static final Logger logger = Logger.getLogger(TcpServerFixedThreadPool.class.getName());
    private static final int N_THREADS_ON_THREAD_POOL = 4;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(N_THREADS_ON_THREAD_POOL);
    private volatile Selector selector = null;
    private Thread acceptThread = null;

    private class ClientHandler {
        private final Selector selector;
        private final SocketChannel channel;
        private final ExecutorService threadPool;

        private ByteBuffer messageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
        private ByteBuffer messageBuffer = null;
        private CopyOnWriteArrayList<ByteBuffer> writeList = new CopyOnWriteArrayList<>();
        private boolean isMessageBody = false;

        ClientHandler(Selector selector, SocketChannel channel, ExecutorService threadPool) {
            this.selector = selector;
            this.channel = channel;
            this.threadPool = threadPool;
        }

        private void closeChannel() {
            for (SelectionKey key : selector.keys()) {
                if (key.channel() == channel) {
                    try {
                        channel.close();
                        key.cancel();
                    } catch (IOException e) {
                        logger.log(Level.INFO, "Can not close client channel", e);
                    }
                }
            }
        }

        public void read() throws IOException {
            if (!isMessageBody) {
                if (channel.read(messageSizeBuffer) == -1) {
                    //logger.log(Level.INFO, "Client from " + channel.socket().getInetAddress() + " leave");
                    closeChannel();
                } else {
                    if (messageSizeBuffer.hasRemaining()) {
                        return;
                    }

                    messageSizeBuffer.flip();
                    messageBuffer = ByteBuffer.allocate(messageSizeBuffer.getInt());
                    messageSizeBuffer.clear();
                    isMessageBody = true;
                }
            }

            if (isMessageBody) {
                if (channel.read(messageBuffer) == -1) {
                    //logger.log(Level.INFO, "Client from " + channel.socket().getInetAddress() + " leave");
                    closeChannel();
                } else {
                    if (messageBuffer.hasRemaining()) {
                        return;
                    }

                    long requestProcessingTimeStart = System.currentTimeMillis();

                    threadPool.execute(() -> {
                        try {
                            messageBuffer.flip();
                            ProtoMessage.Message message = ProtoMessage.Message.parseFrom(messageBuffer.array());
                            ArrayList<Integer> array = new ArrayList<>(message.getItemList());
                            long arrayProcessingTime = sortArray(array);

                            ProtoMessage.Message resultMessage = ProtoMessage.Message.newBuilder()
                                                                                    .setSize(array.size())
                                                                                    .addAllItem(array)
                                                                                    .build();
                            byte[] resultMessageBytes = resultMessage.toByteArray();
                            ByteBuffer writeBuffer = ByteBuffer.allocate(2 * Long.BYTES + Integer.BYTES + resultMessageBytes.length);
                            long requestProcessingTime = System.currentTimeMillis() - requestProcessingTimeStart;

                            writeBuffer.putInt(resultMessageBytes.length);
                            writeBuffer.put(resultMessageBytes);
                            writeBuffer.putLong(arrayProcessingTime);
                            writeBuffer.putLong(requestProcessingTime);
                            writeBuffer.flip();
                            writeList.add(writeBuffer);
                        } catch (InvalidProtocolBufferException e ) {
                            logger.log(Level.INFO, "Invalid protobuff message", e);
                            closeChannel();
                        }
                    });

                    isMessageBody = false;
                }
            }
        }

        public void write() throws IOException {
            if (writeList.size() == 0) {
                return;
            }

            for (int i = 0; i < writeList.size(); i++) {
                if (writeList.get(i).hasRemaining()) {
                    channel.write(writeList.get(i));
                } else {
                    writeList.remove(i);
                }
            }
        }
    }

    void processAccept(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = channel.accept();
        clientChannel.configureBlocking(false);

        ClientHandler handler = new ClientHandler(selector, clientChannel, threadPool);
        clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, handler);
    }

    void processWrite(SelectionKey key) throws IOException {
        ClientHandler handler = (ClientHandler) key.attachment();
        handler.write();
    }

    void processRead(SelectionKey key) throws IOException {
        ClientHandler handler = (ClientHandler) key.attachment();
        handler.read();
    }

    private void accept()
    {
        try {
            while (!Thread.interrupted()) {
                if (selector.select() == 0) {
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    try {
                        if (key.isAcceptable()) {
                            processAccept(key);
                        } else if (key.isReadable()) {
                            processRead(key);
                        } else if (key.isWritable()) {
                            processWrite(key);
                        }
                    } catch (IOException e) {
                        logger.log(Level.INFO, "Request failed", e);
                    }
                }
            }
        } catch (ClosedChannelException ignored) {
        } catch(IOException e) {
            logger.log(Level.WARNING, "Accept error. Server not accept connections", e);
            throw new AcceptException("Accept error. Server not accept connections", e);
        }
    }

    @Override
    public void start() throws ServerException {
        logger.log(Level.INFO, "Start server");
        try {
            if (selector == null) {
                selector = Selector.open();
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                serverChannel.configureBlocking(false);

                serverChannel.socket().bind(new InetSocketAddress(PORT));
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);

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
        logger.log(Level.INFO, "Stop server");
        try {
            if (selector != null) {
                acceptThread.interrupt();
                for (SelectionKey key : selector.keys()) {
                    key.channel().close();
                    key.cancel();
                }
                selector.close();
                selector = null;
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Can not close server channel", e);
            throw new ServerException("Can not close server channel", e);
        }

        threadPool.shutdown();
    }
}
