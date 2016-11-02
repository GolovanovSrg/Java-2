package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;

import ru.spbau.mit.Commit;
import ru.spbau.mit.CommitRef;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Show commit logs")
public class LogCmd implements Command {
    /**
     * Get the history of commits
     *
     * @return the list of the commits
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     */
    public List<Commit> getHistory() throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();

        List<Commit> result = new ArrayList<>();
        for (CommitRef commit : config.getHeadHistory()) {
            result.add(commit.getCommit());
        }

        return result;
    }

    public String getName() {
        return "log";
    }

    public void execute() throws Exception {
        for (Commit commit : getHistory()) {
            System.out.println("\nCommit " + commit.getId());
            System.out.println("Message: " + commit.getMessage());
        }
    }
}
