package mit.spbau.ru.common.protocol;

public class UploadResponse extends Message {
    private final String id;

    public UploadResponse(String id) {
        this.id = id;
    }

    public MessageType getType() {
        return MessageType.UPLOAD_RESPONSE;
    }

    public String getId() {
        return id;
    }
}
