package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import ru.spbau.mit.Configuration;
import ru.spbau.mit.exceptions.ConfigurationException;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Record changes to the repository")
public class CommitCmd implements Command {
    @Parameter(description = "<message>", arity = 1)
    private List<String> messageFromCli = new ArrayList<>();

    /**
     * Create the commit for the configuration
     *
     * @param message
     *        the message of the commit
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     *
     * @throws ConfigurationException
     *         if there are no new files in the index
     */
    public void makeCommit(String message) throws RepositoryException, IOException,
                                                  ClassNotFoundException, ConfigurationException {
        Configuration config = Configuration.load();
        List<String> lastBlobIds = config.head().lastCommit().getCommit().getBlobIds();
        List<String> blobIds = new ArrayList<>(config.getIndexBlobs());

        if (lastBlobIds.equals(blobIds)) {
            throw new ConfigurationException("There are no new files in the index");
        }

        config.makeCommit(message, blobIds);
        config.save();
    }

    public String getName() {
        return "commit";
    }

    public void execute() throws Exception {
        String message = messageFromCli.isEmpty() ? "" : messageFromCli.get(0);
        messageFromCli.clear();

        try {
            makeCommit(message);
        } catch (ConfigurationException e) {
            System.out.println(e.getMessage());
        }
    }
}
