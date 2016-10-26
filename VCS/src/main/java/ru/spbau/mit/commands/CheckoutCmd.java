package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.apache.commons.io.FileUtils;

import ru.spbau.mit.*;
import ru.spbau.mit.exceptions.ConfigurationException;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Parameters(commandDescription = "Checkout a branch or paths to the working tree")
public class CheckoutCmd implements Command {
    @Parameter(description = "<branch | commit>", required = true, arity = 1)
    private List<String> commitIdOrBranchNameFromCli = new ArrayList<>();

    private static final String ANONYMOUS_BRANCH_NAME = "anonymous";

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

    /**
     * Switch to the commit
     *
     * @param idCommit
     *        id of the commit
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     *
     * @throws ConfigurationException
     *         if the commit is not found in the configuration
     */
    public  void switchCommit(String idCommit) throws RepositoryException, IOException,
                                                      ClassNotFoundException, ConfigurationException {
        Configuration config = Configuration.load();

        CommitRef commit = findCommit(idCommit, config);
        if (commit == null) {
            throw new ConfigurationException("Commit " + idCommit + " is not found");
        }

        String headCommitId = config.head().lastCommit().getIdCommit();
        if (idCommit.equals(headCommitId)) {
            return;
        }

        List<Blob> blobs = getBlobs(commit);
        CommitRef headCommit = config.head().lastCommit();
        List<Blob> headBlobs = getBlobs(headCommit);

        if (config.branchExists(ANONYMOUS_BRANCH_NAME)) {
            config.delBranch(ANONYMOUS_BRANCH_NAME);
        }

        Branch anonBranch = new Branch(ANONYMOUS_BRANCH_NAME, commit);
        config.addBranch(ANONYMOUS_BRANCH_NAME, anonBranch);
        config.switchBranch(ANONYMOUS_BRANCH_NAME);

        config.clearIndex();
        blobs.forEach(b -> config.addToIndex(b.getRepoPath(), b.getId()));

        config.save();

        headBlobs.forEach(b -> {
            Path path = Repository.getRoot().resolve(b.getRepoPath());
            if (!FileUtils.deleteQuietly(path.toFile())) {
                System.err.println("Can not delete " + path.toString() + " from working directory");
            }
        });

        blobs.forEach(b -> {
            try {
                b.writeToFile();
            } catch (IOException e) {
                Path path = Repository.getRoot().resolve(b.getRepoPath());
                System.err.println("Can not copy " + path.toString() + " to working directory");
            }
        });
    }

    /**
     * Switch to the branch
     *
     * @param name
     *        iname of the branch
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
     *         if the branch is not found in the configuration
     */
    public void switchBranch(String name) throws RepositoryException, IOException,
                                                  ClassNotFoundException, ConfigurationException {
        Configuration config = Configuration.load();

        if (config.getBranch(name) == null) {
            throw new ConfigurationException("Branch " + name + " is not found");
        }

        String headName = config.headName();
        if (name.equals(headName)) {
            return;
        }

        CommitRef commit = config.getBranch(name).lastCommit();
        List<Blob> blobs = getBlobs(commit);

        CommitRef headCommit = config.head().lastCommit();
        List<Blob> headBlobs = getBlobs(headCommit);

        if (config.branchExists(ANONYMOUS_BRANCH_NAME)) {
            config.delBranch(ANONYMOUS_BRANCH_NAME);
        }

        config.switchBranch(name);

        config.clearIndex();
        blobs.forEach(b -> config.addToIndex(b.getRepoPath(), b.getId()));

        config.save();

        headBlobs.forEach(b -> {
            Path path = Repository.getRoot().resolve(b.getRepoPath());
            if (!FileUtils.deleteQuietly(path.toFile())) {
                System.err.println("Can not delete " + path.toString() + " from working directory");
            }
        });

        blobs.forEach(b -> {
            try {
                b.writeToFile();
            } catch (IOException e) {
                Path path = Repository.getRoot().resolve(b.getRepoPath());
                System.err.println("Can not copy " + path.toString() + " to working directory");
            }
        });
    }

    public String getName() {
        return "checkout";
    }

    public void execute() throws Exception {
        String name = commitIdOrBranchNameFromCli.get(0);
        commitIdOrBranchNameFromCli.clear();

        Configuration config = Configuration.load();

        if (config.getBranch(name) != null) {
            switchBranch(name);
        } else {
            switchCommit(name);
        }
    }
}
