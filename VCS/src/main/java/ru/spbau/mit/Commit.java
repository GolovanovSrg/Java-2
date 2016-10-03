package ru.spbau.mit;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The class stores commit content (message and blobs ids of commited files)
 */
public class Commit implements Serializable {
    public static final Path COMMITS_PATH = Utils.VCS_DIR.resolve("commits"); // important for garbage collector

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
            commitsDirectory.mkdir();
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
