package ru.spbau.mit.protocol;

public class DisconnectRequest extends Message{
    public MessageType getType() {
        return MessageType.DISCONNECT_REQUEST;
    }
}
