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

    @Parameter(names = "-h", help = true)
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

    public InitCmd initCommand() {
        return (InitCmd) commands.get("init");
    }

    public AddCmd addCommand() {
        return (AddCmd) commands.get("add");
    }

    public BranchCmd branchCommand() {
        return (BranchCmd) commands.get("branch");
    }

    public CheckoutCmd checkoutCommand() {
        return (CheckoutCmd) commands.get("chechout");
    }

    public CleanCmd cleanCommand() {
        return (CleanCmd) commands.get("clean");
    }

    public CommitCmd commitCommand() {
        return (CommitCmd) commands.get("commit");
    }

    public GcCmd gcCommand() {
        return (GcCmd) commands.get("gc");
    }

    public LogCmd logCommand() {
        return (LogCmd) commands.get("log");
    }

    public MergeCmd mergeCommand() {
        return (MergeCmd) commands.get("merge");
    }

    public ResetCmd resetCommand() {
        return (ResetCmd) commands.get("reset");
    }

    public RmCmd rmCommand() {
        return (RmCmd) commands.get("rm");
    }

    public StatusCmd statusCommand() {
        return (StatusCmd) commands.get("status");
    }

    private void execCommand(String name) {
        Command cmd = commands.get(name);
        if (cmd == null) {
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
            System.out.println("Unknown command. See help.");
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
