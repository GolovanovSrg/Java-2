package ru.spbau.mit.protocol;

public class GetResponse extends Message {
    private final boolean isExists;
    private final long size;

    public MessageType getType() {
        return MessageType.GET_RESPONSE;
    }

    public GetResponse(boolean isExists, long size) {
        this.isExists = isExists;
        this.size = size;
    }

    public boolean isExists() {
        return isExists;
    }

    public long getSize() {
        return size;
    }
}
