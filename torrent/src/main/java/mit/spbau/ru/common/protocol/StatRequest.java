package mit.spbau.ru.common.protocol;

public class StatRequest extends Message {
    private final String id;

    public StatRequest(String id) {
        this.id = id;
    }

    public MessageType getType() {
        return MessageType.STAT_REQUEST;
    }

    public String getId() {
        return id;
    }
}
