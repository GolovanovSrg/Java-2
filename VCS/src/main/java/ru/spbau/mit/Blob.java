package ru.spbau.mit;

import org.apache.commons.io.FileUtils;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.UUID;

/**
 * The class stores content of a file and path in the repository
 */
public class Blob implements Serializable {
    private static final String BLOBS_STORAGE_NAME = "blobs"; // important for garbage collector

    private final String id = UUID.randomUUID().toString();
    private final String repoPath;
    private final byte[] content;

    /**
     * @param path
     *        the path to the file
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     */
    public Blob(Path path) throws IOException {
        repoPath = Repository.getRepoPath(path).toString();
        content = Files.readAllBytes(path);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Blob) {
            Blob otherBlob = (Blob) other;
            return id.equals(otherBlob.getId()) &&
                   repoPath.equals(otherBlob.getRepoPath().toString()) &&
                   Arrays.equals(content, otherBlob.getContent());
        }

        return false;
    }

    public Path getRepoPath() {
        return Paths.get(repoPath);
    }

    public byte[] getContent() {
        return content;
    }

    public static Path getStoragePath() {
        return Repository.getStorageDirectory().resolve(BLOBS_STORAGE_NAME);
    }

    /**
     * Save the blob to the file
     *
     * @throws IOException
     *         if an I/O error occurs writing to the file
     */
    public void save() throws IOException {
        File blobsDirectory = getStoragePath().toFile();
        if (!blobsDirectory.exists()) {
            FileUtils.forceMkdir(blobsDirectory);
        }

        Path path = getStoragePath().resolve(id);
        try (FileOutputStream fileOutput = new FileOutputStream(path.toFile());
             ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
            objectOutput.writeObject(this);
        }
    }

    /**
     * Load the blob from the file
     *
     * @param id
     *        blobs id
     *
     * @return Blob from the file
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Blob is not found
     */
    public static Blob load(String id) throws IOException, ClassNotFoundException {
        Path path = getStoragePath().resolve(id);
        try (FileInputStream fileInput = new FileInputStream(path.toFile());
             ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
            return (Blob) objectInput.readObject();
        }
    }

    /**
     * Create the file and write content from blob
     *
     * @throws IOException
     *         if an I/O error occurs writing to or creating the file
     */
    public void writeToFile() throws IOException {
        Path path = Repository.getRoot().resolve(repoPath);
        Files.write(path, content);
    }
}
