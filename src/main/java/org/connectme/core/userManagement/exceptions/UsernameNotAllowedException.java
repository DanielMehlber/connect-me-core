package org.connectme.core.userManagement.exceptions;

public class UsernameNotAllowedException extends Exception{

    public static enum Reason {
        PROFANITY,
        SYNTAX,
        LENGTH
    }

    private final String username;
    private final Reason reason;

    public UsernameNotAllowedException(final String username, final Reason reason) {
        /*
         * SECURITY: Do NOT log username, it is raw user input and could be harmful (see Log-Injection).
         */
        super(String.format("the provided username cannot be accepted. Reason: %s", username, reason.name()));
        this.username = username;
        this.reason = reason;
    }

    public String getUsername() {
        return username;
    }

    public Reason getReason() {
        return reason;
    }
}
