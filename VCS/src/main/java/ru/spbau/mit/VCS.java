package ru.spbau.mit;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import ru.spbau.mit.commands.*;
import java.util.*;

public class VCS {
    private final JCommander jc = new JCommander(this);
    private final Map<String, Command> commands = new HashMap<>();

    @Parameter(names = "-h", help = true)
    private boolean help;

    private VCS() {
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

    public void run(String[] args) {
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
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
        //try {
            vcs.run(args);
        //} catch (Exception e) {
            //System.out.println("Error: " + e.getMessage());
        //}
    }
}
