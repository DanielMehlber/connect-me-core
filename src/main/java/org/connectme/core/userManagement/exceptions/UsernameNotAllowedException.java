package org.connectme.core.userManagement.exceptions;

public class UsernameNotAllowedException extends Exception{

    public enum Reason {
        PROFANITY,
        SYNTAX,
        LENGTH
    }

    public UsernameNotAllowedException(final Reason reason) {
        /*
         * SECURITY: Do NOT log username, it is raw user input and could be harmful (see Log-Injection).
         */
        super(String.format("the provided username cannot be accepted. Reason: %s", reason.name()));
    }
}
