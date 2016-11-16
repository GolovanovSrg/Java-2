package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BranchTest {
    private final File repoDir = Paths.get(System.getProperty("user.dir"), "repository").toFile();

    @Before
    public void createRepoDir() throws IOException {
        FileUtils.forceMkdir(repoDir);
        System.setProperty( "user.dir", repoDir.toString());

        assertTrue(repoDir.exists());
    }

    @After
    public void deleteRepoDir() {
        FileUtils.deleteQuietly(repoDir);
    }

    @Test
    public void makeBranch() throws Exception {
        Branch master = new Branch();
        master.makeCommit("test", Arrays.asList("1", "2", "3"));
        Branch newBranch = master.makeBranch("test");

        assertEquals(master.lastCommit(), newBranch.lastCommit().getFirstParent());
        assertEquals(master.lastCommit().getCommit().getBlobIds(),
                      newBranch.lastCommit().getFirstParent().getCommit().getBlobIds());
    }

    @Test
    public void makeCommit() throws Exception {
        Branch master = new Branch();
        CommitRef newCommit1 = master.makeCommit("test1", Arrays.asList("1", "2", "3"));
        CommitRef newCommit2 = master.makeCommit("test2", Arrays.asList("1", "2"));

        assertEquals(newCommit1, newCommit2.getFirstParent());
        assertEquals(master.lastCommit(), newCommit2);
    }

    @Test
    public void getHistory() throws Exception {
        Branch master = new Branch();
        CommitRef initCommit = master.lastCommit();
        CommitRef newCommit1 = master.makeCommit("test1", Arrays.asList("1", "2", "3"));
        CommitRef newCommit2 = master.makeCommit("test2", Arrays.asList("1", "2"));

        Set<CommitRef> history = new LinkedHashSet<>();
        history.addAll(Arrays.asList(newCommit2, newCommit1, initCommit));
        assertEquals(history, master.getHistory());
    }
}