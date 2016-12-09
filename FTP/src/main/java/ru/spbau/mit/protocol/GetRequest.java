package ru.spbau.mit.protocol;

public class GetRequest extends Message {
    private final String path;

    public GetRequest(String path) {
        this.path = path;
    }

    public MessageType getType() {
        return MessageType.GET_REQUEST;
    }

    public String getPath() {
        return path;
    }
}
