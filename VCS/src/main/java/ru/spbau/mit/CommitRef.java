package ru.spbau.mit;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * The class is reference to a commit
 */
public class CommitRef implements Serializable {
    private final String idCommit;
    private final List<CommitRef> parents;

    public CommitRef(String idCommit, List<CommitRef> parents) {
        this.idCommit = idCommit;
        this.parents = parents;
    }

    public boolean equals(Object other) {
        if (other instanceof CommitRef) {
            CommitRef otherCommitRef = (CommitRef) other;
            return idCommit.equals(otherCommitRef.getIdCommit()) &&
                    (parents != null && parents.equals(otherCommitRef.getParents()) ||
                    parents == otherCommitRef.getParents());
        }

        return false;
    }

    public String getIdCommit() {
        return idCommit;
    }

    public List<CommitRef> getParents() {
        return parents;
    }

    public CommitRef getFirstParent() {
        if (parents != null) {
            return parents.get(0);
        }
        return null;
    }

    public Commit getCommit() throws IOException, ClassNotFoundException {
        return Commit.load(idCommit);
    }
}
