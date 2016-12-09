package ru.spbau.mit.protocol;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public MessageType getType() {
        throw new UnsupportedOperationException();
    }
}
