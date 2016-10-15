package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Remove file from the index")
public class CleanCmd implements Command {
    @Override
    public String getName() {
        return "clean";
    }

    @Override
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

        List<Path> repoFiles;
        try {
            repoFiles = Repository.getAllRepoFiles();
        } catch (IOException e) {
            System.out.println("Can not read directory of repository (" + e.getMessage() + ")");
            return;
        }

        final Configuration finalConfig = config;
        repoFiles = repoFiles.stream()
                             .filter(p -> !finalConfig.isIndexed(p))
                             .collect(Collectors.toList());

        for (Path path : repoFiles) {
            if (!FileUtils.deleteQuietly(path.toFile())) {
                System.out.println("Can not delete " + path.toString());
            } else {
                System.out.println(path.toString() + " is deleted");
            }
        }
    }
}
