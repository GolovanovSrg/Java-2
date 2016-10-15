package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;

import java.io.File;
import java.io.IOException;

@Parameters(commandDescription = "Create an empty vcs repository or reinitialize an existing one")
public class InitCmd implements Command {
    public String getName() {
        return "init";
    }

    public void execute() {
        File vcsDirectory = Repository.STORAGE_DIR.toFile();

        if (vcsDirectory.exists()) {
            if (!FileUtils.deleteQuietly(vcsDirectory)) {
                System.out.println("Can not reinitialize repository");
                return;
            }
        }

        if (vcsDirectory.mkdir()) {
            try {
                Configuration config = new Configuration();
                config.save();
                System.out.println("Initialized empty vcs repository in " + vcsDirectory.toString());
                return;
            } catch (IOException e ) {
                System.out.print(e.getMessage());
            }
        }

        System.out.println("Can not make empty repository");
    }
}
