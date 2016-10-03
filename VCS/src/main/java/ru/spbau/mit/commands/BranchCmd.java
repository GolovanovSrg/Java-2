package ru.spbau.mit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.spbau.mit.Configuration;
import ru.spbau.mit.Utils;

import java.io.IOException;
import java.util.List;

@Parameters(commandDescription = "List, create, or delete branches")
public class BranchCmd implements Command {
    @Parameter(names = "-d", description = "Delete branch")
    private boolean delete = false;

    @Parameter(description = "<branch>", arity = 1)
    private List<String> nameParam = null;

    public String getName() {
        return "branch";
    }

    private void showBranches(Configuration config) {
        System.out.println("\nBranches:");
        for (String branchName : config.getBranchesNames()) {
            String prefix = branchName.equals(config.headName()) ? "*" : "";
            System.out.println(prefix + branchName);
        }
    }

    private void createBranch(Configuration config) {
        String name = nameParam.get(0);

        if (config.branchExists(name)) {
            System.out.println("Branch " + name + " already exists");
            return;
        }

        try {
            config.makeBranch(name);
            config.save();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can not make branch (" + e.getMessage() + ")");
        }
    }

    private void deleteBranch(Configuration config) {
        if (nameParam == null) {
            System.out.println("Branch name is not found");
            return;
        }

        String name = nameParam.get(0);
        if (name.equals(config.headName())) {
            System.out.println("Can not delete current branch");
            return;
        }

        if (!config.branchExists(name)) {
            System.out.println("Branch " + name + " not exists");
            return;
        }

        try {
            config.delBranch(name);
            config.save();
        } catch (IOException e) {
            System.out.println("Can not delete branch " + name + " (" + e.getMessage() + ")");
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

        if (delete) {
            deleteBranch(config);
            return;
        }

        if (nameParam == null) {
            showBranches(config);
            return;
        }

        createBranch(config);
    }
}
