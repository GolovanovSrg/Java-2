package ru.spbau.mit;

import java.io.IOException;
import java.io.Serializable;

import java.util.List;

/**
 * The class is reference to a commit.
 * He lets not load content of commit in Configuration class
 */
public class CommitRef implements Serializable {
    private final String idCommit;
    private final List<CommitRef> parents;

    /**
     * @param idCommit
     *        commits id
     *
     * @param parents
     *        list of parents for commit
     */
    public CommitRef(String idCommit, List<CommitRef> parents) {
        this.idCommit = idCommit;
        this.parents = parents;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

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

    /**
     * Get commit of reference
     *
     * @return Commit of reference
     *
     * @throws IOException
     *          if an I/O error occurs reading from the commits file
     *
     * @throws ClassNotFoundException
     *         if Commit class is not found
     */
    public Commit getCommit() throws IOException, ClassNotFoundException {
        return Commit.load(idCommit);
    }
}
