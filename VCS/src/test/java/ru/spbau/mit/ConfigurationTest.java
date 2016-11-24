package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ConfigurationTest {

    @Test
    public void saveLoad() throws Exception {
        File repoDir = Paths.get(System.getProperty("user.dir"), "repository").toFile();
        FileUtils.forceMkdir(repoDir);
        System.setProperty( "user.dir", repoDir.toString());

        Configuration config = new Configuration();
        config.addToIndex(Paths.get("./"), "1");
        config.save();

        Configuration loadConfig = Configuration.load();
        assertEquals(config.head(), loadConfig.head());

        FileUtils.deleteQuietly(repoDir);
    }

    @Test
    public void branchExists() throws Exception {
        Configuration config = new Configuration();
        assertTrue(config.branchExists("master"));
    }

    @Test
    public void getBranch() throws Exception {
        Configuration config = new Configuration();
        Branch branch = config.getBranch("master");
        assertEquals(config.head(), branch);
    }

    @Test
    public void addDelBranch() throws Exception {
        Configuration config = new Configuration();
        String newBranchName = "test";
        Branch newBranch = config.head().makeBranch(newBranchName);

        assertFalse(config.branchExists(newBranchName));

        config.addBranch(newBranchName, newBranch);

        assertTrue(config.branchExists(newBranchName));
        assertEquals(newBranch, config.getBranch(newBranchName));

        config.delBranch(newBranchName);

        assertFalse(config.branchExists(newBranchName));
    }

    @Test
    public void makeBranch() throws Exception {
        Configuration config = new Configuration();
        String newBranchName = "test";
        config.makeBranch(newBranchName);

        assertTrue(config.branchExists(newBranchName));
    }

    @Test
    public void switchBranch() throws Exception {
        Configuration config = new Configuration();
        String newBranchName = "test";
        config.makeBranch(newBranchName);

        config.switchBranch(newBranchName);
        assertEquals(newBranchName, config.headName());
    }

    @Test
    public void getBranchesNames() throws Exception {
        Configuration config = new Configuration();
        String newBranchName = "test";
        config.makeBranch(newBranchName);

        Set<String> branches = config.getBranchesNames();
        assertTrue(branches.containsAll(Arrays.asList(newBranchName, "master")));
        assertTrue(branches.size() == 2);
    }

    @Test
    public void makeCommit() throws Exception {
        Configuration config = new Configuration();
        String message = "test";
        List<String> blodIds = Arrays.asList("1", "2");
        config.makeCommit(message, blodIds);
        Commit commit = config.head().lastCommit().getCommit();
        assertEquals(commit.getMessage(), message);
        assertEquals(commit.getBlobIds(), blodIds);

    }
}