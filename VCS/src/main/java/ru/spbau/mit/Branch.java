package ru.spbau.mit;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The Branch
 */
public class Branch implements Serializable {
    private CommitRef lastCommit;

    public Branch() throws IOException {
        Commit newCommit = new Commit("New branch: master");
        newCommit.save();
        lastCommit = new CommitRef(newCommit.getId(), null);
    }

    public Branch(String name, CommitRef parent) throws IOException, ClassNotFoundException {
        List<String> parentBlobIds = parent.getCommit().getBlobIds();
        Commit newCommit = new Commit("New branch: " + name, parentBlobIds);
        newCommit.save();
        lastCommit = new CommitRef(newCommit.getId(), Arrays.asList(parent));
    }

    public CommitRef lastCommit() {
        return lastCommit;
    }

    public Branch makeBranch(String name) throws IOException, ClassNotFoundException {
        return new Branch(name, lastCommit);
    }

    public CommitRef makeCommit(String message, List<String> blobIds) throws IOException {
        Commit newCommit = new Commit(message, blobIds);
        newCommit.save();
        CommitRef newCommitRef = new CommitRef(newCommit.getId(), Arrays.asList(lastCommit));
        lastCommit = newCommitRef;
        return lastCommit;
    }

    public CommitRef makeMergeCommit(String message, List<String> blobIds, CommitRef otherParent) throws IOException {
        Commit newCommit = new Commit(message, blobIds);
        newCommit.save();
        CommitRef newCommitRef = new CommitRef(newCommit.getId(), Arrays.asList(lastCommit, otherParent));
        lastCommit = newCommitRef;
        return lastCommit;
    }

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
