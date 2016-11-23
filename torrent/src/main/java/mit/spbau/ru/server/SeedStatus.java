package mit.spbau.ru.server;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeedStatus {
    private static final long MIN_TIME_SILENCE_MINUTES = 5;
    private LocalDateTime lastActivity = LocalDateTime.now();
    private List<String> fileIds = new ArrayList<>();

    public SeedStatus(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public void update() {
        lastActivity = LocalDateTime.now();
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return lastActivity.plusMinutes(MIN_TIME_SILENCE_MINUTES).compareTo(now) >= 0;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void addFileId(String id) {
        if (!fileIds.contains(id)) {
            fileIds.add(id);
        }
    }

    public void removeFileId(String id) {
        fileIds.remove(id);
    }

}
