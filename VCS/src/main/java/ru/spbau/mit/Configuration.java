package ru.spbau.mit;

import ru.spbau.mit.exceptions.RepositoryException;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The class stores and controls configuration information (HEAD, branches, index)
 */
public class Configuration implements Serializable {
    private static final String CONFIG_NAME = "configuration.cfg";

    private String headName;
    private Branch head;
    private final Map<String, Branch> branches = new HashMap<>();
    private final Map<String, String> index = new HashMap<>();

    /**
     * @throws IOException
     *         if an I/O error occurs writing to or creating the init commits file of init branch
     */
    public Configuration() throws IOException {
        headName = "master";
        head = new Branch();
        branches.put(headName, head);
    }

    public static Path getConfigurationPath() {
        return Repository.getStorageDirectory().resolve(CONFIG_NAME);
    }

    /**
     * Save the configuration to the file
     *
     * @throws IOException
     *         if an I/O error occurs writing to the file
     */
    public void save() throws IOException {
        try (FileOutputStream fileOutput = new FileOutputStream(getConfigurationPath().toFile());
             ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
            objectOutput.writeObject(this);
        }
    }

    /**
     * Load the configuration from the file
     *
     * @return Configuration from file
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     *
     * @throws RepositoryException
     *         if storage directory of the repository is not exists
     */
    public static Configuration load() throws IOException, ClassNotFoundException, RepositoryException {
        if (!Repository.exists()) {
            throw new RepositoryException("Repository is not found");
        }

        try (FileInputStream fileInput = new FileInputStream(getConfigurationPath().toFile());
             ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
            Configuration config = (Configuration) objectInput.readObject();
            return config;
        }
    }

    public Branch head() {
        return head;
    }

    public String headName() {
        return headName;
    }

    public Branch getBranch(String name) {
        return branches.get(name);
    }

    public void addBranch(String name, Branch branch) {
        branches.put(name, branch);
    }

    public void delBranch(String name) {
        branches.remove(name);
    }

    public void switchBranch(String name) {
        headName = name;
        head = branches.get(name);
    }

    public void makeBranch(String name) throws IOException, ClassNotFoundException {
        Branch newBranch = head.makeBranch(name);
        addBranch(name, newBranch);
    }

    public boolean branchExists(String name) {
        return branches.containsKey(name);
    }

    public Set<String> getBranchesNames() {
        return branches.keySet();
    }

    public void makeCommit(String message, List<String> blobIds) throws IOException {
        head.makeCommit(message, blobIds);
    }

    public void makeCommit(String message, List<String> blobIds, CommitRef otherParent) throws IOException {
        head.makeCommit(message, blobIds, otherParent);
    }

    public void addToIndex(Path path, String blobId) {
        index.put(Repository.getRepoPath(path).toString(), blobId);
    }

    public void delFromIndex(Path path) {
        index.remove(Repository.getRepoPath(path).toString());
    }

    public Set<Path> getIndexPaths() {
        return index.keySet().stream()
                .map(s -> Repository.getRoot().resolve(s))
                .collect(Collectors.toSet());
    }

    public String getBlobId(Path path) {
        return index.get(Repository.getRepoPath(path).toString());
    }

    public void clearIndex() {
        index.clear();
    }

    public List<String> getIndexBlobs() {
        return index.values().stream().collect(Collectors.toList());
    }

    public boolean isIndexed(Path path) {
        return index.containsKey(Repository.getRepoPath(path).toString());
    }

    public Set<CommitRef> getHeadHistory() {
        return head.getHistory();
    }
}
