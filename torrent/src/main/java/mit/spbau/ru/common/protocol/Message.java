package mit.spbau.ru.common.protocol;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public MessageType getType() {
        throw new UnsupportedOperationException("Need implement this method");
    }
}
