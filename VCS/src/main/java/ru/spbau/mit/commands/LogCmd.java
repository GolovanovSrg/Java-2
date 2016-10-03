package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;
import ru.spbau.mit.Commit;
import ru.spbau.mit.CommitRef;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Utils;

import java.io.IOException;

@Parameters(commandDescription = "Show commit logs")
public class LogCmd implements Command {
    public String getName() {
        return "log";
    }

    public void execute() {
        if (!Utils.isRepository()) {
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

        for (CommitRef ref : config.getHeadHistory()) {
            System.out.println("\nCommit " + ref.getIdCommit());
            try {
                Commit commit = ref.getCommit();
                System.out.println("Message: " + commit.getMessage());
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Can not load commit (" + e.getMessage() + ")");
            }
        }
    }
}
