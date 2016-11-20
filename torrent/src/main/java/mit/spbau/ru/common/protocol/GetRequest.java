package mit.spbau.ru.common.protocol;

public class GetRequest extends Message {
    private final String id;
    private final int part;

    public GetRequest(String id, int part) {
        this.id = id;
        this.part = part;
    }

    public MessageType getType() {
        return MessageType.GET_REQUEST;
    }

    public String getId() {
        return id;
    }

    public int getPart() {
        return part;
    }
}
