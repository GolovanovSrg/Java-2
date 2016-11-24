package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;

import org.apache.commons.io.FileUtils;

import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.File;
import java.io.IOException;

@Parameters(commandDescription = "Create an empty vcs repository or reinitialize an existing one")
public class InitCmd implements Command {

    /**
     * Initialize the repository
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists or can not clear the exists repository
     */
    public void initialize() throws RepositoryException {
        File vcsDirectory = Repository.getStorageDirectory().toFile();

        if (vcsDirectory.exists()) {
            if (!FileUtils.deleteQuietly(vcsDirectory)) {
                throw new RepositoryException("Can not reinitialize repository");
            }
        }

        if (!vcsDirectory.mkdir()) {
            throw new RepositoryException("Can not make empty repository in " + vcsDirectory.toString());
        }

        try {
            Configuration config = new Configuration();
            config.save();
        } catch (IOException e) {
            RepositoryException exception = new RepositoryException("Can not make empty repository in " + vcsDirectory.toString());
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public String getName() {
        return "init";
    }

    public void execute() throws Exception {
        initialize();
        System.out.println("Initialized empty vcs repository in " + Repository.getStorageDirectory().toString());
    }
}
