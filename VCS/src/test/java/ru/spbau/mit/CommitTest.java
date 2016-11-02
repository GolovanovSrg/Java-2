package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommitTest {
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
    public void saveLoad() throws Exception {
        Commit commit = new Commit("test");
        commit.save();
        Commit loadCommit = Commit.load(commit.getId());

        assertEquals(commit, loadCommit);

        Commit commit2 = new Commit("test", Arrays.asList("1", "2"));
        commit2.save();
        Commit loadCommit2 = Commit.load(commit2.getId());

        assertEquals(commit2, loadCommit2);
    }
}