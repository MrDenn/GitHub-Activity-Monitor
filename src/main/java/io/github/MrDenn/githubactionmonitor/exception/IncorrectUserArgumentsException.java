package io.github.MrDenn.githubactionmonitor.exception;

public class IncorrectUserArgumentsException extends RuntimeException {
    public IncorrectUserArgumentsException(String message) {
        super("Incorrect arguments given by user: provided " + message + " is not valid");
    }
}
