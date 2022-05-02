package org.connectme.core.userManagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown if a user wants to set/change his phone number to one which is already
 * used by another user.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class PhoneNumberAlreadyInUseException extends Exception {

    public PhoneNumberAlreadyInUseException() {
        super("the passed phone number is already in use by another user");
    }

}
