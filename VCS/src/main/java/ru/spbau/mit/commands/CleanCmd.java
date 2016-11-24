package ru.spbau.mit.commands;

import com.beust.jcommander.Parameters;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import ru.spbau.mit.Configuration;
import ru.spbau.mit.Repository;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Remove all files which in not the index")
public class CleanCmd implements Command {
    /**
     * Remove all files which in not the index
     * @return pair of list of cleared paths and list of not cleared paths
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
    public Pair<List<Path>, List<Path>> removeNotIndex() throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();

        List<Path> notIndexedPaths= Repository.getAllRepoFiles().stream()
                                                                .filter(p -> !config.isIndexed(p))
                                                                .collect(Collectors.toList());

        List<Path> removedPaths = new ArrayList<>();
        List<Path> notRemovedPaths = new ArrayList<>();
        for (Path path : notIndexedPaths) {
            if (!FileUtils.deleteQuietly(path.toFile())) {
                notRemovedPaths.add(path);
            } else {
                removedPaths.add(path);
            }
        }

        return Pair.of(removedPaths, notRemovedPaths);
    }

    public String getName() {
        return "clean";
    }

    public void execute() throws Exception {
        Pair<List<Path>, List<Path>> pairPaths = removeNotIndex();

        if (!pairPaths.getRight().isEmpty()) {
            System.out.println("Can not remove paths from index: ");
            pairPaths.getRight().forEach(System.out::println);
        }
    }
}
