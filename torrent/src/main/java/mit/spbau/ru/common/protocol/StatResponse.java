package mit.spbau.ru.common.protocol;

import java.util.List;

public class StatResponse extends Message {
    private final List<Integer> partNumbers;

    public StatResponse(List<Integer> partNumbers) {
        this.partNumbers = partNumbers;
    }

    public MessageType getType() {
        return MessageType.STAT_RESPONSE;
    }

    public List<Integer> getPartNumbers() {
        return partNumbers;
    }
}
