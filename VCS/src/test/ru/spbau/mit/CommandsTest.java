package ru.spbau.mit;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class CommandsTest {
    private final VCS vcs = new VCS();

    @Rule
    public final TemporaryFolder workDirectory = new TemporaryFolder();

    @Before
    public void cdToWorkDirectory() {
        Repository.setRoot(workDirectory.getRoot().toString());
    }

    @Test
    public void commandsTest() throws Exception {
        Configuration config;

        // InitCmd test
        final String[] initParams = {"init"};
        vcs.run(initParams);

        assertTrue(Repository.exists());


        // BranchCmd test
        final String branchName1 = "test1";
        final String branchName2 = "test2";
        final String[] createBranchParams1 = {"branch", branchName1};
        final String[] createBranchParams2 = {"branch", branchName2};
        final String[] deleteBranchParams1 = {"branch", "-d", branchName2};

        vcs.run(createBranchParams1);
        config = Configuration.load();
        assertTrue(config.branchExists(branchName1));

        vcs.run(createBranchParams2);
        config = Configuration.load();
        assertTrue(config.branchExists(branchName2));

        List<String> branches = VCS.branchCommand().getBranchNames();
        assertTrue(branches.containsAll(Arrays.asList(branchName1, branchName2, "master")));
        assertEquals(branches.size(), 3);

        vcs.run(deleteBranchParams1);
        config = Configuration.load();
        assertFalse(config.branchExists(branchName2));

        Commit commit1 = config.head().lastCommit().getCommit();


        // CheckoutCmd branch test
        final String[] ckeckoutParams1 = {"checkout", branchName1};

        vcs.run(ckeckoutParams1);
        config = Configuration.load();
        assertEquals(branchName1, config.headName());

        Commit commit2 = config.head().lastCommit().getCommit();


        // AddCmd test
        final File file1 = workDirectory.newFile("file1");
        final String[] addParams1 = {"add", file1.getAbsolutePath()};

        vcs.run(addParams1);
        config = Configuration.load();
        assertTrue(config.isIndexed(file1.toPath()));
        String blobId1 = config.getBlobId(file1.toPath());
        assertTrue(Blob.getStoragePath().resolve(blobId1).toFile().exists());


        // CommitCmd test
        final String[] commitParams1 = {"commit"};

        vcs.run(commitParams1);
        config = Configuration.load();
        String commitId1 = config.head().lastCommit().getIdCommit();
        assertTrue(Commit.getStoragePath().resolve(commitId1).toFile().exists());
        assertEquals(config.getIndexBlobs(), Commit.load(commitId1).getBlobIds());

        Commit commit3 = config.head().lastCommit().getCommit();



        // LogCmd test
        assertEquals(Arrays.asList(commit3, commit2, commit1), VCS.logCommand().getHistory());


        // CheckoutCmd commit test
        final File file2 = workDirectory.newFile("file2");
        assertTrue(file2.exists());

        final String[] addParams2 = {"add", file2.getAbsolutePath()};
        final String[] commitParams2 = {"commit"};
        final String[] ckeckoutParams2 = {"checkout", commitId1};

        vcs.run(addParams2);
        vcs.run(commitParams2);
        vcs.run(ckeckoutParams2);
        assertFalse(file2.exists());
        config = Configuration.load();

        assertFalse(config.isIndexed(file2.toPath()));
        assertTrue(file1.exists());


        // Checkout branch test
        final String[] ckeckoutParams3 = {"checkout", "master"};

        vcs.run(ckeckoutParams3);
        assertTrue(Repository.getAllRepoFiles().isEmpty());


        // MergeCmd test
        final String[] mergeParams = {"merge", branchName1};

        vcs.run(mergeParams);
        config = Configuration.load();
        assertTrue(file1.exists());
        assertTrue(config.isIndexed(file1.toPath()));
        assertTrue(file2.exists());
        assertTrue(config.isIndexed(file2.toPath()));


        // CleanCmd test
        final String[] cleanParam = {"clean"};

        File file3 = workDirectory.newFile("test3");
        vcs.run(cleanParam);
        assertFalse(file3.exists());
        assertTrue(file1.exists());


        // StatusCmd test
        file2.delete();
        try(PrintStream stream = new PrintStream(file1)) {
            stream.print("4 8 15 16 23 42");
        }
        file3 = workDirectory.newFile("test3");

        Triple<Set<Path>, Set<Path>, Set<Path>> triple = VCS.statusCommand().getStatus();

        assertEquals(file1.toPath(), triple.getLeft().iterator().next());
        assertEquals(file3.toPath(), triple.getMiddle().iterator().next());
        assertEquals(file2.toPath(), triple.getRight().iterator().next());

        // ResetCmd test
        final String[] resetParams = {"reset", file1.toString()};

        vcs.run(resetParams);
        config = Configuration.load();
        assertFalse(config.isIndexed(file1.toPath()));
        vcs.run(addParams1);


        // RmCmd
        final String[] rmParams = {"rm", file1.toString()};

        config = Configuration.load();
        assertTrue(config.isIndexed(file1.toPath()));
        vcs.run(rmParams);
        config = Configuration.load();
        assertFalse(config.isIndexed(file1.toPath()));
        assertFalse(file1.exists());
    }
}
