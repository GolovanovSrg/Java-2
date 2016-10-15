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
    public static final Path BLOBS_PATH = Repository.STORAGE_DIR.resolve("blobs"); // important for garbage collector

    private final String id = UUID.randomUUID().toString();
    private final String repoPath;
    private final byte[] content;

    public Blob(Path path) throws IOException {
        repoPath = Repository.getRepoPath(path).toString();
        content = Files.readAllBytes(path);
    }

    public String getId() {
        return id;
    }

    public boolean equals(Object other) {
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

    public void save() throws IOException {
        File blobsDirectory = BLOBS_PATH.toFile();
        if (!blobsDirectory.exists()) {
            FileUtils.forceMkdir(blobsDirectory);
        }

        Path path = BLOBS_PATH.resolve(id);
        try (FileOutputStream fileOutput = new FileOutputStream(path.toFile());
             ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
            objectOutput.writeObject(this);
        }
    }

    public static Blob load(String id) throws IOException, ClassNotFoundException {
        Path path = BLOBS_PATH.resolve(id);
        try (FileInputStream fileInput = new FileInputStream(path.toFile());
             ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
            return (Blob) objectInput.readObject();
        }
    }

    public void toFile() throws IOException {
        Path path = Repository.REPO_DIR.resolve(repoPath);
        Files.write(path, content);
    }
}
