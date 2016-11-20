package mit.spbau.ru.common.protocol;

public class SourcesRequest extends Message {
    private final String id;

    public SourcesRequest(String id) {
        this.id = id;
    }

    public MessageType getType() {
        return MessageType.SOURCES_REQUEST;
    }

    public String getId() {
        return id;
    }
}
