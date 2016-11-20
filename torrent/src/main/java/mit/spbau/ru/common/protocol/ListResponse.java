package mit.spbau.ru.common.protocol;

import mit.spbau.ru.common.Torrent;

import java.util.List;

public class ListResponse extends Message {
    private final List<Torrent> files;

    public ListResponse(List<Torrent> files) {
        this.files = files;
    }

    public MessageType getType() {
        return MessageType.LIST_RESPONSE;
    }

    public List<Torrent> getFiles() {
        return files;
    }
}
