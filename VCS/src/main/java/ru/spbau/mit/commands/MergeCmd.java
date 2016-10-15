package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.spbau.mit.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Join two development histories together")
public class MergeCmd implements Command {
    @Parameter (description = "<branch>", required = true)
    private List<String> nameParam;

    public String getName() {
        return "merge";
    }

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

        Commit childCommit= child.getCommit();
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
                .map(b -> Repository.REPO_DIR.resolve(b.getRepoPath()))
                .collect(Collectors.toList());

        List<Path> secondPaths = second.stream()
                .map(b -> Repository.REPO_DIR.resolve(b.getRepoPath()))
                .collect(Collectors.toList());

        secondPaths.retainAll(firstPaths);
        return secondPaths;
    }

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

        String name = nameParam.get(0);

        if (name.equals(config.headName())) {
            System.out.println("Can not merge current branch with current branch");
            return;
        }

        if (!config.branchExists(name)) {
            System.out.println("Branch not exists");
            return;
        }

        Branch branch = config.getBranch(name);
        CommitRef parent = getParentCommit(config.head(), branch);

        try {
            List<Blob> list1 = changedFiles(parent, config.head().lastCommit());
            List<Blob> list2 = changedFiles(parent, branch.lastCommit());

            List<Path> commonPaths = getCommonPaths(list1, list2);
            if (commonPaths.size() != 0) {
                System.out.println("Conflicts for: ");
                commonPaths.forEach(System.out::println);
                return;
            }

            list1.addAll(list2);
            List<String> commonBlobIds = list1.stream()
                    .map(Blob::getId)
                    .collect(Collectors.toList());

            config.makeCommit("Merge with " + name, commonBlobIds, branch.lastCommit());
            config.save();

            list2.forEach(b -> {
                Path path = Repository.REPO_DIR.resolve(b.getRepoPath());
                if (!path.toFile().exists()) {
                    try {
                        b.toFile();
                    } catch (IOException e) {
                        System.out.println("Can not copy " + path.toString() + " (" + e.getMessage() + ")");
                    }
                }
            });

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can not merge (" + e.getMessage() + ")");
        }
    }
}
