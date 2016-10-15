package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Remove file from the index")
public class ResetCmd implements Command {
    @Parameter(description = "<paths>", variableArity = true, required = true)
    private List<String> paths = new ArrayList<>();

    @Override
    public String getName() {
        return "reset";
    }

    private void unindexFile(Path path, Configuration config) {
        try {
            config.delFromIndex(path);
            config.save();
        } catch (IOException e) {
            System.out.println("Can not remove " + path.toString() + " from index");
        }
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

        List<Path> listPaths = paths.stream()
                                    .map(s -> Paths.get(s))
                                    .collect(Collectors.toList());
        listPaths = Repository.filterRepoFiles(listPaths);

        for (Path path : listPaths) {
            if (path.startsWith(Repository.REPO_DIR)) {
                unindexFile(path, config);
            } else {
                System.out.println(path.toString() + " is not in current repository");
            }
        }
    }
}
