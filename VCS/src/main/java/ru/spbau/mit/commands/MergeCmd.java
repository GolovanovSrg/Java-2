package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import ru.spbau.mit.*;
import ru.spbau.mit.exceptions.ConfigurationException;
import ru.spbau.mit.exceptions.MergeConflictException;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.nio.file.Path;

import java.util.*;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Join two development histories together")
public class MergeCmd implements Command {
    @Parameter (description = "<branch>", required = true, arity = 1)
    private List<String> branchFromCli = new ArrayList<>();

    private CommitRef getParentCommit(Branch first, Branch second) {
        Set<CommitRef> firstBranchCommits = new HashSet<>();

        CommitRef currentCommit = first.lastCommit();
        while (currentCommit != null) {
            firstBranchCommits.add(currentCommit);
            currentCommit = currentCommit.getFirstParent();
        }

        currentCommit = second.lastCommit();
        while (currentCommit != null) {
            if (firstBranchCommits.contains(currentCommit)) {
                return currentCommit;
            }
            currentCommit = currentCommit.getFirstParent();
        }

        return null;
    }

    private List<Blob> changedFiles(CommitRef parent, CommitRef child) throws IOException, ClassNotFoundException {
        Commit parentCommit = parent.getCommit();
        Set<String> parentBlobIds = new HashSet<>(parentCommit.getBlobIds());

        Commit childCommit = child.getCommit();
        List<String> childBlobIds = childCommit.getBlobIds();

        List<Blob> result = new ArrayList<>();
        for (String id : childBlobIds) {
            if (!parentBlobIds.contains(id)) {
                result.add(Blob.load(id));
            }
        }

        return result;
    }

    private List<Path> getCommonPaths(List<Blob> first, List<Blob> second) {
        List<Path> firstPaths = first.stream()
                .map(b -> Repository.getRoot().resolve(b.getRepoPath()))
                .collect(Collectors.toList());

        List<Path> secondPaths = second.stream()
                .map(b -> Repository.getRoot().resolve(b.getRepoPath()))
                .collect(Collectors.toList());

        secondPaths.retainAll(firstPaths);
        return secondPaths;
    }

    /**
     * Merge the branch with the current branch
     *
     * @param branchName
     *        name of the branch
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream or when accessing the file
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     *
     * @throws ConfigurationException
     *         if the branch not exists in the configuration or is the current branch
     *
     * @throws MergeConflictException
     *         if there are conflicting files
     */
    public void merge(String branchName) throws RepositoryException, IOException, ClassNotFoundException,
                                                ConfigurationException, MergeConflictException {
        Configuration config = Configuration.load();

        if (!config.branchExists(branchName)) {
            throw new ConfigurationException("Branch " + branchName + " not exists");
        }

        if (branchName.equals(config.headName())) {
            throw new ConfigurationException("Can not merge current branch with current branch");
        }

        Branch branch = config.getBranch(branchName);
        CommitRef parent = getParentCommit(config.head(), branch);


        List<Blob> list1 = changedFiles(parent, config.head().lastCommit());
        List<Blob> list2 = changedFiles(parent, branch.lastCommit());

        List<Path> commonPaths = getCommonPaths(list1, list2);
        if (commonPaths.size() != 0) {
            throw new MergeConflictException("Conflict in merge " + config.headName() + " with " + branchName, commonPaths);
        }

        list2.forEach(b -> config.addToIndex(Repository.getRoot().resolve(b.getRepoPath()), b.getId()));

        list1.addAll(list2);
        List<String> commonBlobIds = list1.stream()
                .map(Blob::getId)
                .collect(Collectors.toList());


        config.makeCommit("Merge with " + branchName, commonBlobIds, branch.lastCommit());
        config.save();

        list2.forEach(b -> {

            Path path = Repository.getRoot().resolve(b.getRepoPath());

            if (!path.toFile().exists()) {
                try {
                    b.writeToFile();
                } catch (IOException e) {
                    System.err.println("Can not copy " + path.toString() + " (" + e.getMessage() + ")");
                }
            }
        });
    }

    public String getName() {
        return "merge";
    }

    public void execute() throws Exception {
        String branchName = branchFromCli.get(0);
        branchFromCli.clear();

        try {
            merge(branchName);
        } catch (MergeConflictException e) {
            System.out.println("Conflicts for: ");
            e.getConflictedPaths().forEach(System.out::println);
        }
    }
}
