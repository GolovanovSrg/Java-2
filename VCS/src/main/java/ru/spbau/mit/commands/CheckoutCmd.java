package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Parameters(commandDescription = "Checkout a branch or paths to the working tree")
public class CheckoutCmd implements Command {
    @Parameter(description = "<branch | commit>", required = true, arity = 1)
    private List<String> nameParam = null;

    private final String anonymousBranchName = "anonymous";

    public String getName() {
        return "checkout";
    }

    private CommitRef findCommit(String idCommit, Configuration config) {
        Set<CommitRef> commitCache = new HashSet<>();

        for (String branchName : config.getBranchesNames()) {
            CommitRef currentCommit = config.getBranch(branchName).lastCommit();

            while (currentCommit != null) {
                if (commitCache.contains(currentCommit)) {
                    break;
                }

                if (idCommit.equals(currentCommit.getIdCommit())) {
                    return currentCommit;
                }

                commitCache.add(currentCommit);
                currentCommit = currentCommit.getFirstParent();
            }
        }

        return null;
    }

    private List<Blob> getBlobs(CommitRef commit) throws IOException, ClassNotFoundException {
        List<String> blobIds = commit.getCommit().getBlobIds();
        List<Blob> blobs = new ArrayList<>();
        for (String id : blobIds) {
            blobs.add(Blob.load(id));
        }

        return blobs;
    }

    private void switchCommit(Configuration config) throws IOException, ClassNotFoundException {
        String name = nameParam.get(0);

        CommitRef commit = findCommit(name, config);
        if (commit == null) {
            System.out.println("Can not find commit " + name);
            return;
        }

        List<Blob> blobs = getBlobs(commit);

        CommitRef headCommit = config.head().lastCommit();
        List<Blob> headBlobs = getBlobs(headCommit);

        if (config.branchExists(anonymousBranchName)) {
            config.delBranch(anonymousBranchName);
        }

        Branch anonBranch = new Branch(anonymousBranchName, commit);
        config.addBranch(anonymousBranchName, anonBranch);
        config.switchBranch(anonymousBranchName);

        config.clearIndex();
        blobs.forEach(b -> config.addToIndex(b.getRepoPath(), b.getId()));

        config.save();

        headBlobs.forEach(b -> {
            Path path = Repository.REPO_DIR.resolve(b.getRepoPath());
            if (!FileUtils.deleteQuietly(path.toFile())) {
                System.out.println("Can not delete " + path.toString() + " from working directory");
            }
        });

        blobs.forEach(b -> {
            try {
                b.toFile();
            } catch (IOException e) {
                Path path = Repository.REPO_DIR.resolve(b.getRepoPath());
                System.out.println("Can not copy " + path.toString() + " to working directory");
            }
        });
    }

    private void switchBranch(Configuration config) throws IOException, ClassNotFoundException {
        String name = nameParam.get(0);

        CommitRef commit = config.getBranch(name).lastCommit();
        List<Blob> blobs = getBlobs(commit);

        CommitRef headCommit = config.head().lastCommit();
        List<Blob> headBlobs = getBlobs(headCommit);

        if (config.branchExists(anonymousBranchName)) {
            config.delBranch(anonymousBranchName);
        }

        config.switchBranch(name);

        config.clearIndex();
        blobs.forEach(b -> config.addToIndex(b.getRepoPath(), b.getId()));

        config.save();

        headBlobs.forEach(b -> {
            Path path = Repository.REPO_DIR.resolve(b.getRepoPath());
            if (!FileUtils.deleteQuietly(path.toFile())) {
                System.out.println("Can not delete " + path.toString() + " from working directory");
            }
        });

        blobs.forEach(b -> {
            try {
                b.toFile();
            } catch (IOException e) {
                Path path = Repository.REPO_DIR.resolve(b.getRepoPath());
                System.out.println("Can not copy " + path.toString() + " to working directory");
            }
        });
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

        try {
            String name = nameParam.get(0);

            if (config.getBranch(name) != null) {
                String headName = config.headName();
                if (name.equals(headName)) {
                    System.out.println("Already in this branch");
                    return;
                }

                switchBranch(config);
            } else {
                String headCommitId = config.head().lastCommit().getIdCommit();
                if (name.equals(headCommitId)) {
                    System.out.println("Already in this commit");
                    return;
                }

                switchCommit(config);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can not switch state (" + e.getMessage() + ")");
        }
    }
}
