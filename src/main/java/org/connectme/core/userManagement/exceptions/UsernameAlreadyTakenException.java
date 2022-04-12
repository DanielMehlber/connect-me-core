package org.connectme.core.userManagement.exceptions;

/**
 * This exception is thrown if a user cannot be created because the username already exists
 */
public class UsernameAlreadyTakenException extends Exception {

    /**
     * Unavailable username
     */
    private final String username;

    public UsernameAlreadyTakenException(final String username) {
        /*
         * Do NOT log username, it is raw user input and could be harmful (see Log-Injection)
         */
        super("The passed username is already taken");
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
