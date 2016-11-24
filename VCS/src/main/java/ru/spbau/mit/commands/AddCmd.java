package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import ru.spbau.mit.Blob;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Add file contents to the index")
public class AddCmd implements Command {
    @Parameter(description = "<paths>", variableArity = true, required = true)
    private List<String> pathsFromCli = new ArrayList<>();

    private void indexFile(Path path, Configuration config) throws IOException, ClassNotFoundException {
        if (config.isIndexed(path)) {
            byte[] content = Files.readAllBytes(path);
            String blobId = config.getBlobId(path);
            byte[] contentBlob = Blob.load(blobId).getContent();

            if (Arrays.deepEquals(new Object[]{content}, new Object[]{contentBlob})) {
                return;
            }
        }

        Blob blob = new Blob(path);
        blob.save();
        config.addToIndex(path, blob.getId());
    }

    /**
     * Add the files to the index
     *
     * @param paths
     *        the list of the paths
     *
     * @return the list of the indexed paths
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
    public List<Path> addFiles(List<Path> paths) throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();

        List<Path> filteredListPaths = Repository.filterRepoFiles(paths);

        for (Path path : filteredListPaths) {
            indexFile(path, config);
        }

        config.save();

        return filteredListPaths;
    }

    public String getName() {
        return "add";
    }

    public void execute() throws Exception {
        List<Path> listPaths = pathsFromCli.stream()
                                           .map(s -> Paths.get(s).toAbsolutePath().normalize())
                                           .collect(Collectors.toList());

        pathsFromCli.clear();

        List<Path> addedPaths = addFiles(listPaths);
        listPaths.removeAll(addedPaths);

        if (!listPaths.isEmpty()) {
            System.out.println("Can not add files which in not the repository:");
            listPaths.forEach(System.out::println);
        }
    }
}
