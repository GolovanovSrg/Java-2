package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;
import ru.spbau.mit.Blob;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Show state working directory")
public class StatusCmd implements Command {
    @Override
    public String getName() {
        return "status";
    }

    private Set<Path> getChangedPaths(Set<Path> repoPaths, Set<Path> indexPaths, Configuration config) {
        Set<Path> result = new HashSet<>(repoPaths);

        return result.stream()
                    .filter(p -> {
                                    if (!indexPaths.contains(p)) {
                                        return false;
                                    }

                                    try {
                                        byte[] content = Files.readAllBytes(p);
                                        String blobId = config.getBlobId(Repository.getRepoPath(p));
                                        byte[] contentBlob = Blob.load(blobId).getContent();
                                        return !Arrays.deepEquals(new Object[]{content}, new Object[]{contentBlob});
                                    } catch (IOException | ClassNotFoundException e) {
                                        System.out.println(e.getMessage());
                                        return true;
                                    }
                                })
                    .collect(Collectors.toSet());
    }

    private Set<Path> getUntreckedPaths(Set<Path> repoPaths, Set<Path> indexPaths) {
        Set<Path> result = new HashSet<>(repoPaths);
        return result.stream()
                .filter(p -> !indexPaths.contains(p))
                .collect(Collectors.toSet());
    }

    private Set<Path> getDeletedPaths(Set<Path> repoPaths, Set<Path> indexPaths) {
        Set<Path> result = new HashSet<>(indexPaths);
        return result.stream()
                .filter(p -> !repoPaths.contains(p))
                .collect(Collectors.toSet());
    }

    @Override
    public void execute() {
        if (!Repository.exists()) {
            System.out.println("Repository is not found");
            return;
        }

        Configuration config;
        try {
            config = Configuration.load();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can not load configuration (" + e.getMessage() + " )");
            return;
        }

        Set<Path> repoPaths;
        try {
            repoPaths = new HashSet<>(Repository.getAllRepoFiles());
        } catch (IOException e) {
            System.out.println("Can not read directory of repository (" + e.getMessage() + ")");
            return;
        }

        Set<Path> indexPaths = config.getIndexPaths().stream()
                                    .map(Repository.REPO_DIR::resolve)
                                    .collect(Collectors.toSet());

        Set<Path> changedPaths = getChangedPaths(repoPaths, indexPaths, config);
        if (changedPaths.size() > 0) {
            System.out.println("\nChanged files:");
            changedPaths.forEach(System.out::println);
        }

        Set<Path> untrackedPaths = getUntreckedPaths(repoPaths, indexPaths);
        if (untrackedPaths.size() > 0) {
            System.out.println("\nUntracked files:");
            untrackedPaths.forEach(System.out::println);
        }

        Set<Path> deletedPaths = getDeletedPaths(repoPaths, indexPaths);
        if (deletedPaths.size() > 0) {
            System.out.println("\nDeleted files:");
            deletedPaths.forEach(System.out::println);
        }
    }
}
