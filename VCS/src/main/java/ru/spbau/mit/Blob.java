package ru.spbau.mit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * The class stores content of a file and path in repository
 */
public class Blob implements Serializable {
    public static final Path BLOBS_PATH = Utils.VCS_DIR.resolve("blobs"); // important for garbage collector

    private final String id = UUID.randomUUID().toString();
    private final String repoPath;
    private final byte[] content;

    public Blob(Path path) throws IOException {
        repoPath = Utils.repoPath(path).toString();
        content = Files.readAllBytes(path);
    }

    public String getId() {
        return id;
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
            blobsDirectory.mkdir();
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
        Path path = Utils.REPO_DIR.resolve(repoPath);
        Files.write(path, content);
    }

}
