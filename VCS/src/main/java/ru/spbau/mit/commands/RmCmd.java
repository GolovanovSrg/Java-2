package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.apache.commons.io.FileUtils;

import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Remove file from the index and repository")
public class RmCmd implements Command {
    @Parameter(description = "<paths>", variableArity = true, required = true)
    private List<String> pathsFromCli = new ArrayList<>();

    /**
     * Remove the paths from the index and the disk
     *
     * @param paths
     *        the list of the paths
     *
     * @return the list of the unindexed and removed files
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
    private List<Path> removeFiles(List<Path> paths) throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();
        List<Path> filteredPaths = Repository.filterRepoFiles(paths);
        filteredPaths.forEach(config::delFromIndex);
        config.save();

        for (Path path : filteredPaths) {
            if (!FileUtils.deleteQuietly(path.toFile())) {
                System.err.println("Can not delete " + path.toString());
            }
        }

        return filteredPaths;
    }

    public String getName() {
        return "rm";
    }

    public void execute() throws Exception {
        List<Path> listPaths = pathsFromCli.stream()
                                           .map(s -> Paths.get(s))
                                           .collect(Collectors.toList());

        pathsFromCli.clear();

        List<Path> removedPaths = removeFiles(listPaths);

        listPaths.removeAll(removedPaths);
        for (Path path : listPaths) {
            System.out.println(path.toString() + " is not in current repository");
        }
    }
}
