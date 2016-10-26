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
    private static final String COMMITS_STORAGE_NAME = "commits"; // important for garbage collector

    private final String id = UUID.randomUUID().toString();
    private final String message;
    private final List<String> blobIds;

    /**
     * @param message
     *        message for commit
     */
    public Commit(String message) {
        this.message = message;
        this.blobIds = new ArrayList<>();
    }

    /**
     * @param message
     *        message for commit
     *
     * @param blobIds
     *        blobs ids which belong commit
     */
    public Commit(String message, List<String> blobIds) {
        this.message = message;
        this.blobIds = blobIds;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

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

    public static Path getStoragePath() {
        return Repository.getStorageDirectory().resolve(COMMITS_STORAGE_NAME);
    }

    /**
     * Save the commit to the file
     *
     * @throws IOException
     *         if an I/O error occurs writing to the file
     */
    public void save() throws IOException {
        File commitsDirectory = getStoragePath().toFile();
        if (!commitsDirectory.exists()) {
            FileUtils.forceMkdir(commitsDirectory);
        }

        Path path = getStoragePath().resolve(id);
        try (FileOutputStream fileOutput = new FileOutputStream(path.toFile());
             ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
            objectOutput.writeObject(this);
        }
    }

    /**
     * Load the commit from the file
     *
     * @param id
     *        commits id
     *
     * @return Commit from the file
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if Commit class is not found
     */
    public static Commit load(String id) throws IOException, ClassNotFoundException {
        Path path = getStoragePath().resolve(id);
        try (FileInputStream fileInput = new FileInputStream(path.toFile());
             ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
            return (Commit) objectInput.readObject();
        }
    }
}
