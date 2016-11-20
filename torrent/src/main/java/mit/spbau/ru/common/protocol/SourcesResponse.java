package mit.spbau.ru.common.protocol;

import mit.spbau.ru.common.Seed;

import java.util.List;

public class SourcesResponse extends Message {
    private final List<Seed> seeds;

    public SourcesResponse(List<Seed> seeds) {
        this.seeds = seeds;
    }

    public MessageType getType() {
        return MessageType.SOURCES_RESPONSE;
    }

    public List<Seed> getSeeds() {
        return seeds;
    }
}
