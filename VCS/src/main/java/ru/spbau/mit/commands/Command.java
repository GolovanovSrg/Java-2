package ru.spbau.mit.commands;

/**
 * The interface describes the methods that need to implement
 * all the command-classes that are executed
 */
public interface Command {
    /**
     * Get name of command
     * @return name command
     */
    String getName();

    /**
     * Execute command
     * @throws Exception
     */
    void execute() throws Exception;
}
