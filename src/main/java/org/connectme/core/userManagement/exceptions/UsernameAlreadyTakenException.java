package org.connectme.core.userManagement.exceptions;

/**
 * This exception is thrown if a user cannot be created because the username already exists
 */
public class UsernameAlreadyTakenException extends Exception {

    /**
     * Unavailable username
     */
    private final String username;

    public UsernameAlreadyTakenException(final String username, final Throwable cause) {
        super(String.format("The username '%s' is already taken", username), cause);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
