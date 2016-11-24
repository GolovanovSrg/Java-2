package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.exceptions.ConfigurationException;
import ru.spbau.mit.exceptions.RepositoryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "List, create, or toDeleteFromCli branches")
public class BranchCmd implements Command {
    @Parameter(names = "-d", description = "Delete branch")
    private boolean toDeleteFromCli = false;

    @Parameter(description = "<branch>", arity = 1)
    private List<String> branchNameFromCli = new ArrayList<>();

    /**
     * Get the names of all branches in the repository
     *
     * @return the list of names
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
    public List<String> getBranchNames() throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();
        return config.getBranchesNames().stream().collect(Collectors.toList());
    }

    /**
     * Get the name of the head branch
     *
     * @return the name of the head branch
     *
     * @throws RepositoryException
     *         if the  directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     */
    public String getCurrentBranchName() throws RepositoryException, IOException, ClassNotFoundException {
        Configuration config = Configuration.load();
        return config.headName();
    }

    /**
     * Create the new branch in the configuration
     *
     * @param name
     *        the name of the new branch
     *
     * @throws RepositoryException
     *         if the  directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     *
     * @throws ConfigurationException
     *         if the branch already exists
     */
    public void createBranch(String name) throws RepositoryException, IOException, ClassNotFoundException, ConfigurationException {
        Configuration config = Configuration.load();

        if (config.branchExists(name)) {
            throw new ConfigurationException("Branch " + name + " already exists");
        }

        config.makeBranch(name);
        config.save();
    }

    /**
     * Delete the branch form the configuration
     *
     * @param name
     *        the name of the branch
     *
     * @throws RepositoryException
     *         if the storage directory of the repository not exists
     *
     * @throws IOException
     *         if an I/O error occurs reading from the stream
     *
     * @throws ClassNotFoundException
     *         if class Configuration is not found
     *
     * @throws ConfigurationException
     *         if the branch not exists or is the current branch
     */
    public void deleteBranch(String name) throws RepositoryException, IOException, ClassNotFoundException, ConfigurationException {
        Configuration config = Configuration.load();

        if (name.equals(config.headName())) {
            throw new ConfigurationException("Can not delete current branch");
        }

        if (!config.branchExists(name)) {
            throw new ConfigurationException("Branch " + name + " not exists");
        }

        config.delBranch(name);
        config.save();
    }

    public String getName() {
        return "branch";
    }

    public void execute() throws Exception {
        if (toDeleteFromCli) {
            toDeleteFromCli = false;

            if (branchNameFromCli.isEmpty()) {
                System.out.println("Branch name is not found");
                return;
            }

            String name = branchNameFromCli.get(0);
            branchNameFromCli.clear();

            deleteBranch(name);
            return;
        }

        if (branchNameFromCli.isEmpty()) {
            String currentBranch = getCurrentBranchName();
            System.out.println("\nBranches:");
            for (String branchName : getBranchNames()) {
                String prefix = branchName.equals(currentBranch) ? "*" : "";
                System.out.println(prefix + branchName);
            }
            return;
        }

        String name = branchNameFromCli.get(0);
        branchNameFromCli.clear();

        createBranch(name);
    }
}
