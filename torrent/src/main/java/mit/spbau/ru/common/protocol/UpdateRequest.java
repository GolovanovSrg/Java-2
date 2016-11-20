package mit.spbau.ru.common.protocol;

import java.util.List;

public class UpdateRequest extends Message {
    private final int port;
    private final List<String> fileIds;

    public UpdateRequest(int port, List<String> fileIds) {
        this.port = port;
        this.fileIds = fileIds;
    }

    public MessageType getType() {
        return MessageType.UPDATE_REQUEST;
    }

    public int getPort() {
        return port;
    }

    public List<String> getFileIds() {
        return fileIds;
    }
}
