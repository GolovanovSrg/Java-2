package ru.spbau.mit;

import java.io.Serializable;

public class Request implements Serializable {
    public enum Type {
        LIST,
        GET
    }

    private final Type type;
    private final String path;

    public Request(Type type, String path) {
        this.type = type;
        this.path = path;
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}
