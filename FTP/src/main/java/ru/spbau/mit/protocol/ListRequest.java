package ru.spbau.mit.protocol;

public class ListRequest extends Message {
    private final String path;

    public ListRequest(String path) {
        this.path = path;
    }

    public MessageType getType() {
        return MessageType.LIST_REQUEST;
    }

    public String getPath() {
        return path;
    }
}
