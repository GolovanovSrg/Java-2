package mit.spbau.ru.common;

import java.io.Serializable;
import java.util.UUID;

public class Torrent implements Serializable {
    private static final long MAX_PART_SIZE = 10 * 1000000;
    private final String id = UUID.randomUUID().toString();
    private final String name;
    private final long size;

    public Torrent(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Torrent) {
            return id.equals(((Torrent) other).getId()) &&
                    name.equals(((Torrent) other).getName()) &&
                    size == ((Torrent) other).getSize();
        }

        return false;
    }

    public long getNumberParts() {
        return size / MAX_PART_SIZE + 1;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
