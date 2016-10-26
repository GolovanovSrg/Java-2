package ru.spbau.mit;

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The class implements a branch on vcs for Configuration class
 */
public class Branch implements Serializable {
    private CommitRef lastCommit;

    /**
     * @throws IOException
     *         if an I/O error occurs writing to or creating the commits file
     */
    public Branch() throws IOException {
        Commit newCommit = new Commit("New branch: master");
        newCommit.save();
        lastCommit = new CommitRef(newCommit.getId(), null);
    }

    /**
     * @param name
     *        name of branch
     *
     * @param parent
     *        commit-parent
     *
     * @throws IOException
     *         if an I/O error occurs reading from the commits file
     *
     * @throws ClassNotFoundException
     *         if class Commit is not found
     */
    public Branch(String name, CommitRef parent) throws IOException, ClassNotFoundException {
        List<String> parentBlobIds = parent.getCommit().getBlobIds();
        Commit newCommit = new Commit("New branch: " + name, parentBlobIds);
        newCommit.save();
        lastCommit = new CommitRef(newCommit.getId(), Arrays.asList(parent));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Branch) {
            Branch otherBranch = (Branch) other;
            return  lastCommit.equals(otherBranch.lastCommit());
        }

        return false;
    }

    public CommitRef lastCommit() {
        return lastCommit;
    }

    /**
     * Make new branch from the branch
     *
     * @param name
     *        name of new branch
     *
     * @return new branch
     *
     * @throws IOException
     *         if an I/O error occurs writing to or creating the init commits file of branch
     *
     * @throws ClassNotFoundException
     *         if class Commit is not found
     */
    public Branch makeBranch(String name) throws IOException, ClassNotFoundException {
        return new Branch(name, lastCommit);
    }

    /**
     * Make new commit for the branch
     *
     * @param message
     *        message for commit
     *
     * @param blobIds
     *        List of blobs ids
     *
     * @return CommitRef for new commit
     *
     * @throws IOException
     *         if an I/O error occurs reading from the commits file
     */
    public CommitRef makeCommit(String message, List<String> blobIds) throws IOException {
        Commit newCommit = new Commit(message, blobIds);
        newCommit.save();
        lastCommit = new CommitRef(newCommit.getId(), Arrays.asList(lastCommit));
        return lastCommit;
    }

    /**
     * Make new commit for the branch
     *
     * @param message
     *        message for commit
     *
     * @param blobIds
     *        List of blobs ids
     *
     * @param otherParent
     *        second parent for commit
     *
     * @return CommitRef for new commit
     *
     * @throws IOException
     *         if an I/O error occurs reading from the commits file
     */
    public CommitRef makeCommit(String message, List<String> blobIds, CommitRef otherParent) throws IOException {
        Commit newCommit = new Commit(message, blobIds);
        newCommit.save();
        lastCommit = new CommitRef(newCommit.getId(), Arrays.asList(lastCommit, otherParent));
        return lastCommit;
    }

    /**
     * Get history of commits
     *
     * @return list of CommitRef
     */
    public List<CommitRef> getHistory() {
        List<CommitRef> history = new LinkedList<>();
        CommitRef commit = lastCommit;
        while (commit != null) {
            history.add(commit);
            commit = commit.getFirstParent();
        }

        return history;
    }
}
