package ru.spbau.mit;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The class stores commit content (message and blobs ids of commited files)
 */
public class Commit implements Serializable {
    public static final Path COMMITS_PATH = Repository.STORAGE_DIR.resolve("commits"); // important for garbage collector

    private final String id = UUID.randomUUID().toString();
    private final String message;
    private final List<String> blobIds;

    public Commit(String message) {
        this.message = message;
        this.blobIds = new ArrayList<>();
    }

    public Commit(String message, List<String> blobIds) {
        this.message = message;
        this.blobIds = blobIds;
    }

    public boolean equals(Object other) {
        if (other instanceof Commit) {
            Commit otherCommit = (Commit) other;
            return id.equals(otherCommit.getId()) &&
                    message.equals(otherCommit.getMessage()) &&
                    blobIds.equals(otherCommit.getBlobIds());
        }

        return false;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getBlobIds() {
        return blobIds;
    }

    public void save() throws IOException {
        File commitsDirectory = COMMITS_PATH.toFile();
        if (!commitsDirectory.exists()) {
            FileUtils.forceMkdir(commitsDirectory);
        }

        Path path = COMMITS_PATH.resolve(id);
        try (FileOutputStream fileOutput = new FileOutputStream(path.toFile());
             ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
            objectOutput.writeObject(this);
        }
    }

    public static Commit load(String id) throws IOException, ClassNotFoundException {
        Path path = COMMITS_PATH.resolve(id);
        try (FileInputStream fileInput = new FileInputStream(path.toFile());
             ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
            return (Commit) objectInput.readObject();
        }
    }
}
