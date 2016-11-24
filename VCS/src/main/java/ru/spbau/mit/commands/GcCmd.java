package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;

import org.apache.commons.io.FileUtils;

import ru.spbau.mit.*;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Collect garbage in the repository")
public class GcCmd implements Command {
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

    /**
     * Collect garbage
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream or when accessing the file
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     */
    public void collect() throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();

        Set<Commit> commits = getAllCommits(config);
        Set<String> commitIds = commits.stream()
                .map(Commit::getId)
                .collect(Collectors.toSet());

        Set<Path> commitFiles = getAllFiles(Commit.getStoragePath());
        commitFiles = commitFiles.stream()
                .filter(p -> !commitIds.contains(p.getFileName().toString()))
                .collect(Collectors.toSet());

        commitFiles.forEach(p -> {
            if (!FileUtils.deleteQuietly(p.toFile())) {
                System.err.println("Can not delete " + p.toString());
            }
        });

        Set<String> blobIds = getAllBlobIds(commits, config);

        Set<Path> blobFiles = getAllFiles(Blob.getStoragePath());
        blobFiles = blobFiles.stream()
                .filter(p -> !blobIds.contains(p.getFileName().toString()))
                .collect(Collectors.toSet());

        blobFiles.forEach(p -> {
            if (!FileUtils.deleteQuietly(p.toFile())) {
                System.err.println("Can not delete " + p.toString());
            }
        });
    }

    public String getName() {
        return "gc";
    }

    public void execute() throws Exception {
        collect();
    }
}
