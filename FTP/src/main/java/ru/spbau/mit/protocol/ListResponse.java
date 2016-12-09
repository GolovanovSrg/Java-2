package ru.spbau.mit.protocol;

import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public class ListResponse extends Message {
    private final boolean isExists;
    private final List<Pair<String, Boolean>> files;

    public ListResponse(boolean isExists, List<Pair<String, Boolean>> files) {
        this.isExists = isExists;
        this.files = files;
    }

    public MessageType getType() {
        return MessageType.LIST_RESPONSE;
    }

    public List<Pair<String, Boolean>> getFiles() {
        return files;
    }

    public boolean isExists() {
        return isExists;
    }
}
