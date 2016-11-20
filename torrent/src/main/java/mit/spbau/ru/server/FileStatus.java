package mit.spbau.ru.server;

import mit.spbau.ru.common.Seed;

import java.util.ArrayList;
import java.util.List;

public class FileStatus {
    private final List<Seed> seeds = new ArrayList<>();

    public List<Seed> getSeeds() {
        return seeds;
    }

    public int getNumberSeeds() {
        return seeds.size();
    }

    public void addSeed(Seed seed) {
        if (!seeds.contains(seed)) {
            seeds.add(seed);
        }
    }

    public void removeSeed(Seed seed) {
        seeds.remove(seed);
    }
}
