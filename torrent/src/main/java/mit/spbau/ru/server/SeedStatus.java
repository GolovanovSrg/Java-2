package mit.spbau.ru.server;


import java.util.ArrayList;
import java.util.List;

public class SeedStatus {
    private boolean isAlive = true;
    private List<String> fileIds = new ArrayList<>();

    public SeedStatus(boolean isAlive, List<String> fileIds) {
        this.isAlive = isAlive;
        this.fileIds = fileIds;
    }

    public void setIsAlive(boolean value) {
        isAlive = value;
    }

    public boolean isAlive() {
        return isAlive;
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
