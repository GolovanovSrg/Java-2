package mit.spbau.ru.server;

import mit.spbau.ru.common.Seed;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Swarm {
    private final Map<Seed, SeedStatus> seeds = new HashMap<>();
    private final Map<String, FileStatus> files = new HashMap<>();

    public void addSeed(String fileId, Seed seed) {
        if (!files.containsKey(fileId)) {
            files.put(fileId, new FileStatus());
        }

        files.get(fileId).addSeed(seed);

        if (!seeds.containsKey(seed)) {
            seeds.put(seed, new SeedStatus(true, Arrays.asList(fileId)));
        }

        seeds.get(seed).addFileId(fileId);
    }

    public void removeSeed(Seed seed) {
        if (!seeds.containsKey(seed)) {
            return;
        }

        for (String fileId : seeds.get(seed).getFileIds()) {
            files.get(fileId).removeSeed(seed);
            if (files.get(fileId).getNumberSeeds() == 0) {
                files.remove(fileId);
            }
        }

        seeds.remove(seed);
    }
}
