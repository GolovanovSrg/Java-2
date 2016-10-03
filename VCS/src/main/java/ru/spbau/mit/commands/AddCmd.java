package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.spbau.mit.Blob;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Add file contents to the index")
public class AddCmd implements Command {
    @Parameter(description = "<paths>", variableArity = true, required = true)
    private List<String> paths = new ArrayList<>();

    public String getName() {
        return "add";
    }

    private void indexFile(Path path, Configuration config) {
        try {
            Blob blob = new Blob(path);
            blob.save();
            config.addToIndex(blob.getRepoPath(), blob.getId());
            config.save();
        } catch (IOException e) {
            System.out.println("Can not add " + path.toString() + " to index (" + e.getMessage() + ")");
        }
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

        List<Path> listPaths = paths.stream()
                                    .map(s -> Paths.get(s))
                                    .collect(Collectors.toList());

        listPaths = Utils.getOnlyFiles(listPaths);

        for (Path path : listPaths) {
            if (path.startsWith(Utils.REPO_DIR)) {
                indexFile(path, config);
            } else {
                System.out.println(path.toString() + " is not in current repository");
            }
        }
    }
}
