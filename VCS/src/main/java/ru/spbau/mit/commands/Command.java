package ru.spbau.mit.commands;

public interface Command {
    String getName();
    void execute() throws Exception;
}
