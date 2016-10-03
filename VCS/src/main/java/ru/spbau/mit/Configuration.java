package ru.spbau.mit;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The class stores and controls configuration information (HEAD, branches, index)
 */
public class Configuration implements Serializable {
    public static final Path CONFIG_PATH = Utils.VCS_DIR.resolve("vcs.cfg");

    private String headName;
    private Branch head;
    private final Map<String, Branch> branches = new HashMap<>();
    private final Map<String, String> index = new HashMap<>();

    public Configuration() throws IOException {
        headName = "master";
        head = new Branch();
        branches.put(headName, head);
    }

    public void save() throws IOException {
        try (FileOutputStream fileOutput = new FileOutputStream(CONFIG_PATH.toFile());
             ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
            objectOutput.writeObject(this);
        }
    }

    public static Configuration load() throws IOException, ClassNotFoundException {
        try (FileInputStream fileInput = new FileInputStream(CONFIG_PATH.toFile());
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

    public void makeMergeCommit(String message, List<String> blobIds, CommitRef otherParent) throws IOException {
        head.makeMergeCommit(message, blobIds, otherParent);
    }

    public void addToIndex(Path path, String blobId) {
        index.put(Utils.repoPath(path).toString(), blobId);
    }

    public void delFromIndex(Path path) {
        index.remove(Utils.repoPath(path).toString());
    }

    public String getBlobId(Path path) {
        return index.get(Utils.repoPath(path).toString());
    }

    public Set<Path> getIndexPaths() {
        return index.keySet().stream()
                .map(s -> Paths.get(s))
                .collect(Collectors.toSet());
    }

    public void clearIndex() {
        index.clear();
    }

    public Collection<String> getIndexBlobs() {
        return index.values();
    }

    public boolean isIndexed(Path path) {
        return index.containsKey(Utils.repoPath(path));
    }

    public List<CommitRef> getHeadHistory() {
        return head.getHistory();
    }
}
