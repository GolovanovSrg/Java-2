package ru.spbau.mit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class designed to work with files in repository
 */
public class Repository {
    public static final Path REPO_DIR = Paths.get(System.getProperty("user.dir"));
    public static final Path STORAGE_DIR = REPO_DIR.resolve(".vcs");

    /**
     * Verify existence of the repository
     * @return true if storage directory exists else false
     */
    public static boolean exists() {
        File storageDirectory = STORAGE_DIR.toFile();
        return storageDirectory.exists() && storageDirectory.isDirectory();
    }

    /**
     * Get path relative to the repository
     * @param path - path of a file
     * @return path relative to the repository
     */
    public static Path getRepoPath(Path path) {
        Path normPath = path.toAbsolutePath().normalize();
        return REPO_DIR.relativize(normPath);
    }

    /**
     * Get all files in the repository
     * @return list of paths files
     * @throws IOException
     */
    public static List<Path> getAllRepoFiles() throws IOException {
        return Files.walk(REPO_DIR)
                .filter(p -> !Files.isDirectory(p))
                .map(p -> p.toAbsolutePath().normalize())
                .filter(p -> !p.startsWith(STORAGE_DIR))
                .collect(Collectors.toList());
    }


    /**
     * Get only files in the repository
     * @param paths - list of paths files
     * @return list of paths files in the repository
     */
    public static List<Path> filterRepoFiles(List<Path> paths) {
        List<Path> result = new ArrayList<>();

        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                try {
                    List<Path> dirFiles = Files.walk(path)
                                               .filter(p -> !Files.isDirectory(p))
                                               .map(p -> p.toAbsolutePath().normalize())
                                               .filter(p -> !p.startsWith(STORAGE_DIR) && p.startsWith(REPO_DIR))
                                               .collect(Collectors.toList());

                    result.addAll(dirFiles);
                } catch (IOException e) {
                    System.err.println("Can not process directory " + path.toString() + " (" + e.getMessage() + ")");
                }
            } else {
                path = path.toAbsolutePath().normalize();
                if (path.startsWith(REPO_DIR)) {
                    result.add(path);
                }
            }
        }

        return result;
    }
}
