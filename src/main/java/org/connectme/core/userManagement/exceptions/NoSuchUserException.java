package org.connectme.core.userManagement.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown if there is no user data for a passed username
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NoSuchUserException extends Exception {

    public NoSuchUserException() {
        /*
         * SECURITY: Do NOT log username, it is raw user input and could be harmful (see Log-Injection)
         */
        super("requested user with passed username does not exist");
    }

}
