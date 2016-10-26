package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Remove file from the index")
public class ResetCmd implements Command {
    @Parameter(description = "<paths>", variableArity = true, required = true)
    private List<String> pathsFromCli = new ArrayList<>();

    /**
     * Remove the paths from the index
     *
     * @param paths
     *        the list of the paths
     *
     * @return the list of the unindexed files
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream or when accessing the file
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     */
    public List<Path> unindex(List<Path> paths) throws RepositoryException, IOException, ClassNotFoundException {
        List<Path> filteredPaths = Repository.filterRepoFiles(paths);

        Configuration config = Configuration.load();
        filteredPaths.forEach(config::delFromIndex);
        config.save();

        return filteredPaths;
    }

    public String getName() {
        return "reset";
    }

    public void execute() throws Exception {
        List<Path> listPaths = pathsFromCli.stream()
                                           .map(s -> Paths.get(s))
                                           .collect(Collectors.toList());

        pathsFromCli.clear();

        List<Path> filteredListPaths = unindex(listPaths);

        listPaths.removeAll(filteredListPaths);
        for (Path path : listPaths) {
            System.out.println(path.toString() + " is not in current repository");
        }
    }
}
