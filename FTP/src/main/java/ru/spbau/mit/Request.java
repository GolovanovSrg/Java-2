package ru.spbau.mit;

import java.io.Serializable;

/**
 * The class describes a request from a client to a server
 */
public class Request implements Serializable {
    public enum Type {
        LIST, // get list files and directories
        GET // get contents file
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
