package mit.spbau.ru.server;

import mit.spbau.ru.common.Seed;

import java.util.*;

public class Swarm {
    private final Map<Seed, SeedStatus> seeds = new HashMap<>();
    private final Map<String, FileStatus> files = new HashMap<>();

    public void updateSeed(Seed seed, List<String> fileIds) {
        if (!seeds.containsKey(seed)) {
            seeds.put(seed, new SeedStatus(fileIds));
        } else {
            seeds.get(seed).update(fileIds);
        }

        for (String id : fileIds) {
            if (!files.containsKey(id)) {
                files.put(id, new FileStatus());
            }
            files.get(id).addSeed(seed);
        }
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

    public List<Seed> getSeeds(String fileId) {
        FileStatus fStatus = files.get(fileId);
        List<Seed> fSeeds = (fStatus == null) ? new ArrayList<>() : fStatus.getSeeds();
        List<Seed> activeSeeds = new ArrayList<>();

        for (Seed s : fSeeds) {
            SeedStatus sStatus = seeds.get(s);
            if (sStatus.isActive()) {
                activeSeeds.add(s);
            } else {
                removeSeed(s);
            }
        }

        return activeSeeds;
    }
}
