package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Collect garbage in the repository")
public class GcCmd implements Command {
    @Override
    public String getName() {
        return "gc";
    }


    private Set<Commit> getAllCommits(Configuration config) throws IOException, ClassNotFoundException {
        Set<Commit> result = new HashSet<>();
        Set<String> branchNames = config.getBranchesNames();

        for (String name : branchNames) {
            Branch branch = config.getBranch(name);

            CommitRef currentCommit = branch.lastCommit();
            while (currentCommit != null) {
                if (result.contains(currentCommit)) {
                    break;
                }

                result.add(currentCommit.getCommit());
                currentCommit = currentCommit.getFirstParent();
            }
        }

        return result;
    }

    private Set<String> getAllBlobIds(Set<Commit> commits, Configuration config) {
        Set<String> blobIds = new HashSet<>(config.getIndexBlobs());
        for (Commit commit : commits) {
            blobIds.addAll(commit.getBlobIds());
        }

        return blobIds;
    }

    private Set<Path> getAllFiles(Path dir) throws IOException {
        return Files.walk(dir)
                .filter(p -> !Files.isDirectory(p))
                .map(p -> p.toAbsolutePath().normalize())
                .collect(Collectors.toSet());
    }

    public void collect(Configuration config) {
        try {
            Set<Commit> commits = getAllCommits(config);
            Set<String> commitIds = commits.stream()
                    .map(Commit::getId)
                    .collect(Collectors.toSet());

            Set<Path> commitFiles = getAllFiles(Commit.COMMITS_PATH);
            commitFiles = commitFiles.stream()
                    .filter(p -> !commitIds.contains(p.getFileName().toString()))
                    .collect(Collectors.toSet());

            commitFiles.forEach(p -> {
                if (!FileUtils.deleteQuietly(p.toFile())) {
                    System.out.println("Can not delete " + p.toString());
                }
            });


            Set<String> blobIds = getAllBlobIds(commits, config);

            Set<Path> blobFiles = getAllFiles(Blob.BLOBS_PATH);
            blobFiles = blobFiles.stream()
                    .filter(p -> !blobIds.contains(p.getFileName().toString()))
                    .collect(Collectors.toSet());

            blobFiles.forEach(p -> {
                if (!FileUtils.deleteQuietly(p.toFile())) {
                    System.out.println("Can not delete " + p.toString());
                }
            });

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can not collect garbage (" + e.getMessage() + ")");
        }
    }

    @Override
    public void execute() {
        if (!Utils.isRepository()) {
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

        collect(config);
    }
}
