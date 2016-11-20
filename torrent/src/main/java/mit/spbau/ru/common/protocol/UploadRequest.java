package mit.spbau.ru.common.protocol;

public class UploadRequest extends Message {
    private final String name;
    private final long size;

    public UploadRequest(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public MessageType getType() {
        return MessageType.UPLOAD_REQUEST;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
