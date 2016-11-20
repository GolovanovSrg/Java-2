package mit.spbau.ru.common.protocol;

public class GetResponse extends Message {
    private final byte[] content;

    public GetResponse(byte[] content) {
        this.content = content;
    }

    public MessageType getType() {
        return MessageType.GET_RESPONSE;
    }

    public byte[] getContent() {
        return content;
    }
}
