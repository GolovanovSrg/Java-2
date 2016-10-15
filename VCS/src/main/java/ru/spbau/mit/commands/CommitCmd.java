package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Record changes to the repository")
public class CommitCmd implements Command {
    @Parameter(description = "<message>", arity = 1)
    private List<String> messageParam = null;

    public String getName() {
        return "commit";
    }

    public void execute() {
        if (!Repository.exists()) {
            System.out.println("Repository is not found");
            return;
        }

        Configuration config;
        try {
            config = Configuration.load();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can not load configuration (" + e.getMessage() + " )");
            return;
        }

        try {
            List<String> blobIds = new ArrayList<>(config.getIndexBlobs());
            String message = messageParam == null ? "" : messageParam.get(0);
            config.makeCommit(message, blobIds);
            config.save();
        } catch (IOException e) {
            System.out.println("Can not make commit (" + e.getMessage() + ")");
        }
    }
}
