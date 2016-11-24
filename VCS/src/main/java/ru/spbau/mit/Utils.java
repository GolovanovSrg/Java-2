package ru.spbau.mit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static final Path CURRENT_DIR = Paths.get(System.getProperty("user.dir"));
    public static final Path REPO_DIR = CURRENT_DIR;
    public static final Path VCS_DIR = REPO_DIR.resolve(".vcs");

    public static boolean isRepository() {
        File vcsDirectory = VCS_DIR.toFile();
        return vcsDirectory.exists() && vcsDirectory.isDirectory();
    }

    public static Path repoPath(Path path) {
        Path normPath = path.toAbsolutePath().normalize();
        return REPO_DIR.relativize(normPath);
    }

    public static List<Path> getOnlyFiles(List<Path> paths) {
        List<Path> result = new ArrayList<>();

        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                try {
                    List<Path> dirFiles = Files.walk(path)
                                               .filter(p -> !Files.isDirectory(p))
                                               .map(p -> p.toAbsolutePath().normalize())
                                               .filter(p -> !p.startsWith(VCS_DIR))
                                               .collect(Collectors.toList());

                    result.addAll(dirFiles);
                } catch (IOException e) {
                    System.out.println("Can not process directory " + path.toString() + " (" + e.getMessage() + ")");
                }
            } else {
                result.add(path.toAbsolutePath().normalize());
            }
        }

        return result;
    }

    public static List<Path> getRepoFiles() throws IOException {
        return Files.walk(REPO_DIR)
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toAbsolutePath().normalize())
                    .filter(p -> !p.startsWith(VCS_DIR))
                    .collect(Collectors.toList());
    }
}
