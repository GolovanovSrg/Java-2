package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BlobTest {
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
    public void saveLoad() throws Exception {
        File tempFile = makeFile();
        Blob blob = new Blob(tempFile.toPath());

        blob.save();
        Blob loadBlod = Blob.load(blob.getId());

        assertEquals(blob, loadBlod);
    }

    @Test
    public void toFile() throws Exception {
        File tempFile = makeFile();
        Blob blob = new Blob(tempFile.toPath());

        FileUtils.deleteQuietly(tempFile);
        assertFalse(tempFile.exists());

        blob.toFile();
        assertTrue(tempFile.exists());

        assertTrue(Arrays.equals(blob.getContent(), Files.readAllBytes(tempFile.toPath())));
    }
}