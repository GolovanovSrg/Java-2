package mit.spbau.ru.common.protocol;

public class ListRequest extends Message {
    public MessageType getType() {
        return MessageType.LIST_REQUEST;
    }
}
