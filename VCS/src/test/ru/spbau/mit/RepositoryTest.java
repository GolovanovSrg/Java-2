package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RepositoryTest {
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

    private File makeFile() throws IOException {
        File tempFile = File.createTempFile("testFile", ".txt", repoDir);
        PrintStream fileStream = new PrintStream(tempFile);
        fileStream.println("4 8 15 16 23 42");
        fileStream.close();

        return tempFile;
    }

    @Test
    public void exists() throws Exception {
        Repository.getStorageDirectory().toFile().mkdir();
        assertTrue(Repository.exists());
    }

    @Test
    public void getRepoPath() throws Exception {
        Path path = makeFile().toPath().toAbsolutePath().normalize();
        assertEquals(Repository.getRoot().relativize(path), Repository.getRepoPath(path));
    }

    @Test
    public void getAllRepoFiles() throws Exception {
        Path path = makeFile().toPath();
        List<Path> repoFiles = Repository.getAllRepoFiles();
        assertEquals(Arrays.asList(path), repoFiles);
    }

    @Test
    public void filterRepoFiles() throws Exception {
        Path path1 = makeFile().toPath();
        Path path2 = Paths.get("../");
        List<Path> repoPaths = Repository.filterRepoFiles(Arrays.asList(path1, path2));

        assertTrue(repoPaths.contains(path1));
        assertFalse(repoPaths.contains(path2));
    }
}