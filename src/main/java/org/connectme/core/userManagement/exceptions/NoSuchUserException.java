package org.connectme.core.userManagement.exceptions;


/**
 * This exception is thrown if there is no user data for a passed username
 */
public class NoSuchUserException extends Exception {

    private final String username;

    public NoSuchUserException(final String username) {
        super(String.format("No user found with username '%s'", username));
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
