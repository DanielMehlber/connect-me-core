package org.connectme.core.userManagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown if a user cannot be created because the username already exists
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyTakenException extends Exception {

    public UsernameAlreadyTakenException() {
        /*
         * Do NOT log username, it is raw user input and could be harmful (see Log-Injection)
         */
        super("The passed username is already taken");
    }
}
