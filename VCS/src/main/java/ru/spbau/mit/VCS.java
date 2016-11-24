package ru.spbau.mit;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import ru.spbau.mit.commands.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class - commands handler.
 */
public class VCS {
    private final JCommander jc = new JCommander(this);
    private final Map<String, Command> commands = new HashMap<>();

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public VCS() {
       List<Command> cmdList = Arrays.asList(new InitCmd(),
                                             new CommitCmd(),
                                             new AddCmd(),
                                             new CheckoutCmd(),
                                             new BranchCmd(),
                                             new LogCmd(),
                                             new MergeCmd(),
                                             new ResetCmd(),
                                             new CleanCmd(),
                                             new RmCmd(),
                                             new StatusCmd(),
                                             new GcCmd());

        for (Command cmd : cmdList) {
            commands.put(cmd.getName(), cmd);
            jc.addCommand(cmd.getName(), cmd);
        }

        jc.setProgramName("VCS");
    }

    public static InitCmd initCommand() {
        return new InitCmd();
    }

    public static AddCmd addCommand() {
        return new AddCmd();
    }

    public static BranchCmd branchCommand() {
        return new BranchCmd();
    }

    public static CheckoutCmd checkoutCommand() {
        return new CheckoutCmd();
    }

    public static CleanCmd cleanCommand() {
        return new CleanCmd();
    }

    public static CommitCmd commitCommand() {
        return new CommitCmd();
    }

    public static GcCmd gcCommand() {
        return new GcCmd();
    }

    public static LogCmd logCommand() {
        return new LogCmd();
    }

    public static MergeCmd mergeCommand() {
        return new MergeCmd();
    }

    public static ResetCmd resetCommand() {
        return new ResetCmd();
    }

    public static RmCmd rmCommand() {
        return new RmCmd();
    }

    public static StatusCmd statusCommand() {
        return new StatusCmd();
    }

    private void execCommand(String name) {
        Command cmd = commands.get(name);
        if (cmd == null) {
            System.out.println("Unknown command");
            jc.usage();
            return;
        }

        try {
            cmd.execute();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Run a command with arguments
     * @param args - command name and its arguments
     */
    public void run(String[] args) {
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.out.println("Unknown command");
            jc.usage();
            return;
        }

        if (help) {
            jc.usage();
            return;
        }

        execCommand(jc.getParsedCommand());
    }

    public static void main(String[] args) {
        VCS vcs = new VCS();
        vcs.run(args);
    }
}
