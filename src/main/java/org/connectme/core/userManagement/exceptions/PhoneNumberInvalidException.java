package org.connectme.core.userManagement.exceptions;

/**
 * This exception is thrown if a passed phone number is not valid syntactically
 */
public class PhoneNumberInvalidException extends Exception {

    public PhoneNumberInvalidException(final String reason) {
        super("user passed a phone number that is syntactically invalid: " + reason);
    }

}
