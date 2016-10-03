package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Remove file from the index and repository")
public class RmCmd implements Command {
    @Parameter(description = "<paths>", variableArity = true, required = true)
    private List<String> paths = new ArrayList<>();

    @Override
    public String getName() {
        return "rm";
    }

    private void removeFile(Path path, Configuration config) {
        try {
            config.delFromIndex(path);
            config.save();
        } catch (IOException e) {
            System.out.println("Can not delete " + path.toString() + " from index (" + e.getMessage() + ")");
            return;
        }

        if (!FileUtils.deleteQuietly(path.toFile())) {
            System.out.println("Can not delete " + path.toString());
        }
    }

    @Override
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
                removeFile(path, config);
            } else {
                System.out.println(path.toString() + " is not in current repository");
            }
        }
    }
}
