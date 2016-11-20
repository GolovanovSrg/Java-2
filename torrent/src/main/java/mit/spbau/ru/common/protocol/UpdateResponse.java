package mit.spbau.ru.common.protocol;

public class UpdateResponse extends Message {
    private final boolean status;

    public UpdateResponse(boolean status) {
        this.status = status;
    }

    public MessageType getType() {
        return MessageType.UPDATE_RESPONSE;
    }

    public boolean isStatus() {
        return status;
    }
}
