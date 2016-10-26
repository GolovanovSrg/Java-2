package ru.spbau.mit.exceptions;

import java.nio.file.Path;

import java.util.List;

public class MergeConflictException extends Exception {
    private final List<Path> conflictedPaths;

    public MergeConflictException(List<Path> conflictedPaths) {
        super();
        this.conflictedPaths = conflictedPaths;
    }

    public MergeConflictException(String message, List<Path> conflictedPaths) {
        super(message);
        this.conflictedPaths = conflictedPaths;
    }

    public List<Path> getConflictedPaths() {
        return conflictedPaths;
    }
}
