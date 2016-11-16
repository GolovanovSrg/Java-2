package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;
import ru.spbau.mit.Blob;
import ru.spbau.mit.Commit;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Show state working directory")
public class StatusCmd implements Command {
    private Set<Path> getChangedPaths(Set<Path> repoPaths, Set<Path> indexPaths, Configuration config) {
        Set<Path> result = new HashSet<>(repoPaths);

        return result.stream()
                    .filter(p -> {
                                    if (!indexPaths.contains(p)) {
                                        return false;
                                    }

                                    try {
                                        byte[] content = Files.readAllBytes(p);
                                        String blobId = config.getBlobId(p);
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

    private Set<Path> getNewIndexedPaths(Configuration config) throws IOException, ClassNotFoundException {
        Commit lastCommit = config.head().lastCommit().getCommit();
        List<String> commitBlobIds = lastCommit.getBlobIds();
        List<String> indexedBlobIds = config.getIndexBlobs();
        indexedBlobIds.removeAll(commitBlobIds);

        Set<Path> newIndexedPaths = new HashSet<>();
        for (String id : indexedBlobIds) {
            Path path = Repository.getRoot().resolve(Blob.load(id).getRepoPath());
            newIndexedPaths.add(path);
        }

        return newIndexedPaths;
    }

    /**
     * Get the changed, untracked and deleted files
     *
     * @return list of sets of indexed, changed, untracked and deleted paths
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     */
    public List<Set<Path>> getStatus() throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();

        Set<Path> repoPaths = new HashSet<>(Repository.getAllRepoFiles());
        Set<Path> indexPaths = config.getIndexPaths().stream()
                                                     .map(Repository.getRoot()::resolve)
                                                     .collect(Collectors.toSet());

        Set<Path> indexedPaths = getNewIndexedPaths(config);
        Set<Path> changedPaths = getChangedPaths(repoPaths, indexPaths, config);
        Set<Path> untrackedPaths = getUntreckedPaths(repoPaths, indexPaths);
        Set<Path> deletedPaths = getDeletedPaths(repoPaths, indexPaths);

        return Arrays.asList(indexedPaths, changedPaths, untrackedPaths, deletedPaths);
    }

    public String getName() {
        return "status";
    }

    public void execute() throws Exception {

        List<Set<Path>> status = getStatus();

        Set<Path> indexedPaths = status.get(0);
        if (indexedPaths.size() > 0) {
            System.out.println("\nIndexed files:");
            indexedPaths.forEach(System.out::println);
        }

        Set<Path> changedPaths = status.get(1);
        if (changedPaths.size() > 0) {
            System.out.println("\nChanged files:");
            changedPaths.forEach(System.out::println);
        }

        Set<Path> untrackedPaths = status.get(2);
        if (untrackedPaths.size() > 0) {
            System.out.println("\nUntracked files:");
            untrackedPaths.forEach(System.out::println);
        }

        Set<Path> deletedPaths = status.get(3);
        if (deletedPaths.size() > 0) {
            System.out.println("\nDeleted files:");
            deletedPaths.forEach(System.out::println);
        }
    }
}
